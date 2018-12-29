package cz.muni.ics.kypo.training.persistence.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * @author Boris Jadus
 */
@Entity(name = "AccessToken")
@Table(name = "access_token")
public class AccessToken implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false, insertable = false)
    private Long id;
    @Column(name = "access_token", nullable = false, unique = true)
    private String accessToken;

    public AccessToken() {
    }

    public AccessToken(Long id, String accessToken) {
        this.accessToken = accessToken;
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AccessToken))
            return false;
        AccessToken accessToken = (AccessToken) o;
        return Objects.equals(id, accessToken.getId()) && Objects.equals(this.accessToken, accessToken.getAccessToken());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, accessToken);
    }

    @Override
    public String toString() {
        return "AccessToken{" +
                "id=" + id +
                ", accessToken='" + accessToken + '\'' +
                '}';
    }
}
