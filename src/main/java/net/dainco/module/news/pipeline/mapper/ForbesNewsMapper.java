package net.dainco.module.news.pipeline.mapper;

import static net.dainco.module.core.support.MorePreconditions.checkNotNullOrEmpty;

import avro.RawResource;
import com.google.api.client.util.Lists;
import com.google.common.base.Strings;
import java.text.DateFormat;
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
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ForbesNewsMapper extends AbstractStockNewsMapper {
  private static final String SOURCE_PR_FORBES = "forbes";
  private static final Pattern REGEX_SYMBOLS = Pattern.compile("[\\(;](\\w+:\\s*\\w*)\\)");
  private static final Pattern REGEX_SPACES = Pattern.compile("\\s+");
  private static final int CONTENT_SAMPLE_SIZE = 20;
  private static final String CONTENT_PROMOTION_TEXT = "PROMOTED";

  private final DateFormat dateFormat;

  public ForbesNewsMapper() {
    super(SOURCE_PR_FORBES);
    this.dateFormat = new SimpleDateFormat("MMM d yyyy h:m a Z");
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

  private Collection<String> getSymbols(String bodyText) {
    List<String> result = Lists.newArrayList();
    Matcher matcher = REGEX_SYMBOLS.matcher(bodyText);
    while (matcher.find()) {
      if (matcher.groupCount() == 1) {
        String symbol = matcher.group(1);
        if (!Strings.isNullOrEmpty(symbol)) {
          result.add(symbol.trim());
        }
      }
    }
    return List.copyOf(result);
  }

  private String getContent(Document document) {
    Element metaDescription = document.select("meta[name=description]").first();
    if (metaDescription == null) {
      throw new IllegalArgumentException("Could not find meta description.");
    }
    String descriptionSample = StringUtils.left(metaDescription.attr("content"), CONTENT_SAMPLE_SIZE);
    StringBuilder htmlBody = new StringBuilder();
    Elements pElements = document.getElementsByTag("p");
    pElements.stream()
        .dropWhile((it) -> !it.text().startsWith(descriptionSample))
        .takeWhile((it) -> !it.text().trim().toUpperCase().equals(CONTENT_PROMOTION_TEXT))
        .forEach((it) -> htmlBody.append(it.outerHtml()));
    String content;
    try {
      content = checkNotNullOrEmpty(HtmlUtils.toPlainText(htmlBody.toString()));
    } catch (Exception e) {
      throw new IllegalArgumentException(String.format("Could convert text to HTML: [%s].", e.getMessage()), e);
    }
    return REGEX_SPACES.matcher(content).replaceAll(" ");
  }

  private Long getPublishedOn(Document document) {
    StringBuilder stringBuilder = new StringBuilder();
    Elements elements = document.getElementsByTag("time");
    for (Element element : elements) {
      stringBuilder
          .append(element.text()
              .replaceAll(",", "")
              .replace("EDT", "-0400")
              .replace("am", " AM")
              .replace("pm", " PM")
              .trim())
          .append(" ");
    }
    String dateString = stringBuilder.toString();
    try {
      Date date = dateFormat.parse(dateString);
      return date.getTime();
    } catch (ParseException e) {
      throw new IllegalArgumentException(
          String.format("Could not parse date [%s]: [%s].", dateString, e.getMessage()), e);
    }
  }
}
