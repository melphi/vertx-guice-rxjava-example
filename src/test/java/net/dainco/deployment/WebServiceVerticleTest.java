package net.dainco.deployment;

import static org.assertj.core.api.Assertions.assertThat;

import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
class WebServiceVerticleTest {
  @BeforeEach
  public void setUp(Vertx vertx, VertxTestContext testContext) {
    vertx.deployVerticle(new VertxModule.WebServiceVerticle(), testContext.completing());
  }

  @AfterEach
  public void tearDown(Vertx vertx, VertxTestContext testContext) {
    vertx.close(testContext.completing());
  }

  @Test
  public void shouldRespondHealthyMessages(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    client.get(8080, "localhost", "/api/health")
        .as(BodyCodec.json(VertxModule.HealthMessage.class))
        .send(testContext.succeeding(response -> testContext.verify(() -> {
          assertThat(response.body().getMessage()).isEqualTo("healthy");
          testContext.completeNow();
        })));
  }
}
