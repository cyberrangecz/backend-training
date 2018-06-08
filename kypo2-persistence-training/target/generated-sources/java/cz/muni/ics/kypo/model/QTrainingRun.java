package cz.muni.ics.kypo.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QTrainingRun is a Querydsl query type for TrainingRun
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QTrainingRun extends EntityPathBase<TrainingRun> {

    private static final long serialVersionUID = -884179692L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QTrainingRun trainingRun = new QTrainingRun("trainingRun");

    public final QAbstractLevel abstractLevel;

    public final StringPath eventLogReference = createString("eventLogReference");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<java.time.LocalDateTime> localDateTime = createDateTime("localDateTime", java.time.LocalDateTime.class);

    public final EnumPath<cz.muni.ics.kypo.model.enums.TRState> state = createEnum("state", cz.muni.ics.kypo.model.enums.TRState.class);

    public final QTrainingInstance trainingInstance;

    public QTrainingRun(String variable) {
        this(TrainingRun.class, forVariable(variable), INITS);
    }

    public QTrainingRun(Path<? extends TrainingRun> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QTrainingRun(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QTrainingRun(PathMetadata metadata, PathInits inits) {
        this(TrainingRun.class, metadata, inits);
    }

    public QTrainingRun(Class<? extends TrainingRun> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.abstractLevel = inits.isInitialized("abstractLevel") ? new QAbstractLevel(forProperty("abstractLevel"), inits.get("abstractLevel")) : null;
        this.trainingInstance = inits.isInitialized("trainingInstance") ? new QTrainingInstance(forProperty("trainingInstance"), inits.get("trainingInstance")) : null;
    }

}

