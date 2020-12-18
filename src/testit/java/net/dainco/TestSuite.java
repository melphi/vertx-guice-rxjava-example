package net.dainco;

import org.junit.jupiter.api.BeforeAll;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.runner.RunWith;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@RunWith(JUnitPlatform.class)
@SelectPackages("net.dainco.features")
@Testcontainers
public class TestSuite {
  private static final String DOCKER_ELASTICSEARCH = "docker.elastic.co/elasticsearch/elasticsearch:7.5.2";

  @Container
  public GenericContainer<?> elasticsearch = new GenericContainer<>(DOCKER_ELASTICSEARCH)
      .withEnv("discovery.type", "single-node")
      .withEnv("bootstrap.memory_lock", "true")
      .withEnv("ES_JAVA_OPTS", "-Xms256m -Xmx512m")
      .withExposedPorts(9200);

  @BeforeAll
  public static void start() {
    Application.main(new String[] {});
  }
}
