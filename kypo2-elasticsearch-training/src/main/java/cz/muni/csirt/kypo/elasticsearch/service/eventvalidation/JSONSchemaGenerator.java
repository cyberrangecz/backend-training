package cz.muni.csirt.kypo.elasticsearch.service.eventvalidation;

import java.io.IOException;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.reflect.ClassPath;
import com.kjetland.jackson.jsonSchema.JsonSchemaGenerator;

/**
 * @author Pavel Šeda
 *
 */
@Component
public class JSONSchemaGenerator {

  private EventTypeAndJSONSchemaDefinition eventTypeAndJSONSchemaDefinition;
  private ObjectMapper objectMapper;

  @Autowired
  public JSONSchemaGenerator(EventTypeAndJSONSchemaDefinition eventTypeAndJSONSchemaDefinition, @Qualifier("objMapperESClient") ObjectMapper objectMapper) {
    this.eventTypeAndJSONSchemaDefinition = eventTypeAndJSONSchemaDefinition;
    this.objectMapper = objectMapper;
  }

  // @PostConstruct
  // public void initGenerateJSONSchemas() {
  // runGenerateJSONSchemas(objectMapper);
  // }
  //
  // /**
  // * Generate json files with draft-v4 validations schema content
  // *
  // * @param objectMapper
  // */
  // private void runGenerateJSONSchemas(ObjectMapper objectMapper) {
  // ClassLoader cl = getClass().getClassLoader();
  // Set<ClassPath.ClassInfo> classInfo;
  // try {
//      // @formatter:off
//			classInfo = ClassPath.from(cl).getTopLevelClassesRecursive("cz.muni.csirt.kypo.events.game").stream()
//					.filter(clazz -> !clazz.getName().contains("common"))
//					.collect(Collectors.toCollection(HashSet::new));
//			// @formatter:on
  // classInfo.forEach(c -> {
  // try {
  // Class<?> clazz = Class.forName(c.getName());
  // Object documentInstanceObj = clazz.newInstance();
  //
  // String docName = c.getName();
  //
  // generateJSONSchemas(objectMapper, documentInstanceObj, docName, docName);
  // } catch (Exception e) {
  // e.printStackTrace();
  // }
  // });
  // } catch (IOException e1) {
  // e1.printStackTrace();
  // }
  // }
  //
  // private void generateJSONSchemas(ObjectMapper objectMapper, Object className, String fileName,
  // String type) {
  // JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(objectMapper);
  // JsonNode jsonSchema = schemaGen.generateJsonSchema(className.getClass());
  //
  // // load Map to container with type and schema for POST validation
  // eventTypeAndJSONSchemaDefinition.putToMap(type, jsonSchema);
  // }

}
