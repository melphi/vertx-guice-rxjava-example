package net.dainco.module.news.pipeline;

import avro.RawResource;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.dainco.module.news.domain.StockNews;

@AllArgsConstructor
@RequiredArgsConstructor
@Getter
@Setter
public class PipelineContext {
  private final RawResource rawResource;
  private List<StockNews> stockNews;
  private List<Throwable> errors;
  private Boolean acknowledge;
}
