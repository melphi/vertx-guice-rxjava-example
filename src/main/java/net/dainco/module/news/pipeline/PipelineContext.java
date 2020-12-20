package net.dainco.module.news.pipeline;

import avro.RawResource;
import java.util.List;
import javax.annotation.concurrent.NotThreadSafe;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.dainco.module.news.domain.StockNews;

/**
 * Pipeline context stores context data used by pipelines.
 *
 * This class is neither thread safe nor immutable and should not be used in pipelines where thread safety and
 * immutability are relevant.
 */
@AllArgsConstructor
@NotThreadSafe
@RequiredArgsConstructor
@Getter
@Setter
// TODO: Make this class thread safe for future pipelines.
public class PipelineContext {
  private final RawResource rawResource;
  private List<StockNews> stockNews;
  private List<Throwable> errors;
  private Boolean acknowledge;
}
