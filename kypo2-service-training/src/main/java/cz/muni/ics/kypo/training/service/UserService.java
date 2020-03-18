package cz.muni.ics.kypo.training.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import cz.muni.ics.kypo.training.annotations.transactions.TransactionalWO;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.enums.RoleType;
import cz.muni.ics.kypo.training.api.responses.PageResultResource;
import cz.muni.ics.kypo.training.exceptions.*;
import cz.muni.ics.kypo.training.persistence.model.UserRef;
import cz.muni.ics.kypo.training.persistence.repository.UserRefRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Set;

/**
 * The type User service.
 */
@Service
public class UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    @Value("${user-and-group-server.uri}")
    private String userAndGroupURI;
    private RestTemplate javaRestTemplate;
    private UserRefRepository userRefRepository;

    /**
     * Instantiates a new User service.
     *
     * @param javaRestTemplate  the rest template
     * @param userRefRepository the user ref repository
     */
    public UserService(@Qualifier("javaRestTemplate") RestTemplate javaRestTemplate, UserRefRepository userRefRepository) {
        this.javaRestTemplate = javaRestTemplate;
        this.userRefRepository = userRefRepository;
    }

    /**
     * Finds specific User reference by login
     *
     * @param userRefId of wanted User reference
     * @return {@link UserRef} with corresponding login
     * @throws EntityNotFoundException UserRef was not found
     */
    public UserRef getUserByUserRefId(Long userRefId) {
        return userRefRepository.findUserByUserRefId(userRefId).orElseThrow(
                () -> new EntityNotFoundException(new EntityErrorDetail(UserRef.class, "id", userRefId.getClass(), userRefId, "UserRef not found.")));
    }

    /**
     * Finds specific User reference by login
     *
     * @param id of wanted User reference
     * @return {@link UserRef} with corresponding login
     * @throws EntityNotFoundException UserRef was not found
     */
    public UserRefDTO getUserRefDTOByUserRefId(Long id) {
        try {
            return javaRestTemplate.getForObject(userAndGroupURI + "/users/" + id, UserRefDTO.class);
        } catch (CustomRestTemplateException ex) {
            throw new MicroserviceApiException("Error when calling UserAndGroup API to obtain info about user(ID: " + id + ")", ex.getApiSubError());
        }
    }

    /**
     * Gets users with given user ref ids.
     *
     * @param userRefIds the user ref ids
     * @param pageable   pageable parameter with information about pagination.
     * @param givenName  optional parameter used for filtration
     * @param familyName optional parameter used for filtration
     * @return the users with given user ref ids
     */
    public PageResultResource<UserRefDTO> getUsersRefDTOByGivenUserIds(Set<Long> userRefIds, Pageable pageable, String givenName, String familyName) {
        if (userRefIds.isEmpty()) {
            return new PageResultResource<>(Collections.emptyList(), new PageResultResource.Pagination(0, 0, pageable.getPageSize(), 0, 0));
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(userAndGroupURI + "/users/ids");
        if (givenName != null) {
            builder.queryParam("givenName", givenName);
        }
        if (familyName != null) {
            builder.queryParam("familyName", familyName);
        }
        builder.queryParam("ids", StringUtils.collectionToDelimitedString(userRefIds, ","));
        builder.queryParam("page", pageable.getPageNumber());
        builder.queryParam("size", pageable.getPageSize());
        URI uri = builder.build().encode().toUri();
        try {
            ResponseEntity<PageResultResource<UserRefDTO>> usersResponse = javaRestTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(httpHeaders),
                    new ParameterizedTypeReference<PageResultResource<UserRefDTO>>() {
                    });
            return usersResponse.getBody();
        } catch (CustomRestTemplateException ex) {
            throw new MicroserviceApiException("Error when calling UserAndGroup API to obtain users by IDs: " + userRefIds, ex.getApiSubError());
        }
    }

    /**
     * Finds all logins of users that have role of designer
     *
     * @param roleType   the role type
     * @param pageable   pageable parameter with information about pagination.
     * @param givenName  optional parameter used for filtration
     * @param familyName optional parameter used for filtration
     * @return list of users with given role
     */
    public PageResultResource<UserRefDTO> getUsersByGivenRole(RoleType roleType, Pageable pageable, String givenName, String familyName) {
        HttpHeaders httpHeaders = new HttpHeaders();
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(userAndGroupURI + "/roles/users");
        if (givenName != null) {
            builder.queryParam("givenName", givenName);
        }
        if (familyName != null) {
            builder.queryParam("familyName", familyName);
        }
        builder.queryParam("roleType", roleType.name());
        builder.queryParam("page", pageable.getPageNumber());
        builder.queryParam("size", pageable.getPageSize());
        URI uri = builder.build().encode().toUri();
        try {
            ResponseEntity<PageResultResource<UserRefDTO>> usersResponse = javaRestTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(httpHeaders),
                    new ParameterizedTypeReference<PageResultResource<UserRefDTO>>() {
                    });
            return usersResponse.getBody();
        } catch (CustomRestTemplateException ex) {
            throw new MicroserviceApiException("Error when calling UserAndGroup API to obtain users with role " + roleType.name(), ex.getApiSubError());
        }
    }

    /**
     * Finds all logins of users that have role of designer
     *
     * @param roleType   the role type
     * @param userRefIds ids of the users who should be excluded from the result set.
     * @param pageable   the pageable
     * @param givenName  optional parameter used for filtration
     * @param familyName optional parameter used for filtration
     * @return list of users with given role
     */
    public PageResultResource<UserRefDTO> getUsersByGivenRoleAndNotWithGivenIds(RoleType roleType, Set<Long> userRefIds, Pageable pageable, String givenName, String familyName) {
        HttpHeaders httpHeaders = new HttpHeaders();
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(userAndGroupURI + "/roles/users-not-with-ids");
        if (givenName != null) {
            builder.queryParam("givenName", givenName);
        }
        if (familyName != null) {
            builder.queryParam("familyName", familyName);
        }
        builder.queryParam("roleType", roleType.name());
        builder.queryParam("ids", StringUtils.collectionToDelimitedString(userRefIds, ","));
        builder.queryParam("page", pageable.getPageNumber());
        builder.queryParam("size", pageable.getPageSize());
        URI uri = builder.build().encode().toUri();
        try {
            ResponseEntity<PageResultResource<UserRefDTO>> usersResponse = javaRestTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(httpHeaders),
                    new ParameterizedTypeReference<PageResultResource<UserRefDTO>>() {
                    });
            return usersResponse.getBody();
        } catch (CustomRestTemplateException ex) {
            throw new MicroserviceApiException("Error when calling UserAndGroup API to obtain users with role " + roleType.name() + " and IDs:", ex.getApiSubError());
        }
    }

    /**
     * Create new user reference
     *
     * @param userRefToCreate user reference to be created
     * @return created {@link UserRef}
     */
    @TransactionalWO
    public UserRef createUserRef(UserRef userRefToCreate) {
        UserRef userRef = userRefRepository.save(userRefToCreate);
        LOG.info("User ref with user_ref_id: {} created.", userRef.getUserRefId());
        return userRef;
    }

    private static <T> T convertJsonBytesToObject(String object, Class<T> objectClass) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.registerModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper.readValue(object, objectClass);
    }
}
