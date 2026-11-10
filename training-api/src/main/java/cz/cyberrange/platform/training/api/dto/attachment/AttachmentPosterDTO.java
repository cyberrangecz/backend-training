package cz.cyberrange.platform.training.api.dto.attachment;

/**
 * The poster image of an attachment file.
 *
 * @param mediaType the media type of the image
 * @param content the image bytes
 */
public record AttachmentPosterDTO(String mediaType, byte[] content) {}
