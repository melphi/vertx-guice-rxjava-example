package net.dainco.module.news.pipeline.mapper;

import avro.RawResource;
import net.dainco.module.core.pipeline.mapper.RxMapper;
import net.dainco.module.news.domain.StockNews;

public interface StockNewsMapper extends RxMapper<RawResource, StockNews> {
  String getSourceName();
}
