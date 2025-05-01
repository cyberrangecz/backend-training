package cz.cyberrange.platform.training.persistence.model;

import java.time.LocalDateTime;
import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "team_message")
public class TeamMessage {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "messageGenerator")
  @SequenceGenerator(name = "messageGenerator", sequenceName = "message_seq")
  @Column(name = "message_id", nullable = false, unique = true)
  private Long messageId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "team_id")
  private Team team;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_ref_id")
  private UserRef sender;

  @Column(name = "time")
  private LocalDateTime time;

  @Column(name = "message")
  private String message;
}
