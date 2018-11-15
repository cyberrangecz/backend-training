package cz.muni.csirt.kypo.utils.schemagenerators;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.avro.AvroFactory;
import com.fasterxml.jackson.dataformat.avro.AvroSchema;
import com.fasterxml.jackson.dataformat.avro.schema.AvroSchemaGenerator;
import com.google.common.reflect.ClassPath;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Pavel Šeda
 */
public class EsAvroSchemaGenerator {

    private ObjectMapper mapper = new ObjectMapper(new AvroFactory());

    public EsAvroSchemaGenerator() {
    }

    /**
     * Run this class to "manually" create AVRO schemas
     */
    public static void main(String[] args) {
        EsAvroSchemaGenerator gen = new EsAvroSchemaGenerator();
        gen.runGenerateAVROSchemas("cz.muni.csirt.kypo.events.trainings", "src/main/resources/validation-schemas/events/trainings/avro",
                Arrays.asList("common"));
    }

    /**
     * Generates avro serialization files.
     *
     */
    private final void runGenerateAVROSchemas(String topLevelClasses, String validationFolderName, List<String> excludes) {
        try {
            Set<ClassPath.ClassInfo> classInfo = ClassPath.from(getClass().getClassLoader())
                    .getTopLevelClassesRecursive(topLevelClasses).stream()
                    .filter(clazz -> Objects.nonNull(clazz) && Objects.nonNull(clazz.getName()) && Objects.nonNull(excludes))
                    .limit(Long.MAX_VALUE) // to prevent infinite loop
                    .filter(clazz -> !excludes.stream().anyMatch(str -> clazz.getName().contains(str)))
                    .collect(Collectors.toCollection(HashSet::new));
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

    private final void generateJSONSchemas(Object className, String fileName, String type, String validationFolderName) {
        // configure mapper, if necessary, then create schema generator
        try {
            AvroSchemaGenerator gen = new AvroSchemaGenerator();
            mapper.acceptJsonFormatVisitor(className.getClass(), gen);
            AvroSchema schemaWrapper = gen.getGeneratedSchema();

            org.apache.avro.Schema avroSchema = schemaWrapper.getAvroSchema();
            String asJson = avroSchema.toString(true);

            Path newSchema = Paths.get(validationFolderName + "/" + fileName + ".avsc");
            // create validation-schemas directory if does not exists
            Path parentDir = newSchema.getParent();
            if (!Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }
            Files.write(newSchema, asJson.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
