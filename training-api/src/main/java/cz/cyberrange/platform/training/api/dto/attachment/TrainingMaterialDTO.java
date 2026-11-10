package cz.cyberrange.platform.training.api.dto.attachment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * A file a training definition keeps as a training material, whether or not level content places
 * it.
 *
 * @param hash the lowercase hexadecimal SHA-256 of the file bytes
 * @param fileName the name the definition gives the file
 */
@Schema(description = "A training material of a definition, placed in content or only stored")
public record TrainingMaterialDTO(
    @Schema(
            requiredMode = Schema.RequiredMode.REQUIRED,
            description = "Lowercase hexadecimal SHA-256 of the file bytes",
            example = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")
        @Pattern(regexp = "[0-9a-f]{64}", message = "{trainingMaterial.hash.Pattern.message}")
        String hash,
    @Schema(
            requiredMode = Schema.RequiredMode.REQUIRED,
            description = "The name the definition gives the file",
            example = "diagram.png")
        @NotBlank(message = "{trainingMaterial.fileName.NotBlank.message}")
        @Size(max = 255, message = "{trainingMaterial.fileName.Size.message}")
        String fileName) {}
