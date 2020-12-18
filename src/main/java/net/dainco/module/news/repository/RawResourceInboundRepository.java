package net.dainco.module.news.repository;

import avro.RawResource;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;

public interface RawResourceInboundRepository {
  Observable<String> list(String folder);
  Maybe<RawResource> readOptional(String filePath);
  Single<Boolean> delete(String filePath);
}
