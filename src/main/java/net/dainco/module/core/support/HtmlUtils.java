package net.dainco.module.core.support;

import static net.dainco.module.core.support.MorePreconditions.checkNotNullOrEmpty;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.HtmlParser;
import org.apache.tika.sax.BodyContentHandler;

public final class HtmlUtils {
  public static String toPlainText(String html) throws Exception {
    checkNotNullOrEmpty(html);
    BodyContentHandler handler = new BodyContentHandler();
    HtmlParser htmlparser = new HtmlParser();
    try (InputStream stream = new ByteArrayInputStream(html.getBytes())) {
      htmlparser.parse(stream, handler, new Metadata(), new ParseContext());
      return handler.toString();
    }
  }
}
