package cz.muni.ics.kypo.training.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.main.JsonValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ValidationException;
import java.io.IOException;

public class AssessmentResponseValidator implements ConstraintValidator<ValidAssessmentResponse, String> {

    @Autowired
    @Qualifier("assessmentResponseSchema")
    private JsonNode assessmentResponseSchema;

    @Autowired
    private JsonSchemaFactory jsonSchemaFactory;


    @Override
    public void initialize(ValidAssessmentResponse constraintAnnotation) {
    }

    @Override
    public boolean isValid(String response, ConstraintValidatorContext context) {
        StringBuilder messageBuilder = new StringBuilder("Invalid assessment response.");
        context.disableDefaultConstraintViolation();
        try {
            JsonNode n = JsonLoader.fromString(response);
            JsonValidator v = jsonSchemaFactory.getValidator();
            ProcessingReport report = v.validate(assessmentResponseSchema, n);
            if(report.isSuccess()) {
                return true;
            } else {
                context.buildConstraintViolationWithTemplate(messageBuilder
                        .append("Reason: ")
                        .append(report.iterator().next().getMessage())
                        .toString())
                        .addConstraintViolation();
                return false;
            }
        } catch (IOException | ProcessingException ex) {
            throw new ValidationException("Could not validate assessment response. Response is not valid JSON and cannot be parsed.");
        }
    }
}
