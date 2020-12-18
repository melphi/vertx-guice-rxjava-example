package net.dainco.module.news.repository;

import avro.RawResource;
import io.reactivex.Completable;

public interface RawResourceErrorRepository {
  Completable save(RawResource resource, String id);
}
