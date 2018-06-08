package cz.muni.ics.kypo.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QInfoLevel is a Querydsl query type for InfoLevel
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QInfoLevel extends EntityPathBase<InfoLevel> {

    private static final long serialVersionUID = -1386102279L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QInfoLevel infoLevel = new QInfoLevel("infoLevel");

    public final QAbstractLevel _super;

    public final ArrayPath<byte[], Byte> content = createArray("content", byte[].class);

    //inherited
    public final NumberPath<Long> id;

    //inherited
    public final NumberPath<Integer> maxScore;

    //inherited
    public final NumberPath<Long> nextLevel;

    //inherited
    public final ArrayPath<byte[], Byte> postHook;

    //inherited
    public final ArrayPath<byte[], Byte> preHook;

    //inherited
    public final StringPath title;

    // inherited
    public final QTrainingDefinition trainingDefinition;

    //inherited
    public final SetPath<TrainingRun, QTrainingRun> trainingRun;

    public QInfoLevel(String variable) {
        this(InfoLevel.class, forVariable(variable), INITS);
    }

    public QInfoLevel(Path<? extends InfoLevel> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QInfoLevel(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QInfoLevel(PathMetadata metadata, PathInits inits) {
        this(InfoLevel.class, metadata, inits);
    }

    public QInfoLevel(Class<? extends InfoLevel> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this._super = new QAbstractLevel(type, metadata, inits);
        this.id = _super.id;
        this.maxScore = _super.maxScore;
        this.nextLevel = _super.nextLevel;
        this.postHook = _super.postHook;
        this.preHook = _super.preHook;
        this.title = _super.title;
        this.trainingDefinition = _super.trainingDefinition;
        this.trainingRun = _super.trainingRun;
    }

}

