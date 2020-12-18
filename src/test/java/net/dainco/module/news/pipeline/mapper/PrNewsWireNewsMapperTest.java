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
class PrNewsWireNewsMapperTest {
  @InjectMocks
  private PrNewsWireNewsMapper prNewsWireNewsMapper;

  @Test
  public void shouldParseItem() {
    RawResource rawResource = TestingDomain.createRawResourceFromHtml(TestingResources.PRNEWSWIRE_SAMPLE_NEWS);
    StockNews news = prNewsWireNewsMapper.parseItem(rawResource).get();
    assertThat(news.getId()).isEqualTo("89ac6535554cf5dafaf4c3f70315245ae730329ac7080fd9e944068a");
    assertThat(news.getUri()).isEqualTo("https://b56639a9d63d470a6429090569bbc0e8bfe7a2477affd6d0f35ddc56");
    assertThat(news.getTitle())
        .isEqualTo("Pintec Cooperates with China Unicom's Unicompay to Expand Handset Financing");
    assertThat(news.getPublishedOn()).isEqualTo(Long.valueOf(1577424240000L));
    assertThat(news.getSource()).isEqualTo("prnewswire");
    assertThat(news.getSymbols()).isEqualTo(List.of("NASDAQ: PT"));
    assertThat(news.getContent()).contains("as of June 30, 2019. Jointly ");
  }
}
