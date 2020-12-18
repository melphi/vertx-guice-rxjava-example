package net.dainco.module.news.repository;

import avro.RawResource;
import io.reactivex.Completable;

public interface RawResourceOutboundRepository {
  Completable save(RawResource resource, String id);
}
