package cz.muni.ics.kypo.training.api.dto;

public class UserRefDTO {
    private Long id;
    private Long userRefId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserRefId() {
        return userRefId;
    }

    public void setUserRefId(Long userRefId) {
        this.userRefId = userRefId;
    }

    @Override
    public String toString() {
        return "UserRefDTO{" + "id=" + id + ", userRefId=" + userRefId + '}';
    }
}
