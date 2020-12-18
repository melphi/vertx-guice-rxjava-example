package net.dainco.module.news.pipeline.writer;

import avro.RawResource;
import io.reactivex.Single;
import java.io.Serializable;
import javax.inject.Inject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dainco.module.core.pipeline.writer.RxWriter;
import net.dainco.module.news.repository.RawResourceErrorRepository;
import net.dainco.module.news.repository.RawResourceOutboundRepository;

public class RawResourceWriter implements RxWriter<RawResourceWriter.WriteRequest, RawResource> {
  private final RawResourceOutboundRepository rawResourceOutboundRepository;
  private final RawResourceErrorRepository rawResourceErrorRepository;

  @Inject
  public RawResourceWriter(
      RawResourceErrorRepository rawResourceErrorRepository,
      RawResourceOutboundRepository rawResourceOutboundRepository
  ) {
    this.rawResourceErrorRepository = rawResourceErrorRepository;
    this.rawResourceOutboundRepository = rawResourceOutboundRepository;
  }

  @Override
  public Single<RawResource> write(WriteRequest request) {
    RawResource rawResource = request.getRawResource();
    switch (request.getDestination()) {
      case ERROR:
        return rawResourceErrorRepository.save(rawResource, rawResource.getId())
            .toSingle(request::getRawResource);
      case SUCCESS:
        return rawResourceOutboundRepository.save(rawResource, rawResource.getId())
            .toSingle(request::getRawResource);
      default:
        return Single.error(new IllegalArgumentException(
            String.format("Unsupported destination [%s].", request.getDestination())));
    }
  }

  public enum WriteDestination {
    ERROR,
    SUCCESS
  }

  @Getter
  @RequiredArgsConstructor
  public static class WriteRequest implements Serializable {
    private final RawResource rawResource;
    private final WriteDestination destination;
  }
}
