package cz.muni.ics.kypo.training.api.dto.visualization.clusteranalysis;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.math3.stat.clustering.Clusterable;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ClusterDTO<T extends Clusterable<T>> {
    private final String name;
    private final List<T> points;
    private final List<T> fullPoints = new ArrayList<>();
    private T center;

    public ClusterDTO(String name, List<T> points) {
        this.name = name;
        this.points = points;
    }
}
