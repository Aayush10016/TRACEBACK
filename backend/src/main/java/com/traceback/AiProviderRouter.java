package com.traceback;

import static org.springframework.http.HttpStatus.*;

import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class AiProviderRouter implements InvestigationProvider {
  private final String selected;
  private final String metaModel;
  private final MetaProvider meta;
  private final NvidiaProvider nvidia;

  public AiProviderRouter(
      @Value("${traceback.ai.provider}") String selected,
      @Value("${traceback.meta.model}") String metaModel,
      MetaProvider meta,
      NvidiaProvider nvidia) {
    this.selected = selected.trim().toLowerCase(Locale.ROOT);
    this.metaModel = metaModel;
    this.meta = meta;
    this.nvidia = nvidia;
  }

  public String name() {
    return selected;
  }

  public String model() {
    return selected.equals("nvidia") ? nvidia.model() : metaModel;
  }

  public Map<String, Object> status() {
    return switch (selected) {
      case "meta" -> meta.status();
      case "nvidia" -> nvidia.status();
      default ->
          Map.of(
              "provider",
              selected,
              "model",
              "",
              "configured",
              false,
              "status",
              "UNSUPPORTED_CONFIGURATION",
              "message",
              "Set AI_PROVIDER to meta or nvidia on the backend.");
    };
  }

  @Override
  public String complete(String prompt) {
    return switch (selected) {
      case "meta" -> meta.complete(prompt);
      case "nvidia" -> nvidia.complete(prompt);
      default ->
          throw new ApiException(
              SERVICE_UNAVAILABLE,
              "AI_CONFIGURATION",
              "Unsupported AI_PROVIDER. Choose meta or nvidia.");
    };
  }
}
