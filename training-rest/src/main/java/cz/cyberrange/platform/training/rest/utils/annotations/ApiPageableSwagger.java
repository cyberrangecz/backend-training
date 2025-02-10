package cz.cyberrange.platform.training.rest.utils.annotations;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The interface Api pageable swagger.
 */
@Target({METHOD, ANNOTATION_TYPE, TYPE})
@Retention(RUNTIME)
@ApiImplicitParams({
        @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query", value = "Results page you want to retrieve (0..N)", example = "0"),
        @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query", value = "Number of records per page.", example = "20"),
        @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query", value = "Sorting criteria in the format: property(,asc|desc). "
                + "Default sort order is ascending. " + "Multiple sort criteria are supported.", example = "asc")})
public @interface ApiPageableSwagger {
}

