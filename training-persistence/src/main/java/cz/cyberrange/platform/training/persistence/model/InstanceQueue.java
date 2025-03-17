package cz.cyberrange.platform.training.persistence.model;

import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import java.util.List;

@Entity()
public class InstanceQueue {

    @Id
    @JoinColumn(name = "id")
    @ForeignKey(name = "queue_id")
    TrainingInstance trainingInstance;

    List<UserRef> queue;

    List<Team> teams;

}
