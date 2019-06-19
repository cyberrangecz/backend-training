package cz.muni.ics.kypo.training.persistence.model.enums;

/**
 * States represented in Training Run entity.
 *
 * @author Pavel Seda (441048)
 */
public enum TRState {

    /**
     * Running run state.
     */
    RUNNING,
    /**
     * Finished run state.
     */
    FINISHED,
    /**
     * Archived run state.
     */
    ARCHIVED;
}
