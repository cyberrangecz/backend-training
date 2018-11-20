package cz.muni.ics.kypo.training.persistence.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * @author Boris Jadus
 */
@Entity(name = "Password")
@Table(name = "password")
public class Password implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false, insertable = false)
    private Long id;
    @Column(name = "password", nullable = false)
    private String password;

    public Password(Long id, String password) {
        this.password = password;
        this.id = id;
    }

    public Password() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Password))
            return false;
        Password keyword = (Password) o;
        return Objects.equals(id, keyword.getId()) && Objects.equals(this.password, keyword.getPassword());
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, password);
    }

    @Override
    public String toString() {
        return "Password{" + "id=" + id + ", password='" + password + '\'' + '}';
    }
}
