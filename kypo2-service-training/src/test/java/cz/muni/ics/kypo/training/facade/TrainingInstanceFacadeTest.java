package cz.muni.ics.kypo.training.facade;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceFindAllResponseDTO;
import cz.muni.ics.kypo.training.api.responses.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceCreateDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceUpdateDTO;
import cz.muni.ics.kypo.training.api.enums.RoleType;
import cz.muni.ics.kypo.training.exceptions.ErrorCode;
import cz.muni.ics.kypo.training.exceptions.FacadeLayerException;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.mapping.mapstruct.*;
import cz.muni.ics.kypo.training.persistence.model.TrainingDefinition;
import cz.muni.ics.kypo.training.persistence.model.TrainingInstance;
import cz.muni.ics.kypo.training.persistence.model.UserRef;
import cz.muni.ics.kypo.training.service.TrainingDefinitionService;
import cz.muni.ics.kypo.training.service.TrainingInstanceService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TrainingInstanceMapperImpl.class, TrainingRunMapperImpl.class,
        TrainingDefinitionMapper.class, UserRefMapper.class, TrainingDefinitionMapperImpl.class,
        UserRefMapperImpl.class, BetaTestingGroupMapperImpl.class})
public class TrainingInstanceFacadeTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private TrainingInstanceFacade trainingInstanceFacade;

    @Autowired
    TrainingRunMapperImpl trainingRunMapper;

    @Autowired
    TrainingInstanceMapper trainingInstanceMapper;

    @Mock
    private TrainingInstanceService trainingInstanceService;

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

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        trainingInstanceFacade = new TrainingInstanceFacadeImpl(trainingInstanceService, trainingDefinitionService, trainingInstanceMapper, trainingRunMapper, userService, securityService);

        pageable = PageRequest.of(0,5);

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

        trainingInstance1 = new TrainingInstance();
        trainingInstance1.setId(1L);
        trainingInstance1.setTitle("test");
        trainingInstance1.setPoolId(1L);
        trainingInstance1.setPoolSize(2);
        trainingInstance1.setOrganizers(new HashSet<>(Set.of(organizer1, organizer2)));

        trainingInstance2 = new TrainingInstance();
        trainingInstance2.setId(2L);
        trainingInstance2.setTitle("test");

        trainingInstanceCreate = new TrainingInstanceCreateDTO();
        trainingInstanceCreate.setTitle("test");
        trainingInstanceCreate.setTrainingDefinitionId(1L);

        trainingInstanceUpdate = new TrainingInstanceUpdateDTO();
        trainingInstanceUpdate.setId(1L);
        trainingInstanceUpdate.setTitle("title");
        trainingInstanceUpdate.setAccessToken("hello");
        trainingInstanceUpdate.setPoolSize(20);
        trainingInstanceUpdate.setEndTime(LocalDateTime.now());
        trainingInstanceUpdate.setStartTime(LocalDateTime.now());
        trainingInstanceUpdate.setTrainingDefinitionId(1L);
    }

    @Test
    public void findTrainingInstanceById() {
        given(trainingInstanceService.findByIdIncludingDefinition(trainingInstance1.getId())).willReturn(trainingInstance1);
        given(trainingInstanceService.findIdsOfAllOccupiedSandboxesByTrainingInstance(trainingInstance1.getId())).willReturn(List.of(1L, 2L));
        trainingInstanceFacade.findById(trainingInstance1.getId());
        then(trainingInstanceService).should().findByIdIncludingDefinition(trainingInstance1.getId());
    }

    @Test
    public void findNonexistentTrainingInstanceById() {
        willThrow(ServiceLayerException.class).given(trainingInstanceService).findByIdIncludingDefinition(1L);
        thrown.expect(FacadeLayerException.class);
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
    public void createTrainingInstanceWithNull() {
        thrown.expect(NullPointerException.class);
        trainingInstanceFacade.create(null);
    }

    @Test
    public void updateTrainingInstance() {
        given(userService.getUsersRefDTOByGivenUserIds(anySet(), any(Pageable.class), anyString(), anyString())).willReturn(new PageResultResource<>(new ArrayList<>()));
        trainingInstanceFacade.update(trainingInstanceUpdate);
        then(trainingInstanceService).should().update(any(TrainingInstance.class));
    }

    @Test
    public void updateTrainingInstanceWithNull() {
        thrown.expect(NullPointerException.class);
        trainingInstanceFacade.update(null);
    }

    @Test
    public void updateTrainingInstanceWithFacadeLayerException() {
        thrown.expect(FacadeLayerException.class);
        given(userService.getUsersRefDTOByGivenUserIds(anySet(), any(Pageable.class), anyString(), anyString())).willReturn(new PageResultResource<>(new ArrayList<>()));
        willThrow(ServiceLayerException.class).given(trainingInstanceService).update(any(TrainingInstance.class));
        trainingInstanceFacade.update(trainingInstanceUpdate);
    }

    @Test
    public void deleteTrainingInstance() {
        given(trainingInstanceService.findById(trainingInstance1.getId())).willReturn(trainingInstance1);
        trainingInstanceFacade.delete(trainingInstance1.getId());
        then(trainingInstanceService).should().delete(trainingInstance1);
    }

    @Test
    public void deleteTrainingInstanceWithNull() {
        thrown.expect(NullPointerException.class);
        trainingInstanceFacade.delete(null);
    }

    @Test
    public void deleteTrainingInstanceWithFacadeLayerException() {
        thrown.expect(FacadeLayerException.class);
        willThrow(ServiceLayerException.class).given(trainingInstanceService).findById(trainingInstance1.getId());
        trainingInstanceFacade.delete(trainingInstance1.getId());
    }

    @Test
    public void allocateSandboxes() {
        given(trainingInstanceService.findById(anyLong())).willReturn(trainingInstance1);
        trainingInstanceFacade.allocateSandboxes(trainingInstance1.getId(), null);
        then(trainingInstanceService).should().allocateSandboxes(trainingInstance1, null);
    }

    @Test
    public void allocateSandboxesWithServiceException() {
        given(trainingInstanceService.findById(anyLong())).willReturn(trainingInstance1);
        willThrow(ServiceLayerException.class).given(trainingInstanceService).allocateSandboxes(trainingInstance1, null);
        thrown.expect(FacadeLayerException.class);
        trainingInstanceFacade.allocateSandboxes(trainingInstance1.getId(), null);
    }

    @Test
    public void deleteSandboxes() {
        Set<Long> ids = new HashSet<>();
        ids.add(1L);
        ids.add(2L);
        given(trainingInstanceService.findById(anyLong())).willReturn(trainingInstance1);
        trainingInstanceFacade.deleteSandboxes(trainingInstance1.getId(), ids);
        then(trainingInstanceService).should().deleteSandbox(trainingInstance1.getId(), 1L);
        then(trainingInstanceService).should().deleteSandbox(trainingInstance1.getId(), 2L);
    }

    @Test
    public void deleteSandboxesWithServiceException() {
        Set<Long> ids = new HashSet<>();
        ids.add(1L);
        given(trainingInstanceService.findById(anyLong())).willReturn(trainingInstance1);
        willThrow(ServiceLayerException.class).given(trainingInstanceService).deleteSandbox(trainingInstance1.getId(), 1L);
        thrown.expect(FacadeLayerException.class);
        trainingInstanceFacade.deleteSandboxes(trainingInstance1.getId(), ids);
    }

    @Test
    public void getOrganizersOfTrainingInstance() {
        pagination = new PageResultResource.Pagination(0,2,5,2,1);
        given(trainingInstanceService.findById(trainingInstance1.getId())).willReturn(trainingInstance1);
        given(userService.getUsersRefDTOByGivenUserIds(Set.of(organizer1.getUserRefId(), organizer2.getUserRefId()), pageable, null, null)).willReturn(
                new PageResultResource<>(List.of(organizerDTO1, organizerDTO2), pagination));
        PageResultResource<UserRefDTO> organizersOfTrainingInstance = trainingInstanceFacade.getOrganizersOfTrainingInstance(trainingInstance1.getId(), pageable, null, null);
        assertEquals(pagination.toString(), organizersOfTrainingInstance.getPagination().toString());
        assertTrue(organizersOfTrainingInstance.getContent().containsAll(Set.of(organizerDTO1, organizerDTO2)));
    }

    @Test
    public void getOrganizersOfTrainingInstanceTrainingInstanceNotFound() {
        willThrow(new ServiceLayerException("Training instance not found.", ErrorCode.RESOURCE_NOT_FOUND)).given(trainingInstanceService).findById(trainingInstance1.getId());
        thrown.expect(FacadeLayerException.class);
        thrown.expectMessage("Training instance not found.");
        trainingInstanceFacade.getOrganizersOfTrainingInstance(trainingInstance1.getId(), pageable, null, null);
    }

    @Test
    public void getOrganizersOfTrainingInstanceTrainingInstanceUserServiceError() {
        given(trainingInstanceService.findById(trainingInstance1.getId())).willReturn(trainingInstance1);
        willThrow(new ServiceLayerException("Error when calling User And Group endpoint.", ErrorCode.UNEXPECTED_ERROR)).given(userService)
                .getUsersRefDTOByGivenUserIds(new HashSet<>(Set.of(organizer1.getUserRefId(), organizer2.getUserRefId())), pageable, null, null);
        thrown.expect(FacadeLayerException.class);
        thrown.expectMessage("Error when calling User And Group endpoint.");
        trainingInstanceFacade.getOrganizersOfTrainingInstance(trainingInstance1.getId(), pageable, null, null);
    }

    @Test
    public void getOrganizersNotInGivenTrainingInstance() {
        pagination = new PageResultResource.Pagination(0,1,1,1,1);
        given(trainingInstanceService.findById(trainingInstance1.getId())).willReturn(trainingInstance1);
        given(userService.getUsersByGivenRoleAndNotWithGivenIds(RoleType.ROLE_TRAINING_ORGANIZER, Set.of(organizer1.getUserRefId(), organizer2.getUserRefId()), pageable, null, null)).willReturn(
                new PageResultResource<>(List.of(organizerDTO3), pagination));
        PageResultResource<UserRefDTO> organizersNotInTrainingInstance = trainingInstanceFacade.getOrganizersNotInGivenTrainingInstance(trainingInstance1.getId(), pageable, null, null);
        assertEquals(pagination.toString(), organizersNotInTrainingInstance.getPagination().toString());
        assertTrue(organizersNotInTrainingInstance.getContent().contains(organizerDTO3));
    }

    @Test
    public void getOrganizersNotInGivenTrainingInstanceTrainingInstanceNotFound() {
        willThrow(new ServiceLayerException("Training instance not found.", ErrorCode.RESOURCE_NOT_FOUND)).given(trainingInstanceService).findById(trainingInstance1.getId());
        thrown.expect(FacadeLayerException.class);
        thrown.expectMessage("Training instance not found.");
        trainingInstanceFacade.getOrganizersNotInGivenTrainingInstance(trainingInstance1.getId(), pageable, null, null);
    }

    @Test
    public void getOrganizersNotInGivenTrainingInstanceUserServiceError() {
        given(trainingInstanceService.findById(trainingInstance1.getId())).willReturn(trainingInstance1);
        willThrow(new ServiceLayerException("Error when calling User And Group endpoint.", ErrorCode.UNEXPECTED_ERROR)).given(userService)
                .getUsersByGivenRoleAndNotWithGivenIds(RoleType.ROLE_TRAINING_ORGANIZER,new HashSet<>(Set.of(organizer1.getUserRefId(), organizer2.getUserRefId())), pageable, null, null);
        thrown.expect(FacadeLayerException.class);
        thrown.expectMessage("Error when calling User And Group endpoint.");
        trainingInstanceFacade.getOrganizersNotInGivenTrainingInstance(trainingInstance1.getId(), pageable, null, null);
    }

    @Test
    public void editOrganizers() {
        PageResultResource.Pagination pagination = new PageResultResource.Pagination(0,1,999,1,1);
        given(trainingInstanceService.findById(trainingInstance1.getId())).willReturn(trainingInstance1);
        given(securityService.getUserRefIdFromUserAndGroup()).willReturn(organizer1.getUserRefId());
        given(userService.getUsersRefDTOByGivenUserIds(Set.of(organizer3.getUserRefId()), PageRequest.of(0,999), null, null)).willReturn(new PageResultResource<>(List.of(organizerDTO3), pagination));
        given(userService.getUserByUserRefId(organizer3.getUserRefId())).willReturn(organizer3);
        trainingInstanceFacade.editOrganizers(trainingInstance1.getId(), new HashSet<>(Set.of(organizer3.getUserRefId())), new HashSet<>(Set.of(organizer2.getUserRefId())));
        Assert.assertEquals(2, trainingInstance1.getOrganizers().size());
        Assert.assertTrue(trainingInstance1.getOrganizers().containsAll(Set.of(organizer1, organizer3)));
    }

    @Test
    public void editOrganizersRemoveLoggedInOrganizer() {
        PageResultResource.Pagination pagination = new PageResultResource.Pagination(0,1,999,1,1);
        given(trainingInstanceService.findById(trainingInstance1.getId())).willReturn(trainingInstance1);
        given(securityService.getUserRefIdFromUserAndGroup()).willReturn(organizer1.getUserRefId());
        given(userService.getUsersRefDTOByGivenUserIds(Set.of(organizer3.getUserRefId()), PageRequest.of(0,999), null, null)).willReturn(new PageResultResource<>(List.of(organizerDTO3), pagination));
        given(userService.getUserByUserRefId(organizer3.getUserRefId())).willReturn(organizer3);
        trainingInstanceFacade.editOrganizers(trainingInstance1.getId(), new HashSet<>(Set.of(organizer3.getUserRefId())), new HashSet<>(Set.of(organizer1.getUserRefId())));
        Assert.assertEquals(3, trainingInstance1.getOrganizers().size());
        Assert.assertTrue(trainingInstance1.getOrganizers().containsAll(Set.of(organizer1, organizer2, organizer3)));
    }

    @Test
    public void editOrganizersConcurrentlyRemoveAndAddOrganizerWhoIsNotInTrainingInstance() {
        PageResultResource.Pagination pagination = new PageResultResource.Pagination(0,1,999,1,1);
        given(trainingInstanceService.findById(trainingInstance1.getId())).willReturn(trainingInstance1);
        given(securityService.getUserRefIdFromUserAndGroup()).willReturn(organizer1.getUserRefId());
        given(userService.getUsersRefDTOByGivenUserIds(Set.of(organizer3.getUserRefId()), PageRequest.of(0,999), null, null)).willReturn(new PageResultResource<>(List.of(organizerDTO3), pagination));
        given(userService.getUserByUserRefId(organizer3.getUserRefId())).willReturn(organizer3);
        trainingInstanceFacade.editOrganizers(trainingInstance1.getId(), new HashSet<>(Set.of(organizer3.getUserRefId())), new HashSet<>(Set.of(organizer3.getUserRefId())));
        Assert.assertEquals(3, trainingInstance1.getOrganizers().size());
        Assert.assertTrue(trainingInstance1.getOrganizers().containsAll(Set.of(organizer1, organizer2, organizer3)));
    }

    @Test
    public void editOrganizersConcurrentlyRemoveAndAddOrganizerWhoIsInTrainingInstance() {
        PageResultResource.Pagination pagination = new PageResultResource.Pagination(0,1,999,1,1);
        given(trainingInstanceService.findById(trainingInstance1.getId())).willReturn(trainingInstance1);
        given(securityService.getUserRefIdFromUserAndGroup()).willReturn(organizer1.getUserRefId());
        given(userService.getUsersRefDTOByGivenUserIds(Set.of(organizer2.getUserRefId()), PageRequest.of(0,999), null, null)).willReturn(new PageResultResource<>(List.of(organizerDTO2), pagination));
        given(userService.getUserByUserRefId(organizer2.getUserRefId())).willReturn(organizer2);
        trainingInstanceFacade.editOrganizers(trainingInstance1.getId(), new HashSet<>(Set.of(organizer2.getUserRefId())), new HashSet<>(Set.of(organizer2.getUserRefId())));
        Assert.assertEquals(2, trainingInstance1.getOrganizers().size());
        Assert.assertTrue(trainingInstance1.getOrganizers().containsAll(Set.of(organizer1, organizer2)));
    }

    @Test
    public void editOrganizersWithEmptySetOfRemovalAndAdditionSets() {
        PageResultResource.Pagination pagination = new PageResultResource.Pagination(0,1,999,1,1);
        given(trainingInstanceService.findById(trainingInstance1.getId())).willReturn(trainingInstance1);
        given(securityService.getUserRefIdFromUserAndGroup()).willReturn(organizer1.getUserRefId());
        given(userService.getUsersRefDTOByGivenUserIds(Set.of(organizer3.getUserRefId()), PageRequest.of(0,999), null, null)).willReturn(new PageResultResource<>(List.of(organizerDTO3), pagination));
        given(userService.getUserByUserRefId(organizer3.getUserRefId())).willReturn(organizer3);
        trainingInstanceFacade.editOrganizers(trainingInstance1.getId(), new HashSet<>(), new HashSet<>());
        Assert.assertEquals(2, trainingInstance1.getOrganizers().size());
        Assert.assertTrue(trainingInstance1.getOrganizers().containsAll(Set.of(organizer1, organizer2)));
    }

    @Test
    public void editOrganizersRemove() {
        PageResultResource.Pagination pagination = new PageResultResource.Pagination(0,1,999,1,1);
        given(trainingInstanceService.findById(trainingInstance1.getId())).willReturn(trainingInstance1);
        given(securityService.getUserRefIdFromUserAndGroup()).willReturn(organizer1.getUserRefId());
        trainingInstanceFacade.editOrganizers(trainingInstance1.getId(), new HashSet<>(), new HashSet<>(Set.of(organizer1.getUserRefId(), organizer2.getUserRefId())));
        Assert.assertEquals(1, trainingInstance1.getOrganizers().size());
        Assert.assertTrue(trainingInstance1.getOrganizers().contains(organizer1));
    }

    @Test
    public void editOrganizersAdd() {
        trainingInstance1.removeOrganizersByUserRefIds(Set.of(organizer2.getUserRefId()));
        PageResultResource.Pagination pagination = new PageResultResource.Pagination(0,1,999,1,1);
        given(trainingInstanceService.findById(trainingInstance1.getId())).willReturn(trainingInstance1);
        given(securityService.getUserRefIdFromUserAndGroup()).willReturn(organizer1.getUserRefId());
        given(userService.getUsersRefDTOByGivenUserIds(Set.of(organizer3.getUserRefId(), organizer2.getUserRefId()), PageRequest.of(0,999), null, null)).willReturn(new PageResultResource<>(List.of(organizerDTO3, organizerDTO2), pagination));
        given(userService.getUserByUserRefId(organizer3.getUserRefId())).willReturn(organizer3);
        given(userService.getUserByUserRefId(organizer2.getUserRefId())).willReturn(organizer2);
        trainingInstanceFacade.editOrganizers(trainingInstance1.getId(), new HashSet<>(Set.of(organizer2.getUserRefId(), organizer3.getUserRefId())), new HashSet<>());
        Assert.assertEquals(3, trainingInstance1.getOrganizers().size());
        Assert.assertTrue(trainingInstance1.getOrganizers().containsAll(Set.of(organizer1, organizer2, organizer3)));
    }

    @Test
    public void editOrganizersAddUserRefNotInDB() {
        trainingInstance1.removeOrganizersByUserRefIds(Set.of(organizer2.getUserRefId()));
        PageResultResource.Pagination pagination = new PageResultResource.Pagination(0,1,999,1,1);
        given(trainingInstanceService.findById(trainingInstance1.getId())).willReturn(trainingInstance1);
        given(securityService.getUserRefIdFromUserAndGroup()).willReturn(organizer1.getUserRefId());
        given(userService.getUsersRefDTOByGivenUserIds(Set.of(organizer3.getUserRefId(), organizer2.getUserRefId()), PageRequest.of(0,999), null, null)).willReturn(new PageResultResource<>(List.of(organizerDTO3, organizerDTO2), pagination));
        given(userService.getUserByUserRefId(organizer3.getUserRefId())).willReturn(organizer3);
        willThrow(ServiceLayerException.class).given(userService).getUserByUserRefId(organizer2.getUserRefId());
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
