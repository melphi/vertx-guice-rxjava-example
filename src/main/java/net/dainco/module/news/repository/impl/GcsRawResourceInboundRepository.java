package net.dainco.module.news.repository.impl;

import avro.RawResource;
import com.google.cloud.storage.Storage;
import com.google.inject.name.Named;
import javax.inject.Inject;
import net.dainco.module.core.repository.AbstractGcsRepository;
import net.dainco.module.news.repository.RawResourceInboundRepository;

public class GcsRawResourceInboundRepository
    extends AbstractGcsRepository<RawResource> implements RawResourceInboundRepository {
  @Inject
  public GcsRawResourceInboundRepository(
      Storage storage, @Named("application.storage.rawresource.inbound") String bucketName
  ) {
    super(storage, bucketName, RawResource.class);
  }
}
