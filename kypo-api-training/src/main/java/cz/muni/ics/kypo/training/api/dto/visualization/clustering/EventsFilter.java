package cz.muni.ics.kypo.training.api.dto.visualization.clustering;

import java.util.List;

public record EventsFilter(
        Long definitionId,
        List<Long> instanceIds,
        Long levelId
) {
}
