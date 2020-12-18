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
class ForbesNewsMapperTest {
  @InjectMocks
  private ForbesNewsMapper forbesNewsMapper;

  @Test
  public void shouldParseItem() {
    RawResource rawResource = TestingDomain.createRawResourceFromHtml(TestingResources.FORBES_SAMPLE_NEWS);
    StockNews news = forbesNewsMapper.parseItem(rawResource).get();
    assertThat(news.getId()).isEqualTo("0a521ae907ce520d323d5a996c34b1b62bef0b2bf1c6ea69e1444c85");
    assertThat(news.getUri()).isEqualTo("https://fe7a8b5c3cfd1c28b13192108dd07cbaa7cbffc0b120b570f2db8e2e");
    assertThat(news.getTitle()).isEqualTo("Lululemon’s Stock Needs A Rest At $339");
    assertThat(news.getPublishedOn()).isEqualTo(Long.valueOf(1597064400000L));
    assertThat(news.getSource()).isEqualTo("forbes");
    assertThat(news.getSymbols()).isEqualTo(List.of("NASDAQ: LULU"));
    assertThat(news.getContent()).startsWith(
        "After a 97% rise since the March 23 lows of this year, "
        + "at the current price of $339 per share we believe Lululemon’s stock");
    assertThat(news.getContent()).doesNotContain("PROMOTED");
  }
}
