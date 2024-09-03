package cz.muni.ics.kypo.training.service.clustering;

/**
 * The enumeration of labels used in ELKI
 * database for clustering.
 */
public enum ELKILabels {

    ID("ID"),
    LEVEL("level"),
    WRONG_ANSWERS("wrongAnswersSubmitted"),
    TIME_PLAYED("timePlayed"),
    TIME_AFTER_HINT("timeSpentAfterHint"),
    WRONG_ANSWERS_AFTER_HINT("wrongAnswersAfterHint"),
    SOLUTION_DISPLAYED_AT("solutionDisplayedAt"),
    TIME_AFTER_SOLUTION_DISPLAYED("timeSpentAfterSolutionDisplayed");

    private final String prefix;

    ELKILabels(String prefix) {
        this.prefix = prefix;
    }

    public String labelValue(String suffix) {
        return "%s_%s".formatted(prefix, suffix);
    }

    public String retrieveValue(String labeled) {
        return labeled.replace(prefix + "_", "");
    }
}
