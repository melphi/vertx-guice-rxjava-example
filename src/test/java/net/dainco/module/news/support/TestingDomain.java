package net.dainco.module.news.support;

import static com.google.common.base.Preconditions.checkNotNull;

import avro.RawResource;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.dainco.TestingConstants;
import net.dainco.module.core.support.HashUtils;
import net.dainco.module.news.domain.StockNews;

public class TestingDomain {
  public static StockNews createStockNews(String url, List<String> symbols) {
    return new StockNews(
        HashUtils.getHash(url),
        null,
        url,
        TestingConstants.DEFAULT_TIMESTAMP,
        "title",
        "content",
        symbols,
        "source"
    );
  }

  public static StockNews createStockNews(String url) {
    return createStockNews(url, List.of("symbol"));
  }

  public static StockNews createStockNews(RawResource rawResource) {
    return new StockNews(
      HashUtils.getHash(rawResource.getUri()),
      rawResource.getId(),
      rawResource.getUri(),
      rawResource.getCrawledOn(),
      "Title " + rawResource.getId(),
      "Content " + rawResource.getId(),
      List.of("symbolA"),
      rawResource.getSource()
    );
  }

  public static RawResource createRawResourceFromHtml(String filePath) {
    String html;
    try {
      InputStream stream =
          checkNotNull(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream(filePath)));
      html = new String(stream.readAllBytes(), Charset.defaultCharset());
      stream.close();
    } catch (Exception e) {
      throw new IllegalArgumentException(e);
    }
    String id = HashUtils.getHash(filePath);
    return createRawResource(id, html);
  }

  public static RawResource createRawResource(String id, String html) {
    return RawResource.newBuilder()
        .setId(id)
        .setUri(String.format("https://%s", id))
        .setContent(html)
        .setStatusCode(200)
        .setSource("test")
        .setDownloadSize(1024)
        .setCrawledOn(1590137622298L)
        .setDownloadTime(220L)
        .setProperties(Map.of())
        .build();
  }

  public static RawResource createRawResource(String id) {
    return createRawResource(id, String.format("%s Lorem ipsum dolor sit amet", id));
  }
}
