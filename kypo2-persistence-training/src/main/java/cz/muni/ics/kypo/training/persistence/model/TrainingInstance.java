package cz.muni.ics.kypo.training.persistence.model;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author Pavel Seda (441048)
 */
@Entity(name = "TrainingInstance")
@Table(name = "training_instance")
public class TrainingInstance implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false, insertable = false)
    private Long id;
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;
    @Column(name = "title", nullable = false)
    private String title;
    @Column(name = "pool_size", nullable = false)
    private int poolSize;
    @Column(name = "pool_id")
    private Long poolId;
    @Column(name = "access_token", nullable = false, unique = true)
    private String accessToken;
    @ManyToOne(fetch = FetchType.LAZY)
    private TrainingDefinition trainingDefinition;
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinTable(name = "training_instance_user_ref",
            joinColumns = @JoinColumn(name = "training_instance_id"),
            inverseJoinColumns = @JoinColumn(name = "user_ref_id")
    )
    private Set<UserRef> organizers = new HashSet<>();
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL, mappedBy = "trainingInstance")
    private Set<SandboxInstanceRef> sandboxInstanceRefs = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public Long getPoolId() {
        return poolId;
    }

    public void setPoolId(Long poolId) {
        this.poolId = poolId;
    }

    public TrainingDefinition getTrainingDefinition() {
        return trainingDefinition;
    }

    public void setTrainingDefinition(TrainingDefinition trainingDefinition) {
        this.trainingDefinition = trainingDefinition;
    }

    public Set<UserRef> getOrganizers() {
        return Collections.unmodifiableSet(organizers);
    }

    public void setOrganizers(Set<UserRef> organizers) {
        this.organizers = organizers;
    }

    public void addOrganizer(UserRef userRef) {
        this.organizers.add(userRef);
        userRef.addTrainingInstance(this);
    }

    public Set<SandboxInstanceRef> getSandboxInstanceRefs() {
        return Collections.unmodifiableSet(sandboxInstanceRefs);
    }

    public void setSandboxInstanceRefs(Set<SandboxInstanceRef> sandboxInstanceRefs) {
        this.sandboxInstanceRefs = sandboxInstanceRefs;
    }

    public void addSandboxInstanceRef(SandboxInstanceRef sandboxInstanceRef) {
        this.sandboxInstanceRefs.add(sandboxInstanceRef);
        sandboxInstanceRef.setTrainingInstance(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accessToken, startTime, endTime, poolSize, title, trainingDefinition);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof TrainingInstance))
            return false;
        TrainingInstance other = (TrainingInstance) obj;
        return Objects.equals(accessToken, other.getAccessToken())
                && Objects.equals(startTime, other.getStartTime())
                && Objects.equals(endTime, other.getEndTime())
                && Objects.equals(poolSize, other.getPoolSize())
                && Objects.equals(title, other.getTitle())
                && Objects.equals(trainingDefinition, other.getTrainingDefinition());
    }

    @Override
    public String toString() {
        return "TrainingInstance{" +
                "id=" + id +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", title='" + title + '\'' +
                ", poolSize=" + poolSize +
                ", accessToken='" + accessToken + '\'' +
                ", trainingDefinition=" + trainingDefinition +
                ", organizers=" + organizers +
                ", sandboxInstanceRefs=" + sandboxInstanceRefs +
                '}';
    }
}
