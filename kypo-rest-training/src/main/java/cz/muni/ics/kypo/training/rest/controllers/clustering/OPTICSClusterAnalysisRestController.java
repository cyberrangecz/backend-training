package cz.muni.ics.kypo.training.rest.controllers.clustering;

import cz.muni.ics.kypo.training.api.dto.visualization.clusteranalysis.OPTICSParametersDTO;
import cz.muni.ics.kypo.training.facade.clustering.OPTICSClusterAnalysisFacade;
import cz.muni.ics.kypo.training.rest.ApiError;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for clustering visualization utilising OPTICS algorithm.
 */
@Api(value = "/clusters/optics",
        tags = "OPTICS Clustering",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        authorizations = @Authorization(value = "bearerAuth"))
@ApiResponses(value = {
        @ApiResponse(code = 401, message = "Full authentication is required to access this resource.", response = ApiError.class),
        @ApiResponse(code = 403, message = "The necessary permissions are required for a resource.", response = ApiError.class)
})
@RestController
@RequestMapping(value = "/clusters/optics", produces = MediaType.APPLICATION_JSON_VALUE)
public class OPTICSClusterAnalysisRestController extends AbstractClusterAnalysisRestController<OPTICSParametersDTO> {
    
    @Autowired
    public OPTICSClusterAnalysisRestController(OPTICSClusterAnalysisFacade clusterKMeansAnalysisFacade) {
        super(clusterKMeansAnalysisFacade);
    }
}
