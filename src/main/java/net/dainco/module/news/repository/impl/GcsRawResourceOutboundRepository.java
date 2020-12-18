package net.dainco.module.news.repository.impl;

import avro.RawResource;
import com.google.cloud.storage.Storage;
import com.google.inject.name.Named;
import javax.inject.Inject;
import net.dainco.module.core.repository.AbstractGcsRepository;
import net.dainco.module.news.repository.RawResourceOutboundRepository;

public class GcsRawResourceOutboundRepository
    extends AbstractGcsRepository<RawResource> implements RawResourceOutboundRepository {
  @Inject
  public GcsRawResourceOutboundRepository(
      Storage storage, @Named("application.storage.rawresource.outbound") String bucketName
  ) {
    super(storage, bucketName, RawResource.class);
  }
}
