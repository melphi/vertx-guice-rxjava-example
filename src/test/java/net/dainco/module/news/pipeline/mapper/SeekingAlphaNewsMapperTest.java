package net.dainco.module.news.pipeline.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import avro.RawResource;
import java.util.List;
import net.dainco.TestingResources;
import net.dainco.module.news.domain.StockNews;
import net.dainco.module.news.support.TestingDomain;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SeekingAlphaNewsMapperTest {
  @InjectMocks
  private SeekingAlphaNewsMapper seekingAlphaNewsMapper;

  @Test
  public void shouldParseItem() {
    RawResource rawResource = TestingDomain.createRawResourceFromHtml(TestingResources.SEEKINGALPHA_SAMPLE_NEWS);
    StockNews news = seekingAlphaNewsMapper.parseItem(rawResource).get();
    assertThat(news.getId()).isEqualTo("8daacf66cd4375488979442ea01b305f55e73b86e01f2dac07b020c8");
    assertThat(news.getUri()).isEqualTo("https://d412e11eaaada63317f5da88fd7a6b04c65d8541c90fba667ad1d2cb");
    assertThat(news.getTitle()).isEqualTo(
        "Weyland Tech high post Fixel AI acquisition agreement (OTCMKTS:WEYL) | Seeking Alpha");
    assertThat(news.getPublishedOn()).isEqualTo(Long.valueOf(1597335312000L));
    assertThat(news.getSource()).isEqualTo("seekingalpha");
    assertThat(news.getSymbols()).isEqualTo(List.of("OTCQX:WEYL", "NASDAQ:EQ"));
    assertThat(news.getContent()).startsWith(
        "Weyland Tech (OTCQX:WEYL +12.0%) entered into an agreement "
        + "to acquire Fixel AI, innovator of digital marketing technology. Through A.I. machine learning power, "
        + "Fixel automatically analyzes user interactions on a company’s website");
  }
}
