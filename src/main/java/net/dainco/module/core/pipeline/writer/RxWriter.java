package net.dainco.module.core.pipeline.writer;

import io.reactivex.Single;
import java.io.Serializable;

public interface RxWriter<T extends Serializable, S extends Serializable> {
  Single<S> write(T request);
}
