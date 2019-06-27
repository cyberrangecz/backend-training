package cz.muni.ics.kypo.training.api.enums;

/**
 * States represented in Training Run entity.
 *
 * @author Pavel Seda (441048)
 */
public enum TRState {

    /**
     * Running Training Run state.
     */
    RUNNING,
    /**
     * Finished Training Run state.
     */
    FINISHED,
    /**
     * Archived Training Run state.
     */
    ARCHIVED;
}
