package net.dainco.module.news.repository.impl;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.YearMonth;
import javax.inject.Inject;
import net.dainco.module.core.repository.AbstractElasticSearchRepository;
import net.dainco.module.core.support.DateUtils;
import net.dainco.module.news.domain.StockNews;
import net.dainco.module.news.repository.StockNewsRepository;
import org.elasticsearch.client.RestHighLevelClient;

public class ElasticSearchStockNewsRepository
    extends AbstractElasticSearchRepository<StockNews> implements StockNewsRepository {
  private static final String INDEX_PREFIX = "stock-news";

  @Inject
  public ElasticSearchStockNewsRepository(RestHighLevelClient restClient) {
    super(restClient, INDEX_PREFIX);
  }

  @Override
  protected void applyIndexTemplateProperties(ObjectNode properties) {
    properties.putObject("id").put("type", "keyword");
    properties.putObject("resourceId").put("type", "keyword");
    properties.putObject("uri").put("type", "keyword");
    properties.putObject("publishedOn").put("type", "date");
    properties.putObject("title").put("type", "text");
    properties.putObject("content").put("type", "text");
    properties.putObject("symbols").put("type", "keyword");
    properties.putObject("source").put("type", "keyword");

    ObjectNode html = properties.putObject("html");
    html.put("type", "text");
    html.put("index", "false");
    html.put("store", "false");
  }

  @Override
  protected String getIndexPartition(StockNews value) {
    YearMonth yearMonth = DateUtils.getYearMonth(value.getPublishedOn());
    return String.format("%d-%02d", yearMonth.getYear(), yearMonth.getMonthValue());
  }
}
