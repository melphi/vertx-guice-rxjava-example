package net.dainco.deployment;

import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;
import static io.vertx.core.json.Json.encode;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VertxModule extends AbstractModule {
  private static final Logger LOGGER = LoggerFactory.getLogger(VertxModule.class);
  private static final int DEFAULT_PORT = 8080;

  @Provides
  public Vertx vertx() {
    return Vertx.vertx();
  }

  @Provides
  public WebServiceVerticle webServiceVerticle() {
    return new WebServiceVerticle();
  }

  @AllArgsConstructor
  @Getter
  @NoArgsConstructor
  public static class HealthMessage {
    private String message;
  }

  public static class WebServiceVerticle extends AbstractVerticle {
    @Override
    public void start(Promise<Void> startPromise) {
      LOGGER.info(String.format("Starting web server at port [%d].", DEFAULT_PORT));
      vertx.createHttpServer()
          .requestHandler(createRouter())
          .listen(
              DEFAULT_PORT,
              it -> startPromise.handle(it.mapEmpty())
          );
    }

    public void deploy(Vertx vertx) {
      vertx.deployVerticle(this);
    }

    private Router createRouter() {
      Router router = Router.router(vertx);
      router.route("/api/health")
          .handler(it -> it.response()
              .putHeader(CONTENT_TYPE, APPLICATION_JSON.toString())
              .end(encode(new HealthMessage("healthy")))
          );
      return router;
    }
  }
}
