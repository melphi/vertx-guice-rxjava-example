package net.dainco.module.core.pipeline.lookup;

import io.reactivex.Single;
import java.io.Serializable;

public interface RxLookup<T extends Serializable, S extends Serializable> {
  Single<S> lookup(T request);
}
