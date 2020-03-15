package cz.muni.ics.kypo.training.persistence.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Class represents Training instance.
 * Training instances can be created based on definitions.
 * Training runs can be created based on instances.
 */
@Entity
@Table(name = "training_instance")
@NamedEntityGraphs({
        @NamedEntityGraph(
                name = "TrainingInstance.findAllAuthorsOrganizersBetaTestingGroupBetaTestingGroupOrganizers",
                attributeNodes = {
                        @NamedAttributeNode(value = "organizers"),
                        @NamedAttributeNode(value = "trainingDefinition", subgraph = "trainingDefinition.authors"),
                        @NamedAttributeNode(value = "trainingDefinition", subgraph = "trainingDefinition.betaTestingGroup")
                },
                subgraphs = {
                        @NamedSubgraph(name = "trainingDefinition.authors", attributeNodes = @NamedAttributeNode(value = "authors")),
                        @NamedSubgraph(name = "trainingDefinition.betaTestingGroup", attributeNodes = @NamedAttributeNode(value = "betaTestingGroup", subgraph = "betaTestingGroup.organizers")),
                        @NamedSubgraph(name = "betaTestingGroup.organizers", attributeNodes = @NamedAttributeNode(value = "organizers"))
                }
        ),
        @NamedEntityGraph(
                name = "TrainingInstance.findByIdAuthorsOrganizers",
                attributeNodes = {
                        @NamedAttributeNode(value = "organizers"),
                        @NamedAttributeNode(value = "trainingDefinition", subgraph = "trainingDefinition.authors")
                },
                subgraphs = {
                        @NamedSubgraph(name = "trainingDefinition.authors", attributeNodes = @NamedAttributeNode(value = "authors"))
                }
        )
})
@NamedQueries({
        @NamedQuery(
                name = "TrainingInstance.findByStartTimeAfterAndEndTimeBeforeAndAccessToken",
                query = "SELECT ti FROM TrainingInstance ti " +
                        "JOIN FETCH ti.trainingDefinition td " +
                        "WHERE ti.startTime < :datetime AND ti.endTime > :datetime AND ti.accessToken = :accessToken"
        ),
        @NamedQuery(
                name = "TrainingInstance.findByIdIncludingDefinition",
                query = "SELECT ti FROM TrainingInstance ti " +
                        "LEFT OUTER JOIN FETCH ti.organizers " +
                        "JOIN FETCH ti.trainingDefinition td " +
                        "LEFT OUTER JOIN FETCH td.authors " +
                        "LEFT OUTER JOIN FETCH td.betaTestingGroup btg " +
                        "LEFT OUTER JOIN FETCH btg.organizers " +
                        "WHERE ti.id = :instanceId"
        ),
        @NamedQuery(
                name = "TrainingInstance.findAllByTrainingDefinitionId",
                query = "SELECT ti FROM TrainingInstance ti JOIN FETCH ti.trainingDefinition td WHERE td.id = :trainingDefId"
        ),
        @NamedQuery(
                name = "TrainingInstance.existsAnyForTrainingDefinition",
                query = "SELECT (COUNT(ti) > 0) FROM TrainingInstance ti " +
                        "INNER JOIN ti.trainingDefinition td WHERE td.id = :trainingDefinitionId"
        ),
        @NamedQuery(
                name = "TrainingInstance.isFinished",
                query = "SELECT (COUNT(ti) > 0) FROM TrainingInstance ti WHERE ti.id = :instanceId AND ti.endTime < :currentTime"
        )
})
public class TrainingInstance extends AbstractEntity<Long> {

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

    /**
     * Gets unique identification number of Training instance
     *
     * @return the id
     */
    public Long getId() {
        return super.getId();
    }

    /**
     * Sets unique identification number of Training instance
     *
     * @param id the id
     */
    public void setId(Long id) {
        super.setId(id);
    }

    /**
     * Gets initiation time of Training instance
     *
     * @return the start time
     */
    public LocalDateTime getStartTime() {
        return startTime;
    }

    /**
     * Sets initiation time of Training instance
     *
     * @param startTime the start time
     */
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    /**
     * Gets finish time of Training instance
     *
     * @return the end time
     */
    public LocalDateTime getEndTime() {
        return endTime;
    }

    /**
     * Sets finish time of Training instance
     *
     * @param endTime the end time
     */
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    /**
     * Gets access token needed to start Training runs associated with given Training Instance
     *
     * @return the access token
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * Sets access token needed to start Training runs associated with given Training Instance
     *
     * @param accessToken the access token
     */
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    /**
     * Gets title of Training Instance
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets title of Training Instance
     *
     * @param title the title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets maximal size of pools that can be allocated for Training Instance
     *
     * @return the pool size
     */
    public int getPoolSize() {
        return poolSize;
    }

    /**
     * Sets maximal size of pools that can be allocated for Training Instance
     *
     * @param poolSize the pool size
     */
    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    /**
     * Gets unique identification number of sandbox pool associated with given Training instance
     *
     * @return the pool id
     */
    public Long getPoolId() {
        return poolId;
    }

    /**
     * Sets unique identification number of sandbox pool associated with given Training instance
     *
     * @param poolId the pool id
     */
    public void setPoolId(Long poolId) {
        this.poolId = poolId;
    }

    /**
     * Gets Training definition associated with given Training instance
     *
     * @return the training definition
     */
    public TrainingDefinition getTrainingDefinition() {
        return trainingDefinition;
    }

    /**
     * Sets Training definition associated with given Training instance
     *
     * @param trainingDefinition the training definition
     */
    public void setTrainingDefinition(TrainingDefinition trainingDefinition) {
        this.trainingDefinition = trainingDefinition;
    }

    /**
     * Gets set of users that can make changes to the Training instance
     *
     * @return the organizers
     */
    public Set<UserRef> getOrganizers() {
        return Collections.unmodifiableSet(organizers);
    }

    /**
     * Sets set of users that can make changes to the Training instance
     *
     * @param organizers the organizers
     */
    public void setOrganizers(Set<UserRef> organizers) {
        this.organizers = organizers;
    }

    /**
     * Adds user to the set of users that can make changes to the Training instance
     *
     * @param userRef the user ref
     */
    public void addOrganizer(UserRef userRef) {
        this.organizers.add(userRef);
        userRef.addTrainingInstance(this);
    }

    /**
     * Remove organizers with given ids from the set of organizers.
     *
     * @param userRefIds ids of the organizers to be removed.
     */
    public void removeOrganizersByUserRefIds(Set<Long> userRefIds) {
        this.organizers.removeIf(userRef -> userRefIds.contains(userRef.getUserRefId()));
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
                "id=" + super.getId() +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", title='" + title + '\'' +
                ", poolSize=" + poolSize +
                ", accessToken='" + accessToken + '\'' +
                '}';
    }
}
