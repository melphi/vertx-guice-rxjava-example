package net.dainco.module.news.pipeline.mapper;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static net.dainco.module.core.support.MorePreconditions.checkNotNullOrEmpty;

import avro.RawResource;
import com.google.api.client.util.Lists;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.dainco.module.core.support.HtmlUtils;
import net.dainco.module.news.domain.StockNews;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SeekingAlphaNewsMapper extends AbstractStockNewsMapper {
  private static final String SOURCE_SEEKING_ALPHA = "seekingalpha";
  private static final Pattern REGEX_SYMBOLS = Pattern.compile("\\((\\w+\\:?\\s?\\w*)\\s?\\S*\\)");
  private static final Pattern REGEX_SPACES = Pattern.compile("\\s+");

  private final SimpleDateFormat dateFormat;

  public SeekingAlphaNewsMapper() {
    super(SOURCE_SEEKING_ALPHA);
    this.dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    this.dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  @Override
  protected Optional<StockNews> parseItem(RawResource webDocument) {
    checkNotNullOrEmpty(webDocument.getContent(), "Html content is empty or null.");
    Document document = Jsoup.parse(webDocument.getContent());
    String title = document.title().trim();
    Long publishedOn = getPublishedOn(document);
    String text = getContent(document);
    Collection<String> symbols = getSymbols(text);
    StockNews stockNews = createStockNews(webDocument, publishedOn, title, text, symbols);
    return Optional.of(stockNews);
  }

  private Collection<String> getSymbols(String content) {
    List<String> result = Lists.newArrayList();
    Matcher matcher = REGEX_SYMBOLS.matcher(content);
    while (matcher.find()) {
      if (matcher.groupCount() == 1) {
        String symbol = matcher.group(1);
        if (!isNullOrEmpty(symbol)) {
          result.add(symbol.trim());
        }
      }
    }
    checkArgument(!result.isEmpty(), "Could not find symbols in the document.");
    return List.copyOf(result);
  }

  private String getContent(Document document) {
    Elements elements = document.select("p.bullets_li");
    checkArgument(elements.size() > 1, "Content elements not found.");
    StringBuilder content = new StringBuilder();
    for (Element element : elements) {
      try {
        content.append(HtmlUtils.toPlainText(element.html()))
            .append("\n");
      } catch (Exception e) {
        throw new IllegalArgumentException(String.format("Could not parse html text: [%s].", e.getMessage()), e);
      }
    }
    return REGEX_SPACES.matcher(content).replaceAll(" ");
  }

  private Long getPublishedOn(Document document) {
    Elements elements = document.select("time[content]");
    for (Element element : elements) {
      String dateString = element.attr("content");
      if (!isNullOrEmpty(dateString)) {
        try {
          Date date = dateFormat.parse(dateString);
          return date.getTime();
        } catch (ParseException e) {
          throw new IllegalArgumentException(
              String.format("Could not parse date [%s]: [%s].", dateString, e.getMessage()), e);
        }
      }
    }
    throw new IllegalArgumentException("Date not found in the document.");
  }
}
