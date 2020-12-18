package net.dainco;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.vertx.core.Vertx;
import net.dainco.deployment.ElasticSearchModule;
import net.dainco.deployment.GoogleCloudPlatformModule;
import net.dainco.deployment.PropertiesModule;
import net.dainco.deployment.VertxModule;
import net.dainco.module.news.NewsModule;
import net.dainco.module.news.pipeline.StockNewsPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {
  private static final Logger LOGGER = LoggerFactory.getLogger(Application.class.getName());

  public static void main(String[] arg) {
    Injector injector = Guice.createInjector(
        new ElasticSearchModule(),
        new GoogleCloudPlatformModule(),
        new NewsModule(),
        new PropertiesModule(),
        new VertxModule());

    VertxModule.WebServiceVerticle webServiceVerticle = injector.getInstance(VertxModule.WebServiceVerticle.class);
    webServiceVerticle.deploy(injector.getInstance(Vertx.class));

    LOGGER.info("Starting pipelines.");
    injector.getInstance(StockNewsPipeline.class).run();
  }
}
