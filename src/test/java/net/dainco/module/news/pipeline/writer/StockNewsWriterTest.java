package net.dainco.module.news.pipeline.writer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import net.dainco.module.news.domain.StockNews;
import net.dainco.module.news.repository.StockNewsRepository;
import net.dainco.module.news.support.TestingDomain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StockNewsWriterTest {
  @Mock
  private StockNewsRepository stockNewsRepository;

  @InjectMocks
  private StockNewsWriter stockNewsWriter;

  @BeforeEach
  public void init() {
    when(stockNewsRepository.save(any(), any()))
        .thenAnswer((it) -> Single.fromCallable(() -> it.getArgument(0)));
  }

  @Test
  public void testWrite() {
    // Given.
    StockNews stockNews = TestingDomain.createStockNews("http://foo.com/foo.html");

    // When.
    TestObserver<StockNews> testObserver = stockNewsWriter.write(stockNews).test();

    // Then.
    testObserver.assertComplete();
    testObserver.assertNoErrors();
    testObserver.assertValue(stockNews);
  }
}
