package net.dainco.module.news.pipeline.mapper;

import static com.google.common.base.Preconditions.checkArgument;
import static net.dainco.module.core.support.MorePreconditions.checkNotNullOrEmpty;

import avro.RawResource;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
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

public class PrNewsWireNewsMapper extends AbstractStockNewsMapper {
  private static final String SOURCE_PR_NEWS_WIRE = "prnewswire";
  private static final Pattern REGEX_SYMBOLS = Pattern.compile("[\\(;](\\w+:\\s*\\w*)\\)");
  private static final Pattern REGEX_SPACES = Pattern.compile("\\s+");

  private final SimpleDateFormat dateFormat;

  public PrNewsWireNewsMapper() {
    super(SOURCE_PR_NEWS_WIRE);
    this.dateFormat = new SimpleDateFormat("MMM d, yyyy, H:m Z");
    this.dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  protected Optional<StockNews> parseItem(RawResource webDocument) {
    checkNotNullOrEmpty(webDocument.getContent(), "Html content is empty or null.");
    Document document = Jsoup.parse(webDocument.getContent());
    String title = document.title().trim();
    Long publishedOn = getPublishedOn(document);
    String text = getContent(document);
    Collection<String> symbols = getSymbols(text, document);
    StockNews stockNews = createStockNews(webDocument, publishedOn, title, text, symbols);
    return Optional.of(stockNews);
  }

  private Collection<String> getSymbols(String text, Document document) {
    ImmutableList.Builder<String> builder = ImmutableList.builder();
    Matcher matcher = REGEX_SYMBOLS.matcher(text);
    while (matcher.find()) {
      if (matcher.groupCount() == 1) {
        String symbol = matcher.group(1);
        if (!Strings.isNullOrEmpty(symbol)) {
          builder.add(symbol.trim());
        }
      }
    }
    List<String> result = builder.build();
    if (result.isEmpty() && !document.getElementsByClass("ticket-symbol").isEmpty()) {
      throw new IllegalArgumentException("Document symbols are present but were not parsed.");
    }
    return result;
  }

  private String getContent(Document document) {
    Elements elements = document.getElementsByClass("release-body");
    checkArgument(elements.size() == 1,
        String.format("Expected one release-body but [%d] found.", elements.size()));
    String html = elements.first().html();
    String text;
    try {
      text = HtmlUtils.toPlainText(html);
    } catch (Exception e) {
      throw new IllegalArgumentException(String.format("Could not extract text: [%s].", e.getMessage()), e);
    }
    return REGEX_SPACES.matcher(text).replaceAll(" ");
  }

  private Long getPublishedOn(Document document) {
    Optional<Element> element = Optional.ofNullable(document.getElementById("prop26"));
    if (element.isEmpty()) {
      return null;
    }
    String pubDate = element.get().attr("value");
    if (Strings.isNullOrEmpty(pubDate)) {
      return null;
    }
    pubDate = pubDate.trim().replace("ET", "-0500");
    try {
      Date date = dateFormat.parse(pubDate);
      return date.getTime();
    } catch (ParseException e) {
      throw new IllegalArgumentException(String.format("Could not parse date [%s]: [%s].", pubDate, e.getMessage()), e);
    }
  }
}
