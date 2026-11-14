package cz.cyberrange.platform.training.api.dto.attachment;

/**
 * An attachment file together with its description.
 *
 * @param metadata the description of the file
 * @param content the bytes of the file
 */
public record AttachmentDownloadDTO(AttachmentMetadataDTO metadata, byte[] content) {}
