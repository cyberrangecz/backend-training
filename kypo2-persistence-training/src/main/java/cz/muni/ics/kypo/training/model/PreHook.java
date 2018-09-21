package cz.muni.ics.kypo.training.model;

import javax.persistence.*;
import java.io.Serializable;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
@Entity(name = "PreHook")
@Table(name = "pre_hook")
public class PreHook implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false, insertable = false)
	private Long id;
	@OneToOne(mappedBy = "preHook", fetch = FetchType.LAZY)
	private AbstractLevel abstractLevel;

	public PreHook() {}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public AbstractLevel getAbstractLevel() {
		return abstractLevel;
	}

	public void setAbstractLevel(AbstractLevel abstractLevel) {
		this.abstractLevel = abstractLevel;
	}

	@Override
	public String toString() {
		return "PreHook [id=" + id + ", abstractLevel=" + abstractLevel + "]";
	}

}
