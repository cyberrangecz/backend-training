package cz.muni.ics.kypo.training;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import cz.muni.ics.kypo.training.api.dto.sandboxdefinition.SandboxDefinitionCreateDTO;

import java.io.IOException;
import java.io.InputStream;

public class Test {

    public static void main(String[] args) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

        InputStream inJson = SandboxDefinitionCreateDTO.class.getResourceAsStream("/sandbox-definition-create.yml");
        SandboxDefinitionCreateDTO sd = objectMapper.readValue(inJson, SandboxDefinitionCreateDTO.class);
        System.out.println(sd);
    }

}
