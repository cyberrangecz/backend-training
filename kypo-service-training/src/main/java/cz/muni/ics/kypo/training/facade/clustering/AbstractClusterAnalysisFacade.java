package cz.muni.ics.kypo.training.facade.clustering;

import cz.muni.csirt.kypo.events.AbstractAuditPOJO;
import cz.muni.ics.kypo.training.api.dto.visualization.clusteranalysis.ClusterDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.clusteranalysis.TimeAfterHintClusterableDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.clusteranalysis.TimeAfterSolutionClusterableDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.clusteranalysis.WrongAnswersClusterableDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.clustering.EventsFilter;
import cz.muni.ics.kypo.training.api.enums.NormalizationStrategy;
import cz.muni.ics.kypo.training.exceptions.EntityConflictException;
import cz.muni.ics.kypo.training.exceptions.EntityErrorDetail;
import cz.muni.ics.kypo.training.persistence.model.TrainingInstance;
import cz.muni.ics.kypo.training.service.TrainingInstanceService;
import cz.muni.ics.kypo.training.service.api.ElasticsearchApiService;
import cz.muni.ics.kypo.training.service.clustering.ClusterableDataTransformer;
import cz.muni.ics.kypo.training.service.clustering.ELKIDataTransformer;
import elki.database.Database;
import org.apache.commons.math3.stat.clustering.Clusterable;
import org.apache.commons.math3.stat.clustering.EuclideanDoublePoint;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * Abstract class for clustering analysis facade.
 * Allows for clustering analysis with different algorithms.
 *
 * @param <T> type of algorithm parameters
 */
public abstract class AbstractClusterAnalysisFacade<T> {


    protected final ElasticsearchApiService elasticsearchApiService;
    protected final TrainingInstanceService trainingInstanceService;
    protected final ClusterableDataTransformer clusterableDataTransformer;
    protected final ELKIDataTransformer elkiDataTransformer;

    AbstractClusterAnalysisFacade(ElasticsearchApiService elasticsearchApiService,
                                  TrainingInstanceService trainingInstanceService,
                                  ClusterableDataTransformer clusterableDataTransformer,
                                  ELKIDataTransformer elkiDataTransformer) {
        this.elasticsearchApiService = elasticsearchApiService;
        this.trainingInstanceService = trainingInstanceService;
        this.clusterableDataTransformer = clusterableDataTransformer;
        this.elkiDataTransformer = elkiDataTransformer;
    }


    /**
     * Create collective cluster from training events.
     *
     * @param filter                events to be clustered
     * @param algorithmParameters   parameters for clustering algorithm
     * @param normalizationStrategy normalization strategy
     * @return list of clusters
     */
    @PreAuthorize("hasAnyAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR, " +
            "T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ORGANIZER)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    public List<ClusterDTO<EuclideanDoublePoint>> getNDimensionalCluster(EventsFilter filter,
                                                                         T algorithmParameters,
                                                                         NormalizationStrategy normalizationStrategy) {
        Database elkiDatabase = elkiDataTransformer
                .transformNDimensionalClusterableToElkiDatabase(
                        clusterableDataTransformer
                                .transformToNDimensionalClusterables(
                                        loadTrainingEvents(filter),
                                        normalizationStrategy));

        return getClusters(elkiDatabase, algorithmParameters, EuclideanDoublePoint.class);
    }


    /**
     * Create cluster from wrong answers.
     *
     * @param filter                events to be clustered
     * @param algorithmParameters   parameters for clustering algorithm
     * @param normalizationStrategy normalization strategy
     * @return list of clusters
     */
    @PreAuthorize("hasAnyAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR, " +
            "T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ORGANIZER)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    public List<ClusterDTO<WrongAnswersClusterableDTO>> getWrongAnswersCluster(EventsFilter filter,
                                                                               T algorithmParameters,
                                                                               NormalizationStrategy normalizationStrategy) {
        Database elkiDatabase = elkiDataTransformer
                .transformWrongFlagsClusterableToElkiInputFormat(
                        clusterableDataTransformer
                                .transformToWrongAnswersClusterableDTO(
                                        loadTrainingEvents(filter),
                                        normalizationStrategy));

        return getClusters(elkiDatabase, algorithmParameters, WrongAnswersClusterableDTO.class);
    }


    /**
     * Create cluster from time it took to solve after a hint.
     *
     * @param filter                events to be clustered
     * @param algorithmParameters   parameters for clustering algorithm
     * @param normalizationStrategy normalization strategy
     * @return list of clusters
     */
    @PreAuthorize("hasAnyAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR, " +
            "T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ORGANIZER)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    public List<ClusterDTO<TimeAfterHintClusterableDTO>> getTimeAfterHintCluster(EventsFilter filter,
                                                                                 T algorithmParameters,
                                                                                 NormalizationStrategy normalizationStrategy) {
        Database elkiDatabase = elkiDataTransformer.
                transformHintClusterableToElkiInputFormat(
                        clusterableDataTransformer
                                .transformToTimeAfterHintClusterableDTO(
                                        loadTrainingEvents(filter),
                                        normalizationStrategy));

        return getClusters(elkiDatabase, algorithmParameters, TimeAfterHintClusterableDTO.class);
    }


    /**
     * Create cluster from time it took to solve after displaying a solution.
     *
     * @param filter                events to be clustered
     * @param algorithmParameters   parameters for clustering algorithm
     * @param normalizationStrategy normalization strategy
     * @return list of clusters
     */
    @PreAuthorize("hasAnyAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR, " +
            "T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ORGANIZER)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    public List<ClusterDTO<TimeAfterSolutionClusterableDTO>> getTimeAfterSolutionCluster(EventsFilter filter,
                                                                                         T algorithmParameters,
                                                                                         NormalizationStrategy normalizationStrategy) {
        Database elkiDatabase = elkiDataTransformer
                .transformSolutionClusterableToElkiInputFormat(
                        clusterableDataTransformer
                                .transformToTimeAfterSolutionClusterableDTO(
                                        loadTrainingEvents(filter),
                                        normalizationStrategy));

        return getClusters(elkiDatabase, algorithmParameters, TimeAfterSolutionClusterableDTO.class);
    }


    /**
     * Retrieve clusters from database.
     * <p>
     * This method should be implemented in subclasses with specific clustering algorithm.
     * <p>
     * todo remove clazz parameter
     *
     * @param elkiDatabase        database with data
     * @param algorithmParameters parameters for clustering algorithm
     * @param clazz               class of clusterable
     * @param <C>                 type of clusterable
     * @return list of clusters
     */
    protected abstract <C extends Clusterable<C>>
    List<ClusterDTO<C>> getClusters(Database elkiDatabase, T algorithmParameters, Class<C> clazz);


    private Map<Long, List<AbstractAuditPOJO>> loadTrainingEvents(EventsFilter filter) {
        List<AbstractAuditPOJO> events;
        if (!CollectionUtils.isEmpty(filter.instanceIds())) {
            List<TrainingInstance> trainingInstances = trainingInstanceService.findAllByIds(filter.instanceIds());
            checkForInstancesOfDifferentDefinition(trainingInstances, filter.definitionId());
            events = trainingInstances.stream()
                    .flatMap(ti -> elasticsearchApiService.findAllEventsFromTrainingInstance(ti).stream())
                    .collect(Collectors.toList());
        } else {
            events = elasticsearchApiService.findAllEventsFromTrainingDefinition(filter.definitionId());
        }
        return (filter.levelId() == null ? events : filterTrainingEvents(events, filter.levelId()))
                .stream()
                .collect(Collectors.groupingBy(
                        AbstractAuditPOJO::getUserRefId,
                        LinkedHashMap::new,
                        Collectors.toList())
                );
    }

    private List<AbstractAuditPOJO> filterTrainingEvents(List<AbstractAuditPOJO> events, Long levelId) {
        return events.stream().filter(event -> event.getLevel() == levelId).toList();
    }

    private void checkForInstancesOfDifferentDefinition(List<TrainingInstance> trainingInstances, Long trainingDefinitionId) {
        Optional<TrainingInstance> trainingInstance = trainingInstances.stream()
                .filter(ti -> !ti.getTrainingDefinition().getId().equals(trainingDefinitionId))
                .findFirst();
        if (trainingInstance.isPresent()) {
            throw new EntityConflictException(new EntityErrorDetail(
                    TrainingInstance.class,
                    "id",
                    trainingInstance.get().getId().getClass(),
                    trainingInstance.get().getId(),
                    "Training instance has not assigned the correct training definition (ID: " + trainingDefinitionId + ").")
            );
        }
    }
}

