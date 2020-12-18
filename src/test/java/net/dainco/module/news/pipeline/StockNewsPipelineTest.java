package net.dainco.module.news.pipeline;

import static java.util.List.of;
import static net.dainco.module.news.support.TestingDomain.createRawResource;
import static net.dainco.module.news.support.TestingDomain.createStockNews;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import avro.RawResource;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import java.util.List;
import java.util.Set;
import net.dainco.module.news.domain.StockNews;
import net.dainco.module.news.pipeline.mapper.StockNewsMapper;
import net.dainco.module.news.pipeline.reader.RawResourceReader;
import net.dainco.module.news.pipeline.writer.RawResourceWriter;
import net.dainco.module.news.pipeline.writer.StockNewsWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StockNewsPipelineTest {
  private static final String defaultSourceName = "test";

  @Mock
  private StockNewsWriter stockNewsWriter;

  @Mock
  private RawResourceReader rawResourceReader;

  @Mock
  private RawResourceWriter rawResourceWriter;

  @Mock
  private StockNewsMapper stockNewsMapper;

  private StockNewsPipeline stockNewsPipeline;

  private TestObserver<PipelineContext> downStreamObserver;

  @BeforeEach
  public void init() {
    when(stockNewsMapper.getSourceName()).thenReturn(defaultSourceName);
    downStreamObserver = new TestObserver<>();
    stockNewsPipeline = new StockNewsPipeline(
        Set.of(stockNewsMapper), stockNewsWriter, rawResourceReader, rawResourceWriter, downStreamObserver
    );
  }

  @Test
  public void testReaderIsEmpty() {
    // Given.
    initRawResourceReader(Observable.empty());

    // When.
    stockNewsPipeline.run();

    // Then.
    downStreamObserver.assertNoErrors();
    downStreamObserver.assertComplete();
    downStreamObserver.assertNoValues();
    verify(rawResourceWriter, never()).write(any());
    verify(stockNewsWriter, never()).write(any());
  }

  @Test
  public void testReaderMultipleElements() {
    // Given.
    initRawResourceReader(Observable.fromIterable(of(
        createRawResource("1"),
        createRawResource("2")))
    );
    initRawResourceReaderAck();
    initStockNewsMapper();
    initRawResourceWriter();
    initStockNewsWriter();

    // When.
    stockNewsPipeline.run();

    // Then.
    downStreamObserver.assertNoErrors();
    downStreamObserver.assertComplete();
    downStreamObserver.assertValueCount(2);

    verify(stockNewsWriter, times(2)).write(any());
    ArgumentCaptor<RawResourceWriter.WriteRequest> captor = ArgumentCaptor.forClass(
        RawResourceWriter.WriteRequest.class);
    verify(rawResourceWriter, times(2)).write(captor.capture());
    assertThat(captor.getAllValues()).allMatch(
        (it) -> RawResourceWriter.WriteDestination.SUCCESS == it.getDestination()
    );
    verify(rawResourceReader, times(2)).acknowledge(any());
  }

  @Test
  public void testErrorOnRead() {
    // Given.
    Exception error = new Exception("test");
    initRawResourceReader(Observable.error(error));

    // When.
    stockNewsPipeline.run();

    // Then.
    downStreamObserver.assertError(error);
    downStreamObserver.assertNotComplete();
    downStreamObserver.assertNoValues();
    verify(rawResourceWriter, never()).write(any());
    verify(stockNewsWriter, never()).write(any());
    verify(rawResourceReader, never()).acknowledge(any());
  }

  @Test
  public void testErrorOnMapStockNews() {
    // Given.
    Exception error = new Exception("test");
    initRawResourceReader(Observable.fromIterable(of(createRawResource("1"))));
    initRawResourceReaderAck();
    initStockNewsMapper(Observable.error(error));
    initRawResourceWriter();

    // When.
    stockNewsPipeline.run();

    // Then.
    downStreamObserver.assertNoErrors();
    downStreamObserver.assertComplete();
    downStreamObserver.assertValueCount(1);
    verify(stockNewsWriter, never()).write(any());
    ArgumentCaptor<RawResourceWriter.WriteRequest> captor = ArgumentCaptor.forClass(
        RawResourceWriter.WriteRequest.class);
    verify(rawResourceWriter, times(1)).write(captor.capture());
    assertThat(captor.getAllValues()).allMatch(
        (it) -> RawResourceWriter.WriteDestination.ERROR == it.getDestination()
    );
    verify(rawResourceReader, times(1)).acknowledge(any());
  }

  @Test
  public void testErrorOnSaveStockNews() {
    // Given.
    Exception error = new Exception("test");
    initRawResourceReader(Observable.fromIterable(of(createRawResource("1"))));
    initRawResourceReaderAck();
    initStockNewsMapper();
    initStockNewsWriter(Single.error(error));
    initRawResourceWriter();

    // When.
    stockNewsPipeline.run();

    // Then.
    downStreamObserver.assertNoErrors();
    downStreamObserver.assertComplete();
    downStreamObserver.assertValueCount(1);
    verify(stockNewsWriter, times(1)).write(any());
    ArgumentCaptor<RawResourceWriter.WriteRequest> captor = ArgumentCaptor.forClass(
        RawResourceWriter.WriteRequest.class);
    verify(rawResourceWriter, times(1)).write(captor.capture());
    assertThat(captor.getAllValues()).allMatch(
        (it) -> RawResourceWriter.WriteDestination.ERROR == it.getDestination()
    );
    verify(rawResourceReader, times(1)).acknowledge(any());
  }

  @Test
  public void testErrorOnCopyToStorage() {
    // Given.
    Exception error = new Exception("test");
    initRawResourceReader(Observable.fromIterable(of(createRawResource("1"))));
    initStockNewsMapper();
    initStockNewsWriter();
    initRawResourceWriter(Single.error(error));

    // When.
    stockNewsPipeline.run();

    // Then.
    downStreamObserver.assertNoErrors();
    downStreamObserver.assertComplete();
    downStreamObserver.assertValueCount(1);
    verify(stockNewsWriter, times(1)).write(any());
    verify(rawResourceWriter, times(1)).write(any());
    verify(rawResourceReader, never()).acknowledge(any());
  }

  @Test
  public void testErrorOnAcknowledge() {
    // Given.
    Exception error = new Exception("test");
    initRawResourceReader(Observable.fromIterable(of(createRawResource("1"))));
    initRawResourceReaderAck(Single.error(error));
    initStockNewsMapper();
    initStockNewsWriter();
    initRawResourceWriter();

    // When.
    stockNewsPipeline.run();

    // Then.
    downStreamObserver.assertError(error);
    downStreamObserver.assertNotComplete();
    downStreamObserver.assertNoValues();
    verify(stockNewsWriter, times(1)).write(any());
    verify(rawResourceWriter, times(1)).write(any());
    verify(rawResourceReader, times(1)).acknowledge(any());
  }

  @Test
  public void testFilterWhenNoSymbols() {
    // Given.
    StockNews stockNews = createStockNews("http://foo", List.of());
    initRawResourceReader(Observable.fromIterable(of(createRawResource("1"))));
    initRawResourceReaderAck();
    initStockNewsMapper(Observable.fromIterable(of(stockNews)));
    initRawResourceWriter();

    // When.
    stockNewsPipeline.run();

    // Then.
    downStreamObserver.assertNoErrors();
    downStreamObserver.assertComplete();
    downStreamObserver.assertValueCount(1);

    verify(stockNewsWriter, never()).write(any());
    ArgumentCaptor<RawResourceWriter.WriteRequest> captor = ArgumentCaptor.forClass(
        RawResourceWriter.WriteRequest.class);
    verify(rawResourceWriter, times(1)).write(captor.capture());
    assertThat(captor.getAllValues()).allMatch(
        (it) -> RawResourceWriter.WriteDestination.SUCCESS == it.getDestination()
    );
    verify(rawResourceReader, times(1)).acknowledge(any());
  }

  @Test
  public void testContinueInCaseOfErrors() {
    // Given.
    RawResource rawResource1 = createRawResource("1");
    RawResource rawResource2 = createRawResource("2");
    initRawResourceReader(Observable.fromIterable(of(rawResource1, rawResource2)));
    initRawResourceReaderAck();
    initRawResourceWriter();
    initStockNewsWriter();

    when(stockNewsMapper.map(eq(rawResource1))).thenAnswer((it) ->
        Observable.fromIterable(of(createStockNews((RawResource) it.getArgument(0)))));
    when(stockNewsMapper.map(eq(rawResource2))).thenReturn(Observable.error(new Exception("test")));

    // When.
    stockNewsPipeline.run();

    // Then.
    downStreamObserver.assertNoErrors();
    downStreamObserver.assertComplete();
    downStreamObserver.assertValueCount(2);

    verify(stockNewsWriter, times(1)).write(any());
    ArgumentCaptor<RawResourceWriter.WriteRequest> captor = ArgumentCaptor.forClass(
        RawResourceWriter.WriteRequest.class);
    verify(rawResourceWriter, times(2)).write(captor.capture());
    assertThat(captor.getAllValues()).first().matches(
        (it) -> RawResourceWriter.WriteDestination.SUCCESS == it.getDestination());
    assertThat(captor.getAllValues()).last().matches(
        (it) -> RawResourceWriter.WriteDestination.ERROR == it.getDestination());
    verify(rawResourceReader, times(2)).acknowledge(any());
  }

  private void initStockNewsMapper() {
    when(stockNewsMapper.map(any())).thenAnswer((it) ->
        Observable.fromIterable(of(createStockNews((RawResource) it.getArgument(0)))));
  }

  private void initStockNewsMapper(Observable<StockNews> observable) {
    when(stockNewsMapper.map(any())).thenReturn(observable);
  }

  private void initStockNewsWriter() {
    when(stockNewsWriter.write(any())).thenAnswer((it) -> Single.fromCallable(() -> it.getArgument(0)));
  }

  private void initStockNewsWriter(Single<StockNews> result) {
    when(stockNewsWriter.write(any())).thenReturn(result);
  }

  private void initRawResourceWriter() {
    when(rawResourceWriter.write(any())).thenAnswer((it) -> Single.fromCallable(() -> {
      RawResourceWriter.WriteRequest request = it.getArgument(0);
      return request.getRawResource();
    }));
  }

  private void initRawResourceWriter(Single<RawResource> result) {
    when(rawResourceWriter.write(any())).thenReturn(result);
  }

  private void initRawResourceReader(Observable<RawResource> observable) {
    when(rawResourceReader.read(any())).thenReturn(observable);
  }

  private void initRawResourceReaderAck(Single<Boolean> result) {
    when(rawResourceReader.acknowledge(any())).thenReturn(result);
  }

  private void initRawResourceReaderAck() {
    initRawResourceReaderAck(Single.fromCallable(() -> true));
  }
}
