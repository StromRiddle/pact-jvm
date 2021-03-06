package au.com.dius.pact.consumer;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslJsonArray;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.RequestResponsePact;
import groovy.json.JsonSlurper;
import org.apache.http.client.fluent.Request;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class Defect266Test {

  @Rule
  public PactProviderRuleMk2 provider = new PactProviderRuleMk2("266_provider", "localhost", 8080, this);

  @Pact(provider = "266_provider", consumer = "test_consumer")
  public RequestResponsePact getUsersFragment(PactDslWithProvider builder) {
    Map<String, Map<String, Object>> matchers = (Map<String, Map<String, Object>>) new JsonSlurper().parseText("{" +
      "\"$.body[0][*].userName\": {\"match\": \"type\"}," +
      "\"$.body[0][*].id\": {\"match\": \"regex\", \"regex\": \"[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\"}," +
      "\"$.body[0]\": {\"match\": \"type\", \"max\": 5}," +
      "\"$.body[0][*].email\": {\"match\": \"type\"}" +
      "}");
    DslPart body = new PactDslJsonArray().maxArrayLike(5)
      .uuid("id")
      .stringType("userName")
      .stringType("email")
      .closeObject();
    RequestResponsePact pact = builder
      .given("a user with an id named 'user' exists")
      .uponReceiving("get all users for max")
      .path("/idm/user")
      .method("GET")
      .willRespondWith()
      .status(200)
      .body(body)
      .toPact();
    assertThat(pact.getInteractions().get(0).getResponse().getMatchingRules(), is(equalTo(matchers)));
    return pact;
  }

  @Pact(provider = "266_provider", consumer = "test_consumer")
  public RequestResponsePact getUsersFragment2(PactDslWithProvider builder) {
    Map<String, Map<String, Object>> matchers = (Map<String, Map<String, Object>>) new JsonSlurper().parseText("{" +
      "\"$.body[0][*].userName\": {\"match\": \"type\"}," +
      "\"$.body[0][*].id\": {\"match\": \"regex\", \"regex\": \"[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\"}," +
      "\"$.body[0]\": {\"match\": \"type\", \"min\": 5}," +
      "\"$.body[0][*].email\": {\"match\": \"type\"}" +
      "}");
    DslPart body = new PactDslJsonArray().minArrayLike(5)
      .uuid("id")
      .stringType("userName")
      .stringType("email")
      .closeObject();
    RequestResponsePact pact = builder
      .given("a user with an id named 'user' exists")
      .uponReceiving("get all users for min")
      .path("/idm/user")
      .method("GET")
      .willRespondWith()
      .status(200)
      .body(body)
      .toPact();
    assertThat(pact.getInteractions().get(0).getResponse().getMatchingRules(), is(equalTo(matchers)));
    return pact;
  }

  @Test
  @PactVerification(value = "266_provider", fragment = "getUsersFragment")
  public void runTest() throws IOException {
    Request.Get("http://localhost:8080/idm/user").execute().returnContent().asString();
  }

  @Test
  @PactVerification(value = "266_provider", fragment = "getUsersFragment2")
  public void runTest2() throws IOException {
    Request.Get("http://localhost:8080/idm/user").execute().returnContent().asString();
  }
}
