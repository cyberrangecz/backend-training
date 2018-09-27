package cz.muni.csirt.kypo.utils.schemagenerators;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.reflect.ClassPath;
import com.kjetland.jackson.jsonSchema.JsonSchemaGenerator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Pavel Šeda
 *
 */
public class EsJSONSchemaDraftV4Generator {

	private ObjectMapper objectMapper = new ObjectMapper();

	public EsJSONSchemaDraftV4Generator() {}

	/**
	 * Run this class to "manually" create json draft-v4 schemas
	 *
	 */
	public static void main(String[] args) {
		EsJSONSchemaDraftV4Generator gen = new EsJSONSchemaDraftV4Generator();
		gen.runGenerateJSONSchemas("cz.muni.csirt.kypo.events.game", "src/main/resources/validation-schemas/events/draft-v4",
				Arrays.asList("common"));
	}

	/**
	 * Generate json files with draft-v4 validations schema content
	 * 
	 * @param objectMapper
	 */
	private void runGenerateJSONSchemas(String topLevelClasses, String validationFolderName, List<String> excludes) {
		try {
			// @formatter:off
  	  Set<ClassPath.ClassInfo> classInfo = ClassPath.from(getClass().getClassLoader())
  			.getTopLevelClassesRecursive(topLevelClasses).stream()
  			.filter(clazz -> Objects.nonNull(clazz) && Objects.nonNull(clazz.getName()) && Objects.nonNull(excludes))
  			.limit(Long.MAX_VALUE) // to prevent infinite loop
  			.filter(clazz -> !excludes.stream().anyMatch(str -> clazz.getName().contains(str)))
  			.collect(Collectors.toCollection(HashSet::new));
  	  // @formatter:on
			classInfo.forEach(c -> {
				try {
					Class<?> clazz = Class.forName(c.getName());
					Object documentInstanceObj = clazz.newInstance();

					String docName = c.getName();

					generateJSONSchemas(documentInstanceObj, docName, docName, validationFolderName);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	private void generateJSONSchemas(Object className, String fileName, String type, String validationFolderName) {
		// configure mapper, if necessary, then create schema generator
		try {
			JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(objectMapper);
			JsonNode jsonSchema = schemaGen.generateJsonSchema(className.getClass());

			byte[] jsonSchemaAsBytes = objectMapper.writeValueAsBytes(jsonSchema);

			Path newSchema = Paths.get(validationFolderName + "/" + fileName + ".json");
			// create validation-schemas directory if does not exists
			Path parentDir = newSchema.getParent();
			if (!Files.exists(parentDir)) {
				Files.createDirectories(parentDir);
			}
			Files.write(newSchema, jsonSchemaAsBytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
