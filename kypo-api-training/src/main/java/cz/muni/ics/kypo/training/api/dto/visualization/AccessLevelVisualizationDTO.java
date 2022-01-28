package cz.muni.ics.kypo.training.api.dto.visualization;

import cz.muni.ics.kypo.training.api.dto.export.AbstractLevelExportDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 * Encapsulates information about access level. Inherits from {@link AbstractLevelVisualizationDTO}
 * Used for visualization.
 */
@ApiModel(value = "AccessLevelVisualizationDTO", description = "Information about access level needed for visualizations.", parent = AbstractLevelExportDTO.class)
public class AccessLevelVisualizationDTO extends AbstractLevelVisualizationDTO {

    @ApiModelProperty(value = "Keyword found in training, used for access next level.", example = "secretAnswer")
    private String passkey;
    @ApiModelProperty(value = "The instructions on how to connect to the machine in cloud environment.", example = "Connect using SSH config.")
    private String cloudContent;
    @ApiModelProperty(value = "The instructions on how to connect to the machine in local (non-cloud) environment.", example = "Use vagrant SSH connection.")
    private String localContent;


    /**
     * Instantiates a new Training level visualization dto.
     */
    public AccessLevelVisualizationDTO() {
    }

    /**
     * Gets passkey.
     *
     * @return the passkey
     */
    public String getPasskey() {
        return passkey;
    }

    /**
     * Sets passkey.
     *
     * @param passkey the passkey
     */
    public void setPasskey(String passkey) {
        this.passkey = passkey;
    }

    /**
     * Gets content.
     *
     * @return the content
     */
    public String getCloudContent() {
        return cloudContent;
    }

    /**
     * Sets content.
     *
     * @param cloudContent the content
     */
    public void setCloudContent(String cloudContent) {
        this.cloudContent = cloudContent;
    }

    /**
     * Gets instructions on how to access machine in local (non-cloud) environment
     *
     * @return the local content
     */
    public String getLocalContent() {
        return localContent;
    }

    /**
     * Sets instructions on how to access machine in local (non-cloud) environment
     *
     * @param localContent the local content
     */
    public void setLocalContent(String localContent) {
        this.localContent = localContent;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof AccessLevelVisualizationDTO)) return false;
        if (!super.equals(object)) return false;
        AccessLevelVisualizationDTO that = (AccessLevelVisualizationDTO) object;
        return Objects.equals(this.getPasskey(), that.getPasskey());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getPasskey());
    }
    @Override
    public String toString() {
        return "AccessLevelVisualizationDTO{" +
                "id=" + this.getId() +
                ", title='" + this.getTitle() + '\'' +
                ", levelType=" + this.getLevelType() +
                ", order=" + this.getOrder() +
                ", maxScore=" + this.getMaxScore() +
                ", estimatedDuration=" + this.getEstimatedDuration() +
                ", passkey='" + passkey + '\'' +
                ", cloudContent='" + cloudContent + '\'' +
                ", localContent='" + localContent + '\'' +
                '}';
    }
}
