package cz.muni.ics.kypo.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QGameLevel is a Querydsl query type for GameLevel
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QGameLevel extends EntityPathBase<GameLevel> {

    private static final long serialVersionUID = -1616591563L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QGameLevel gameLevel = new QGameLevel("gameLevel");

    public final QAbstractLevel _super;

    public final ArrayPath<byte[], Byte> content = createArray("content", byte[].class);

    public final StringPath flag = createString("flag");

    public final SetPath<Hint, QHint> hints = this.<Hint, QHint>createSet("hints", Hint.class, QHint.class, PathInits.DIRECT2);

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

    public final StringPath solution = createString("solution");

    //inherited
    public final StringPath title;

    // inherited
    public final QTrainingDefinition trainingDefinition;

    //inherited
    public final SetPath<TrainingRun, QTrainingRun> trainingRun;

    public QGameLevel(String variable) {
        this(GameLevel.class, forVariable(variable), INITS);
    }

    public QGameLevel(Path<? extends GameLevel> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QGameLevel(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QGameLevel(PathMetadata metadata, PathInits inits) {
        this(GameLevel.class, metadata, inits);
    }

    public QGameLevel(Class<? extends GameLevel> type, PathMetadata metadata, PathInits inits) {
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

