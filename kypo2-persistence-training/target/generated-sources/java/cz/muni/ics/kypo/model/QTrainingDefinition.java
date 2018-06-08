package cz.muni.ics.kypo.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QTrainingDefinition is a Querydsl query type for TrainingDefinition
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QTrainingDefinition extends EntityPathBase<TrainingDefinition> {

    private static final long serialVersionUID = -863947094L;

    public static final QTrainingDefinition trainingDefinition = new QTrainingDefinition("trainingDefinition");

    public final SetPath<AbstractLevel, QAbstractLevel> abstractLevel = this.<AbstractLevel, QAbstractLevel>createSet("abstractLevel", AbstractLevel.class, QAbstractLevel.class, PathInits.DIRECT2);

    public final StringPath description = createString("description");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath initialLevel = createString("initialLevel");

    public final ArrayPath<String[], String> outcomes = createArray("outcomes", String[].class);

    public final ArrayPath<String[], String> prerequisities = createArray("prerequisities", String[].class);

    public final EnumPath<cz.muni.ics.kypo.model.enums.TDState> state = createEnum("state", cz.muni.ics.kypo.model.enums.TDState.class);

    public final StringPath title = createString("title");

    public final SetPath<TrainingInstance, QTrainingInstance> trainingInstance = this.<TrainingInstance, QTrainingInstance>createSet("trainingInstance", TrainingInstance.class, QTrainingInstance.class, PathInits.DIRECT2);

    public QTrainingDefinition(String variable) {
        super(TrainingDefinition.class, forVariable(variable));
    }

    public QTrainingDefinition(Path<? extends TrainingDefinition> path) {
        super(path.getType(), path.getMetadata());
    }

    public QTrainingDefinition(PathMetadata metadata) {
        super(TrainingDefinition.class, metadata);
    }

}

