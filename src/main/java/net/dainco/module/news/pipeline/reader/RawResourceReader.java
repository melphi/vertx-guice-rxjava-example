package net.dainco.module.news.pipeline.reader;

import static com.google.common.base.Preconditions.checkArgument;

import avro.RawResource;
import com.google.inject.name.Named;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import net.dainco.module.core.pipeline.EmptyRequest;
import net.dainco.module.core.pipeline.reader.RxReader;
import net.dainco.module.news.repository.RawResourceInboundRepository;

public class RawResourceReader implements RxReader<EmptyRequest, RawResource> {
  private final RawResourceInboundRepository webItemInboundRepository;
  private final String pollingFolder;
  private final Integer pollingFrequency;
  private final Integer pollingSize;
  private final Scheduler scheduler;

  private ObservableSource<RawResource> observableSource;

  @Inject
  public RawResourceReader(
      RawResourceInboundRepository webItemInboundRepository,
      @Named("application.storage.rawresource.inbound.polling.folder") String pollingFolder,
      @Named("application.storage.rawresource.inbound.polling.frequency.seconds") String pollingFrequency,
      @Named("application.storage.rawresource.inbound.polling.size") String pollingSize
  ) {
    this(webItemInboundRepository, pollingFolder, pollingFrequency, pollingSize, Schedulers.computation());
  }

  public RawResourceReader(
      RawResourceInboundRepository webItemInboundRepository,
      String pollingFolder,
      String pollingFrequency,
      String pollingSize,
      Scheduler scheduler
  ) {
    this.webItemInboundRepository = webItemInboundRepository;
    this.pollingFolder = pollingFolder;
    this.pollingFrequency = Integer.valueOf(pollingFrequency);
    this.pollingSize = Integer.valueOf(pollingSize);
    this.scheduler = scheduler;
  }

  @Override
  public Observable<RawResource> read(EmptyRequest request) {
    checkArgument(observableSource == null, "Can not read twice.");
    observableSource = observer -> schedulePolls().subscribe(observer);
    return Observable.unsafeCreate(observableSource);
  }

  public Single<Boolean> acknowledge(RawResource rawResource) {
    String filePath = String.format("%s/%s", pollingFolder, rawResource.getId());
    return webItemInboundRepository.delete(filePath);
  }

  private Observable<RawResource> pollOnce() {
    return webItemInboundRepository.list(pollingFolder)
        .take(pollingSize)
        .flatMapMaybe(webItemInboundRepository::readOptional);
  }

  private Observable<RawResource> schedulePolls() {
    return Observable.interval(0, pollingFrequency, TimeUnit.SECONDS, scheduler)
        .flatMap((it) -> pollOnce());
  }
}
