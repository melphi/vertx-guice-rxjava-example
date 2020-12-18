package net.dainco.module.news.pipeline.reader;

import static io.reactivex.Observable.fromIterable;
import static net.dainco.module.core.pipeline.EmptyRequest.empty;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import avro.RawResource;
import io.reactivex.Maybe;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.TestScheduler;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import net.dainco.module.news.repository.RawResourceInboundRepository;
import net.dainco.module.news.support.TestingDomain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RawResourceReaderTest {
  private final String defaultPollingFolder = "foo";
  private final int defaultPollingFrequency = 5;
  private final int defaultPollingSize = 5;
  private final Map<String, Maybe<RawResource>> defaultItems = Map.of(
      "item_a", Maybe.fromCallable(() -> TestingDomain.createRawResource("item_a")),
      "item_b", Maybe.fromCallable(() -> null),
      "item_c", Maybe.fromCallable(() -> TestingDomain.createRawResource("item_c")));

  private RawResourceReader webItemReader;
  private TestScheduler scheduler;

  @Mock
  private RawResourceInboundRepository webItemInboundRepository;

  @BeforeEach
  public void setUp() {
    when(webItemInboundRepository.list(defaultPollingFolder)).thenReturn(fromIterable(defaultItems.keySet()));
    for(Map.Entry<String, Maybe<RawResource>> entry: defaultItems.entrySet()) {
      when(webItemInboundRepository.readOptional(eq(entry.getKey()))).thenReturn(entry.getValue());
    }
    scheduler = new TestScheduler();
    webItemReader = new RawResourceReader(
        webItemInboundRepository,
        defaultPollingFolder,
        Integer.toString(defaultPollingFrequency),
        Integer.toString(defaultPollingSize),
        scheduler
    );
  }

  @Test
  public void testShouldReadItemsInitial() {
    // When
    TestObserver<RawResource> testObserver = webItemReader.read(empty()).test();
    scheduler.advanceTimeBy(1, TimeUnit.SECONDS);

    // Then
    testObserver
        .awaitCount(2)
        .assertSubscribed()
        .assertNoErrors()
        .assertNotComplete()
        .assertValueCount(2);
  }

  @Test
  public void testShouldReadItemsPolls() {
    // When
    TestObserver<RawResource> testObserver = webItemReader.read(empty()).test();
    scheduler.advanceTimeBy(5, TimeUnit.SECONDS);

    // Then
    testObserver
        .assertSubscribed()
        .assertNoErrors()
        .assertNotComplete()
        .assertValueCount(4);
  }
}
