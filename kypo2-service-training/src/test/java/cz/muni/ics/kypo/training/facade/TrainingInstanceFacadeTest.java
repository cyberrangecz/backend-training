package cz.muni.ics.kypo.training.facade;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import cz.muni.csirt.kypo.elasticsearch.service.TrainingEventsService;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.*;
import cz.muni.ics.kypo.training.api.enums.RoleType;
import cz.muni.ics.kypo.training.api.responses.LockedPoolInfo;
import cz.muni.ics.kypo.training.api.responses.PageResultResource;
import cz.muni.ics.kypo.training.enums.RoleTypeSecurity;
import cz.muni.ics.kypo.training.exceptions.EntityConflictException;
import cz.muni.ics.kypo.training.exceptions.EntityNotFoundException;
import cz.muni.ics.kypo.training.mapping.mapstruct.*;
import cz.muni.ics.kypo.training.persistence.model.TrainingDefinition;
import cz.muni.ics.kypo.training.persistence.model.TrainingInstance;
import cz.muni.ics.kypo.training.persistence.model.UserRef;
import cz.muni.ics.kypo.training.persistence.util.TestDataFactory;
import cz.muni.ics.kypo.training.service.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TestDataFactory.class})
@SpringBootTest(classes = {TrainingInstanceMapperImpl.class, TrainingRunMapperImpl.class,
        TrainingDefinitionMapper.class, UserRefMapper.class, TrainingDefinitionMapperImpl.class,
        UserRefMapperImpl.class, BetaTestingGroupMapperImpl.class, AttachmentMapperImpl.class})
public class TrainingInstanceFacadeTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private TrainingInstanceFacade trainingInstanceFacade;

    @Autowired
    TestDataFactory testDataFactory;

    @Autowired
    TrainingRunMapperImpl trainingRunMapper;

    @Autowired
    TrainingInstanceMapper trainingInstanceMapper;

    @Mock
    private TrainingInstanceService trainingInstanceService;
    @Mock
    private TrainingRunService trainingRunService;
    @Mock
    private TrainingEventsService trainingEventsService;
    @Mock
    private TrainingDefinitionService trainingDefinitionService;
    @Mock
    private SecurityService securityService;
    @Mock
    private UserService userService;

    private TrainingInstance trainingInstance1, trainingInstance2;
    private TrainingInstanceCreateDTO trainingInstanceCreate;
    private TrainingInstanceUpdateDTO trainingInstanceUpdate;
    private UserRef organizer1, organizer2, organizer3;
    private UserRefDTO organizerDTO1, organizerDTO2, organizerDTO3;
    private Pageable pageable;
    private PageResultResource.Pagination pagination;
    private LockedPoolInfo lockedPoolInfo;
    private TrainingInstanceAssignPoolIdDTO trainingInstanceAssignPoolIdDTO;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        trainingInstanceFacade = new TrainingInstanceFacade(trainingInstanceService, trainingDefinitionService, trainingRunService, trainingEventsService,
                trainingInstanceMapper, trainingRunMapper, userService, securityService);

        pageable = PageRequest.of(0, 5);

        organizer1 = new UserRef();
        organizer1.setId(1L);
        organizer1.setUserRefId(10L);
        organizer2 = new UserRef();
        organizer2.setId(2L);
        organizer2.setUserRefId(20L);
        organizer3 = new UserRef();
        organizer3.setId(3L);
        organizer3.setUserRefId(30L);

        organizerDTO1 = createUserRefDTO(10L, "Bc. Dominik Meškal", "Meškal", "Dominik", "445533@muni.cz", "https://oidc.muni.cz/oidc", null);
        organizerDTO2 = createUserRefDTO(20L, "Bc. Boris Makal", "Makal", "Boris", "772211@muni.cz", "https://oidc.muni.cz/oidc", null);
        organizerDTO3 = createUserRefDTO(30L, "Ing. Pavel Flákal", "Flákal", "Pavel", "221133@muni.cz", "https://oidc.muni.cz/oidc", null);

        trainingInstance1 = testDataFactory.getConcludedInstance();
        trainingInstance1.setId(1L);
        trainingInstance1.setOrganizers(new HashSet<>(Set.of(organizer1, organizer2)));

        trainingInstance2 = testDataFactory.getFutureInstance();
        trainingInstance2.setId(2L);

        trainingInstanceCreate = testDataFactory.getTrainingInstanceCreateDTO();
        trainingInstanceCreate.setTitle("test");
        trainingInstanceCreate.setTrainingDefinitionId(1L);

        trainingInstanceUpdate = testDataFactory.getTrainingInstanceUpdateDTO();
        trainingInstanceUpdate.setId(1L);
        trainingInstanceUpdate.setTrainingDefinitionId(1L);

        lockedPoolInfo = new LockedPoolInfo();
        lockedPoolInfo.setId(1L);
        lockedPoolInfo.setPool(1L);

        trainingInstanceAssignPoolIdDTO = new TrainingInstanceAssignPoolIdDTO();
        trainingInstanceAssignPoolIdDTO.setPoolId(1L);
    }

    @Test
    public void findTrainingInstanceById() {
        given(trainingInstanceService.findByIdIncludingDefinition(trainingInstance1.getId())).willReturn(trainingInstance1);
        trainingInstanceFacade.findById(trainingInstance1.getId());
        then(trainingInstanceService).should().findByIdIncludingDefinition(trainingInstance1.getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void findNonexistentTrainingInstanceById() {
        willThrow(EntityNotFoundException.class).given(trainingInstanceService).findByIdIncludingDefinition(1L);
        trainingInstanceFacade.findById(1L);
    }

    @Test
    public void findAllTrainingInstances() {
        List<TrainingInstance> expected = new ArrayList<>();
        expected.add(trainingInstance1);
        expected.add(trainingInstance2);

        Page p = new PageImpl<TrainingInstance>(expected);

        PathBuilder<TrainingInstance> tI = new PathBuilder<TrainingInstance>(TrainingInstance.class, "trainingInstance");
        Predicate predicate = tI.isNotNull();

        given(securityService.hasRole(RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR)).willReturn(true);
        given(trainingInstanceService.findAll(any(Predicate.class), any(Pageable.class))).willReturn(p);

        PageResultResource<TrainingInstanceFindAllResponseDTO> trainingInstanceDTO = trainingInstanceFacade.findAll(predicate, PageRequest.of(0, 2));
        deepEqualsTrainingInstanceFindAllView(trainingInstance1, trainingInstanceDTO.getContent().get(0));
        deepEqualsTrainingInstanceFindAllView(trainingInstance2, trainingInstanceDTO.getContent().get(1));

        then(trainingInstanceService).should().findAll(predicate, PageRequest.of(0, 2));
    }

    @Test
    public void createTrainingInstance() {
        given(trainingInstanceService.create(any(TrainingInstance.class))).willReturn(trainingInstance1);
        given(trainingDefinitionService.findById(any(Long.class))).willReturn(new TrainingDefinition());
        given(trainingInstanceService.findUserRefsByUserRefIds(any(Set.class))).willReturn(new HashSet());
        given(userService.getUsersRefDTOByGivenUserIds(anySet(), any(Pageable.class), anyString(), anyString())).willReturn(new PageResultResource<>(new ArrayList<>()));
        trainingInstanceFacade.create(trainingInstanceCreate);
        then(trainingInstanceService).should().create(any(TrainingInstance.class));
    }

    @Test
    public void updateTrainingInstance() {
        given(userService.getUsersRefDTOByGivenUserIds(anySet(), any(Pageable.class), anyString(), anyString())).willReturn(new PageResultResource<>(new ArrayList<>()));
        trainingInstanceFacade.update(trainingInstanceUpdate);
        then(trainingInstanceService).should().update(any(TrainingInstance.class));
    }

    @Test(expected = EntityConflictException.class)
    public void updateTrainingInstanceWithFacadeLayerException() {
        given(userService.getUsersRefDTOByGivenUserIds(anySet(), any(Pageable.class), anyString(), anyString())).willReturn(new PageResultResource<>(new ArrayList<>()));
        willThrow(EntityConflictException.class).given(trainingInstanceService).update(any(TrainingInstance.class));
        trainingInstanceFacade.update(trainingInstanceUpdate);
    }

    @Test
    public void deleteTrainingInstance() {
        trainingInstance1.setPoolId(null);
        given(trainingInstanceService.findById(trainingInstance1.getId())).willReturn(trainingInstance1);
        trainingInstanceFacade.delete(trainingInstance1.getId(), false);
        then(trainingInstanceService).should().delete(trainingInstance1);
    }

    @Test
    public void assignPoolToTrainingInstance() {
        trainingInstance1.setPoolId(null);
        given(trainingInstanceService.findById(anyLong())).willReturn(trainingInstance1);
        given(trainingInstanceService.lockPool(anyLong())).willReturn(lockedPoolInfo);

        trainingInstanceFacade.assignPoolToTrainingInstance(trainingInstance1.getId(), trainingInstanceAssignPoolIdDTO);
        then(trainingInstanceService).should().lockPool(trainingInstance1.getId());
    }

    @Test(expected = EntityConflictException.class)
    public void allocateSandboxesWithServiceException() {
        given(trainingInstanceService.findById(anyLong())).willReturn(trainingInstance1);
        willThrow(EntityConflictException.class).given(trainingInstanceService).lockPool(anyLong());
        trainingInstanceFacade.assignPoolToTrainingInstance(trainingInstance1.getId(), trainingInstanceAssignPoolIdDTO);
    }

    @Test(expected = EntityConflictException.class)
    public void unassignPoolWithConflictException() {
        trainingInstance1.setPoolId(null);
        given(trainingInstanceService.findById(anyLong())).willReturn(trainingInstance1);
        trainingInstanceFacade.unassignPoolInTrainingInstance(trainingInstance1.getId());
    }

    @Test
    public void getOrganizersOfTrainingInstance() {
        pagination = new PageResultResource.Pagination(0, 2, 5, 2, 1);
        given(trainingInstanceService.findById(trainingInstance1.getId())).willReturn(trainingInstance1);
        given(userService.getUsersRefDTOByGivenUserIds(Set.of(organizer1.getUserRefId(), organizer2.getUserRefId()), pageable, null, null)).willReturn(
                new PageResultResource<>(List.of(organizerDTO1, organizerDTO2), pagination));
        PageResultResource<UserRefDTO> organizersOfTrainingInstance = trainingInstanceFacade.getOrganizersOfTrainingInstance(trainingInstance1.getId(), pageable, null, null);
        assertEquals(pagination.toString(), organizersOfTrainingInstance.getPagination().toString());
        assertTrue(organizersOfTrainingInstance.getContent().containsAll(Set.of(organizerDTO1, organizerDTO2)));
    }

    @Test(expected = EntityNotFoundException.class)
    public void getOrganizersOfTrainingInstanceTrainingInstanceNotFound() {
        willThrow(new EntityNotFoundException()).given(trainingInstanceService).findById(trainingInstance1.getId());
        trainingInstanceFacade.getOrganizersOfTrainingInstance(trainingInstance1.getId(), pageable, null, null);
    }

    @Test(expected = EntityNotFoundException.class)
    public void getOrganizersOfTrainingInstanceTrainingInstanceUserServiceError() {
        given(trainingInstanceService.findById(trainingInstance1.getId())).willReturn(trainingInstance1);
        willThrow(new EntityNotFoundException()).given(userService)
                .getUsersRefDTOByGivenUserIds(new HashSet<>(Set.of(organizer1.getUserRefId(), organizer2.getUserRefId())), pageable, null, null);
        trainingInstanceFacade.getOrganizersOfTrainingInstance(trainingInstance1.getId(), pageable, null, null);
    }

    @Test
    public void getOrganizersNotInGivenTrainingInstance() {
        pagination = new PageResultResource.Pagination(0, 1, 1, 1, 1);
        given(trainingInstanceService.findById(trainingInstance1.getId())).willReturn(trainingInstance1);
        given(userService.getUsersByGivenRoleAndNotWithGivenIds(RoleType.ROLE_TRAINING_ORGANIZER, Set.of(organizer1.getUserRefId(), organizer2.getUserRefId()), pageable, null, null)).willReturn(
                new PageResultResource<>(List.of(organizerDTO3), pagination));
        PageResultResource<UserRefDTO> organizersNotInTrainingInstance = trainingInstanceFacade.getOrganizersNotInGivenTrainingInstance(trainingInstance1.getId(), pageable, null, null);
        assertEquals(pagination.toString(), organizersNotInTrainingInstance.getPagination().toString());
        assertTrue(organizersNotInTrainingInstance.getContent().contains(organizerDTO3));
    }

    @Test(expected = EntityNotFoundException.class)
    public void getOrganizersNotInGivenTrainingInstanceTrainingInstanceNotFound() {
        willThrow(new EntityNotFoundException()).given(trainingInstanceService).findById(trainingInstance1.getId());
        trainingInstanceFacade.getOrganizersNotInGivenTrainingInstance(trainingInstance1.getId(), pageable, null, null);
    }

    @Test(expected = EntityNotFoundException.class)
    public void getOrganizersNotInGivenTrainingInstanceUserServiceError() {
        given(trainingInstanceService.findById(trainingInstance1.getId())).willReturn(trainingInstance1);
        willThrow(new EntityNotFoundException()).given(userService)
                .getUsersByGivenRoleAndNotWithGivenIds(RoleType.ROLE_TRAINING_ORGANIZER, new HashSet<>(Set.of(organizer1.getUserRefId(), organizer2.getUserRefId())), pageable, null, null);
        trainingInstanceFacade.getOrganizersNotInGivenTrainingInstance(trainingInstance1.getId(), pageable, null, null);
    }

    @Test
    public void editOrganizers() {
        PageResultResource.Pagination pagination = new PageResultResource.Pagination(0, 1, 999, 1, 1);
        given(trainingInstanceService.findById(trainingInstance1.getId())).willReturn(trainingInstance1);
        given(securityService.getUserRefIdFromUserAndGroup()).willReturn(organizer1.getUserRefId());
        given(userService.getUsersRefDTOByGivenUserIds(Set.of(organizer3.getUserRefId()), PageRequest.of(0, 999), null, null)).willReturn(new PageResultResource<>(List.of(organizerDTO3), pagination));
        given(userService.getUserByUserRefId(organizer3.getUserRefId())).willReturn(organizer3);
        trainingInstanceFacade.editOrganizers(trainingInstance1.getId(), new HashSet<>(Set.of(organizer3.getUserRefId())), new HashSet<>(Set.of(organizer2.getUserRefId())));
        Assert.assertEquals(2, trainingInstance1.getOrganizers().size());
        Assert.assertTrue(trainingInstance1.getOrganizers().containsAll(Set.of(organizer1, organizer3)));
    }

    @Test
    public void editOrganizersRemoveLoggedInOrganizer() {
        PageResultResource.Pagination pagination = new PageResultResource.Pagination(0, 1, 999, 1, 1);
        given(trainingInstanceService.findById(trainingInstance1.getId())).willReturn(trainingInstance1);
        given(securityService.getUserRefIdFromUserAndGroup()).willReturn(organizer1.getUserRefId());
        given(userService.getUsersRefDTOByGivenUserIds(Set.of(organizer3.getUserRefId()), PageRequest.of(0, 999), null, null)).willReturn(new PageResultResource<>(List.of(organizerDTO3), pagination));
        given(userService.getUserByUserRefId(organizer3.getUserRefId())).willReturn(organizer3);
        trainingInstanceFacade.editOrganizers(trainingInstance1.getId(), new HashSet<>(Set.of(organizer3.getUserRefId())), new HashSet<>(Set.of(organizer1.getUserRefId())));
        Assert.assertEquals(3, trainingInstance1.getOrganizers().size());
        Assert.assertTrue(trainingInstance1.getOrganizers().containsAll(Set.of(organizer1, organizer2, organizer3)));
    }

    @Test
    public void editOrganizersConcurrentlyRemoveAndAddOrganizerWhoIsNotInTrainingInstance() {
        PageResultResource.Pagination pagination = new PageResultResource.Pagination(0, 1, 999, 1, 1);
        given(trainingInstanceService.findById(trainingInstance1.getId())).willReturn(trainingInstance1);
        given(securityService.getUserRefIdFromUserAndGroup()).willReturn(organizer1.getUserRefId());
        given(userService.getUsersRefDTOByGivenUserIds(Set.of(organizer3.getUserRefId()), PageRequest.of(0, 999), null, null)).willReturn(new PageResultResource<>(List.of(organizerDTO3), pagination));
        given(userService.getUserByUserRefId(organizer3.getUserRefId())).willReturn(organizer3);
        trainingInstanceFacade.editOrganizers(trainingInstance1.getId(), new HashSet<>(Set.of(organizer3.getUserRefId())), new HashSet<>(Set.of(organizer3.getUserRefId())));
        Assert.assertEquals(3, trainingInstance1.getOrganizers().size());
        Assert.assertTrue(trainingInstance1.getOrganizers().containsAll(Set.of(organizer1, organizer2, organizer3)));
    }

    @Test
    public void editOrganizersConcurrentlyRemoveAndAddOrganizerWhoIsInTrainingInstance() {
        PageResultResource.Pagination pagination = new PageResultResource.Pagination(0, 1, 999, 1, 1);
        given(trainingInstanceService.findById(trainingInstance1.getId())).willReturn(trainingInstance1);
        given(securityService.getUserRefIdFromUserAndGroup()).willReturn(organizer1.getUserRefId());
        given(userService.getUsersRefDTOByGivenUserIds(Set.of(organizer2.getUserRefId()), PageRequest.of(0, 999), null, null)).willReturn(new PageResultResource<>(List.of(organizerDTO2), pagination));
        given(userService.getUserByUserRefId(organizer2.getUserRefId())).willReturn(organizer2);
        trainingInstanceFacade.editOrganizers(trainingInstance1.getId(), new HashSet<>(Set.of(organizer2.getUserRefId())), new HashSet<>(Set.of(organizer2.getUserRefId())));
        Assert.assertEquals(2, trainingInstance1.getOrganizers().size());
        Assert.assertTrue(trainingInstance1.getOrganizers().containsAll(Set.of(organizer1, organizer2)));
    }

    @Test
    public void editOrganizersWithEmptySetOfRemovalAndAdditionSets() {
        PageResultResource.Pagination pagination = new PageResultResource.Pagination(0, 1, 999, 1, 1);
        given(trainingInstanceService.findById(trainingInstance1.getId())).willReturn(trainingInstance1);
        given(securityService.getUserRefIdFromUserAndGroup()).willReturn(organizer1.getUserRefId());
        given(userService.getUsersRefDTOByGivenUserIds(Set.of(organizer3.getUserRefId()), PageRequest.of(0, 999), null, null)).willReturn(new PageResultResource<>(List.of(organizerDTO3), pagination));
        given(userService.getUserByUserRefId(organizer3.getUserRefId())).willReturn(organizer3);
        trainingInstanceFacade.editOrganizers(trainingInstance1.getId(), new HashSet<>(), new HashSet<>());
        Assert.assertEquals(2, trainingInstance1.getOrganizers().size());
        Assert.assertTrue(trainingInstance1.getOrganizers().containsAll(Set.of(organizer1, organizer2)));
    }

    @Test
    public void editOrganizersRemove() {
        PageResultResource.Pagination pagination = new PageResultResource.Pagination(0, 1, 999, 1, 1);
        given(trainingInstanceService.findById(trainingInstance1.getId())).willReturn(trainingInstance1);
        given(securityService.getUserRefIdFromUserAndGroup()).willReturn(organizer1.getUserRefId());
        trainingInstanceFacade.editOrganizers(trainingInstance1.getId(), new HashSet<>(), new HashSet<>(Set.of(organizer1.getUserRefId(), organizer2.getUserRefId())));
        Assert.assertEquals(1, trainingInstance1.getOrganizers().size());
        Assert.assertTrue(trainingInstance1.getOrganizers().contains(organizer1));
    }

    @Test
    public void editOrganizersAdd() {
        trainingInstance1.removeOrganizersByUserRefIds(Set.of(organizer2.getUserRefId()));
        PageResultResource.Pagination pagination = new PageResultResource.Pagination(0, 1, 999, 1, 1);
        given(trainingInstanceService.findById(trainingInstance1.getId())).willReturn(trainingInstance1);
        given(securityService.getUserRefIdFromUserAndGroup()).willReturn(organizer1.getUserRefId());
        given(userService.getUsersRefDTOByGivenUserIds(Set.of(organizer3.getUserRefId(), organizer2.getUserRefId()), PageRequest.of(0, 999), null, null)).willReturn(new PageResultResource<>(List.of(organizerDTO3, organizerDTO2), pagination));
        given(userService.getUserByUserRefId(organizer3.getUserRefId())).willReturn(organizer3);
        given(userService.getUserByUserRefId(organizer2.getUserRefId())).willReturn(organizer2);
        trainingInstanceFacade.editOrganizers(trainingInstance1.getId(), new HashSet<>(Set.of(organizer2.getUserRefId(), organizer3.getUserRefId())), new HashSet<>());
        Assert.assertEquals(3, trainingInstance1.getOrganizers().size());
        Assert.assertTrue(trainingInstance1.getOrganizers().containsAll(Set.of(organizer1, organizer2, organizer3)));
    }

    @Test
    public void editOrganizersAddUserRefNotInDB() {
        trainingInstance1.removeOrganizersByUserRefIds(Set.of(organizer2.getUserRefId()));
        PageResultResource.Pagination pagination = new PageResultResource.Pagination(0, 1, 999, 1, 1);
        given(trainingInstanceService.findById(trainingInstance1.getId())).willReturn(trainingInstance1);
        given(securityService.getUserRefIdFromUserAndGroup()).willReturn(organizer1.getUserRefId());
        given(userService.getUsersRefDTOByGivenUserIds(Set.of(organizer3.getUserRefId(), organizer2.getUserRefId()), PageRequest.of(0, 999), null, null)).willReturn(new PageResultResource<>(List.of(organizerDTO3, organizerDTO2), pagination));
        given(userService.getUserByUserRefId(organizer3.getUserRefId())).willReturn(organizer3);
        willThrow(EntityNotFoundException.class).given(userService).getUserByUserRefId(organizer2.getUserRefId());
        given(userService.createUserRef(any(UserRef.class))).willReturn(organizer2);
        trainingInstanceFacade.editOrganizers(trainingInstance1.getId(), new HashSet<>(Set.of(organizer2.getUserRefId(), organizer3.getUserRefId())), new HashSet<>());
        Assert.assertEquals(3, trainingInstance1.getOrganizers().size());
        Assert.assertTrue(trainingInstance1.getOrganizers().containsAll(Set.of(organizer1, organizer2, organizer3)));
    }


    private void deepEquals(TrainingInstance expected, TrainingInstanceDTO actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getTitle(), actual.getTitle());
    }

    private void deepEqualsTrainingInstanceFindAllView(TrainingInstance expected, TrainingInstanceFindAllResponseDTO actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getTitle(), actual.getTitle());
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

}
