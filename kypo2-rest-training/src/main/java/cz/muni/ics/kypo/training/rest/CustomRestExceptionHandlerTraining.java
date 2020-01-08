package cz.muni.ics.kypo.training.rest;

import cz.muni.ics.kypo.training.rest.exceptions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = "cz.muni.ics.kypo.training")
public class CustomRestExceptionHandlerTraining extends ResponseEntityExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(CustomRestExceptionHandlerTraining.class);
    protected static final UrlPathHelper URL_PATH_HELPER = new UrlPathHelper();

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(final MethodArgumentNotValidException ex, final HttpHeaders headers,
                                                                  final HttpStatus status, final WebRequest request) {

        final List<String> errors = new ArrayList<>();
        for (final FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(error.getField() + ": " + error.getDefaultMessage());
        }
        for (final ObjectError error : ex.getBindingResult().getGlobalErrors()) {
            errors.add(error.getObjectName() + ": " + error.getDefaultMessage());
        }
        final ApiErrorTraining apiError = new ApiErrorTraining.ApiErrorBuilder(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage())
                .setErrors(errors).setPath(request.getContextPath()).build();

        return handleExceptionInternal(ex, apiError, headers, apiError.getStatus(), request);
    }

    @Override
    protected ResponseEntity<Object> handleBindException(final BindException ex, final HttpHeaders headers, final HttpStatus status,
                                                         final WebRequest request) {

        final List<String> errors = new ArrayList<>();
        for (final FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(error.getField() + ": " + error.getDefaultMessage());
        }
        for (final ObjectError error : ex.getBindingResult().getGlobalErrors()) {
            errors.add(error.getObjectName() + ": " + error.getDefaultMessage());
        }
        final ApiErrorTraining apiError = new ApiErrorTraining.ApiErrorBuilder(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage())
                .setErrors(errors).setPath(request.getContextPath()).build();
        return handleExceptionInternal(ex, apiError, headers, apiError.getStatus(), request);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(final TypeMismatchException ex, final HttpHeaders headers, final HttpStatus status,
                                                        final WebRequest request) {

        final String error = ex.getValue() + " value for " + ex.getPropertyName() + " should be of type " + ex.getRequiredType();

        final ApiErrorTraining apiError = new ApiErrorTraining.ApiErrorBuilder(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage()).setError(error)
                .setPath(request.getContextPath()).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestPart(final MissingServletRequestPartException ex, final HttpHeaders headers,
                                                                     final HttpStatus status, final WebRequest request) {

        final String error = ex.getRequestPartName() + " part is missing";
        final ApiErrorTraining apiError = new ApiErrorTraining.ApiErrorBuilder(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage()).setError(error)
                .setPath(request.getContextPath()).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(final MissingServletRequestParameterException ex,
                                                                          final HttpHeaders headers, final HttpStatus status, final WebRequest request) {

        final String error = ex.getParameterName() + " parameter is missing";
        final ApiErrorTraining apiError = new ApiErrorTraining.ApiErrorBuilder(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage()).setError(error)
                .setPath(request.getContextPath()).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(final NoHandlerFoundException ex, final HttpHeaders headers,
                                                                   final HttpStatus status, final WebRequest request) {

        final String error = "No handler found for " + ex.getHttpMethod() + " " + ex.getRequestURL();
        final ApiErrorTraining apiError = new ApiErrorTraining.ApiErrorBuilder(HttpStatus.NOT_FOUND, getInitialException(ex).getLocalizedMessage()).setError(error)
                .setPath(request.getContextPath()).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(final HttpRequestMethodNotSupportedException ex,
                                                                         final HttpHeaders headers, final HttpStatus status, final WebRequest request) {
        LOG.error("handleHttpRequestMethodNotSupported({}, {}, {}, {})", new Object[]{ex, headers, status, request});

        final StringBuilder builder = new StringBuilder();
        builder.append(ex.getMethod());
        builder.append(" method is not supported for this request. Supported methods are ");
        ex.getSupportedHttpMethods().forEach(t -> builder.append(t + " "));

        final ApiErrorTraining apiError = new ApiErrorTraining.ApiErrorBuilder(HttpStatus.METHOD_NOT_ALLOWED, ex.getLocalizedMessage())
                .setPath(request.getContextPath()).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(final HttpMediaTypeNotSupportedException ex, final HttpHeaders headers,
                                                                     final HttpStatus status, final WebRequest request) {

        final StringBuilder builder = new StringBuilder();
        builder.append(ex.getContentType());
        builder.append(" media type is not supported. Supported media types are ");
        ex.getSupportedMediaTypes().forEach(t -> builder.append(t + " "));

        final ApiErrorTraining apiError =
                new ApiErrorTraining.ApiErrorBuilder(HttpStatus.UNSUPPORTED_MEDIA_TYPE, builder.substring(0, builder.length() - 2))
                        .setPath(request.getContextPath()).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler(BadGatewayException.class)
    public ResponseEntity<Object> handleBadGatewayException(final BadGatewayException ex, final WebRequest request, HttpServletRequest req) {

        final ApiErrorTraining apiError =
                new ApiErrorTraining.ApiErrorBuilder(BadGatewayException.class.getAnnotation(ResponseStatus.class).value(),
                        getInitialException(ex).getLocalizedMessage()).setError(BadGatewayException.class.getAnnotation(ResponseStatus.class).reason())
                        .setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Object> handleBadRequestException(final BadRequestException ex, final WebRequest request, HttpServletRequest req) {

        final ApiErrorTraining apiError =
                new ApiErrorTraining.ApiErrorBuilder(BadRequestException.class.getAnnotation(ResponseStatus.class).value(),
                        getInitialException(ex).getLocalizedMessage()).setError(BadRequestException.class.getAnnotation(ResponseStatus.class).reason())
                        .setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    public ResponseEntity<Object> handleForbiddenException(final ForbiddenException ex, final WebRequest request, HttpServletRequest req) {

        final ApiErrorTraining apiError =
                new ApiErrorTraining.ApiErrorBuilder(ForbiddenException.class.getAnnotation(ResponseStatus.class).value(), getInitialException(ex).getLocalizedMessage())
                        .setError(ForbiddenException.class.getAnnotation(ResponseStatus.class).reason()).setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler(GatewayTimeoutException.class)
    public ResponseEntity<Object> handleGatewayTimeoutException(final GatewayTimeoutException ex, final WebRequest request,
                                                                HttpServletRequest req) {

        final ApiErrorTraining apiError =
                new ApiErrorTraining.ApiErrorBuilder(GatewayTimeoutException.class.getAnnotation(ResponseStatus.class).value(),
                        ex.getLocalizedMessage()).setError(GatewayTimeoutException.class.getAnnotation(ResponseStatus.class).reason())
                        .setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler(HTTPVersionNotSupportedException.class)
    public ResponseEntity<Object> handleHTTPVersionNotSupportedException(final HTTPVersionNotSupportedException ex, final WebRequest request,
                                                                         HttpServletRequest req) {

        final ApiErrorTraining apiError =
                new ApiErrorTraining.ApiErrorBuilder(HTTPVersionNotSupportedException.class.getAnnotation(ResponseStatus.class).value(),
                        getInitialException(ex).getLocalizedMessage()).setError(HTTPVersionNotSupportedException.class.getAnnotation(ResponseStatus.class).reason())
                        .setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler(InsufficientStorageException.class)
    public ResponseEntity<Object> handleInsufficientStorageException(final InsufficientStorageException ex, final WebRequest request,
                                                                     HttpServletRequest req) {

        final ApiErrorTraining apiError =
                new ApiErrorTraining.ApiErrorBuilder(InsufficientStorageException.class.getAnnotation(ResponseStatus.class).value(),
                        getInitialException(ex).getLocalizedMessage()).setError(InsufficientStorageException.class.getAnnotation(ResponseStatus.class).reason())
                        .setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler(InternalServerErrorException.class)
    public ResponseEntity<Object> handleInternalServerErrorException(final InternalServerErrorException ex, final WebRequest request,
                                                                     HttpServletRequest req) {

        final ApiErrorTraining apiError =
                new ApiErrorTraining.ApiErrorBuilder(InternalServerErrorException.class.getAnnotation(ResponseStatus.class).value(),
                        getInitialException(ex).getLocalizedMessage()).setError(InternalServerErrorException.class.getAnnotation(ResponseStatus.class).reason())
                        .setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler(InvalidParameterException.class)
    public ResponseEntity<Object> handleInvalidParameterException(final InvalidParameterException ex, final WebRequest request,
                                                                  HttpServletRequest req) {

        final ApiErrorTraining apiError =
                new ApiErrorTraining.ApiErrorBuilder(InvalidParameterException.class.getAnnotation(ResponseStatus.class).value(),
                        getInitialException(ex).getLocalizedMessage()).setError(InvalidParameterException.class.getAnnotation(ResponseStatus.class).reason())
                        .setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler(LoopDetectedException.class)
    public ResponseEntity<Object> handleLoopDetectedException(final LoopDetectedException ex, final WebRequest request,
                                                              HttpServletRequest req) {

        final ApiErrorTraining apiError =
                new ApiErrorTraining.ApiErrorBuilder(LoopDetectedException.class.getAnnotation(ResponseStatus.class).value(),
                        getInitialException(ex).getLocalizedMessage()).setError(LoopDetectedException.class.getAnnotation(ResponseStatus.class).reason())
                        .setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler(MethodNotAllowedException.class)
    public ResponseEntity<Object> handleMethodNotAllowedException(final MethodNotAllowedException ex, final WebRequest request,
                                                                  HttpServletRequest req) {

        final ApiErrorTraining apiError =
                new ApiErrorTraining.ApiErrorBuilder(MethodNotAllowedException.class.getAnnotation(ResponseStatus.class).value(),
                        getInitialException(ex).getLocalizedMessage()).setError(MethodNotAllowedException.class.getAnnotation(ResponseStatus.class).reason())
                        .setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler(MovedPermanentlyException.class)
    public ResponseEntity<Object> handleMovedPermanentlyException(final MovedPermanentlyException ex, final WebRequest request,
                                                                  HttpServletRequest req) {

        final ApiErrorTraining apiError =
                new ApiErrorTraining.ApiErrorBuilder(MovedPermanentlyException.class.getAnnotation(ResponseStatus.class).value(),
                        getInitialException(ex).getLocalizedMessage()).setError(MovedPermanentlyException.class.getAnnotation(ResponseStatus.class).reason())
                        .setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler(NetworkAuthenticationRequiredException.class)
    public ResponseEntity<Object> handleNetworkAuthenticationRequiredException(final NetworkAuthenticationRequiredException ex,
                                                                               final WebRequest request, HttpServletRequest req) {

        final ApiErrorTraining apiError =
                new ApiErrorTraining.ApiErrorBuilder(NetworkAuthenticationRequiredException.class.getAnnotation(ResponseStatus.class).value(),
                        getInitialException(ex).getLocalizedMessage()).setError(NetworkAuthenticationRequiredException.class.getAnnotation(ResponseStatus.class).reason())
                        .setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler(NoContentException.class)
    public ResponseEntity<Object> handleNoContentException(final NoContentException ex, final WebRequest request, HttpServletRequest req) {

        final ApiErrorTraining apiError =
                new ApiErrorTraining.ApiErrorBuilder(NoContentException.class.getAnnotation(ResponseStatus.class).value(), getInitialException(ex).getLocalizedMessage())
                        .setError(NoContentException.class.getAnnotation(ResponseStatus.class).reason()).setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler(NotExtendedException.class)
    public ResponseEntity<Object> handleNotExtendedException(final NotExtendedException ex, final WebRequest request,
                                                             HttpServletRequest req) {

        final ApiErrorTraining apiError =
                new ApiErrorTraining.ApiErrorBuilder(NotExtendedException.class.getAnnotation(ResponseStatus.class).value(),
                        getInitialException(ex).getLocalizedMessage()).setError(NotExtendedException.class.getAnnotation(ResponseStatus.class).reason())
                        .setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler(NotImplementedException.class)
    public ResponseEntity<Object> handleNotImplementedException(final NotImplementedException ex, final WebRequest request,
                                                                HttpServletRequest req) {

        final ApiErrorTraining apiError =
                new ApiErrorTraining.ApiErrorBuilder(NotImplementedException.class.getAnnotation(ResponseStatus.class).value(),
                        getInitialException(ex).getLocalizedMessage()).setError(NotImplementedException.class.getAnnotation(ResponseStatus.class).reason())
                        .setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler(PayloadTooLargeException.class)
    public ResponseEntity<Object> handlePayloadTooLargeException(final PayloadTooLargeException ex, final WebRequest request,
                                                                 HttpServletRequest req) {

        final ApiErrorTraining apiError =
                new ApiErrorTraining.ApiErrorBuilder(PayloadTooLargeException.class.getAnnotation(ResponseStatus.class).value(),
                        getInitialException(ex).getLocalizedMessage()).setError(PayloadTooLargeException.class.getAnnotation(ResponseStatus.class).reason())
                        .setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler(ProxyAuthenticationRequiredException.class)
    public ResponseEntity<Object> handleProxyAuthenticationRequiredException(final ProxyAuthenticationRequiredException ex,
                                                                             final WebRequest request, HttpServletRequest req) {

        final ApiErrorTraining apiError =
                new ApiErrorTraining.ApiErrorBuilder(ProxyAuthenticationRequiredException.class.getAnnotation(ResponseStatus.class).value(),
                        getInitialException(ex).getLocalizedMessage()).setError(ProxyAuthenticationRequiredException.class.getAnnotation(ResponseStatus.class).reason())
                        .setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler(RangeNotSatisfiableException.class)
    public ResponseEntity<Object> handleRangeNotSatisfiableException(final RangeNotSatisfiableException ex, final WebRequest request,
                                                                     HttpServletRequest req) {

        final ApiErrorTraining apiError =
                new ApiErrorTraining.ApiErrorBuilder(RangeNotSatisfiableException.class.getAnnotation(ResponseStatus.class).value(),
                        getInitialException(ex).getLocalizedMessage()).setError(RangeNotSatisfiableException.class.getAnnotation(ResponseStatus.class).reason())
                        .setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler(RequestTimeoutException.class)
    public ResponseEntity<Object> handleRequestTimeoutException(final RequestTimeoutException ex, final WebRequest request,
                                                                HttpServletRequest req) {

        final ApiErrorTraining apiError =
                new ApiErrorTraining.ApiErrorBuilder(RequestTimeoutException.class.getAnnotation(ResponseStatus.class).value(),
                        getInitialException(ex).getLocalizedMessage()).setError(RequestTimeoutException.class.getAnnotation(ResponseStatus.class).reason())
                        .setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler(ResourceAlreadyExistingException.class)
    public ResponseEntity<Object> handleResourceAlreadyExistingException(final ResourceAlreadyExistingException ex, final WebRequest request,
                                                                         HttpServletRequest req) {

        final ApiErrorTraining apiError =
                new ApiErrorTraining.ApiErrorBuilder(ResourceAlreadyExistingException.class.getAnnotation(ResponseStatus.class).value(),
                        getInitialException(ex).getLocalizedMessage()).setError(ResourceAlreadyExistingException.class.getAnnotation(ResponseStatus.class).reason())
                        .setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler({ResourceNotCreatedException.class})
    public ResponseEntity<Object> handleResourceNotCreatedException(final ResourceNotCreatedException ex, final WebRequest request,
                                                                    HttpServletRequest req) {

        final ApiErrorTraining apiError =
                new ApiErrorTraining.ApiErrorBuilder(ResourceNotCreatedException.class.getAnnotation(ResponseStatus.class).value(),
                        getInitialException(ex).getLocalizedMessage()).setError(ResourceNotCreatedException.class.getAnnotation(ResponseStatus.class).reason())
                        .setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(final ResourceNotFoundException ex, final WebRequest request,
                                                                  HttpServletRequest req) {

        final ApiErrorTraining apiError =
                new ApiErrorTraining.ApiErrorBuilder(ResourceNotFoundException.class.getAnnotation(ResponseStatus.class).value(),
                        getInitialException(ex).getLocalizedMessage()).setError(ResourceNotFoundException.class.getAnnotation(ResponseStatus.class).reason())
                        .setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler({ResourceNotModifiedException.class})
    public ResponseEntity<Object> handleResourceNotModifiedException(final ResourceNotModifiedException ex, final WebRequest request,
                                                                     HttpServletRequest req) {

        final ApiErrorTraining apiError =
                new ApiErrorTraining.ApiErrorBuilder(ResourceNotModifiedException.class.getAnnotation(ResponseStatus.class).value(),
                        getInitialException(ex).getLocalizedMessage()).setError(ResourceNotModifiedException.class.getAnnotation(ResponseStatus.class).reason())
                        .setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler({ConflictException.class})
    public ResponseEntity<Object> handleConflictException(final ConflictException ex, final WebRequest request,
                                                                     HttpServletRequest req) {
        final ApiErrorTraining apiError =
                new ApiErrorTraining.ApiErrorBuilder(ConflictException.class.getAnnotation(ResponseStatus.class).value(),
                        getInitialException(ex).getLocalizedMessage()).setError(ConflictException.class.getAnnotation(ResponseStatus.class).reason())
                        .setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler({ServiceUnavailableException.class})
    public ResponseEntity<Object> handleServiceUnavailableException(final ServiceUnavailableException ex, final WebRequest request,
                                                                    HttpServletRequest req) {

        final ApiErrorTraining apiError =
                new ApiErrorTraining.ApiErrorBuilder(ServiceUnavailableException.class.getAnnotation(ResponseStatus.class).value(),
                        getInitialException(ex).getLocalizedMessage()).setError(ServiceUnavailableException.class.getAnnotation(ResponseStatus.class).reason())
                        .setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler({TooManyRequestsException.class})
    public ResponseEntity<Object> handleTooManyRequestsException(final TooManyRequestsException ex, final WebRequest request,
                                                                 HttpServletRequest req) {

        final ApiErrorTraining apiError =
                new ApiErrorTraining.ApiErrorBuilder(TooManyRequestsException.class.getAnnotation(ResponseStatus.class).value(),
                        getInitialException(ex).getLocalizedMessage()).setError(TooManyRequestsException.class.getAnnotation(ResponseStatus.class).reason())
                        .setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler({UnauthorizedException.class})
    public ResponseEntity<Object> handleUnauthorizedException(final UnauthorizedException ex, final WebRequest request,
                                                              HttpServletRequest req) {

        final ApiErrorTraining apiError =
                new ApiErrorTraining.ApiErrorBuilder(UnauthorizedException.class.getAnnotation(ResponseStatus.class).value(),
                        getInitialException(ex).getLocalizedMessage()).setError(UnauthorizedException.class.getAnnotation(ResponseStatus.class).reason())
                        .setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler({UnprocessableEntityException.class})
    public ResponseEntity<Object> handleUnprocessableEntityException(final UnprocessableEntityException ex, final WebRequest request,
                                                                     HttpServletRequest req) {

        final ApiErrorTraining apiError =
                new ApiErrorTraining.ApiErrorBuilder(UnprocessableEntityException.class.getAnnotation(ResponseStatus.class).value(),
                        getInitialException(ex).getLocalizedMessage()).setError(UnprocessableEntityException.class.getAnnotation(ResponseStatus.class).reason())
                        .setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler({UnsupportedMediaTypeException.class})
    public ResponseEntity<Object> handleUnsupportedMediaTypeException(final UnsupportedMediaTypeException ex, final WebRequest request,
                                                                      HttpServletRequest req) {

        final ApiErrorTraining apiError =
                new ApiErrorTraining.ApiErrorBuilder(UnsupportedMediaTypeException.class.getAnnotation(ResponseStatus.class).value(),
                        getInitialException(ex).getLocalizedMessage()).setError(UnsupportedMediaTypeException.class.getAnnotation(ResponseStatus.class).reason())
                        .setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler({URITooLongException.class})
    public ResponseEntity<Object> handleURITooLongException(final URITooLongException ex, final WebRequest request, HttpServletRequest req) {

        final ApiErrorTraining apiError =
                new ApiErrorTraining.ApiErrorBuilder(URITooLongException.class.getAnnotation(ResponseStatus.class).value(),
                        getInitialException(ex).getLocalizedMessage()).setError(URITooLongException.class.getAnnotation(ResponseStatus.class).reason())
                        .setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler({VariantAlsoNegotiatesException.class})
    public ResponseEntity<Object> handleVariantAlsoNegotiatesException(final VariantAlsoNegotiatesException ex, final WebRequest request,
                                                                       HttpServletRequest req) {
        LOG.error("handleVariantAlsoNegotiatesException({}, {}, {})", new Object[]{ex, request, req});

        final ApiErrorTraining apiError =
                new ApiErrorTraining.ApiErrorBuilder(VariantAlsoNegotiatesException.class.getAnnotation(ResponseStatus.class).value(),
                        getInitialException(ex).getLocalizedMessage()).setError(VariantAlsoNegotiatesException.class.getAnnotation(ResponseStatus.class).reason())
                        .setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler({java.nio.file.AccessDeniedException.class})
    public ResponseEntity<Object> handleAccessDeniedException(java.nio.file.AccessDeniedException ex, WebRequest request) {

        return new ResponseEntity<>("Access denied message here", new HttpHeaders(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({org.springframework.security.access.AccessDeniedException.class})
    public ResponseEntity<Object> handleSpringAccessDeniedException(org.springframework.security.access.AccessDeniedException ex, WebRequest request, HttpServletRequest req) {

        final ApiErrorTraining apiError = new ApiErrorTraining.ApiErrorBuilder(HttpStatus.FORBIDDEN, ex.getLocalizedMessage()).setError("Access denied")
                .setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(final IllegalArgumentException ex, final WebRequest request,
                                                                 HttpServletRequest req) {

        final ApiErrorTraining apiError = new ApiErrorTraining.ApiErrorBuilder(HttpStatus.NOT_ACCEPTABLE, getInitialException(ex).getLocalizedMessage())
                .setError("Illegal Argument").setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Object> handleMethodArgumentTypeMismatch(final MethodArgumentTypeMismatchException ex, final WebRequest request,
                                                                   HttpServletRequest req) {

        final String error = ex.getName() + " should be of type " + ex.getRequiredType().getName();
        final ApiErrorTraining apiError = new ApiErrorTraining.ApiErrorBuilder(HttpStatus.BAD_REQUEST, getInitialException(ex).getLocalizedMessage()).setError(error)
                .setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler({ConstraintViolationException.class})
    public ResponseEntity<Object> handleConstraintViolation(final ConstraintViolationException ex,
                                                            HttpServletRequest req) {


        System.out.println("Constraint violation reached..." + System.lineSeparator() + System.lineSeparator() + System.lineSeparator());
        final List<String> errors = new ArrayList<>();
        for (final ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            errors.add(violation.getRootBeanClass().getName() + " " + violation.getPropertyPath() + ": " + violation.getMessage());
        }

        final ApiErrorTraining apiError = new ApiErrorTraining.ApiErrorBuilder(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage())
                .setErrors(errors).setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<Object> handleAll(final Exception ex, final WebRequest request, HttpServletRequest req) {

        String err = "Some error occurred. Please try again or contact administrator.";

        final ApiErrorTraining apiError = new ApiErrorTraining.ApiErrorBuilder(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage())
                .setError(err).setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    private Exception getInitialException(Exception exception) {
        while(exception.getCause() != null)  {
            exception = (Exception) exception.getCause();
        }
        return exception;

    }

}
