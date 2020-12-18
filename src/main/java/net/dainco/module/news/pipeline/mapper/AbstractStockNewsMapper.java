package net.dainco.module.news.pipeline.mapper;

import static net.dainco.module.core.support.MorePreconditions.checkNotNullOrEmpty;

import avro.RawResource;
import io.reactivex.Observable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.dainco.module.core.support.HashUtils;
import net.dainco.module.news.domain.StockNews;

public abstract class AbstractStockNewsMapper implements StockNewsMapper {
  protected final String sourceName;

  public AbstractStockNewsMapper(String sourceName) {
    this.sourceName = checkNotNullOrEmpty(sourceName);
  }

  @Override
  public String getSourceName() {
    return sourceName;
  }

  @Override
  public Observable<StockNews> map(RawResource request) {
    try {
      Optional<StockNews> stockNews = parseItem(request);
      return stockNews.isEmpty()
          ? Observable.empty()
          : Observable.fromIterable(List.of(stockNews.get()));
    } catch (Exception e) {
      return Observable.error(e);
    }
  }

  abstract protected Optional<StockNews> parseItem(RawResource webDocument);

  protected StockNews createStockNews(
      RawResource rawResource, Long publishedOn, String title, String text, Collection<String> symbols) {
    String id = HashUtils.getHash(checkNotNullOrEmpty(rawResource.getUri(), "Uri can not be empty."));
    return new StockNews(id, rawResource.getId(), rawResource.getUri(), publishedOn, title, text, symbols, sourceName);
  }
}
