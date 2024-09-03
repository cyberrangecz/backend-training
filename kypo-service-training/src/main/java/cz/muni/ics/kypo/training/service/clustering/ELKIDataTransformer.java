package cz.muni.ics.kypo.training.service.clustering;

import cz.muni.ics.kypo.training.api.dto.visualization.clusteranalysis.ClusterDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.clusteranalysis.TimeAfterHintClusterableDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.clusteranalysis.TimeSolutionDisplayedClusterableDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.clusteranalysis.WrongAnswersClusterableDTO;
import elki.data.DoubleVector;
import elki.data.LabelList;
import elki.data.NumberVector;
import elki.data.model.KMeansModel;
import elki.data.model.Model;
import elki.database.Database;
import elki.database.StaticArrayDatabase;
import elki.database.ids.DBIDIter;
import elki.database.relation.Relation;
import elki.datasource.DatabaseConnection;
import elki.datasource.InputStreamDatabaseConnection;
import elki.datasource.parser.NumberVectorLabelParser;
import elki.datasource.parser.Parser;
import org.apache.commons.math3.stat.clustering.Clusterable;
import org.apache.commons.math3.stat.clustering.EuclideanDoublePoint;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ELKIDataTransformer {


    public Database transformWrongAnswersClusterableToElkiInputFormat(List<WrongAnswersClusterableDTO> clusterables) {
        return initDatabase(clusterables.stream()
                .map(clusterable -> {
                    List<String> axisValues = List.of(
                            clusterable.getWrongAnswersSubmittedNormalized().toString(),
                            clusterable.getTimePlayedNormalized().toString()
                    );
                    List<String> labels = List.of(
                            ELKILabels.ID.labelValue(clusterable.getUserRefId().toString()),
                            ELKILabels.WRONG_ANSWERS.labelValue(clusterable.getWrongAnswersSubmitted().toString()),
                            ELKILabels.TIME_PLAYED.labelValue(clusterable.getTimePlayed().toString())
                    );

                    return transformToElkiInputLine(axisValues, labels);
                }).toList());
    }


    public Database transformHintClusterableToElkiInputFormat(List<TimeAfterHintClusterableDTO> clusterables) {
        return initDatabase(clusterables.stream()
                .map(clusterable -> {
                    List<String> axisValues = List.of(
                            clusterable.getTimeSpentAfterHintNormalized().toString(),
                            clusterable.getWrongAnswersAfterHintNormalized().toString()
                    );
                    List<String> labels = List.of(
                            ELKILabels.ID.labelValue(clusterable.getUserRefId().toString()),
                            ELKILabels.LEVEL.labelValue(clusterable.getLevel().toString()),
                            ELKILabels.TIME_AFTER_HINT.labelValue(clusterable.getTimeSpentAfterHint().toString()),
                            ELKILabels.WRONG_ANSWERS_AFTER_HINT.labelValue(clusterable
                                    .getWrongAnswersAfterHint().toString())
                    );

                    return transformToElkiInputLine(axisValues, labels);
                }).toList());
    }


    public Database transformSolutionClusterableToElkiInputFormat(List<TimeSolutionDisplayedClusterableDTO> clusterables) {
        return initDatabase(clusterables.stream()
                .map(clusterable -> {
                    List<String> axisValues = List.of(
                            clusterable.getSolutionDisplayedAtNormalized().toString(),
                            clusterable.getTimeSpentAfterSolutionDisplayedNormalized().toString()
                    );
                    List<String> labels = List.of(
                            ELKILabels.ID.labelValue(clusterable.getUserRefId().toString()),
                            ELKILabels.LEVEL.labelValue(clusterable.getLevel().toString()),
                            ELKILabels.SOLUTION_DISPLAYED_AT.labelValue(clusterable.getSolutionDisplayedAt().toString()),
                            ELKILabels.TIME_AFTER_SOLUTION_DISPLAYED.labelValue(clusterable
                                    .getTimeSpentAfterSolutionDisplayed().toString())
                    );

                    return transformToElkiInputLine(axisValues, labels);
                }).toList());
    }


    public Database transformNDimensionalClusterableToElkiDatabase(List<EuclideanDoublePoint> clusterables) {
        return initDatabase(clusterables.stream()
                .map(clusterable -> {
                    List<String> axisValues = Arrays.stream(clusterable.getPoint())
                            .mapToObj(String::valueOf)
                            .toList();

                    return transformToElkiInputLine(axisValues, Collections.emptyList());
                }).toList());
    }

    // Ref: https://elki-project.github.io/howto/inputformat#default-input-format
    private String transformToElkiInputLine(List<String> axisValues, List<String> labels) {
        return String.format("%s %s",
                String.join(" ", axisValues),
                String.join(" ", labels)
        );
    }

    private Database initDatabase(List<String> elkiInputFormat) {
        ByteArrayInputStream bais = new ByteArrayInputStream(
                elkiInputFormat.stream()
                        .collect(Collectors.joining(System.lineSeparator(), "", System.lineSeparator()))
                        .getBytes()
        );

        NumberVector.Factory<DoubleVector> factory = new DoubleVector.Factory();
        Parser parser = new NumberVectorLabelParser<>(factory);
        DatabaseConnection dbc = new InputStreamDatabaseConnection(bais, null, parser);
        Database db = new StaticArrayDatabase(dbc, null);
        db.initialize();
        return db;
    }


    public <T extends Clusterable<T>, M extends Model>
    List<ClusterDTO<T>> transformElkiOutputToClusters(List<elki.data.Cluster<M>> elkiClusters,
                                                      List<elki.data.Cluster<M>> fullElkiClusters,
                                                      Relation<NumberVector> vectors,
                                                      Relation<LabelList> labels,
                                                      Class<T> clazz) {

        if (fullElkiClusters != null) { // full (OPTICS) clusters are present
            if (elkiClusters.size() != fullElkiClusters.size()) {
                throw new IllegalArgumentException("Invalid input");
            }
            elkiClusters.sort(Comparator.comparing(elki.data.Cluster::getName));
            fullElkiClusters.sort(Comparator.comparing(elki.data.Cluster::getName));
        }

        List<ClusterDTO<T>> result = new ArrayList<>();
        for (int index = 0; index < elkiClusters.size(); index++) {
            elki.data.Cluster<M> elkiCluster = elkiClusters.get(index);
            List<T> clusterables = constructClusterables(elkiCluster, vectors, labels, clazz);
            ClusterDTO<T> cluster = new ClusterDTO<>(elkiCluster.getName(), clusterables);

            if (elkiCluster.getModel() instanceof KMeansModel) {
                // Set centroid
                setCenter(elkiCluster, cluster, clazz);
            } else { // OPTICSModel
                // Set full points
                if (fullElkiClusters != null) {
                    elki.data.Cluster<M> elkiFullCluster = fullElkiClusters.get(index);
                    List<T> fullClusterables = constructClusterables(elkiFullCluster, vectors, labels, clazz);
                    cluster.getFullPoints().addAll(fullClusterables);

                    // Set center of radar chart
                    if (clazz.equals(EuclideanDoublePoint.class)) {
                        cluster.setCenter(fullClusterables.get(0).centroidOf(fullClusterables));
                    }
                }
            }
            result.add(cluster);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private <T extends Clusterable<T>, M extends Model>
    List<T> constructClusterables(elki.data.Cluster<M> cluster,
                                  Relation<NumberVector> vectors,
                                  Relation<LabelList> labels,
                                  Class<T> clazz) {

        List<T> clusterables = new ArrayList<>();
        for (DBIDIter it = cluster.getIDs().iter(); it.valid(); it.advance()) {
            if (clazz.equals(WrongAnswersClusterableDTO.class)) {
                clusterables.add((T) constructWrongAnswersClusterable(vectors.get(it), labels.get(it)));
                continue;
            }
            if (clazz.equals(TimeAfterHintClusterableDTO.class)) {
                clusterables.add((T) constructHintClusterable(vectors.get(it), labels.get(it)));
                continue;
            }
            if (clazz.equals(TimeSolutionDisplayedClusterableDTO.class)) {
                clusterables.add((T) constructSolutionsClusterable(vectors.get(it), labels.get(it)));
                continue;
            }
            if (clazz.equals(EuclideanDoublePoint.class)) {
                clusterables.add((T) constructEuclideanDoublePoint(vectors.get(it)));
                continue;
            }
            throw new IllegalArgumentException("Invalid input");
        }
        return clusterables;
    }

    @SuppressWarnings("unchecked")
    private <T extends Clusterable<T>, M extends Model>
    void setCenter(elki.data.Cluster<M> elkiCluster, ClusterDTO<T> cluster, Class<T> clazz) {
        double[] center = ((KMeansModel) elkiCluster.getModel()).getMean();
        if (clazz.equals(WrongAnswersClusterableDTO.class)) {
            cluster.setCenter((T) new WrongAnswersClusterableDTO(0L, center[0], center[1]));
        }
        if (clazz.equals(TimeAfterHintClusterableDTO.class)) {
            cluster.setCenter((T) new TimeAfterHintClusterableDTO(0L, 0L, center[0], center[1]));
        }
        if (clazz.equals(TimeSolutionDisplayedClusterableDTO.class)) {
            cluster.setCenter((T) new TimeSolutionDisplayedClusterableDTO(0L, 0L, center[0], center[1]));
        }
        if (clazz.equals(EuclideanDoublePoint.class)) {
            cluster.setCenter((T) new EuclideanDoublePoint(center));
        }
    }

    private WrongAnswersClusterableDTO constructWrongAnswersClusterable(NumberVector vector, LabelList labelList) {
        return new WrongAnswersClusterableDTO(
                Long.parseLong(ELKILabels.ID.retrieveValue(labelList.get(0))),
                Double.parseDouble(ELKILabels.WRONG_ANSWERS.retrieveValue(labelList.get(1))),
                Double.parseDouble(ELKILabels.TIME_PLAYED.retrieveValue(labelList.get(2))),
                vector.doubleValue(0),
                vector.doubleValue(1)
        );
    }

    private TimeAfterHintClusterableDTO constructHintClusterable(NumberVector vector, LabelList labelList) {
        return new TimeAfterHintClusterableDTO(
                Long.parseLong(ELKILabels.ID.retrieveValue(labelList.get(0))),
                Long.parseLong(ELKILabels.LEVEL.retrieveValue(labelList.get(1))),
                Double.parseDouble(ELKILabels.TIME_AFTER_HINT.retrieveValue(labelList.get(2))),
                Double.parseDouble(ELKILabels.WRONG_ANSWERS_AFTER_HINT.retrieveValue(labelList.get(3))),
                vector.doubleValue(0),
                vector.doubleValue(1)
        );
    }

    private TimeSolutionDisplayedClusterableDTO constructSolutionsClusterable(NumberVector vector, LabelList labelList) {
        return new TimeSolutionDisplayedClusterableDTO(
                Long.parseLong(ELKILabels.ID.retrieveValue(labelList.get(0))),
                Long.parseLong(ELKILabels.LEVEL.retrieveValue(labelList.get(1))),
                Double.parseDouble(ELKILabels.SOLUTION_DISPLAYED_AT.retrieveValue(labelList.get(2))),
                Double.parseDouble(ELKILabels.TIME_AFTER_SOLUTION_DISPLAYED.retrieveValue(labelList.get(3))),
                vector.doubleValue(0),
                vector.doubleValue(1)
        );
    }

    private EuclideanDoublePoint constructEuclideanDoublePoint(NumberVector vector) {
        return new EuclideanDoublePoint(new double[]{
                vector.doubleValue(0),
                vector.doubleValue(1),
                vector.doubleValue(2),
                vector.doubleValue(3),
                vector.doubleValue(4),
                vector.doubleValue(5),
                vector.doubleValue(6)
        });
    }
}
