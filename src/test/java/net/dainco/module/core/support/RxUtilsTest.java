package net.dainco.module.core.support;

import io.reactivex.observers.TestObserver;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.reactivex.schedulers.TestScheduler;

class RxUtilsTest {
  private TestScheduler testScheduler;

  @BeforeEach
  public void init() {
    testScheduler = new TestScheduler();
    RxUtils.schedulerIo = testScheduler;
  }

  @Test
  public void testCompleteAsyncIo() {
    // Given.
    TestObserver<Void> testObserver = RxUtils.completeAsyncIo(() -> true).test();

    // When.
    testScheduler.triggerActions();

    // Then.
    testObserver.assertComplete();
    testObserver.assertNoErrors();
  }

  @Test
  public void testSingleAsyncIo() {
    // Given.
    TestObserver<Boolean> testObserver = RxUtils.singleAsyncIo(() -> true).test();

    // When.
    testScheduler.triggerActions();

    // Then.
    testObserver.assertComplete();
    testObserver.assertNoErrors();
    testObserver.assertValue(true);
  }

  @Test
  public void testMaybeAsyncIo() {
    // Given.
    TestObserver<Boolean> testObserver = RxUtils.maybeAsyncIo(() -> true).test();

    // When.
    testScheduler.triggerActions();

    // Then.
    testObserver.assertComplete();
    testObserver.assertNoErrors();
    testObserver.assertValue(true);
  }

  @Test
  public void testObserveAsyncIo() {
    // Given.
    TestObserver<Boolean> testObserver = RxUtils.observeAsyncIo(() -> List.of(true, false, true)).test();

    // When.
    testScheduler.triggerActions();

    // Then.
    testObserver.assertComplete();
    testObserver.assertNoErrors();
    testObserver.assertValues(true, false, true);
  }
}
