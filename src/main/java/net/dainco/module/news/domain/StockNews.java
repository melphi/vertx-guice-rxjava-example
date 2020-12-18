package net.dainco.module.news.domain;

import java.io.Serializable;
import java.util.Collection;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode
@Getter
@RequiredArgsConstructor
public class StockNews implements Serializable {
  private final String id;
  private final String resourceId;
  private final String uri;
  private final Long publishedOn;
  private final String title;
  private final String content;
  private final Collection<String> symbols;
  private final String source;
}
