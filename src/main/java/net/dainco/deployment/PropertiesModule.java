package net.dainco.deployment;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

public class PropertiesModule extends AbstractModule {
  private static final String PROPERTIES_FILE = "application.properties";

  @Override
  protected void configure() {
    configureSystemProperties();
    configureApplicationProperties();
  }

  private void configureApplicationProperties() {
    try {
      Properties properties = new Properties();
      properties.load(ClassLoader.getSystemResourceAsStream(PROPERTIES_FILE));
      Map<String, String> envValues = System.getenv();
      for (Object property: properties.keySet()) {
        String envKey = ((String) property).replaceAll("\\.", "_").toUpperCase();
        if (envValues.containsKey(envKey)) {
          properties.setProperty((String) property, envValues.get(envKey));
        }
      }
      Names.bindProperties(binder(), properties);
    } catch (IOException ex) {
      throw new IllegalArgumentException(String.format("Can not read properties file [%s].", PROPERTIES_FILE));
    }
  }

  private void configureSystemProperties() {
    System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
  }
}
