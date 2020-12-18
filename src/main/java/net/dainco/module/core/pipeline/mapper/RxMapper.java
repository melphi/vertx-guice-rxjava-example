package net.dainco.module.core.pipeline.mapper;

import io.reactivex.Observable;
import java.io.Serializable;

public interface RxMapper<T extends Serializable, S extends Serializable> {
  Observable<S> map(T request);
}
