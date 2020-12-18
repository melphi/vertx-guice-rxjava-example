package net.dainco.module.news.repository;

import io.reactivex.Single;
import net.dainco.module.news.domain.StockNews;

public interface StockNewsRepository {
  Single<StockNews> save(StockNews value, String id);
}
