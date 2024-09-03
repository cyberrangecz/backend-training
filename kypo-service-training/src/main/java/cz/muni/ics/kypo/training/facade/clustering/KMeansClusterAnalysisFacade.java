package cz.muni.ics.kypo.training.facade.clustering;

import cz.muni.ics.kypo.training.api.dto.visualization.clusteranalysis.ClusterDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.clusteranalysis.KMeansParametersDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.clustering.EventsFilter;
import cz.muni.ics.kypo.training.api.enums.NormalizationStrategy;
import cz.muni.ics.kypo.training.service.TrainingInstanceService;
import cz.muni.ics.kypo.training.service.api.ElasticsearchApiService;
import cz.muni.ics.kypo.training.service.clustering.ClusterableDataTransformer;
import cz.muni.ics.kypo.training.service.clustering.ELKIDataTransformer;
import elki.clustering.kmeans.LloydKMeans;
import elki.clustering.kmeans.initialization.KMeansPlusPlus;
import elki.data.Clustering;
import elki.data.NumberVector;
import elki.data.model.KMeansModel;
import elki.data.type.TypeUtil;
import elki.database.Database;
import elki.distance.minkowski.SquaredEuclideanDistance;
import elki.utilities.ELKIBuilder;
import elki.utilities.exceptions.AbortException;
import elki.utilities.random.RandomFactory;
import org.apache.commons.math3.stat.clustering.Clusterable;
import org.apache.commons.math3.stat.clustering.EuclideanDoublePoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Facade for K-Means clustering analysis.
 */
@Service
@Transactional
public class KMeansClusterAnalysisFacade extends AbstractClusterAnalysisFacade<KMeansParametersDTO> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public KMeansClusterAnalysisFacade(ElasticsearchApiService elasticsearchApiService,
                                       TrainingInstanceService trainingInstanceService,
                                       ClusterableDataTransformer clusterableDataTransformer,
                                       ELKIDataTransformer elkiDataTransformer) {
        super(elasticsearchApiService, trainingInstanceService, clusterableDataTransformer, elkiDataTransformer);
    }

    /**
     * Calculate the sum of squared errors for clusters of wrong answers.
     *
     * @param filter                events to be clustered
     * @param kMeansParameters      parameters for clustering algorithm
     * @param normalizationStrategy normalization strategy
     * @return list of SSEs
     */
    @PreAuthorize("hasAnyAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR, " +
            "T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ORGANIZER)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    public List<Double> getWrongAnswersClusterSEE(EventsFilter filter, KMeansParametersDTO kMeansParameters,
                                                  NormalizationStrategy normalizationStrategy) {
        return IntStream.range(2, kMeansParameters.getNumberOfClusters() + 1)
                .mapToObj(i -> calculateSSE(
                        super.getWrongAnswersCluster(filter, new KMeansParametersDTO(i), normalizationStrategy)
                )).toList();
    }

    /**
     * Calculate the sum of squared errors for clusters of hints.
     *
     * @param filter                events to be clustered
     * @param kMeansParameters      parameters for clustering algorithm
     * @param normalizationStrategy normalization strategy
     * @return list of SSEs
     */
    @PreAuthorize("hasAnyAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR, " +
            "T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ORGANIZER)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    public List<Double> getTimeAfterHintClusterSSE(EventsFilter filter, KMeansParametersDTO kMeansParameters,
                                                   NormalizationStrategy normalizationStrategy) {
        return IntStream.range(2, kMeansParameters.getNumberOfClusters() + 1)
                .mapToObj(i -> calculateSSE(
                        super.getTimeAfterHintCluster(filter, new KMeansParametersDTO(i), normalizationStrategy)
                )).toList();
    }

    /**
     * Calculate the sum of squared errors for clusters of solutions.
     *
     * @param filter                events to be clustered
     * @param kMeansParameters      parameters for clustering algorithm
     * @param normalizationStrategy normalization strategy
     * @return list of SSEs
     */
    @PreAuthorize("hasAnyAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR, " +
            "T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ORGANIZER)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    public List<Double> getTimeAfterSolutionClusterSSE(EventsFilter filter, KMeansParametersDTO kMeansParameters,
                                                       NormalizationStrategy normalizationStrategy) {
        return IntStream.range(2, kMeansParameters.getNumberOfClusters() + 1)
                .mapToObj(i -> calculateSSE(
                        super.getTimeAfterSolutionCluster(filter, new KMeansParametersDTO(i), normalizationStrategy)
                )).toList();
    }


    /**
     * Calculate the sum of squared errors for a collective cluster.
     *
     * @param filter                events to be clustered
     * @param kMeansParameters      parameters for clustering algorithm
     * @param normalizationStrategy normalization strategy
     * @return list of SSEs
     */
    @PreAuthorize("hasAnyAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR, " +
            "T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ORGANIZER)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    public List<Double> getNDimensionalClusterSSE(EventsFilter filter, KMeansParametersDTO kMeansParameters,
                                                  NormalizationStrategy normalizationStrategy) {
        return IntStream.range(2, kMeansParameters.getNumberOfClusters() + 1)
                .mapToObj(i -> calculateSSE(
                        super.getNDimensionalCluster(filter, new KMeansParametersDTO(i), normalizationStrategy)
                )).toList();
    }

    @Override
    protected <C extends Clusterable<C>> List<ClusterDTO<C>> getClusters(Database elkiDatabase,
                                                                         KMeansParametersDTO algorithmParameters,
                                                                         Class<C> clazz) {
        Clustering<KMeansModel> clustering = executeAlgorithm(elkiDatabase, algorithmParameters);

        return elkiDataTransformer
                .transformElkiOutputToClusters(
                        clustering.getAllClusters(),
                        null,
                        elkiDatabase.getRelation(TypeUtil.NUMBER_VECTOR_FIELD), // vectors
                        clazz.equals(EuclideanDoublePoint.class) ? null : elkiDatabase.getRelation(TypeUtil.LABELLIST), // labels
                        clazz
                );
    }

    private Clustering<KMeansModel> executeAlgorithm(Database db, KMeansParametersDTO algorithmParameters) {
        int numberOfClusters = algorithmParameters.getNumberOfClusters();

        try {
            LloydKMeans<NumberVector> lloydKMeans = buildKMeansPlusPlus(numberOfClusters);
            return lloydKMeans.autorun(db);
        } catch (AbortException exception) {
            logger.error(exception.getMessage());
            logger.warn("K-Means parameters:");
            logger.warn(String.format("\tnumberOfClusters: %d (should be greater equal than 1)",
                    numberOfClusters));
        } catch (IllegalArgumentException exception) {
            logger.warn(String.format("The number of clusters (%d) is greater than the number of objects!",
                    numberOfClusters));
        }
        return new Clustering<>();
    }

    private LloydKMeans<NumberVector> buildKMeansPlusPlus(int numberOfClusters) {
        return new ELKIBuilder<>(LloydKMeans.class)
                .with(LloydKMeans.DISTANCE_FUNCTION_ID, SquaredEuclideanDistance.STATIC)
                .with(LloydKMeans.K_ID, numberOfClusters)
                .with(LloydKMeans.INIT_ID, new KMeansPlusPlus<>(RandomFactory.DEFAULT))
                .build();
    }


    private <T extends Clusterable<T>> double calculateSSE(List<ClusterDTO<T>> clusters) {
        double sum = 0;
        for (ClusterDTO<T> cluster : clusters) {
            T center = cluster.getCenter();
            for (T point : cluster.getPoints()) {
                double d = center.distanceFrom(point);
                sum += Math.pow(d, 2);
            }
        }
        return sum;
    }
}
