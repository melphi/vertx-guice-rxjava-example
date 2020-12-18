package net.dainco.module.core.support;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import java.util.concurrent.Callable;

public final class RxUtils {
  protected static Scheduler schedulerIo = Schedulers.io();

  public static <T> Observable<T> observeAsyncIo(Callable<Iterable<T>> callable) {
    return Observable.fromCallable(callable)
        .subscribeOn(schedulerIo)
        .flatMap(Observable::fromIterable);
  }

  public static <T> Single<T> singleAsyncIo(Callable<T> callable) {
    return Single.fromCallable(callable)
        .subscribeOn(schedulerIo);
  }

  public static <T> Maybe<T> maybeAsyncIo(Callable<T> callable) {
    return Maybe.fromCallable(callable)
        .subscribeOn(schedulerIo);
  }

  public static Completable completeAsyncIo(Callable<?> callable) {
    return Completable.fromCallable(callable)
        .subscribeOn(schedulerIo);
  }
}
