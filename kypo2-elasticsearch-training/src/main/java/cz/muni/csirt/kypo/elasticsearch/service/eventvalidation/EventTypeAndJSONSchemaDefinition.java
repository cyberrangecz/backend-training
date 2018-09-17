package cz.muni.csirt.kypo.elasticsearch.service.eventvalidation;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Pavel Šeda
 *
 */
@Component
@Scope("singleton")
public class EventTypeAndJSONSchemaDefinition {

  private Map<String, JsonNode> map = new HashMap<>();

  public EventTypeAndJSONSchemaDefinition() {}

  @Cacheable(value = "eventTypeAndValidationSchemas", cacheManager = "springCM")
  public Map<String, JsonNode> getMap() {
    return map;
  }

  public void setMap(Map<String, JsonNode> map) {
    this.map = map;
  }

  public void putToMap(String type, JsonNode jsonNode) {
    map.put(type, jsonNode);
  }

}
