package cz.muni.ics.kypo.training.rest.controllers.clustering;

import cz.muni.ics.kypo.training.api.dto.visualization.clusteranalysis.KMeansParameters;
import cz.muni.ics.kypo.training.api.dto.visualization.clustering.EventsFilter;
import cz.muni.ics.kypo.training.api.enums.NormalizationStrategy;
import cz.muni.ics.kypo.training.facade.clustering.AbstractClusterAnalysisFacade;
import cz.muni.ics.kypo.training.facade.clustering.KmeansClusterAnalysisFacade;
import cz.muni.ics.kypo.training.rest.ApiError;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller for clustering visualization utilising KMeans algorithm and providing additional methods for assessing
 * the quality of the clustering.
 */
@Api(value = "/clusters/kmeans",
        tags = "KMeans Clustering",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        authorizations = @Authorization(value = "bearerAuth"))
@ApiResponses(value = {
        @ApiResponse(code = 401, message = "Full authentication is required to access this resource.", response = ApiError.class),
        @ApiResponse(code = 403, message = "The necessary permissions are required for a resource.", response = ApiError.class)
})
@RestController
@RequestMapping(value = "/clusters/kmeans", produces = MediaType.APPLICATION_JSON_VALUE)
public class KmeansClusterAnalysisRestController extends AbstractClusterAnalysisRestController<KMeansParameters> {

    private final KmeansClusterAnalysisFacade clusterKMeansAnalysisFacade;

    public KmeansClusterAnalysisRestController(KmeansClusterAnalysisFacade clusterKMeansAnalysisFacade) {
        super();
        this.clusterKMeansAnalysisFacade = clusterKMeansAnalysisFacade;
    }

    @Override
    AbstractClusterAnalysisFacade<KMeansParameters> getClusterAnalysisFacade() {
        return clusterKMeansAnalysisFacade;
    }

    /**
     * Get sum of squared errors for n-dimensional clusters
     *
     * @param definitionId        id of definition
     * @param instanceIds         optional list of instance ids (all instances must be from the same definition)
     * @param levelId             optional level id
     * @param algorithmParameters algorithm specific parameters
     * @return list of values
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get n-dimensional cluster SSE.",
            response = Double[].class,
            nickname = "getNDimensionalClusterSSE",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "N-dimensional cluster SSE found.", response = Double[].class),
            @ApiResponse(code = 404, message = "Training run with given id not found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/training-definitions/{definitionId}/n-dimensional/sse", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Double>> getNDimensionalClusterSSE(
            @ApiParam(value = "Training definition ID", required = true) @PathVariable("definitionId") Long definitionId,
            @ApiParam(value = "List of training instance IDs", required = false) @RequestParam(value = "instanceIds", required = false) List<Long> instanceIds,
            @ApiParam(value = "Level id", required = false) @RequestParam(value = "levelId", required = false) Long levelId,
            @ApiParam(value = "Normalization strategy", required = false, defaultValue = "MIN_MAX")
            @RequestParam(value = "normalizationStrategy", required = false, defaultValue = "MIN_MAX") NormalizationStrategy normalizationStrategy,
            @ApiParam(value = "Algorithm parameters", required = true) @RequestBody KMeansParameters algorithmParameters) {
        List<Double> nDimensionalClusterSSE = clusterKMeansAnalysisFacade.getNDimensionalClusterSSE(
                new EventsFilter(definitionId, instanceIds, levelId),
                algorithmParameters,
                normalizationStrategy);
        return ResponseEntity.ok(nDimensionalClusterSSE);
    }

    /**
     * Get sum of squared errors for wrong answers clusters
     *
     * @param definitionId        id of definition
     * @param instanceIds         optional list of instance ids (all instances must be from the same definition)
     * @param levelId             optional level id
     * @param algorithmParameters algorithm specific parameters
     * @return list of values
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get wrong answers SSE.",
            response = Double[].class,
            nickname = "getWrongAnswersClusterSSE",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Wrong answers clusters SSE found.", response = Double[].class),
            @ApiResponse(code = 404, message = "Training run with given id not found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/training-definitions/{definitionId}/wrong-answers/sse", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Double>> getWrongAnswersClusterSSE(
            @ApiParam(value = "Training definition ID", required = true) @PathVariable("definitionId") Long definitionId,
            @ApiParam(value = "List of training instance IDs", required = false) @RequestParam(value = "instanceIds", required = false) List<Long> instanceIds,
            @ApiParam(value = "Level id", required = false) @RequestParam(value = "levelId", required = false) Long levelId,
            @ApiParam(value = "Normalization strategy", required = false, defaultValue = "MIN_MAX")
            @RequestParam(value = "normalizationStrategy", required = false, defaultValue = "MIN_MAX") NormalizationStrategy normalizationStrategy,
            @ApiParam(value = "Algorithm parameters", required = true) @RequestBody KMeansParameters algorithmParameters) {
        List<Double> wrongAnswersSSE = clusterKMeansAnalysisFacade.getWrongAnswersClusterSEE(
                new EventsFilter(definitionId, instanceIds, levelId),
                algorithmParameters,
                normalizationStrategy);
        return ResponseEntity.ok(wrongAnswersSSE);
    }


    /**
     * Get sum of squared errors for time-spent clusters
     *
     * @param definitionId        id of definition
     * @param instanceIds         optional list of instance ids (all instances must be from the same definition)
     * @param levelId             optional level id
     * @param algorithmParameters algorithm specific parameters
     * @return list of values
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get wrong answers SSE.",
            response = Double[].class,
            nickname = "getWrongAnswersClusterSSE",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Time spent after hint cluster SSE found.", response = Double[].class),
            @ApiResponse(code = 404, message = "Training run with given id not found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/training-definitions/{definitionId}/hint-time/sse", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Double>> getTimeAfterHintSSE(
            @ApiParam(value = "Training definition ID", required = true) @PathVariable("definitionId") Long definitionId,
            @ApiParam(value = "List of training instance IDs", required = false) @RequestParam(value = "instanceIds", required = false) List<Long> instanceIds,
            @ApiParam(value = "Level id", required = false) @RequestParam(value = "levelId", required = false) Long levelId,
            @ApiParam(value = "Normalization strategy", required = false, defaultValue = "MIN_MAX")
            @RequestParam(value = "normalizationStrategy", required = false, defaultValue = "MIN_MAX") NormalizationStrategy normalizationStrategy,
            @ApiParam(value = "Algorithm parameters", required = true) @RequestBody KMeansParameters algorithmParameters) {
        List<Double> timeAfterHintSSE = clusterKMeansAnalysisFacade.getTimeAfterHintClusterSSE(
                new EventsFilter(definitionId, instanceIds, levelId),
                algorithmParameters,
                normalizationStrategy);
        return ResponseEntity.ok(timeAfterHintSSE);
    }
}
