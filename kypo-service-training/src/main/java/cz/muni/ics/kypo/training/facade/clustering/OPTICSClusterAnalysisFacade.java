package cz.muni.ics.kypo.training.facade.clustering;

import cz.muni.ics.kypo.training.api.dto.visualization.clusteranalysis.ClusterDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.clusteranalysis.OPTICSParameters;
import cz.muni.ics.kypo.training.service.TrainingInstanceService;
import cz.muni.ics.kypo.training.service.api.ElasticsearchApiService;
import cz.muni.ics.kypo.training.service.clustering.ClusterableDataTransformer;
import cz.muni.ics.kypo.training.service.clustering.ELKIDataTransformer;
import elki.clustering.ClusteringAlgorithm;
import elki.clustering.optics.OPTICSHeap;
import elki.clustering.optics.OPTICSXi;
import elki.data.Clustering;
import elki.data.model.OPTICSModel;
import elki.data.type.TypeUtil;
import elki.database.Database;
import elki.database.ids.DBIDUtil;
import elki.database.ids.DBIDs;
import elki.utilities.ELKIBuilder;
import elki.utilities.datastructures.hierarchy.Hierarchy;
import elki.utilities.datastructures.iterator.It;
import elki.utilities.exceptions.AbortException;
import org.apache.commons.math3.stat.clustering.Clusterable;
import org.apache.commons.math3.stat.clustering.EuclideanDoublePoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

/**
 * Facade for OPTICS clustering analysis.
 */
@Service
@Transactional
public class OPTICSClusterAnalysisFacade extends AbstractClusterAnalysisFacade<OPTICSParameters> {


    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public OPTICSClusterAnalysisFacade(ElasticsearchApiService elasticsearchApiService,
                                       TrainingInstanceService trainingInstanceService,
                                       ClusterableDataTransformer clusterableDataTransformer,
                                       ELKIDataTransformer elkiDataTransformer) {
        super(elasticsearchApiService, trainingInstanceService, clusterableDataTransformer, elkiDataTransformer);
    }

    @Override
    protected <C extends Clusterable<C>> List<ClusterDTO<C>> getClusters(Database elkiDatabase,
                                                                         OPTICSParameters algorithmParameters,
                                                                         Class<C> clazz) {
        Clustering<OPTICSModel> clustering = executeAlgorithm(elkiDatabase, algorithmParameters);

        return elkiDataTransformer
                .transformElkiOutputToClusters(
                        clustering.getAllClusters(), // visible clusters
                        getFullClusters(clustering),
                        elkiDatabase.getRelation(TypeUtil.NUMBER_VECTOR_FIELD), // vectors
                        clazz.equals(EuclideanDoublePoint.class) ? null :
                                elkiDatabase.getRelation(TypeUtil.LABELLIST), // labels
                        clazz
                );
    }

    private List<elki.data.Cluster<OPTICSModel>> getFullClusters(Clustering<OPTICSModel> clustering) {
        List<elki.data.Cluster<OPTICSModel>> fullClusters = new ArrayList<>();

        Hierarchy<elki.data.Cluster<OPTICSModel>> clusterHierarchy = clustering.getClusterHierarchy();

        for (elki.data.Cluster<OPTICSModel> topLevelCluster : clustering.getToplevelClusters()) {
            fullClusters.addAll(getFullClusters(topLevelCluster, clusterHierarchy));
        }

        return fullClusters;
    }

    private List<elki.data.Cluster<OPTICSModel>> getFullClusters(elki.data.Cluster<OPTICSModel> node,
                                                                 Hierarchy<elki.data.Cluster<OPTICSModel>> clusterHierarchy) {

        // Iterate through all children nodes
        It<elki.data.Cluster<OPTICSModel>> clusterIterator = clusterHierarchy.iterChildren(node);
        if (!clusterIterator.valid()) {
            return List.of(node); // leaf
        }

        List<elki.data.Cluster<OPTICSModel>> childFullClusters = new ArrayList<>();
        for (; clusterIterator.valid(); clusterIterator.advance()) {
            elki.data.Cluster<OPTICSModel> childCluster = clusterIterator.get();
            childFullClusters.addAll(getFullClusters(childCluster, clusterHierarchy));
        }

        // Update DBIDs and create full OPTICS cluster
        DBIDs nodeDBIDs = node.getIDs();
        for (elki.data.Cluster<OPTICSModel> childCluster : childFullClusters) {
            nodeDBIDs = DBIDUtil.union(nodeDBIDs, childCluster.getIDs());
        }
        elki.data.Cluster<OPTICSModel> fullCluster = new elki.data.Cluster<>(
                node.getName(),
                nodeDBIDs,
                node.isNoise(),
                node.getModel()
        );

        // Update child full clusters and pass the result to parent node
        childFullClusters.add(fullCluster);
        return childFullClusters;
    }

    private Clustering<OPTICSModel> executeAlgorithm(Database db, OPTICSParameters opticsParameters) {
        int minPts = opticsParameters.getMinPts();
        double xi = opticsParameters.getXi();
        Double epsilon = opticsParameters.getEpsilon();

        try {
            ClusteringAlgorithm<Clustering<OPTICSModel>> opticsXi = this.buildOpticsXi(minPts, xi, epsilon);
            return opticsXi.autorun(db);
        } catch (AbortException exception) {
            logger.error(exception.getMessage());
            logger.info("OPTICS parameters:");
            logger.warn(String.format("\tminPts: %d (should be greater equal than 1)", minPts));
            logger.warn(String.format("\txi: %f (should be 0 <= xi < 1)", xi));
            logger.warn(String.format("\tepsilon: %f (optional)", epsilon));
        }
        return new Clustering<>();
    }

    private ClusteringAlgorithm<Clustering<OPTICSModel>> buildOpticsXi(int minPts, double xi, Double epsilon) {
        ELKIBuilder<OPTICSXi> opticsXiBuilder = new ELKIBuilder<>(OPTICSXi.class)
                .with(OPTICSHeap.Par.MINPTS_ID, minPts)
                .with(OPTICSXi.Par.XI_ID, xi)
                .with(OPTICSXi.Par.XIALG_ID, OPTICSHeap.class);

        if (epsilon != null) {
            opticsXiBuilder.with(OPTICSHeap.Par.EPSILON_ID, epsilon);
        }

        return opticsXiBuilder.build();
    }
}
