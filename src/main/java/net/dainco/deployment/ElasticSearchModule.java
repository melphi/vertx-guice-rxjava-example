package net.dainco.deployment;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import javax.inject.Named;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElasticSearchModule extends AbstractModule {
  private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchModule.class);

  @Provides
  public RestHighLevelClient restHighLevelClient(@Named("application.elastic.url") String elasticSearchUrl) {
    RestClientBuilder restClientBuilder = RestClient.builder(HttpHost.create(elasticSearchUrl));
    RestHighLevelClient client = new RestHighLevelClient(restClientBuilder);
    LOGGER.info(String.format("Connecting to ElasticSearch [%s].", elasticSearchUrl));
    // TODO: Add ElasticSearch connection check to Health endpoint.
    return checkElasticSearchHealth(client);
  }

  private RestHighLevelClient checkElasticSearchHealth(RestHighLevelClient client) {
    ClusterHealthRequest request = new ClusterHealthRequest();
    try {
      ClusterHealthResponse response = client.cluster().health(request, RequestOptions.DEFAULT);
      checkArgument(!ClusterHealthStatus.RED.equals(response.getStatus()), "Status is RED");
    } catch (Exception e) {
      throw new IllegalArgumentException(String.format("ElasticSearch connection failed: [%s]", e.getMessage()));
    }
    return client;
  }
}
