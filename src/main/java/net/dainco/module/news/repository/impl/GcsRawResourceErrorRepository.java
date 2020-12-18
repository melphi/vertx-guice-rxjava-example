package net.dainco.module.news.repository.impl;

import avro.RawResource;
import com.google.cloud.storage.Storage;
import com.google.inject.name.Named;
import javax.inject.Inject;
import net.dainco.module.core.repository.AbstractGcsRepository;
import net.dainco.module.news.repository.RawResourceErrorRepository;

public class GcsRawResourceErrorRepository
    extends AbstractGcsRepository<RawResource> implements RawResourceErrorRepository {
  @Inject
  public GcsRawResourceErrorRepository(
      Storage storage, @Named("application.storage.rawresource.error") String bucketName
  ) {
    super(storage, bucketName, RawResource.class);
  }
}
