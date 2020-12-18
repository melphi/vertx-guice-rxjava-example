package net.dainco.module.core.pipeline.reader;


import io.reactivex.Observable;

public interface RxReader<T, S> {
  Observable<S> read(T request);
}
