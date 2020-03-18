package cz.muni.ics.kypo.training.service;

import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.enums.RoleType;
import cz.muni.ics.kypo.training.api.responses.PageResultResource;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.repository.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@RunWith(SpringRunner.class)
public class UserServiceTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private UserService userService;

    @Mock
    private UserRefRepository userRefRepository;
    @Mock
    private RestTemplate restTemplate;

    private UserRef userRef1, userRef2, userRef3;
    private UserRefDTO userRefDTO1, userRefDTO2;
    private PageResultResource.Pagination pagination;
    private Pageable pageable;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        userService = new UserService(restTemplate, userRefRepository);
        ReflectionTestUtils.setField(userService, "userAndGroupURI", "https://localhost:8083/kypo2-rest-user-and-group/");

        userRef1 = new UserRef();
        userRef1.setId(1L);
        userRef1.setUserRefId(10L);
        userRef2 = new UserRef();
        userRef2.setId(2L);
        userRef2.setUserRefId(20L);
        userRef3 = new UserRef();
        userRef3.setId(3L);
        userRef3.setUserRefId(30L);

        userRefDTO1 = createUserRefDTO(10L, "Bc. Dominik Meškal", "Meškal", "Dominik", "445533@muni.cz", "https://oidc.muni.cz/oidc", null);
        userRefDTO2 = createUserRefDTO(20L, "Bc. Boris Makal", "Makal", "Boris", "772211@muni.cz", "https://oidc.muni.cz/oidc", null);

        pageable = PageRequest.of(0,5);
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

    @Test
    public void getUserByUserRefId() {
        given(userRefRepository.findUserByUserRefId(userRef1.getUserRefId())).willReturn(Optional.of(userRef1));

        UserRef foundUserRef = userService.getUserByUserRefId(userRef1.getUserRefId());
        Assert.assertEquals(userRef1, foundUserRef);
    }

    @Test
    public void getUserRefDTOByUserRefId() {
        given(restTemplate.getForObject(anyString(), eq(UserRefDTO.class))).
                willReturn(userRefDTO1);
        UserRefDTO foundUserRefDTO = userService.getUserRefDTOByUserRefId(userRef1.getUserRefId());
        Assert.assertEquals(userRefDTO1, foundUserRefDTO);
    }

    @Test
    public void getUsersRefDTOByGivenUserIds() {
        pagination = new PageResultResource.Pagination(0, 2, 5, 2, 1);
        given(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).
                willReturn(new ResponseEntity<PageResultResource<UserRefDTO>>(new PageResultResource<UserRefDTO>(List.of(userRefDTO1, userRefDTO2), pagination), HttpStatus.OK));
        PageResultResource<UserRefDTO> userRefDTOPageResultResource = userService.getUsersRefDTOByGivenUserIds(Set.of(userRef1.getUserRefId(), userRef2.getUserRefId()), pageable, null, null);
        Assert.assertTrue(userRefDTOPageResultResource.getContent().containsAll(List.of(userRefDTO1, userRefDTO2)));
        Assert.assertEquals(pagination, userRefDTOPageResultResource.getPagination());
    }

    @Test
    public void getUsersRefDTOByGivenUserIdsWithEmptyIds() {
        pagination = new PageResultResource.Pagination(0, 0, 5, 0, 0);
        PageResultResource<UserRefDTO> userRefDTOPageResultResource = userService.getUsersRefDTOByGivenUserIds(new HashSet<>(), pageable, null, null);
        Assert.assertTrue(userRefDTOPageResultResource.getContent().isEmpty());
        Assert.assertEquals(pagination.toString(), userRefDTOPageResultResource.getPagination().toString());
    }

    @Test
    public void getUsersByGivenRole() {
        pagination = new PageResultResource.Pagination(0, 2, 5, 2, 1);
        given(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).
                willReturn(new ResponseEntity<PageResultResource<UserRefDTO>>(new PageResultResource<UserRefDTO>(List.of(userRefDTO1, userRefDTO2), pagination), HttpStatus.OK));
        PageResultResource<UserRefDTO> userRefDTOPageResultResource = userService.getUsersByGivenRole(RoleType.ROLE_TRAINING_DESIGNER, pageable, null, null);
        Assert.assertTrue(userRefDTOPageResultResource.getContent().containsAll(List.of(userRefDTO1, userRefDTO2)));
    }

    @Test
    public void getUsersByGivenRoleAndNotWithGivenIds() {
        pagination = new PageResultResource.Pagination(0, 2, 5, 2, 1);
        given(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).
                willReturn(new ResponseEntity<PageResultResource<UserRefDTO>>(new PageResultResource<UserRefDTO>(List.of(userRefDTO1, userRefDTO2), pagination), HttpStatus.OK));
        PageResultResource<UserRefDTO> userRefDTOPageResultResource = userService.getUsersByGivenRoleAndNotWithGivenIds(RoleType.ROLE_TRAINING_ORGANIZER, Set.of(userRef1.getUserRefId(), userRef2.getUserRefId()), pageable, null, null);
        Assert.assertTrue(userRefDTOPageResultResource.getContent().containsAll(List.of(userRefDTO1, userRefDTO2)));
        Assert.assertEquals(pagination, userRefDTOPageResultResource.getPagination());
    }

    @Test
    public void createUserRef() {
        UserRef userRef = new UserRef();
        userRef.setUserRefId(userRef1.getUserRefId());
        given(userRefRepository.save(userRef)).willReturn(userRef1);
        userService.createUserRef(userRef);
        then(userRefRepository).should().save(userRef);
    }
}
