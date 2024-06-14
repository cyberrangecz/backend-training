package cz.muni.ics.kypo.training.rest.controllers.clustering;

import cz.muni.ics.kypo.training.api.dto.visualization.VisualizationInfoDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.clusteranalysis.ClusterDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.clusteranalysis.TimeAfterHintClusterableDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.clusteranalysis.TimeAfterSolutionClusterableDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.clusteranalysis.WrongAnswersClusterableDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.clustering.EventsFilter;
import cz.muni.ics.kypo.training.api.enums.NormalizationStrategy;
import cz.muni.ics.kypo.training.facade.clustering.AbstractClusterAnalysisFacade;
import cz.muni.ics.kypo.training.rest.ApiError;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.math3.stat.clustering.EuclideanDoublePoint;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * The abstract rest controller for clustering visualizations.
 * Allows for getting clusters with different clustering algorithm parameters and adding additional analysis methods.
 */
public abstract class AbstractClusterAnalysisRestController<T> {

    abstract AbstractClusterAnalysisFacade<T> getClusterAnalysisFacade();

    /**
     * Get N-dimensional clusters
     *
     * @param definitionId        id of definition
     * @param instanceIds         optional list of instance ids (all instances must be from the same definition)
     * @param levelId             optional level id
     * @param algorithmParameters algorithm specific parameters
     * @return list of {@link ClusterDTO}s of {@link EuclideanDoublePoint}
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get n-dimensional cluster.",
            response = VisualizationInfoDTO.class,
            nickname = "getNDimensionalCluster",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "N-dimensional cluster found.", response = VisualizationInfoDTO.class),
            @ApiResponse(code = 404, message = "Training run with given id not found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/training-definitions/{definitionId}/n-dimensional", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ClusterDTO<EuclideanDoublePoint>>> getNDimensionalCluster(
            @ApiParam(value = "Training definition ID", required = true) @PathVariable("definitionId") Long definitionId,
            @ApiParam(value = "List of training instance IDs", required = false) @RequestParam(value = "instanceIds", required = false) List<Long> instanceIds,
            @ApiParam(value = "Level id", required = false) @RequestParam(value = "levelId", required = false) Long levelId,
            @ApiParam(value = "Normalization strategy", required = true) @RequestParam(value = "normalizationStrategy", required = true) NormalizationStrategy normalizationStrategy,
            @ApiParam(value = "Algorithm parameters", required = true) @RequestBody T algorithmParameters) {
        List<ClusterDTO<EuclideanDoublePoint>> nDimensionalCluster = getClusterAnalysisFacade()
                .getNDimensionalCluster(
                        new EventsFilter(definitionId, instanceIds, levelId),
                        algorithmParameters,
                        normalizationStrategy);
        return ResponseEntity.ok(nDimensionalCluster);
    }

    /**
     * Get wrong answers clusters
     *
     * @param definitionId        id of definition
     * @param instanceIds         optional list of instance ids (all instances must be from the same definition)
     * @param levelId             optional level id
     * @param algorithmParameters algorithm specific parameters
     * @return list of {@link ClusterDTO}s of {@link WrongAnswersClusterableDTO}
     */
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Wrong answers cluster found.", response = VisualizationInfoDTO.class),
            @ApiResponse(code = 404, message = "Training run with given id not found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/training-definitions/{definitionId}/wrong-answers", produces =
            MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ClusterDTO<WrongAnswersClusterableDTO>>> getWrongAnswersCluster(
            @ApiParam(value = "Training definition ID", required = true) @PathVariable("definitionId") Long definitionId,
            @ApiParam(value = "List of training instance IDs", required = false) @RequestParam(value = "instanceIds", required = false) List<Long> instanceIds,
            @ApiParam(value = "Level id", required = false) @RequestParam(value = "levelId", required = false) Long levelId,
            @ApiParam(value = "Normalization strategy", required = true) @RequestParam(value = "normalizationStrategy", required = true) NormalizationStrategy normalizationStrategy,
            @ApiParam(value = "Algorithm parameters", required = true) @RequestBody T algorithmParameters) {
        List<ClusterDTO<WrongAnswersClusterableDTO>> wrongAnswers =
                getClusterAnalysisFacade().getWrongAnswersCluster(
                        new EventsFilter(definitionId, instanceIds, levelId),
                        algorithmParameters,
                        normalizationStrategy);
        return ResponseEntity.ok(wrongAnswers);
    }

    /**
     * Get time spent after using hint clusters
     *
     * @param definitionId        id of definition
     * @param instanceIds         optional list of instance ids (all instances must be from the same definition)
     * @param levelId             optional level id
     * @param algorithmParameters algorithm specific parameters
     * @return list of {@link ClusterDTO}s of {@link TimeAfterHintClusterableDTO}
     */
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Time spent after hint cluster found.", response =
                    VisualizationInfoDTO.class),
            @ApiResponse(code = 404, message = "Training run with given id not found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/training-definitions/{definitionId}/hint-time", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ClusterDTO<TimeAfterHintClusterableDTO>>> getTimeAfterHintCluster(
            @ApiParam(value = "Training definition ID", required = true) @PathVariable("definitionId") Long definitionId,
            @ApiParam(value = "List of training instance IDs", required = false) @RequestParam(value = "instanceIds", required = false) List<Long> instanceIds,
            @ApiParam(value = "Level id", required = false) @RequestParam(value = "levelId", required = false) Long levelId,
            @ApiParam(value = "Normalization strategy", required = true) @RequestParam(value = "normalizationStrategy", required = true) NormalizationStrategy normalizationStrategy,
            @ApiParam(value = "Algorithm parameters", required = true) @RequestBody T algorithmParameters) {
        List<ClusterDTO<TimeAfterHintClusterableDTO>> timeAfterHint =
                getClusterAnalysisFacade().getTimeAfterHintCluster(
                        new EventsFilter(definitionId, instanceIds, levelId),
                        algorithmParameters,
                        normalizationStrategy);
        return ResponseEntity.ok(timeAfterHint);
    }


    /**
     * Get time spent after solution displayed clusters
     *
     * @param definitionId        id of definition
     * @param instanceIds         optional list of instance ids (all instances must be from the same definition)
     * @param levelId             optional level id
     * @param algorithmParameters algorithm specific parameters
     * @return list of {@link Cluster}s of {@link TimeAfterSolutionClusterableDTO}
     */
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Time after solution shown cluster found.", response =
                    VisualizationInfoDTO.class),
            @ApiResponse(code = 404, message = "Training run with given id not found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/training-definitions/{definitionId}/solution-shown-time", produces =
            MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ClusterDTO<TimeAfterSolutionClusterableDTO>>> getTimeAfterSolutionCluster(
            @ApiParam(value = "Training definition ID", required = true) @PathVariable("definitionId") Long definitionId,
            @ApiParam(value = "List of training instance IDs", required = false) @RequestParam(value = "instanceIds", required = false) List<Long> instanceIds,
            @ApiParam(value = "Level id", required = false) @RequestParam(value = "levelId", required = false) Long levelId,
            @ApiParam(value = "Normalization strategy", required = true) @RequestParam(value = "normalizationStrategy", required = true) NormalizationStrategy normalizationStrategy,
            @ApiParam(value = "Algorithm parameters", required = true) @RequestBody T algorithmParameters) {
        List<ClusterDTO<TimeAfterSolutionClusterableDTO>> timeAfterSolution =
                getClusterAnalysisFacade().getTimeAfterSolutionCluster(
                        new EventsFilter(definitionId, instanceIds, levelId),
                        algorithmParameters,
                        normalizationStrategy
                );
        return ResponseEntity.ok(timeAfterSolution);
    }

}
