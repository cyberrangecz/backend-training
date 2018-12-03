package cz.muni.ics.kypo.training.api.dto;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author Dominik Pilar
 */
public class ParticipantRefDTO {

    private Long id;
    private String participantRefLogin;

    @ApiModelProperty(value = "Main identifier of participant ref.", example = "1")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ApiModelProperty(value = "Reference to participant in another microservice.", example = "Participant1")
    public String getParticipantRefLogin() {
        return participantRefLogin;
    }

    public void setParticipantRefLogin(String participantRefLogin) {
        this.participantRefLogin = participantRefLogin;
    }

    @Override
    public String toString() {
        return "ParticipantRefDTO{" +
                "id=" + id +
                ", participantRefLogin='" + participantRefLogin + '\'' +
                '}';
    }
}
