package io.vertx.conduit;


import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.vertx.conduit.MainVerticle.CONTENT_TYPE_JSON;
import static io.vertx.conduit.MainVerticle.HEADER_CONTENT_TYPE;
import static io.vertx.conduit.TestConstants.TOKEN_INVALID;
import static io.vertx.conduit.TestConstants.TOKEN_JAKE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Get Current User Endpoint Tests")
@ExtendWith(VertxExtension.class)
public class GetCurrentUserEndpointTest {

  /**
   * This method tests the endpoint
   * GET /api/user
   *
   * Should return the following:
     {
       "user": {
       "email": "jake@jake.jake",
       "token": "jwt.token.here",
       "username": "jake",
       "bio": "I work at statefarm",
       "image": null
       }
     }
   *
   * @param vertx
   * @param testContext
   */
  @Test
  public void testGetCurrentUser(Vertx vertx, VertxTestContext testContext){

    Checkpoint responseReceived = testContext.checkpoint();
    Checkpoint testComplete = testContext.checkpoint();

    vertx.deployVerticle(new MainVerticle(), testContext.succeeding(id -> {

      WebClient webClient = WebClient.create(vertx);

      webClient.get(8080, "localhost", "/api/user")
        .putHeader("Authorization", TOKEN_JAKE)
        .as(BodyCodec.jsonObject())
        .send(testContext.succeeding(response -> {
          testContext.verify(()->{
            responseReceived.flag();
            assertEquals(CONTENT_TYPE_JSON, response.getHeader(HEADER_CONTENT_TYPE));
            assertEquals(200, response.statusCode());
            JsonObject user = response.body();
            System.out.println(user);
            assertNotNull(user);
            testComplete.flag();
          });
        }));
    }));
  }

  @Test
  public void testInvalidJWTToken(Vertx vertx, VertxTestContext testContext) {
    Checkpoint responseReceived = testContext.checkpoint();
    Checkpoint testComplete = testContext.checkpoint();

    vertx.deployVerticle(new MainVerticle(), testContext.succeeding(id -> {

      WebClient webClient = WebClient.create(vertx);

      webClient.get(8080, "localhost", "/api/user")
        .putHeader("Authorization", TOKEN_INVALID)
        .send(testContext.succeeding(response -> {
          testContext.verify(()->{
            responseReceived.flag();
            assertEquals(401, response.statusCode());
            assertEquals("Unauthorized", response.bodyAsString());
            testComplete.flag();
          });
        }));
    }));

  }
}
