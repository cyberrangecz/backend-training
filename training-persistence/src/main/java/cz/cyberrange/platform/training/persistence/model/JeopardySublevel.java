package cz.cyberrange.platform.training.persistence.model;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.util.Objects;

@Getter
@Entity
public class JeopardySublevel extends TrainingLevel {

    @Column
    @Setter
    String description;

    @ManyToOne
    @JoinColumn(name = "category_id")
    JeopardyCategory category;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return hashCode() == o.hashCode();
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, super.hashCode());
    }
}
