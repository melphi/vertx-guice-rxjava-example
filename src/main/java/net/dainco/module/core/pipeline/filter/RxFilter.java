package net.dainco.module.core.pipeline.filter;

import io.reactivex.Single;
import java.io.Serializable;

public interface RxFilter<T extends Serializable> {
  Single<Boolean> filter(T request);
}
