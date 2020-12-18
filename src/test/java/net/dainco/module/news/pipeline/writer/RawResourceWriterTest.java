package net.dainco.module.news.pipeline.writer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import avro.RawResource;
import io.reactivex.Completable;
import io.reactivex.observers.TestObserver;
import net.dainco.module.news.repository.RawResourceErrorRepository;
import net.dainco.module.news.repository.RawResourceOutboundRepository;
import net.dainco.module.news.support.TestingDomain;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RawResourceWriterTest {
  private final String defaultResourceId = "id1";

  @Mock
  private RawResourceOutboundRepository rawResourceOutboundRepository;

  @Mock
  private RawResourceErrorRepository rawResourceErrorRepository;

  @InjectMocks
  private RawResourceWriter rawResourceWriter;

  @Test
  public void testWriteSuccess() {
    // Given.
    when(rawResourceOutboundRepository.save(any(), any())).thenAnswer((it) -> Completable.complete());
    RawResource rawResource = TestingDomain.createRawResource(defaultResourceId);
    RawResourceWriter.WriteRequest writeRequest = new RawResourceWriter.WriteRequest(
        rawResource, RawResourceWriter.WriteDestination.SUCCESS);

    // When.
    TestObserver<RawResource> testObserver = rawResourceWriter.write(writeRequest).test();

    // Then.
    testObserver.assertComplete();
    testObserver.assertNoErrors();
    testObserver.assertValue(rawResource);
    verify(rawResourceOutboundRepository, times(1)).save(any(), eq(defaultResourceId));
    verify(rawResourceErrorRepository, never()).save(any(), any());
  }

  @Test
  public void testWriteError() {
    // Given.
    when(rawResourceErrorRepository.save(any(), any())).thenAnswer((it) -> Completable.complete());
    RawResource rawResource = TestingDomain.createRawResource(defaultResourceId);
    RawResourceWriter.WriteRequest writeRequest = new RawResourceWriter.WriteRequest(
        rawResource, RawResourceWriter.WriteDestination.ERROR);

    // When.
    TestObserver<RawResource> testObserver = rawResourceWriter.write(writeRequest).test();

    // Then.
    testObserver.assertComplete();
    testObserver.assertNoErrors();
    testObserver.assertValue(rawResource);
    verify(rawResourceErrorRepository, times(1)).save(any(), eq(defaultResourceId));
    verify(rawResourceOutboundRepository, never()).save(any(), any());
  }
}
