package cz.muni.ics.kypo.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QAbstractLevel is a Querydsl query type for AbstractLevel
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QAbstractLevel extends EntityPathBase<AbstractLevel> {

    private static final long serialVersionUID = -729725563L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QAbstractLevel abstractLevel = new QAbstractLevel("abstractLevel");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> maxScore = createNumber("maxScore", Integer.class);

    public final NumberPath<Long> nextLevel = createNumber("nextLevel", Long.class);

    public final ArrayPath<byte[], Byte> postHook = createArray("postHook", byte[].class);

    public final ArrayPath<byte[], Byte> preHook = createArray("preHook", byte[].class);

    public final StringPath title = createString("title");

    public final QTrainingDefinition trainingDefinition;

    public final SetPath<TrainingRun, QTrainingRun> trainingRun = this.<TrainingRun, QTrainingRun>createSet("trainingRun", TrainingRun.class, QTrainingRun.class, PathInits.DIRECT2);

    public QAbstractLevel(String variable) {
        this(AbstractLevel.class, forVariable(variable), INITS);
    }

    public QAbstractLevel(Path<? extends AbstractLevel> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QAbstractLevel(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QAbstractLevel(PathMetadata metadata, PathInits inits) {
        this(AbstractLevel.class, metadata, inits);
    }

    public QAbstractLevel(Class<? extends AbstractLevel> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.trainingDefinition = inits.isInitialized("trainingDefinition") ? new QTrainingDefinition(forProperty("trainingDefinition")) : null;
    }

}

