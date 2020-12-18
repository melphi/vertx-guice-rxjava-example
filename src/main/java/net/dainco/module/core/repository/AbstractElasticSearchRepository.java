package net.dainco.module.core.repository;

import static com.google.common.base.Preconditions.checkArgument;
import static net.dainco.module.core.support.MorePreconditions.checkNotNullOrEmpty;
import static net.dainco.module.core.support.RxUtils.singleAsyncIo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.google.common.collect.Range;
import io.reactivex.Single;
import io.vertx.core.json.Json;
import java.io.Serializable;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.PutIndexTemplateRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractElasticSearchRepository<T extends Serializable> {
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractElasticSearchRepository.class);
  private static final Range<Integer> VALID_STATUS = Range.closedOpen(200, 300);

  protected final RestHighLevelClient restClient;
  protected final String indexName;

  public AbstractElasticSearchRepository(RestHighLevelClient restClient, String indexName) {
    this.restClient = restClient;
    this.indexName = checkNotNullOrEmpty(indexName);
    checkArgument(!indexName.endsWith("-") && !indexName.endsWith("*"), "Index name should not end with - or *.");
    setUpIndexTemplates();
  }

  protected abstract void applyIndexTemplateProperties(ObjectNode jsonDefinition);

  protected abstract String getIndexPartition(T value);

  public Single<T> save(T value, String id) {
    return singleAsyncIo(() -> saveBlocking(value, id));
  }

  public T saveBlocking(T value, String id) {
    try {
      IndexRequest indexRequest = createIndexSaveRequest(value, id);
      IndexResponse indexResponse = restClient.index(indexRequest, RequestOptions.DEFAULT);
      checkArgument(VALID_STATUS.contains(indexResponse.status().getStatus()),
          String.format("Unexpected response status [%d].", indexResponse.status().getStatus()));
      return value;
    } catch (Exception e) {
      LOGGER.error(String.format(
          "Could not index document [%s] [%s]: [%s].", id, value, e.getMessage()), e);
      throw new IllegalArgumentException(e);
    }
  }

  private IndexRequest createIndexSaveRequest(T value, String id) {
    checkNotNullOrEmpty(id, "Id can not be null.");
    String indexFullName = getFullIndexName(getIndexPartition(value));
    String json = Json.encode(value);
    return new IndexRequest(indexFullName)
        .id(id)
        .source(json, XContentType.JSON);
  }

  private String getFullIndexName(String indexPartition) {
    checkArgument(!Strings.isNullOrEmpty(indexPartition), "Index partition is required.");
    checkArgument(!indexPartition.startsWith("-"), "Index partition should not start with -.");
    return String.format("%s-%s", indexName, indexPartition);
  }

  private void setUpIndexTemplates() {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode definition = mapper.createObjectNode();

    ObjectNode settings = definition.putObject("settings");
    settings.put("number_of_shards", "1");

    ArrayNode patterns = definition.putArray("index_patterns");
    patterns.add(String.format("%s-*", indexName));

    ObjectNode mappings = definition.putObject("mappings");
    mappings.put("dynamic", "false");
    ObjectNode properties = mappings.putObject("properties");
    applyIndexTemplateProperties(properties);
    checkArgument(!properties.isEmpty(), "Index template properties must be defined.");

    try {
      String definitionJson = mapper.writeValueAsString(definition);
      PutIndexTemplateRequest indexTemplateRequest = new PutIndexTemplateRequest(indexName)
          .source(definitionJson, XContentType.JSON);
      restClient.indices().putTemplate(indexTemplateRequest, RequestOptions.DEFAULT);
    } catch (Exception e) {
      throw new IllegalArgumentException(String.format(
          "Could not setup index pattern for [%s]: [%s].", indexName, e.getMessage()), e);
    }
  }
}
