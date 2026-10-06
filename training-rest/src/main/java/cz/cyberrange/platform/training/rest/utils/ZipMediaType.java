package cz.cyberrange.platform.training.rest.utils;

import org.springframework.http.MediaType;

/** The media type a zip archive is served under */
public final class ZipMediaType {

  public static final String APPLICATION_ZIP_VALUE = "application/zip";

  public static final MediaType APPLICATION_ZIP = MediaType.parseMediaType(APPLICATION_ZIP_VALUE);

  private ZipMediaType() {}
}
