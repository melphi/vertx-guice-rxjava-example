package net.dainco.module.news;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import javax.inject.Singleton;
import net.dainco.module.news.pipeline.StockNewsPipeline;
import net.dainco.module.news.pipeline.mapper.ForbesNewsMapper;
import net.dainco.module.news.pipeline.mapper.PrNewsWireNewsMapper;
import net.dainco.module.news.pipeline.mapper.SeekingAlphaNewsMapper;
import net.dainco.module.news.pipeline.mapper.StockNewsMapper;
import net.dainco.module.news.pipeline.reader.RawResourceReader;
import net.dainco.module.news.pipeline.writer.RawResourceWriter;
import net.dainco.module.news.pipeline.writer.StockNewsWriter;
import net.dainco.module.news.repository.RawResourceErrorRepository;
import net.dainco.module.news.repository.RawResourceInboundRepository;
import net.dainco.module.news.repository.RawResourceOutboundRepository;
import net.dainco.module.news.repository.StockNewsRepository;
import net.dainco.module.news.repository.impl.ElasticSearchStockNewsRepository;
import net.dainco.module.news.repository.impl.GcsRawResourceErrorRepository;
import net.dainco.module.news.repository.impl.GcsRawResourceInboundRepository;
import net.dainco.module.news.repository.impl.GcsRawResourceOutboundRepository;

public class NewsModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(StockNewsRepository.class).to(ElasticSearchStockNewsRepository.class);
    bind(RawResourceErrorRepository.class).to(GcsRawResourceErrorRepository.class);
    bind(RawResourceInboundRepository.class).to(GcsRawResourceInboundRepository.class);
    bind(RawResourceOutboundRepository.class).to(GcsRawResourceOutboundRepository.class);

    Multibinder<StockNewsMapper> multiBinder = Multibinder.newSetBinder(binder(), StockNewsMapper.class);
    multiBinder.addBinding().to(ForbesNewsMapper.class);
    multiBinder.addBinding().to(PrNewsWireNewsMapper.class);
    multiBinder.addBinding().to(SeekingAlphaNewsMapper.class);

    bind(StockNewsPipeline.class).in(Singleton.class);
    bind(StockNewsWriter.class).in(Singleton.class);
    bind(RawResourceReader.class).in(Singleton.class);
    bind(RawResourceWriter.class).in(Singleton.class);
  }
}
