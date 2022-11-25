package cz.muni.ics.kypo.training.api.dto.cheatingdetection;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;
import java.util.List;

/**
 * Encapsulates information about location similarity detection event.
 */
@ApiModel(value = "LocationSimilarityDetectionEventDTO", description = "A detection event of type Location Similarity.", parent = AbstractDetectionEventDTO.class)
public class LocationSimilarityDetectionEventDTO extends AbstractDetectionEventDTO {

    @ApiModelProperty(value = "Ip address of participant.", example = "1.1.1.1")
    private String ipAddress;
    @ApiModelProperty(value = "DNS of participant.", example = "fi.muni.cz")
    private String dns;
    @ApiModelProperty(value = "If the address is the same as deployment.", example = "false")
    private boolean isAddressDeploy;
    @ApiModelProperty(value = "participants of the detection event.", example = "1")
    private List<DetectionEventParticipantDTO> participants;

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getDns() {
        return dns;
    }

    public void setDns(String dns) {
        this.dns = dns;
    }

    public boolean isAddressDeploy() {
        return isAddressDeploy;
    }

    public void setAddressDeploy(boolean addressDeploy) {
        isAddressDeploy = addressDeploy;
    }

    public List<DetectionEventParticipantDTO> getParticipants() {
        return participants;
    }

    public void setParticipants(List<DetectionEventParticipantDTO> participants) {
        this.participants = participants;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LocationSimilarityDetectionEventDTO that = (LocationSimilarityDetectionEventDTO) o;
        return isAddressDeploy == that.isAddressDeploy && Objects.equals(ipAddress, that.ipAddress) && Objects.equals(dns, that.dns) && Objects.equals(participants, that.participants);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), ipAddress, dns, isAddressDeploy, participants);
    }

    @Override
    public String toString() {
        return "LocationSimilarityDetectionEventDTO{" +
                "ipAddress='" + ipAddress + '\'' +
                ", dns='" + dns + '\'' +
                ", isAddressDeploy=" + isAddressDeploy +
                ", participants=" + participants +
                '}';
    }
}
