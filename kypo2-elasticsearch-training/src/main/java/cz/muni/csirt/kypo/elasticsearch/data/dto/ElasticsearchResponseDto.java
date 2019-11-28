package cz.muni.csirt.kypo.elasticsearch.data.dto;

/**
 * Encapsulates response from Elasticsearch.
 *
 * @author Pavel Seda
 */
public class ElasticsearchResponseDto {

    private boolean acknowledged;

    /**
     * Instantiates a new Elasticsearch response dto.
     */
    public ElasticsearchResponseDto() {
    }

    /**
     * Is acknowledged boolean.
     *
     * @return the boolean
     */
    public boolean isAcknowledged() {
        return acknowledged;
    }

    /**
     * Sets acknowledged.
     *
     * @param acknowledged the acknowledged
     */
    public void setAcknowledged(boolean acknowledged) {
        this.acknowledged = acknowledged;
    }
}
