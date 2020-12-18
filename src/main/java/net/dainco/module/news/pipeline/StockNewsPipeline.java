package net.dainco.module.news.pipeline;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toUnmodifiableMap;
import static net.dainco.module.core.pipeline.EmptyRequest.empty;
import static net.dainco.module.core.support.MorePreconditions.checkNotNullOrEmpty;

import avro.RawResource;
import com.google.api.client.util.Lists;
import com.google.inject.Inject;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.dainco.module.news.domain.StockNews;
import net.dainco.module.news.pipeline.mapper.StockNewsMapper;
import net.dainco.module.news.pipeline.reader.RawResourceReader;
import net.dainco.module.news.pipeline.writer.RawResourceWriter;
import net.dainco.module.news.pipeline.writer.StockNewsWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StockNewsPipeline {
  private static final Logger LOGGER = LoggerFactory.getLogger(StockNewsPipeline.class);

  private final Map<String, StockNewsMapper> mappers;
  private final StockNewsWriter stockNewsWriter;
  private final RawResourceReader rawResourceReader;
  private final RawResourceWriter rawResourceWriter;
  private final Observer<PipelineContext> downStreamObserver;

  @Inject
  public StockNewsPipeline(
      Set<StockNewsMapper> mappers,
      StockNewsWriter stockNewsWriter,
      RawResourceReader rawResourceReader,
      RawResourceWriter rawResourceWriter
  ) {
    this(mappers, stockNewsWriter, rawResourceReader, rawResourceWriter, new DefaultDownStreamObserver());
  }

  public StockNewsPipeline(
      Set<StockNewsMapper> mappers,
      StockNewsWriter stockNewsWriter,
      RawResourceReader rawResourceReader,
      RawResourceWriter rawResourceWriter,
      Observer<PipelineContext> downStreamObserver
  ) {
    checkArgument(!mappers.isEmpty(), "Parsers list can not be empty.");
    this.mappers = mappers.stream()
        .collect(toUnmodifiableMap(StockNewsMapper::getSourceName, identity()));
    this.stockNewsWriter = stockNewsWriter;
    this.rawResourceReader = rawResourceReader;
    this.rawResourceWriter = rawResourceWriter;
    this.downStreamObserver = downStreamObserver;
  }

  public void run() {
    rawResourceReader.read(empty())
        .map(this::createExecutionContext)
        .flatMapSingle(this::extractStockNews)
        .flatMapSingle(this::saveStockNewsIfNecessary)
        .flatMapSingle(this::copyToStorage)
        .flatMapSingle(this::acknowledge)
        .subscribe(downStreamObserver);
  }

  private PipelineContext createExecutionContext(RawResource rawResource) {
    return new PipelineContext(rawResource, Lists.newArrayList(), Lists.newArrayList(), false);
  }

  private Single<PipelineContext> saveStockNewsIfNecessary(PipelineContext context) {
    Observable<StockNews> written = Observable.fromIterable(context.getStockNews())
        .filter(this::filterStockNews)
        .flatMapSingle(stockNewsWriter::write);
    return safeSingleResult(written, context);
  }

  private boolean filterStockNews(StockNews stockNews) {
    if (stockNews.getSymbols().isEmpty()) {
      LOGGER.info(String.format(
          "Resource [%s] contains news [%s] ignored because no symbol was found.",
          stockNews.getResourceId(),
          stockNews.getUri()));
      return false;
    }
    return true;
  }

  private Single<PipelineContext> acknowledge(PipelineContext context) {
    if (Boolean.TRUE.equals(context.getAcknowledge())) {
      return rawResourceReader.acknowledge(context.getRawResource())
          .doOnSuccess((it) -> {
            if (!it) {
              LOGGER.warn(String.format(
                  "Resource [%s] acknowledge returned false.", context.getRawResource().getId()));
            }
          })
          .map((it) -> context);
    }
    return Single.fromCallable(() -> context);
  }

  private Single<PipelineContext> copyToStorage(PipelineContext context) {
    RawResourceWriter.WriteDestination destination = context.getErrors().isEmpty()
        ? RawResourceWriter.WriteDestination.SUCCESS
        : RawResourceWriter.WriteDestination.ERROR;
    RawResourceWriter.WriteRequest writeRequest = new RawResourceWriter.WriteRequest(
        context.getRawResource(), destination);
    Single<RawResource> written = rawResourceWriter.write(writeRequest)
        .doOnSuccess((it) -> context.setAcknowledge(true));
    return safeSingleResult(written, context);
  }

  private Single<PipelineContext> extractStockNews(PipelineContext context) {
    try {
      Single<List<StockNews>> mapped = findMapper(context.getRawResource())
          .map(context.getRawResource())
          .collect(context::getStockNews, List::add);
      return safeSingleResult(mapped, context);
    } catch (Exception e) {
      handleError(context, e);
      return Single.fromCallable(() -> context);
    }
  }

  private Single<PipelineContext> safeSingleResult(Single<?> single, PipelineContext context) {
    return single.map((it) -> context)
        .onErrorReturn((it) -> handleError(context, it));
  }

  private Single<PipelineContext> safeSingleResult(
      Observable<? extends Serializable> observable, PipelineContext context)
  {
    return observable
        .map((it) -> context)
        .onErrorReturn((it) -> handleError(context, it))
        .lastElement()
        .toSingle(context);
  }

  private StockNewsMapper findMapper(RawResource rawResource) {
    String source = checkNotNullOrEmpty(
        rawResource.getSource(),
        String.format("Resource [%s] does not contain required fields.", rawResource.getId())
    );
    StockNewsMapper mapper = mappers.get(source);
    checkNotNull(mapper, String.format(
        "Resource [%s] does not have mapper registered for source [%s].",
        rawResource.getId(),
        rawResource.getSource()));
    return mapper;
  }

  private PipelineContext handleError(PipelineContext context, Throwable cause) {
    context.getErrors().add(cause);
    LOGGER.error(cause.getMessage(), cause);
    return context;
  }

  private static class DefaultDownStreamObserver implements Observer<PipelineContext> {
    @Override
    public void onSubscribe(@NonNull Disposable d) {
      LOGGER.info("Stock news pipeline started.");
    }

    @Override
    public void onNext(@NonNull PipelineContext pipelineContext) {
      if (pipelineContext.getErrors().isEmpty()) {
        LOGGER.info(String.format(
            "Processed [%s], uri [%s].",
            pipelineContext.getRawResource().getId(),
            pipelineContext.getRawResource().getUri())
        );
      } else {
        String errorMsg = pipelineContext.getErrors()
            .stream()
            .map(Throwable::getMessage)
            .collect(Collectors.joining("\n"));
        LOGGER.warn(String.format(
            "Error(s) processing [%s], uri [%s], [%d] errors: [%s].",
            pipelineContext.getRawResource().getId(),
            pipelineContext.getRawResource().getUri(),
            pipelineContext.getErrors().size(),
            errorMsg)
        );
      }
    }

    @Override
    public void onError(@NonNull Throwable e) {
      LOGGER.error(String.format("Unmanaged pipeline error [%s].", e.getMessage()), e);
    }

    @Override
    public void onComplete() {
      LOGGER.info("Stock news pipeline reader terminated.");
    }
  }
}
