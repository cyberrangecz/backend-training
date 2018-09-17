package cz.muni.csirt.kypo.elasticsearch.service.eventvalidation;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.main.JsonValidator;
import cz.muni.csirt.kypo.elasticsearch.service.audit.exceptions.ElasticsearchTrainingServiceLayerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * 
 * @author Pavel Seda
 *
 */
@Component
public class EventValidation {

  private EventTypeAndJSONSchemaDefinition eventTypeAndJSONSchemaDefinition;

  @Autowired
  public EventValidation(EventTypeAndJSONSchemaDefinition eventTypeAndJSONSchemaDefinition) {
    this.eventTypeAndJSONSchemaDefinition = eventTypeAndJSONSchemaDefinition;
  }

  /**
   * Validate the given JSON data against the given JSON schema
   * 
   * @param jsonSchema as String
   * @param jsonData as String
   */
  public boolean isValid(String type, String payload) {
    Map<String, JsonNode> map = eventTypeAndJSONSchemaDefinition.getMap();
    if (map != null) {
      JsonNode jsonSchema = map.get(type);
      if (jsonSchema != null) {
        try {
          final JsonNode d = JsonLoader.fromString(payload);
          final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
          JsonValidator v = factory.getValidator();

          ProcessingReport report = v.validate(jsonSchema, d);
          if (report.toString().contains("success")) {
            return true;
          } else {
            throw new IllegalArgumentException("This event is not valid.");
          }
        } catch (IOException | ProcessingException e) {
          e.printStackTrace();
          throw new ElasticsearchTrainingServiceLayerException("This event is not valid.");
        }
      } else {
        throw new ElasticsearchTrainingServiceLayerException("This event type is not supported.");
      }
    } else {
      throw new ElasticsearchTrainingServiceLayerException("There are no events created in app.");
    }

  }
}
