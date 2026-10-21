package cz.cyberrange.platform.training.persistence.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cz.cyberrange.platform.training.persistence.model.InfoLevel;
import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.persistence.model.UserRef;
import cz.cyberrange.platform.training.persistence.model.enums.TDState;
import cz.cyberrange.platform.training.persistence.util.TestDataFactory;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

@DataJpaTest
public class TrainingDefinitionRepositoryTest {

  @Autowired private TestEntityManager entityManager;
  @Autowired private TestDataFactory testDataFactory;
  @Autowired private TrainingDefinitionRepository trainingDefinitionRepository;

  private TrainingDefinition trainingDefinition1,
      trainingDefinition2,
      trainingDefinition3,
      trainingDefinition4,
      trainingDefinition5,
      trainingDefinition6;
  private UserRef author1, author2;

  @BeforeEach
  public void init() {
    author1 = new UserRef();
    author1.setUserRefId(3L);
    author2 = new UserRef();
    author2.setUserRefId(8L);

    trainingDefinition1 = testDataFactory.getReleasedDefinition();
    trainingDefinition2 = testDataFactory.getUnreleasedDefinition();

    trainingDefinition3 = testDataFactory.getReleasedDefinition();
    trainingDefinition4 = testDataFactory.getReleasedDefinition();
    trainingDefinition5 = testDataFactory.getReleasedDefinition();
    trainingDefinition6 = testDataFactory.getReleasedDefinition();

    entityManager.persist(trainingDefinition1);
    entityManager.persist(trainingDefinition2);
    entityManager.persist(trainingDefinition3);
    entityManager.persist(trainingDefinition4);
    entityManager.persist(trainingDefinition5);
    entityManager.persist(trainingDefinition6);
  }

  @Test
  public void findAllAsDesigner() {
    entityManager.persist(author1);
    entityManager.persist(author2);

    trainingDefinition2.addAuthor(author1);
    trainingDefinition2.addAuthor(author2);
    trainingDefinition3.addAuthor(author1);
    trainingDefinition4.addAuthor(author1);
    trainingDefinition4.addAuthor(author2);

    trainingDefinition5.addAuthor(author2);

    List<TrainingDefinition> trainingDefinitions =
        trainingDefinitionRepository
            .findAll(null, PageRequest.of(0, 10), author1.getUserRefId())
            .getContent();
    assertTrue(
        trainingDefinitions.containsAll(
            List.of(trainingDefinition2, trainingDefinition3, trainingDefinition4)));
    assertEquals(3, trainingDefinitions.size());
  }

  @Test
  public void findPlayedDefinitionIdsByUser() {
    InfoLevel infoLevel1 = testDataFactory.getInfoLevel1();
    InfoLevel infoLevel2 = testDataFactory.getInfoLevel2();
    InfoLevel infoLevel3 = testDataFactory.getInfoLevel1();

    infoLevel1.setTrainingDefinition(trainingDefinition1);
    infoLevel2.setTrainingDefinition(trainingDefinition2);
    infoLevel3.setTrainingDefinition(trainingDefinition5);

    entityManager.persist(infoLevel1);
    entityManager.persist(infoLevel2);
    entityManager.persist(infoLevel3);

    UserRef participantRef1 = testDataFactory.getUserRef1();
    UserRef participantRef2 = testDataFactory.getUserRef2();
    entityManager.persist(participantRef1);
    entityManager.persist(participantRef2);

    TrainingInstance trainingInstance1 = testDataFactory.getConcludedInstance();
    TrainingInstance trainingInstance2 = testDataFactory.getOngoingInstance();
    TrainingInstance trainingInstance3 = testDataFactory.getFutureInstance();

    trainingInstance1.setTrainingDefinition(trainingDefinition1);
    trainingInstance2.setTrainingDefinition(trainingDefinition2);
    trainingInstance3.setTrainingDefinition(trainingDefinition5);

    entityManager.persist(trainingInstance1);
    entityManager.persist(trainingInstance2);
    entityManager.persist(trainingInstance3);

    TrainingRun trainingRun1 = testDataFactory.getRunningRun();
    trainingRun1.setParticipantRef(participantRef1);
    trainingRun1.setTrainingInstance(trainingInstance1);
    trainingRun1.setCurrentLevel(infoLevel1);

    TrainingRun trainingRun2 = testDataFactory.getArchivedRun();
    trainingRun2.setParticipantRef(participantRef1);
    trainingRun2.setTrainingInstance(trainingInstance2);
    trainingRun2.setCurrentLevel(infoLevel2);

    TrainingRun trainingRun3 = testDataFactory.getFinishedRun();
    trainingRun3.setParticipantRef(participantRef2);
    trainingRun3.setTrainingInstance(trainingInstance1);
    trainingRun3.setCurrentLevel(infoLevel1);

    TrainingRun trainingRun4 = testDataFactory.getRunningRun();
    trainingRun4.setParticipantRef(participantRef2);
    trainingRun4.setTrainingInstance(trainingInstance3);
    trainingRun4.setCurrentLevel(infoLevel3);

    entityManager.persist(trainingRun1);
    entityManager.persist(trainingRun2);
    entityManager.persist(trainingRun3);
    entityManager.persist(trainingRun4);

    Set<Long> playedDefinitionIds =
        trainingDefinitionRepository.findPlayedDefinitionIdsByUser(participantRef1.getUserRefId());
    assertEquals(
        Set.of(trainingDefinition1.getId(), trainingDefinition2.getId()), playedDefinitionIds);
  }

  @Test
  @DisplayName("findAllByState, page sorted by id descending: returns definitions in that order")
  void findAllByState_pageSortedByIdDescending_returnsDefinitionsInThatOrder() {
    List<Long> releasedIdsDescending =
        trainingDefinitionRepository
            .findAllByState(TDState.RELEASED, PageRequest.of(0, 10, Sort.by(Direction.DESC, "id")))
            .map(TrainingDefinition::getId)
            .getContent();

    assertEquals(
        List.of(
            trainingDefinition6.getId(),
            trainingDefinition5.getId(),
            trainingDefinition4.getId(),
            trainingDefinition3.getId(),
            trainingDefinition1.getId()),
        releasedIdsDescending);
  }
}
