package cz.cyberrange.platform.training.rest.utils.error;

import cz.cyberrange.platform.training.rest.controllers.ExportImportRestController;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Answers a training definition file that was submitted for import but could not be accepted,
 * replacing the reader's and the validator's own wording with a description addressed to the person
 * who wrote that file. Applies to the import endpoints alone; every other endpoint keeps the
 * descriptions produced by {@link CustomRestExceptionHandlerTraining}, which this advice precedes.
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = ExportImportRestController.class)
public class ImportedFileErrorAdvice {

  private final ImportedFileErrorDescriber importedFileErrorDescriber;
  private final ApiErrorResponder apiErrorResponder;

  /**
   * Creates the advice.
   *
   * @param importedFileErrorDescriber describes the rejected part of the file to its author
   * @param apiErrorResponder assembles the response carrying that description
   */
  public ImportedFileErrorAdvice(
      ImportedFileErrorDescriber importedFileErrorDescriber, ApiErrorResponder apiErrorResponder) {
    this.importedFileErrorDescriber = importedFileErrorDescriber;
    this.apiErrorResponder = apiErrorResponder;
  }

  /**
   * Answers a submitted file that could not be read as a training definition at all with {@link
   * HttpStatus#BAD_REQUEST}, describing the part of the file that was rejected.
   *
   * @param exception the failure raised while reading the submitted file
   * @param request the request carrying the file, supplying the reported path
   * @return the error body describing the rejected file
   */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<Object> handleUnreadableImportedFile(
      final HttpMessageNotReadableException exception, final HttpServletRequest request) {
    String path = apiErrorResponder.pathOf(request);
    log.warn("The file submitted to {} could not be read", path, exception);
    return apiErrorResponder.respond(
        HttpStatus.BAD_REQUEST,
        importedFileErrorDescriber.describeUnreadableContent(exception),
        exception.getMessage(),
        path);
  }

  /**
   * Answers a submitted file whose fields were read but refused with {@link
   * HttpStatus#BAD_REQUEST}, describing every refused field.
   *
   * @param exception the failure raised while validating the submitted file
   * @param request the request carrying the file, supplying the reported path
   * @return the error body describing the refused fields
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Object> handleInvalidImportedFile(
      final MethodArgumentNotValidException exception, final HttpServletRequest request) {
    String path = apiErrorResponder.pathOf(request);
    log.warn("The file submitted to {} was refused", path, exception);
    return apiErrorResponder.respond(
        HttpStatus.BAD_REQUEST,
        importedFileErrorDescriber.describeInvalidFields(exception),
        exception.getMessage(),
        path);
  }
}
