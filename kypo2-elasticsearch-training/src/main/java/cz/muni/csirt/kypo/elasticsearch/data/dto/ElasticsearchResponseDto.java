package cz.muni.csirt.kypo.elasticsearch.data.dto;

/**
 * @author Pavel Seda
 */
public class ElasticsearchResponseDto {

    private boolean acknowledged;

    public ElasticsearchResponseDto() {
    }

    public boolean isAcknowledged() {
        return acknowledged;
    }

    public void setAcknowledged(boolean acknowledged) {
        this.acknowledged = acknowledged;
    }
}
