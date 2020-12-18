package net.dainco.module.news.pipeline.writer;

import io.reactivex.Single;
import javax.inject.Inject;
import net.dainco.module.core.pipeline.writer.RxWriter;
import net.dainco.module.news.domain.StockNews;
import net.dainco.module.news.repository.StockNewsRepository;

public class StockNewsWriter implements RxWriter<StockNews, StockNews> {
  private final StockNewsRepository stockNewsRepository;

  @Inject
  public StockNewsWriter(StockNewsRepository stockNewsRepository) {
    this.stockNewsRepository = stockNewsRepository;
  }

  @Override
  public Single<StockNews> write(StockNews request) {
    return stockNewsRepository.save(request, request.getId());
  }
}
