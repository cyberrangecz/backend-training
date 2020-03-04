package cz.muni.ics.kypo.training.facade;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import cz.muni.ics.kypo.training.api.responses.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.AbstractLevelDTO;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.betatestinggroup.BetaTestingGroupCreateDTO;
import cz.muni.ics.kypo.training.api.dto.betatestinggroup.BetaTestingGroupUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.gamelevel.GameLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionCreateDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionUpdateDTO;
import cz.muni.ics.kypo.training.api.enums.AssessmentType;
import cz.muni.ics.kypo.training.api.enums.LevelType;
import cz.muni.ics.kypo.training.api.enums.RoleType;
import cz.muni.ics.kypo.training.exceptions.EntityNotFoundException;
import cz.muni.ics.kypo.training.exceptions.InternalServerErrorException;
import cz.muni.ics.kypo.training.mapping.mapstruct.*;
import cz.muni.ics.kypo.training.mapping.modelmapper.BeanMapping;
import cz.muni.ics.kypo.training.mapping.modelmapper.BeanMappingImpl;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.enums.TDState;
import cz.muni.ics.kypo.training.service.TrainingDefinitionService;
import cz.muni.ics.kypo.training.service.UserService;
import cz.muni.ics.kypo.training.service.impl.SecurityService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {InfoLevelMapperImpl.class, TrainingDefinitionMapperImpl.class,
        UserRefMapperImpl.class, GameLevelMapperImpl.class, InfoLevelMapperImpl.class, BetaTestingGroupMapperImpl.class,
        AssessmentLevelMapperImpl.class, HintMapperImpl.class, BasicLevelInfoMapperImpl.class})
public class TrainingDefinitionFacadeTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private TrainingDefinitionFacade trainingDefinitionFacade;

    @Autowired
    private TrainingDefinitionMapperImpl trainingDefinitionMapper;
    @Autowired
    private GameLevelMapperImpl gameLevelMapper;
    @Autowired
    private InfoLevelMapperImpl infoLevelMapper;
    @Autowired
    private AssessmentLevelMapperImpl assessmentLevelMapper;
    @Autowired
    private BasicLevelInfoMapperImpl basicLevelInfoMapper;

    @Mock
    private TrainingDefinitionService trainingDefinitionService;
    @Mock
    private SecurityService securityService;
    @Mock
    private UserService userService;

    private BeanMapping beanMapping;

    private TrainingDefinition trainingDefinition1, trainingDefinition2;
    private TrainingDefinitionUpdateDTO trainingDefinitionUpdate;
    private TrainingDefinitionCreateDTO trainingDefinitionCreate;

    private AssessmentLevel assessmentLevel;
    private AssessmentLevelUpdateDTO alUpdate;

    private GameLevel gameLevel;
    private GameLevelUpdateDTO gameLevelUpdate;

    private InfoLevel infoLevel;
    private InfoLevelUpdateDTO infoLevelUpdate;

    private BetaTestingGroupUpdateDTO betaTestingGroupUpdateDTO;
    private BetaTestingGroupCreateDTO betaTestingGroupCreateDTO;

    private UserRef author1, author2, author3;
    private UserRefDTO authorDTO1, authorDTO2, authorDTO3;
    private Pageable pageable;
    private PageResultResource.Pagination pagination;

    private UserRefDTO userInfoDTO1, userInfoDTO2;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        trainingDefinitionFacade = new TrainingDefinitionFacadeImpl(trainingDefinitionService,
                trainingDefinitionMapper, gameLevelMapper, infoLevelMapper, assessmentLevelMapper, basicLevelInfoMapper, userService, securityService);
        beanMapping = new BeanMappingImpl(new ModelMapper());
        assessmentLevel = new AssessmentLevel();
        assessmentLevel.setId(1L);
        assessmentLevel.setOrder(1);

        alUpdate = new AssessmentLevelUpdateDTO();
        alUpdate.setId(2L);

        author1 = new UserRef();
        author1.setId(1L);
        author1.setUserRefId(10L);
        author2 = new UserRef();
        author2.setId(2L);
        author2.setUserRefId(20L);
        author3 = new UserRef();
        author3.setId(3L);
        author3.setUserRefId(30L);

        authorDTO1 = createUserRefDTO(10L, "Bc. Dominik Meškal", "Meškal", "Dominik", "445533@muni.cz", "https://oidc.muni.cz/oidc", null);
        authorDTO2 = createUserRefDTO(20L, "Bc. Boris Makal", "Makal", "Boris", "772211@muni.cz", "https://oidc.muni.cz/oidc", null);
        authorDTO3 = createUserRefDTO(30L, "Ing. Pavel Flákal", "Flákal", "Pavel", "221133@muni.cz", "https://oidc.muni.cz/oidc", null);

        gameLevel = new GameLevel();
        gameLevel.setId(2L);
        gameLevel.setOrder(2);
        gameLevel.setSolution("solution");

        gameLevelUpdate = new GameLevelUpdateDTO();
        gameLevelUpdate.setId(2L);
        gameLevelUpdate.setTitle("title");
        gameLevelUpdate.setContent("Content");
        gameLevelUpdate.setEstimatedDuration(1000);
        gameLevelUpdate.setFlag("flag1");
        gameLevelUpdate.setIncorrectFlagLimit(4);
        gameLevelUpdate.setSolutionPenalized(true);
        gameLevelUpdate.setSolution("solution");

        infoLevel = new InfoLevel();
        infoLevel.setId(3L);
        infoLevel.setOrder(3);

        infoLevelUpdate = new InfoLevelUpdateDTO();
        infoLevelUpdate.setId(3L);
        infoLevelUpdate.setTitle("some title");
        infoLevelUpdate.setContent("some content");

        alUpdate = new AssessmentLevelUpdateDTO();
        alUpdate.setInstructions("instructions");
        alUpdate.setMaxScore(50);
        alUpdate.setQuestions("test");
        alUpdate.setTitle("Some title");
        alUpdate.setType(AssessmentType.QUESTIONNAIRE);

        trainingDefinition1 = new TrainingDefinition();
        trainingDefinition1.setId(1L);
        trainingDefinition1.setState(TDState.RELEASED);
        trainingDefinition1.setAuthors(new HashSet<>(Set.of(author1, author2)));

        trainingDefinition2 = new TrainingDefinition();
        trainingDefinition2.setId(2L);
        trainingDefinition2.setState(TDState.UNRELEASED);

        betaTestingGroupUpdateDTO = new BetaTestingGroupUpdateDTO();
        betaTestingGroupUpdateDTO.setOrganizersRefIds(Set.of());

        trainingDefinitionUpdate = new TrainingDefinitionUpdateDTO();
        trainingDefinitionUpdate.setId(1L);
        trainingDefinitionUpdate.setState(cz.muni.ics.kypo.training.api.enums.TDState.UNRELEASED);
        trainingDefinitionUpdate.setBetaTestingGroup(betaTestingGroupUpdateDTO);

        betaTestingGroupCreateDTO = new BetaTestingGroupCreateDTO();
        betaTestingGroupCreateDTO.setOrganizersRefIds(Set.of());

        trainingDefinitionCreate = new TrainingDefinitionCreateDTO();
        trainingDefinitionCreate.setDescription("TD desc");
        trainingDefinitionCreate.setOutcomes(new String[0]);
        trainingDefinitionCreate.setPrerequisities(new String[0]);
        trainingDefinitionCreate.setState(cz.muni.ics.kypo.training.api.enums.TDState.ARCHIVED);
        trainingDefinitionCreate.setTitle("TD some title");
        trainingDefinitionCreate.setBetaTestingGroup(betaTestingGroupCreateDTO);
    }

    @Test
    public void findTrainingDefinitionById() {
        given(trainingDefinitionService.findById(1L)).willReturn(trainingDefinition1);
        trainingDefinitionFacade.findById(1L);
        then(trainingDefinitionService).should().findById(1L);
    }

    @Test(expected = EntityNotFoundException.class)
    public void findTrainingDefinitionByIdWithFacadeLayerException() {
        willThrow(EntityNotFoundException.class).given(trainingDefinitionService).findById(any(long.class));
        trainingDefinitionFacade.findById(any(Long.class));
    }

    @Test
    public void findAllTrainingDefinitions() {
        List<TrainingDefinition> expected = new ArrayList<>();
        expected.add(trainingDefinition1);
        expected.add(trainingDefinition2);

        Page<TrainingDefinition> p = new PageImpl<TrainingDefinition>(expected);
        PathBuilder<TrainingDefinition> tD = new PathBuilder<TrainingDefinition>(TrainingDefinition.class, "trainingDefinition");
        Predicate predicate = tD.isNotNull();

        given(trainingDefinitionService.findAll(any(Predicate.class), any(Pageable.class))).willReturn(p);

        PageResultResource<TrainingDefinitionDTO> trainingDefinitionDTO = trainingDefinitionFacade.findAll(predicate, PageRequest.of(0, 2));
        deepEquals(trainingDefinition1, trainingDefinitionDTO.getContent().get(0));
        deepEquals(trainingDefinition2, trainingDefinitionDTO.getContent().get(1));

        then(trainingDefinitionService).should().findAll(predicate, PageRequest.of(0, 2));
    }

    @Test
    public void updateTrainingDefinition() {
        BetaTestingGroup viewGroup = new BetaTestingGroup();
        viewGroup.setTrainingDefinition(trainingDefinition1);
        viewGroup.setId(1L);
        viewGroup.setOrganizers(Set.of());
        given(trainingDefinitionService.findById(anyLong())).willReturn(trainingDefinition1);
        given(userService.getUsersRefDTOByGivenUserIds(anySet(), any(), eq(null), eq(null))).willReturn(new PageResultResource<>(new ArrayList<>()));
        trainingDefinitionFacade.update(trainingDefinitionUpdate);
        then(trainingDefinitionService).should().update(trainingDefinitionMapper.mapUpdateToEntity(trainingDefinitionUpdate));
    }

    @Test
    public void updateTrainingDefinitionWithNull() {
        thrown.expect(NullPointerException.class);
        trainingDefinitionFacade.update(null);
    }

    @Test(expected = EntityNotFoundException.class)
    public void updateTrainingDefinitionWithFacadeLayerException() {
        BetaTestingGroup viewGroup = new BetaTestingGroup();
        viewGroup.setTrainingDefinition(trainingDefinition1);
        viewGroup.setId(1L);
        given(trainingDefinitionService.findById(anyLong())).willReturn(trainingDefinition1);
        given(userService.getUsersRefDTOByGivenUserIds(anySet(), any(Pageable.class), eq(null), eq(null))).willReturn(new PageResultResource<>(new ArrayList<>()));
        willThrow(EntityNotFoundException.class).given(trainingDefinitionService)
                .update(any(TrainingDefinition.class));
        trainingDefinitionFacade.update(trainingDefinitionUpdate);
    }

    @Test
    public void createTrainingDefinition() {
        given(trainingDefinitionService.create(trainingDefinitionMapper.mapCreateToEntity(trainingDefinitionCreate)))
                .willReturn(trainingDefinitionMapper.mapCreateToEntity(trainingDefinitionCreate));
        given(userService.getUserByUserRefId(anyLong())).willReturn(author1);
        given(userService.getUsersRefDTOByGivenUserIds(anySet(), any(), eq(null), eq(null))).willReturn(new PageResultResource<>(new ArrayList<>()));
        trainingDefinitionFacade.create(trainingDefinitionCreate);
        then(trainingDefinitionService).should().create(trainingDefinitionMapper.mapCreateToEntity(trainingDefinitionCreate));
    }

    @Test
    public void createTrainingDefinitionWithNull() {
        thrown.expect(NullPointerException.class);
        trainingDefinitionFacade.create(null);
    }

    @Test
    public void cloneTrainingDefinition() {
        given(trainingDefinitionService.clone(trainingDefinition1.getId(), "title")).willReturn(trainingDefinition1);
        trainingDefinitionFacade.clone(trainingDefinition1.getId(), "title");
        then(trainingDefinitionService).should().clone(trainingDefinition1.getId(), "title");
    }

    @Test
    public void cloneTrainingDefinitionWithNull() {
        thrown.expect(IllegalArgumentException.class);
        trainingDefinitionFacade.clone(null, "title");
    }

    @Test
    public void deleteTrainingDefinition() {
        trainingDefinitionFacade.delete(trainingDefinition1.getId());
        then(trainingDefinitionService).should().delete(trainingDefinition1.getId());
    }

    @Test
    public void deleteTrainingDefinitionWithNull() {
        thrown.expect(NullPointerException.class);
        trainingDefinitionFacade.delete(null);
    }

    @Test(expected = EntityNotFoundException.class)
    public void deleteTrainingDefinitionWithFacadeLayerException() {
        willThrow(EntityNotFoundException.class).given(trainingDefinitionService).delete(any(Long.class));
        trainingDefinitionFacade.delete(1L);
    }

    @Test
    public void deleteOneLevel() {
        trainingDefinitionFacade.deleteOneLevel(trainingDefinition1.getId(), assessmentLevel.getId());
        then(trainingDefinitionService).should().deleteOneLevel(trainingDefinition1.getId(), assessmentLevel.getId());
    }

    @Test
    public void deleteOneLevelWithNullDefinition() {
        thrown.expect(NullPointerException.class);
        trainingDefinitionFacade.deleteOneLevel(null, assessmentLevel.getId());
    }

    @Test
    public void deleteOneLevelWithNullLevel() {
        thrown.expect(NullPointerException.class);
        trainingDefinitionFacade.deleteOneLevel(trainingDefinition1.getId(), null);
    }

    @Test(expected = EntityNotFoundException.class)
    public void deleteOneLevelWithFacadeLayerException() {
        willThrow(EntityNotFoundException.class).given(trainingDefinitionService).deleteOneLevel(any(Long.class), any(Long.class));
        trainingDefinitionFacade.deleteOneLevel(1L, 1L);
    }

    @Test
    public void updateAssessmentLevel() {
        trainingDefinitionFacade.updateAssessmentLevel(trainingDefinition1.getId(), alUpdate);
        then(trainingDefinitionService).should().updateAssessmentLevel(trainingDefinition1.getId(),
                assessmentLevelMapper.mapUpdateToEntity(alUpdate));
    }

    @Test
    public void updateAssessmentLevelWithNullDefinition() {
        thrown.expect(NullPointerException.class);
        trainingDefinitionFacade.updateAssessmentLevel(null, alUpdate);
    }

    @Test
    public void updateAssessmentLevelWithNullLevel() {
        thrown.expect(NullPointerException.class);
        trainingDefinitionFacade.updateAssessmentLevel(trainingDefinition1.getId(), null);
    }

    @Test
    public void updateGameLevel() {
        trainingDefinitionFacade.updateGameLevel(trainingDefinition2.getId(), gameLevelUpdate);
        then(trainingDefinitionService).should().updateGameLevel(trainingDefinition2.getId(),
                gameLevelMapper.mapUpdateToEntity(gameLevelUpdate));
    }

    @Test
    public void updateGameLevelWithNullDefinition() {
        thrown.expect(NullPointerException.class);
        trainingDefinitionFacade.updateGameLevel(null, gameLevelUpdate);
    }

    @Test
    public void updateGameLevelWithNullLevel() {
        thrown.expect(NullPointerException.class);
        trainingDefinitionFacade.updateGameLevel(trainingDefinition2.getId(), null);
    }


    @Test
    public void updateInfoLevel() {
        trainingDefinitionFacade.updateInfoLevel(trainingDefinition2.getId(), infoLevelUpdate);
        then(trainingDefinitionService).should().updateInfoLevel(trainingDefinition2.getId(),
                infoLevelMapper.mapUpdateToEntity(infoLevelUpdate));
    }

    @Test
    public void updateInfoLevelWithNullDefinition() {
        thrown.expect(NullPointerException.class);
        trainingDefinitionFacade.updateInfoLevel(null, infoLevelUpdate);
    }

    @Test
    public void updateInfoLevelWithNullLevel() {
        thrown.expect(NullPointerException.class);
        trainingDefinitionFacade.updateInfoLevel(trainingDefinition2.getId(), null);
    }



    @Test
    public void createInfoLevel() {
        given(trainingDefinitionService.createInfoLevel(trainingDefinition1.getId())).willReturn(infoLevel);
        trainingDefinitionFacade.createInfoLevel(trainingDefinition1.getId());
        then(trainingDefinitionService).should().createInfoLevel(trainingDefinition1.getId());
    }

    @Test
    public void createInfoLevelWithNullDefinitionId() {
        thrown.expect(NullPointerException.class);
        trainingDefinitionFacade.createInfoLevel(null);
    }

    @Test
    public void createGameLevel() {
        given(trainingDefinitionService.createGameLevel(trainingDefinition1.getId())).willReturn(gameLevel);
        trainingDefinitionFacade.createGameLevel(trainingDefinition1.getId());
        then(trainingDefinitionService).should().createGameLevel(trainingDefinition1.getId());
    }

    @Test
    public void createGameLevelWithNullDefinitionId() {
        thrown.expect(NullPointerException.class);
        trainingDefinitionFacade.createGameLevel(null);
    }

    @Test
    public void createAssessmentLevel() {
        given(trainingDefinitionService.createAssessmentLevel(trainingDefinition1.getId())).willReturn(assessmentLevel);
        trainingDefinitionFacade.createAssessmentLevel(trainingDefinition1.getId());
        then(trainingDefinitionService).should().createAssessmentLevel(trainingDefinition1.getId());
    }

    @Test
    public void createAssessmentLevelWithNullDefinitionId() {
        thrown.expect(NullPointerException.class);
        trainingDefinitionFacade.createAssessmentLevel(null);
    }

    @Test
    public void findLevelByIdGameLevel() {
        given(trainingDefinitionService.findLevelById(gameLevel.getId())).willReturn(gameLevel);
        AbstractLevelDTO g = trainingDefinitionFacade.findLevelById(gameLevel.getId());

        assertEquals(gameLevelMapper.mapToDTO(gameLevel), g);
        assertEquals(cz.muni.ics.kypo.training.api.enums.LevelType.GAME_LEVEL, g.getLevelType());
    }

    @Test
    public void findLevelByIdInfoLevel() {
        given(trainingDefinitionService.findLevelById(infoLevel.getId())).willReturn(infoLevel);
        AbstractLevelDTO i = trainingDefinitionFacade.findLevelById(infoLevel.getId());

        assertEquals(infoLevelMapper.mapToDTO(infoLevel), i);
        assertEquals(LevelType.INFO_LEVEL, i.getLevelType());

    }

    @Test
    public void findLevelByIdAssessmentLevel() {
        given(trainingDefinitionService.findLevelById(assessmentLevel.getId())).willReturn(assessmentLevel);
        AbstractLevelDTO a = trainingDefinitionFacade.findLevelById(assessmentLevel.getId());

        assertEquals(assessmentLevelMapper.mapToDTO(assessmentLevel), a);
        assertEquals(cz.muni.ics.kypo.training.api.enums.LevelType.ASSESSMENT_LEVEL, a.getLevelType());
    }

    @Test
    public void switchState() {
        trainingDefinitionFacade.switchState(trainingDefinition2.getId(), cz.muni.ics.kypo.training.api.enums.TDState.UNRELEASED);
        then(trainingDefinitionService).should().switchState(trainingDefinition2.getId(), cz.muni.ics.kypo.training.api.enums.TDState.UNRELEASED);
    }

    @Test
    public void getAuthors() {
        pagination = new PageResultResource.Pagination(0,2,5,2,1);
        given(trainingDefinitionService.findById(trainingDefinition1.getId())).willReturn(trainingDefinition1);
        given(userService.getUsersRefDTOByGivenUserIds(trainingDefinition1.getAuthors().stream().map(UserRef::getUserRefId).collect(Collectors.toSet()), pageable, null, null))
                .willReturn(new PageResultResource<>(List.of(authorDTO1, authorDTO2), pagination));
        PageResultResource<UserRefDTO> authors = trainingDefinitionFacade.getAuthors(trainingDefinition1.getId(), pageable, null, null);
        Assert.assertEquals(authors.getPagination(), pagination);
        Assert.assertTrue(authors.getContent().containsAll(Set.of(authorDTO1, authorDTO2)));
    }

    @Test(expected = EntityNotFoundException.class)
    public void getAuthorsTrainingDefinitionNotFound() {
        willThrow(new EntityNotFoundException()).given(trainingDefinitionService).findById(trainingDefinition1.getId());
        trainingDefinitionFacade.getAuthors(trainingDefinition1.getId(), pageable, null, null);
    }

    @Test
    public void getAuthorsNotInGivenTrainingDefinition() {
        pagination = new PageResultResource.Pagination(0,1,1,1,1);
        given(trainingDefinitionService.findById(trainingDefinition1.getId())).willReturn(trainingDefinition1);
        given(userService.getUsersByGivenRoleAndNotWithGivenIds(RoleType.ROLE_TRAINING_DESIGNER, Set.of(author1.getUserRefId(), author2.getUserRefId()), pageable, null, null)).willReturn(
                new PageResultResource<>(List.of(authorDTO3), pagination));
        PageResultResource<UserRefDTO> organizersNotInTrainingInstance = trainingDefinitionFacade.getDesignersNotInGivenTrainingDefinition(trainingDefinition1.getId(), pageable, null, null);
        assertEquals(pagination.toString(), organizersNotInTrainingInstance.getPagination().toString());
        assertTrue(organizersNotInTrainingInstance.getContent().contains(authorDTO3));
    }

    @Test(expected = EntityNotFoundException.class)
    public void getAuthorsNotInGivenTrainingDefinitionTrainingDefinitionNotFound() {
        willThrow(new EntityNotFoundException()).given(trainingDefinitionService).findById(trainingDefinition1.getId());
        trainingDefinitionFacade.getDesignersNotInGivenTrainingDefinition(trainingDefinition1.getId(), pageable, null, null);
    }

    @Test
    public void getAuthorsNotInGivenTrainingInstanceUserServiceError() {
        given(trainingDefinitionService.findById(trainingDefinition1.getId())).willReturn(trainingDefinition1);
        willThrow(new InternalServerErrorException("Error when calling User And Group endpoint.")).given(userService)
                .getUsersByGivenRoleAndNotWithGivenIds(RoleType.ROLE_TRAINING_DESIGNER,new HashSet<>(Set.of(author1.getUserRefId(), author2.getUserRefId())), pageable, null, null);
        thrown.expect(InternalServerErrorException.class);
        thrown.expectMessage("Error when calling User And Group endpoint.");
        trainingDefinitionFacade.getDesignersNotInGivenTrainingDefinition(trainingDefinition1.getId(), pageable, null, null);
    }


    @Test
    public void editAuthors() {
        PageResultResource.Pagination pagination = new PageResultResource.Pagination(0,1,999,1,1);
        given(trainingDefinitionService.findById(trainingDefinition1.getId())).willReturn(trainingDefinition1);
        given(securityService.getUserRefIdFromUserAndGroup()).willReturn(author1.getUserRefId());
        given(userService.getUsersRefDTOByGivenUserIds(Set.of(author3.getUserRefId()), PageRequest.of(0,999), null, null)).willReturn(new PageResultResource<>(List.of(authorDTO3), pagination));
        given(userService.getUserByUserRefId(author3.getUserRefId())).willReturn(author3);
        trainingDefinitionFacade.editAuthors(trainingDefinition1.getId(), new HashSet<>(Set.of(author3.getUserRefId())), new HashSet<>(Set.of(author2.getUserRefId())));
        Assert.assertEquals(2, trainingDefinition1.getAuthors().size());
        Assert.assertTrue(trainingDefinition1.getAuthors().containsAll(Set.of(author1, author3)));
    }

    @Test
    public void editAuthorsRemoveLoggedInAuthor() {
        PageResultResource.Pagination pagination = new PageResultResource.Pagination(0,1,999,1,1);
        given(trainingDefinitionService.findById(trainingDefinition1.getId())).willReturn(trainingDefinition1);
        given(securityService.getUserRefIdFromUserAndGroup()).willReturn(author1.getUserRefId());
        given(userService.getUsersRefDTOByGivenUserIds(Set.of(author3.getUserRefId()), PageRequest.of(0,999), null, null)).willReturn(new PageResultResource<>(List.of(authorDTO3), pagination));
        given(userService.getUserByUserRefId(author3.getUserRefId())).willReturn(author3);
        trainingDefinitionFacade.editAuthors(trainingDefinition1.getId(), new HashSet<>(Set.of(author3.getUserRefId())), new HashSet<>(Set.of(author1.getUserRefId())));
        Assert.assertEquals(3, trainingDefinition1.getAuthors().size());
        Assert.assertTrue(trainingDefinition1.getAuthors().containsAll(Set.of(author1, author2, author3)));
    }

    @Test
    public void editAuthorsConcurrentlyRemoveAndAddOAuthorWhoIsNotInTrainingDefinition() {
        PageResultResource.Pagination pagination = new PageResultResource.Pagination(0,1,999,1,1);
        given(trainingDefinitionService.findById(trainingDefinition1.getId())).willReturn(trainingDefinition1);
        given(securityService.getUserRefIdFromUserAndGroup()).willReturn(author1.getUserRefId());
        given(userService.getUsersRefDTOByGivenUserIds(Set.of(author3.getUserRefId()), PageRequest.of(0,999), null, null)).willReturn(new PageResultResource<>(List.of(authorDTO3), pagination));
        given(userService.getUserByUserRefId(author3.getUserRefId())).willReturn(author3);
        trainingDefinitionFacade.editAuthors(trainingDefinition1.getId(), new HashSet<>(Set.of(author3.getUserRefId())), new HashSet<>(Set.of(author3.getUserRefId())));
        Assert.assertEquals(3, trainingDefinition1.getAuthors().size());
        Assert.assertTrue(trainingDefinition1.getAuthors().containsAll(Set.of(author1, author2, author3)));
    }

    @Test
    public void editAuthorsConcurrentlyRemoveAndAddAuthorWhoIsInTrainingDefinition() {
        PageResultResource.Pagination pagination = new PageResultResource.Pagination(0,1,999,1,1);
        given(trainingDefinitionService.findById(trainingDefinition1.getId())).willReturn(trainingDefinition1);
        given(securityService.getUserRefIdFromUserAndGroup()).willReturn(author1.getUserRefId());
        given(userService.getUsersRefDTOByGivenUserIds(Set.of(author2.getUserRefId()), PageRequest.of(0,999), null, null)).willReturn(new PageResultResource<>(List.of(authorDTO2), pagination));
        given(userService.getUserByUserRefId(author2.getUserRefId())).willReturn(author2);
        trainingDefinitionFacade.editAuthors(trainingDefinition1.getId(), new HashSet<>(Set.of(author2.getUserRefId())), new HashSet<>(Set.of(author2.getUserRefId())));
        Assert.assertEquals(2, trainingDefinition1.getAuthors().size());
        Assert.assertTrue(trainingDefinition1.getAuthors().containsAll(Set.of(author1, author2)));
    }

    @Test
    public void editAuthorsWithEmptySetOfRemovalAndAdditionSets() {
        PageResultResource.Pagination pagination = new PageResultResource.Pagination(0,1,999,1,1);
        given(trainingDefinitionService.findById(trainingDefinition1.getId())).willReturn(trainingDefinition1);
        given(securityService.getUserRefIdFromUserAndGroup()).willReturn(author1.getUserRefId());
        given(userService.getUsersRefDTOByGivenUserIds(Set.of(author2.getUserRefId()), PageRequest.of(0,999), null, null)).willReturn(new PageResultResource<>(List.of(authorDTO2), pagination));
        given(userService.getUserByUserRefId(author3.getUserRefId())).willReturn(author2);
        trainingDefinitionFacade.editAuthors(trainingDefinition1.getId(), new HashSet<>(), new HashSet<>());
        Assert.assertEquals(2, trainingDefinition1.getAuthors().size());
        Assert.assertTrue(trainingDefinition1.getAuthors().containsAll(Set.of(author1, author2)));
    }

    @Test
    public void editAuthorsRemove() {
        PageResultResource.Pagination pagination = new PageResultResource.Pagination(0,1,999,1,1);
        given(trainingDefinitionService.findById(trainingDefinition1.getId())).willReturn(trainingDefinition1);
        given(securityService.getUserRefIdFromUserAndGroup()).willReturn(author1.getUserRefId());
        trainingDefinitionFacade.editAuthors(trainingDefinition1.getId(), new HashSet<>(), new HashSet<>(Set.of(author1.getUserRefId(), author2.getUserRefId())));
        Assert.assertEquals(1, trainingDefinition1.getAuthors().size());
        Assert.assertTrue(trainingDefinition1.getAuthors().contains(author1));
    }

    @Test
    public void editAuthorsAdd() {
        trainingDefinition1.removeAuthorsByUserRefIds(Set.of(author2.getUserRefId()));
        PageResultResource.Pagination pagination = new PageResultResource.Pagination(0,1,999,1,1);
        given(trainingDefinitionService.findById(trainingDefinition1.getId())).willReturn(trainingDefinition1);
        given(securityService.getUserRefIdFromUserAndGroup()).willReturn(author1.getUserRefId());
        given(userService.getUsersRefDTOByGivenUserIds(Set.of(author3.getUserRefId(), author2.getUserRefId()), PageRequest.of(0,999), null, null)).willReturn(new PageResultResource<>(List.of(authorDTO3, authorDTO2), pagination));
        given(userService.getUserByUserRefId(author3.getUserRefId())).willReturn(author3);
        given(userService.getUserByUserRefId(author2.getUserRefId())).willReturn(author2);
        trainingDefinitionFacade.editAuthors(trainingDefinition1.getId(), new HashSet<>(Set.of(author2.getUserRefId(), author3.getUserRefId())), new HashSet<>());
        Assert.assertEquals(3, trainingDefinition1.getAuthors().size());
        Assert.assertTrue(trainingDefinition1.getAuthors().containsAll(Set.of(author1, author2, author3)));
    }

    @Test
    public void editAuthorsAddUserRefNotInDB() {
        trainingDefinition1.removeAuthorsByUserRefIds(Set.of(author2.getUserRefId()));
        PageResultResource.Pagination pagination = new PageResultResource.Pagination(0,1,999,1,1);
        given(trainingDefinitionService.findById(trainingDefinition1.getId())).willReturn(trainingDefinition1);
        given(securityService.getUserRefIdFromUserAndGroup()).willReturn(author1.getUserRefId());
        given(userService.getUsersRefDTOByGivenUserIds(Set.of(author3.getUserRefId(), author2.getUserRefId()), PageRequest.of(0,999), null, null)).willReturn(new PageResultResource<>(List.of(authorDTO3, authorDTO2), pagination));
        given(userService.getUserByUserRefId(author3.getUserRefId())).willReturn(author3);
        willThrow(EntityNotFoundException.class).given(userService).getUserByUserRefId(author2.getUserRefId());
        given(userService.createUserRef(any(UserRef.class))).willReturn(author2);
        trainingDefinitionFacade.editAuthors(trainingDefinition1.getId(), new HashSet<>(Set.of(author2.getUserRefId(), author3.getUserRefId())), new HashSet<>());
        Assert.assertEquals(3, trainingDefinition1.getAuthors().size());
        Assert.assertTrue(trainingDefinition1.getAuthors().containsAll(Set.of(author1, author2, author3)));
    }

    private UserRefDTO createUserRefDTO(Long userRefId, String fullName, String familyName, String givenName, String login, String iss, byte[] picture) {
        UserRefDTO userRefDTO = new UserRefDTO();
        userRefDTO.setUserRefId(userRefId);
        userRefDTO.setUserRefFullName(fullName);
        userRefDTO.setUserRefFamilyName(familyName);
        userRefDTO.setUserRefGivenName(givenName);
        userRefDTO.setUserRefLogin(login);
        userRefDTO.setIss(iss);
        userRefDTO.setPicture(picture);
        return userRefDTO;
    }

    private void deepEquals(TrainingDefinition expected, TrainingDefinitionDTO actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getTitle(), actual.getTitle());
    }

}
