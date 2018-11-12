package cz.muni.ics.kypo.training.api.dto;

public class UserRefDTO {
    private Long id;
    private String userRefLogin;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserRefLogin() {
        return userRefLogin;
    }

    public void setUserRefLogin(String userRefLogin) {
        this.userRefLogin = userRefLogin;
    }

    @Override public String toString() {
        return "UserRefDTO{" + "id=" + id + ", userRefLogin='" + userRefLogin + '\'' + '}';
    }
}
