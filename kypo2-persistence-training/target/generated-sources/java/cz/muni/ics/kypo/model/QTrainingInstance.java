package cz.muni.ics.kypo.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QTrainingInstance is a Querydsl query type for TrainingInstance
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QTrainingInstance extends EntityPathBase<TrainingInstance> {

    private static final long serialVersionUID = -906166356L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QTrainingInstance trainingInstance = new QTrainingInstance("trainingInstance");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath keywords = createString("keywords");

    public final NumberPath<Integer> lifeTime = createNumber("lifeTime", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> localDateTime = createDateTime("localDateTime", java.time.LocalDateTime.class);

    public final NumberPath<Integer> poolSize = createNumber("poolSize", Integer.class);

    public final StringPath title = createString("title");

    public final QTrainingDefinition trainingDefinition;

    public final SetPath<TrainingRun, QTrainingRun> trainingRun = this.<TrainingRun, QTrainingRun>createSet("trainingRun", TrainingRun.class, QTrainingRun.class, PathInits.DIRECT2);

    public QTrainingInstance(String variable) {
        this(TrainingInstance.class, forVariable(variable), INITS);
    }

    public QTrainingInstance(Path<? extends TrainingInstance> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QTrainingInstance(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QTrainingInstance(PathMetadata metadata, PathInits inits) {
        this(TrainingInstance.class, metadata, inits);
    }

    public QTrainingInstance(Class<? extends TrainingInstance> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.trainingDefinition = inits.isInitialized("trainingDefinition") ? new QTrainingDefinition(forProperty("trainingDefinition")) : null;
    }

}

