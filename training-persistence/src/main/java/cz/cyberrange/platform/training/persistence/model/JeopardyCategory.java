package cz.cyberrange.platform.training.persistence.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "jeopardy_category")
public class JeopardyCategory extends AbstractEntity<Long> {

    @Getter
    @Setter
    @Column(name = "title")
    private String title;

    @Getter
    @Setter
    @Column(name = "color")
    private Integer color;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "level_id")
    private JeopardyLevel jeopardyLevel;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "category", fetch = FetchType.LAZY)
    private List<JeopardySublevel> sublevels = new ArrayList<>();

    public void setSublevels(List<JeopardySublevel> sublevels) {
        this.sublevels = new ArrayList<>(sublevels);
    }

    public void addSublevel(JeopardySublevel sublevel) {
        sublevels.add(sublevel);
    }

    public List<JeopardySublevel> getSublevels() {
        return new ArrayList<>(sublevels);
    }


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JeopardyCategory otherCategory) {
            return this.hashCode() == otherCategory.hashCode();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, color, Arrays.deepHashCode(sublevels.toArray()));
    }
}
