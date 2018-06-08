package cz.muni.ics.kypo.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QHint is a Querydsl query type for Hint
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QHint extends EntityPathBase<Hint> {

    private static final long serialVersionUID = -122430940L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QHint hint = new QHint("hint");

    public final ArrayPath<byte[], Byte> content = createArray("content", byte[].class);

    public final QGameLevel gameLevel;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> points = createNumber("points", Integer.class);

    public final StringPath title = createString("title");

    public QHint(String variable) {
        this(Hint.class, forVariable(variable), INITS);
    }

    public QHint(Path<? extends Hint> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QHint(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QHint(PathMetadata metadata, PathInits inits) {
        this(Hint.class, metadata, inits);
    }

    public QHint(Class<? extends Hint> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.gameLevel = inits.isInitialized("gameLevel") ? new QGameLevel(forProperty("gameLevel"), inits.get("gameLevel")) : null;
    }

}

