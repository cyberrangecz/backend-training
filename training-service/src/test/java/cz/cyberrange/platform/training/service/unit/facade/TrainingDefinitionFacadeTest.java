package cz.cyberrange.platform.training.service.unit.facade;

import static cz.cyberrange.platform.training.service.unit.facade.FacadeTestStubs.stubInstanceCounts;
import static cz.cyberrange.platform.training.service.unit.facade.FacadeTestStubs.stubUserLookup;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import cz.cyberrange.platform.training.api.dto.AbstractLevelDTO;
import cz.cyberrange.platform.training.api.dto.UserRefDTO;
import cz.cyberrange.platform.training.api.dto.infolevel.InfoLevelCreateDTO;
import cz.cyberrange.platform.training.api.dto.infolevel.InfoLevelUpdateDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionBasicDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionCreateDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionUpdateDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionWithLevelsDTO;
import cz.cyberrange.platform.training.api.enums.LevelType;
import cz.cyberrange.platform.training.api.enums.RoleType;
import cz.cyberrange.platform.training.api.enums.TDState;
import cz.cyberrange.platform.training.api.exceptions.EntityConflictException;
import cz.cyberrange.platform.training.api.exceptions.EntityNotFoundException;
import cz.cyberrange.platform.training.api.exceptions.InternalServerErrorException;
import cz.cyberrange.platform.training.api.responses.PageResultResource;
import cz.cyberrange.platform.training.persistence.model.AbstractLevel;
import cz.cyberrange.platform.training.persistence.model.AssessmentLevel;
import cz.cyberrange.platform.training.persistence.model.InfoLevel;
import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import cz.cyberrange.platform.training.persistence.model.TrainingLevel;
import cz.cyberrange.platform.training.persistence.model.UserRef;
import cz.cyberrange.platform.training.persistence.util.TestDataFactory;
import cz.cyberrange.platform.training.service.enums.RoleTypeSecurity;
import cz.cyberrange.platform.training.service.facade.TrainingDefinitionFacade;
import cz.cyberrange.platform.training.service.mapping.mapstruct.AttachmentMapperImpl;
import cz.cyberrange.platform.training.service.mapping.mapstruct.EnumMapperImpl;
import cz.cyberrange.platform.training.service.mapping.mapstruct.HintMapperImpl;
import cz.cyberrange.platform.training.service.mapping.mapstruct.LevelMapperImpl;
import cz.cyberrange.platform.training.service.mapping.mapstruct.MitreTechniqueMapperImpl;
import cz.cyberrange.platform.training.service.mapping.mapstruct.QuestionMapperImpl;
import cz.cyberrange.platform.training.service.mapping.mapstruct.TrainingDefinitionMapperImpl;
import cz.cyberrange.platform.training.service.services.SecurityService;
import cz.cyberrange.platform.training.service.services.TrainingDefinitionService;
import cz.cyberrange.platform.training.service.services.UserNameFilter;
import cz.cyberrange.platform.training.service.services.UserService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(
    classes = {
      TestDataFactory.class,
      EnumMapperImpl.class,
      TrainingDefinitionMapperImpl.class,
      LevelMapperImpl.class,
      QuestionMapperImpl.class,
      HintMapperImpl.class,
      AttachmentMapperImpl.class,
      MitreTechniqueMapperImpl.class
    })
public class TrainingDefinitionFacadeTest {

  private static final UserNameFilter JANE_DOE_FILTER = new UserNameFilter(null, null, "Jane Doe");

  private TrainingDefinitionFacade trainingDefinitionFacade;

  @Autowired private TestDataFactory testDataFactory;
  @Autowired private TrainingDefinitionMapperImpl trainingDefinitionMapper;
  @Autowired private LevelMapperImpl levelMapper;
  @Autowired private HintMapperImpl hintMapper;

  @MockitoBean private TrainingDefinitionService trainingDefinitionService;
  @MockitoBean private SecurityService securityService;
  @MockitoBean private UserService userService;

  private TrainingDefinition trainingDefinition1, trainingDefinition2;
  private TrainingDefinitionUpdateDTO trainingDefinitionUpdate;
  private TrainingDefinitionCreateDTO trainingDefinitionCreate;

  private AssessmentLevel assessmentLevel;
  private TrainingLevel trainingLevel;
  private InfoLevel infoLevel;

  private UserRef author1, author2, author3;
  private UserRefDTO authorDTO1, authorDTO2, authorDTO3;
  private Pageable pageable;
  private PageResultResource.Pagination pagination;

  @Captor private ArgumentCaptor<TrainingDefinition> definitionCaptor;
  @Captor private ArgumentCaptor<List<AbstractLevel>> levelsCaptor;
  private AutoCloseable mocks;

  @BeforeEach
  public void init() {
    mocks = MockitoAnnotations.openMocks(this);
    trainingDefinitionFacade =
        new TrainingDefinitionFacade(
            trainingDefinitionService,
            userService,
            securityService,
            trainingDefinitionMapper,
            levelMapper,
            hintMapper);

    author1 = new UserRef();
    author1.setId(1L);
    author1.setUserRefId(10L);
    author2 = new UserRef();
    author2.setId(2L);
    author2.setUserRefId(20L);
    author3 = new UserRef();
    author3.setId(3L);
    author3.setUserRefId(30L);

    authorDTO1 =
        createUserRefDTO(
            10L,
            "Bc. Alexander Howell",
            "Howell",
            "Alexander",
            "mail1@test.cz",
            "https://oidc.provider.cz/oidc",
            null);
    authorDTO2 =
        createUserRefDTO(
            20L,
            "Bc. Peter Reeves",
            "Reeves",
            "Peter",
            "mail2@test.cz",
            "https://oidc.provider.cz/oidc",
            null);
    authorDTO3 =
        createUserRefDTO(
            30L,
            "Ing. Lee Nicholls",
            "Nicholls",
            "Lee",
            "mail3@test.cz",
            "https://oidc.provider.cz/oidc",
            null);

    trainingLevel = testDataFactory.getPenalizedLevel();
    trainingLevel.setId(2L);
    trainingLevel.setOrder(2);

    infoLevel = testDataFactory.getInfoLevel1();
    infoLevel.setId(3L);
    infoLevel.setOrder(3);

    assessmentLevel = testDataFactory.getQuestionnaire();
    assessmentLevel.setId(1L);
    assessmentLevel.setOrder(1);

    trainingDefinition1 = testDataFactory.getReleasedDefinition();
    trainingDefinition1.setId(1L);
    trainingDefinition1.setAuthors(new HashSet<>(Set.of(author1, author2)));

    trainingDefinition2 = testDataFactory.getUnreleasedDefinition();
    trainingDefinition2.setId(2L);

    InfoLevelUpdateDTO infoLevelUpdate = testDataFactory.getInfoLevelUpdateDTO();
    infoLevelUpdate.setLevelType(LevelType.INFO_LEVEL);
    trainingDefinitionUpdate = testDataFactory.getTrainingDefinitionUpdateDTO();
    trainingDefinitionUpdate.setVersion(2L);
    trainingDefinitionUpdate.setLevels(List.of(infoLevelUpdate));

    trainingDefinitionCreate = testDataFactory.getTrainingDefinitionCreateDTO();
    given(userService.getUserRefFromUserAndGroup()).willReturn(authorDTO1);
  }

  @AfterEach
  public void closeMocks() throws Exception {
    mocks.close();
  }

  @Test
  public void findTrainingDefinitionById() {
    given(trainingDefinitionService.findById(1L)).willReturn(trainingDefinition1);
    given(trainingDefinitionService.findAllLevelsFromDefinition(anyLong()))
        .willReturn(List.of(trainingLevel));
    stubInstanceCounts(trainingDefinitionService, Map.of(1L, 3L));
    TrainingDefinitionWithLevelsDTO definition = trainingDefinitionFacade.findById(1L);
    assertEquals(3L, definition.getInstanceCount());
    assertEquals(1, definition.getLevels().size());
    AbstractLevelDTO level = definition.getLevels().get(0);
    assertEquals(trainingLevel.getTitle(), level.getTitle());
    assertEquals(trainingLevel.getOrder(), level.getOrder());
    assertEquals(trainingLevel.getId(), level.getId());
    then(trainingDefinitionService).should().findById(1L);
  }

  @Test
  public void findAllTrainingDefinitions() {
    List<TrainingDefinition> expected = new ArrayList<>();
    expected.add(trainingDefinition1);
    expected.add(trainingDefinition2);

    Page<TrainingDefinition> p = new PageImpl<TrainingDefinition>(expected);
    PathBuilder<TrainingDefinition> tD =
        new PathBuilder<TrainingDefinition>(TrainingDefinition.class, "trainingDefinition");
    Predicate predicate = tD.isNotNull();

    given(securityService.hasRole(RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR)).willReturn(true);
    given(trainingDefinitionService.findAll(any(Predicate.class), any(Pageable.class)))
        .willReturn(p);
    stubInstanceCounts(trainingDefinitionService, Map.of(1L, 3L, 2L, 4L));

    PageResultResource<TrainingDefinitionDTO> trainingDefinitionDTO =
        trainingDefinitionFacade.findAll(predicate, PageRequest.of(0, 2));
    deepEquals(trainingDefinition1, trainingDefinitionDTO.getContent().get(0));
    deepEquals(trainingDefinition2, trainingDefinitionDTO.getContent().get(1));
    assertEquals(3L, trainingDefinitionDTO.getContent().get(0).getInstanceCount());
    assertEquals(4L, trainingDefinitionDTO.getContent().get(1).getInstanceCount());

    then(trainingDefinitionService).should().findAll(predicate, PageRequest.of(0, 2));
  }

  @Test
  public void findTrainingDefinitionsByIdsSetsInstanceCountOfEachDefinition() {
    given(trainingDefinitionService.findAllByIds(List.of(1L, 2L)))
        .willReturn(List.of(trainingDefinition1, trainingDefinition2));
    stubInstanceCounts(trainingDefinitionService, Map.of(1L, 3L));

    List<TrainingDefinitionBasicDTO> definitions =
        trainingDefinitionFacade.findTrainingDefinitionsByIds(List.of(1L, 2L));

    assertEquals(3L, definitions.get(0).getInstanceCount());
    assertEquals(0L, definitions.get(1).getInstanceCount());
  }

  @Test
  public void updateTrainingDefinition() {
    given(trainingDefinitionService.update(eq(2L), eq(2L), any(), anyList()))
        .willReturn(trainingDefinition2);
    given(trainingDefinitionService.findAllLevelsFromDefinition(2L)).willReturn(List.of(infoLevel));
    given(trainingDefinitionService.canBeArchived(2L)).willReturn(false);
    stubInstanceCounts(trainingDefinitionService, Map.of(2L, 3L));

    TrainingDefinitionWithLevelsDTO updatedDefinition =
        trainingDefinitionFacade.update(2L, trainingDefinitionUpdate);

    then(trainingDefinitionService)
        .should()
        .update(eq(2L), eq(2L), definitionCaptor.capture(), levelsCaptor.capture());
    assertEquals(trainingDefinitionUpdate.getTitle(), definitionCaptor.getValue().getTitle());
    assertEquals(
        trainingDefinitionUpdate.getDescription(), definitionCaptor.getValue().getDescription());
    assertEquals(1, levelsCaptor.getValue().size());
    assertInstanceOf(InfoLevel.class, levelsCaptor.getValue().get(0));
    assertFalse(updatedDefinition.isCanBeArchived());
    assertEquals(3L, updatedDefinition.getInstanceCount());
    assertEquals(1, updatedDefinition.getLevels().size());
    assertEquals(infoLevel.getId(), updatedDefinition.getLevels().get(0).getId());
  }

  @Test
  public void updateTrainingDefinitionRefusedByServicePropagatesConflict() {
    willThrow(new EntityConflictException())
        .given(trainingDefinitionService)
        .update(eq(2L), eq(2L), any(), anyList());

    assertThrows(
        EntityConflictException.class,
        () -> trainingDefinitionFacade.update(2L, trainingDefinitionUpdate));
  }

  @Test
  public void createTrainingDefinition() {
    InfoLevelCreateDTO infoLevelCreate = testDataFactory.getInfoLevelCreateDTO();
    infoLevelCreate.setLevelType(LevelType.INFO_LEVEL);
    trainingDefinitionCreate.setLevels(List.of(infoLevelCreate));
    given(trainingDefinitionService.create(any(TrainingDefinition.class), anyList()))
        .willReturn(trainingDefinition2);

    trainingDefinitionFacade.create(trainingDefinitionCreate);

    then(trainingDefinitionService)
        .should()
        .create(definitionCaptor.capture(), levelsCaptor.capture());
    assertEquals(trainingDefinitionCreate.getTitle(), definitionCaptor.getValue().getTitle());
    assertEquals(1, levelsCaptor.getValue().size());
    assertInstanceOf(InfoLevel.class, levelsCaptor.getValue().get(0));
    assertNull(levelsCaptor.getValue().get(0).getId());
  }

  @Test
  public void cloneTrainingDefinition() {
    given(trainingDefinitionService.clone(trainingDefinition1.getId(), "title"))
        .willReturn(trainingDefinition1);
    given(trainingDefinitionService.findAllLevelsFromDefinition(anyLong()))
        .willReturn(List.of(trainingLevel));
    TrainingDefinitionWithLevelsDTO definition =
        trainingDefinitionFacade.clone(trainingDefinition1.getId(), "title");
    assertEquals(1, definition.getLevels().size());
    AbstractLevelDTO level = definition.getLevels().get(0);
    assertEquals(trainingLevel.getTitle(), level.getTitle());
    assertEquals(trainingLevel.getOrder(), level.getOrder());
    assertEquals(trainingLevel.getId(), level.getId());
    then(trainingDefinitionService).should().clone(trainingDefinition1.getId(), "title");
  }

  @Test
  public void deleteTrainingDefinition() {
    trainingDefinitionFacade.delete(trainingDefinition1.getId());
    then(trainingDefinitionService).should().delete(trainingDefinition1.getId());
  }

  @Test
  public void findLevelByIdGameLevel() {
    given(trainingDefinitionService.findLevelById(trainingLevel.getId())).willReturn(trainingLevel);
    AbstractLevelDTO g = trainingDefinitionFacade.findLevelById(trainingLevel.getId());

    assertEquals(levelMapper.mapToDTO(trainingLevel), g);
    assertEquals(LevelType.TRAINING_LEVEL, g.getLevelType());
  }

  @Test
  public void findLevelByIdInfoLevel() {
    given(trainingDefinitionService.findLevelById(infoLevel.getId())).willReturn(infoLevel);
    AbstractLevelDTO i = trainingDefinitionFacade.findLevelById(infoLevel.getId());

    assertEquals(levelMapper.mapToDTO(infoLevel), i);
    assertEquals(LevelType.INFO_LEVEL, i.getLevelType());
  }

  @Test
  public void findLevelByIdAssessmentLevel() {
    given(trainingDefinitionService.findLevelById(assessmentLevel.getId()))
        .willReturn(assessmentLevel);
    AbstractLevelDTO a = trainingDefinitionFacade.findLevelById(assessmentLevel.getId());

    assertEquals(levelMapper.mapToDTO(assessmentLevel), a);
    assertEquals(LevelType.ASSESSMENT_LEVEL, a.getLevelType());
  }

  @Test
  public void switchState() {
    trainingDefinitionFacade.switchState(trainingDefinition2.getId(), TDState.UNRELEASED);
    then(trainingDefinitionService)
        .should()
        .switchState(trainingDefinition2.getId(), TDState.UNRELEASED);
  }

  @Test
  public void getAuthors() {
    pagination = new PageResultResource.Pagination(0, 2, 5, 2, 1);
    given(trainingDefinitionService.findById(trainingDefinition1.getId()))
        .willReturn(trainingDefinition1);
    given(
            userService.getUsersRefDTOByGivenUserIds(
                trainingDefinition1.getAuthors().stream().map(UserRef::getUserRefId).toList(),
                pageable,
                JANE_DOE_FILTER))
        .willReturn(new PageResultResource<>(List.of(authorDTO1, authorDTO2), pagination));
    PageResultResource<UserRefDTO> authors =
        trainingDefinitionFacade.getAuthors(trainingDefinition1.getId(), pageable, JANE_DOE_FILTER);
    assertEquals(authors.getPagination(), pagination);
    assertTrue(authors.getContent().containsAll(Set.of(authorDTO1, authorDTO2)));
  }

  @Test
  public void getAuthorsNotInGivenTrainingDefinition() {
    pagination = new PageResultResource.Pagination(0, 1, 1, 1, 1);
    given(trainingDefinitionService.findById(trainingDefinition1.getId()))
        .willReturn(trainingDefinition1);
    given(
            userService.getUsersByGivenRoleAndNotWithGivenIds(
                RoleType.ROLE_TRAINING_DESIGNER,
                Set.of(author1.getUserRefId(), author2.getUserRefId()),
                pageable,
                JANE_DOE_FILTER))
        .willReturn(new PageResultResource<>(List.of(authorDTO3), pagination));
    PageResultResource<UserRefDTO> organizersNotInTrainingInstance =
        trainingDefinitionFacade.getDesignersNotInGivenTrainingDefinition(
            trainingDefinition1.getId(), pageable, JANE_DOE_FILTER);
    assertEquals(pagination.toString(), organizersNotInTrainingInstance.getPagination().toString());
    assertTrue(organizersNotInTrainingInstance.getContent().contains(authorDTO3));
  }

  @Test
  public void getAuthorsNotInGivenTrainingInstanceUserServiceError() {
    given(trainingDefinitionService.findById(trainingDefinition1.getId()))
        .willReturn(trainingDefinition1);
    willThrow(new InternalServerErrorException("Error when calling User And Group endpoint."))
        .given(userService)
        .getUsersByGivenRoleAndNotWithGivenIds(
            RoleType.ROLE_TRAINING_DESIGNER,
            new HashSet<>(Set.of(author1.getUserRefId(), author2.getUserRefId())),
            pageable,
            UserNameFilter.NONE);
    Exception ex =
        assertThrows(
            InternalServerErrorException.class,
            () ->
                trainingDefinitionFacade.getDesignersNotInGivenTrainingDefinition(
                    trainingDefinition1.getId(), pageable, UserNameFilter.NONE));
    assertEquals("Error when calling User And Group endpoint.", ex.getMessage());
  }

  @Test
  public void editAuthors() {
    given(trainingDefinitionService.findById(trainingDefinition1.getId()))
        .willReturn(trainingDefinition1);
    given(securityService.getUserRefIdFromUserAndGroup()).willReturn(author1.getUserRefId());
    given(userService.createOrGetUserRef(author1.getUserRefId())).willReturn(author1);
    stubUserLookup(userService, List.of(author3.getUserRefId()), List.of(authorDTO3));
    given(userService.createOrGetUserRef(author3.getUserRefId())).willReturn(author3);
    trainingDefinitionFacade.editAuthors(
        trainingDefinition1.getId(),
        new HashSet<>(Set.of(author3.getUserRefId())),
        new HashSet<>(Set.of(author2.getUserRefId())));
    assertEquals(2, trainingDefinition1.getAuthors().size());
    assertTrue(trainingDefinition1.getAuthors().containsAll(Set.of(author1, author3)));
  }

  @Test
  public void editAuthorsRemoveLoggedInAuthor() {
    given(trainingDefinitionService.findById(trainingDefinition1.getId()))
        .willReturn(trainingDefinition1);
    given(securityService.getUserRefIdFromUserAndGroup()).willReturn(author1.getUserRefId());
    given(userService.createOrGetUserRef(author1.getUserRefId())).willReturn(author1);
    stubUserLookup(userService, List.of(author3.getUserRefId()), List.of(authorDTO3));
    given(userService.createOrGetUserRef(author3.getUserRefId())).willReturn(author3);
    trainingDefinitionFacade.editAuthors(
        trainingDefinition1.getId(),
        new HashSet<>(Set.of(author3.getUserRefId())),
        new HashSet<>(Set.of(author1.getUserRefId())));
    assertEquals(3, trainingDefinition1.getAuthors().size());
    assertTrue(trainingDefinition1.getAuthors().containsAll(Set.of(author1, author2, author3)));
  }

  @Test
  public void editAuthorsConcurrentlyRemoveAndAddOAuthorWhoIsNotInTrainingDefinition() {
    given(trainingDefinitionService.findById(trainingDefinition1.getId()))
        .willReturn(trainingDefinition1);
    given(securityService.getUserRefIdFromUserAndGroup()).willReturn(author1.getUserRefId());
    given(userService.createOrGetUserRef(author1.getUserRefId())).willReturn(author1);
    stubUserLookup(userService, List.of(author3.getUserRefId()), List.of(authorDTO3));
    given(userService.createOrGetUserRef(author3.getUserRefId())).willReturn(author3);
    trainingDefinitionFacade.editAuthors(
        trainingDefinition1.getId(),
        new HashSet<>(Set.of(author3.getUserRefId())),
        new HashSet<>(Set.of(author3.getUserRefId())));
    assertEquals(3, trainingDefinition1.getAuthors().size());
    assertTrue(trainingDefinition1.getAuthors().containsAll(Set.of(author1, author2, author3)));
  }

  @Test
  public void editAuthorsConcurrentlyRemoveAndAddAuthorWhoIsInTrainingDefinition() {
    given(trainingDefinitionService.findById(trainingDefinition1.getId()))
        .willReturn(trainingDefinition1);
    given(securityService.getUserRefIdFromUserAndGroup()).willReturn(author1.getUserRefId());
    given(userService.createOrGetUserRef(author1.getUserRefId())).willReturn(author1);
    stubUserLookup(userService, List.of(author2.getUserRefId()), List.of(authorDTO2));
    given(userService.createOrGetUserRef(author2.getUserRefId())).willReturn(author2);
    trainingDefinitionFacade.editAuthors(
        trainingDefinition1.getId(),
        new HashSet<>(Set.of(author2.getUserRefId())),
        new HashSet<>(Set.of(author2.getUserRefId())));
    assertEquals(2, trainingDefinition1.getAuthors().size());
    assertTrue(trainingDefinition1.getAuthors().containsAll(Set.of(author1, author2)));
  }

  @Test
  public void editAuthorsWithEmptySetOfRemovalAndAdditionSets() {
    given(trainingDefinitionService.findById(trainingDefinition1.getId()))
        .willReturn(trainingDefinition1);
    given(securityService.getUserRefIdFromUserAndGroup()).willReturn(author1.getUserRefId());
    trainingDefinitionFacade.editAuthors(
        trainingDefinition1.getId(), new HashSet<>(), new HashSet<>());
    assertEquals(2, trainingDefinition1.getAuthors().size());
    assertTrue(trainingDefinition1.getAuthors().containsAll(Set.of(author1, author2)));
  }

  @Test
  public void editAuthorsRemove() {
    given(trainingDefinitionService.findById(trainingDefinition1.getId()))
        .willReturn(trainingDefinition1);
    given(securityService.getUserRefIdFromUserAndGroup()).willReturn(author1.getUserRefId());
    trainingDefinitionFacade.editAuthors(
        trainingDefinition1.getId(),
        new HashSet<>(),
        new HashSet<>(Set.of(author1.getUserRefId(), author2.getUserRefId())));
    assertEquals(1, trainingDefinition1.getAuthors().size());
    assertTrue(trainingDefinition1.getAuthors().contains(author1));
  }

  @Test
  public void editAuthorsAdd() {
    trainingDefinition1.removeAuthorsByUserRefIds(Set.of(author2.getUserRefId()));
    given(trainingDefinitionService.findById(trainingDefinition1.getId()))
        .willReturn(trainingDefinition1);
    given(securityService.getUserRefIdFromUserAndGroup()).willReturn(author1.getUserRefId());
    given(userService.createOrGetUserRef(author1.getUserRefId())).willReturn(author1);
    stubUserLookup(
        userService,
        List.of(author2.getUserRefId(), author3.getUserRefId()),
        List.of(authorDTO3, authorDTO2));
    given(userService.createOrGetUserRef(author3.getUserRefId())).willReturn(author3);
    given(userService.createOrGetUserRef(author2.getUserRefId())).willReturn(author2);
    trainingDefinitionFacade.editAuthors(
        trainingDefinition1.getId(),
        new HashSet<>(Set.of(author2.getUserRefId(), author3.getUserRefId())),
        new HashSet<>());
    assertEquals(3, trainingDefinition1.getAuthors().size());
    assertTrue(trainingDefinition1.getAuthors().containsAll(Set.of(author1, author2, author3)));
  }

  @Test
  public void editAuthorsAddUserRefNotInDB() {
    trainingDefinition1.removeAuthorsByUserRefIds(Set.of(author2.getUserRefId()));
    given(trainingDefinitionService.findById(trainingDefinition1.getId()))
        .willReturn(trainingDefinition1);
    given(securityService.getUserRefIdFromUserAndGroup()).willReturn(author1.getUserRefId());
    given(userService.createOrGetUserRef(author1.getUserRefId())).willReturn(author1);
    stubUserLookup(
        userService,
        List.of(author2.getUserRefId(), author3.getUserRefId()),
        List.of(authorDTO3, authorDTO2));
    given(userService.createOrGetUserRef(author3.getUserRefId())).willReturn(author3);
    willThrow(EntityNotFoundException.class)
        .given(userService)
        .getUserByUserRefId(author2.getUserRefId());
    given(userService.createOrGetUserRef(author2.getUserRefId())).willReturn(author2);
    trainingDefinitionFacade.editAuthors(
        trainingDefinition1.getId(),
        new HashSet<>(Set.of(author2.getUserRefId(), author3.getUserRefId())),
        new HashSet<>());
    assertEquals(3, trainingDefinition1.getAuthors().size());
    assertTrue(trainingDefinition1.getAuthors().containsAll(Set.of(author1, author2, author3)));
  }

  private UserRefDTO createUserRefDTO(
      Long userRefId,
      String fullName,
      String familyName,
      String givenName,
      String sub,
      String iss,
      byte[] picture) {
    UserRefDTO userRefDTO = new UserRefDTO();
    userRefDTO.setUserRefId(userRefId);
    userRefDTO.setUserRefFullName(fullName);
    userRefDTO.setUserRefFamilyName(familyName);
    userRefDTO.setUserRefGivenName(givenName);
    userRefDTO.setUserRefSub(sub);
    userRefDTO.setIss(iss);
    userRefDTO.setPicture(picture);
    return userRefDTO;
  }

  private void deepEquals(TrainingDefinition expected, TrainingDefinitionDTO actual) {
    assertEquals(expected.getId(), actual.getId());
    assertEquals(expected.getTitle(), actual.getTitle());
    assertEquals(expected.getEstimatedDuration(), actual.getEstimatedDuration());
    assertEquals(expected.getLastEdited(), actual.getLastEdited());
    assertEquals(expected.getDescription(), actual.getDescription());
  }
}
