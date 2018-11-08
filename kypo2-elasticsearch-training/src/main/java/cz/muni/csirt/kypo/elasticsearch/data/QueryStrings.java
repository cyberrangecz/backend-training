package cz.muni.csirt.kypo.elasticsearch.data;

/**
 * @author Pavel Šeda
 */
public abstract class QueryStrings {

    // general info like index, type..
    public static final String ES_EVENTS_INDEX_WILDCHARD = "kypo2-cz.muni.csirt.kypo.events.game*";
    public static final String ES_EVENTS_TYPE_WILDCHARD = "";

    // query based on game_instance_id
    public static final String ES_EVENT_GAME_DETAILS_GAME_INSTANCE_ID = "game_details.game_instance_id";

}
