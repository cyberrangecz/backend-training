package cz.muni.ics.kypo.training.service;

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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.ConstraintViolationException;
import java.net.URI;
import java.util.Collections;
import java.util.Set;

/**
 * The type User service.
 */
@Service
public class UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    private RestTemplate javaRestTemplate;
    private UserRefRepository userRefRepository;

    /**
     * Instantiates a new User service.
     *
     * @param javaRestTemplate  the rest template
     * @param userRefRepository the user ref repository
     */
    public UserService(@Qualifier("javaRestTemplate") RestTemplate javaRestTemplate,
                       UserRefRepository userRefRepository) {
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
        return userRefRepository.findUserByUserRefId(userRefId)
                .orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(UserRef.class, "id", userRefId.getClass(), userRefId,
                        "UserRef not found.")));
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
            return javaRestTemplate.getForObject("/users/{id}", UserRefDTO.class, Long.toString(id));
        } catch (CustomRestTemplateException ex) {
            throw new MicroserviceApiException("Error when calling UserAndGroup API to obtain info about user(ID: " + id + ").", ex.getApiSubError());
        } catch (ConstraintViolationException ex) {
            throw new MicroserviceApiException("Error in response when calling user management API to obtain info about user(ID: " + id + ")", ex);
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
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("/users/ids");
        setCommonParams(givenName, familyName, pageable, builder);
        builder.queryParam("ids", StringUtils.collectionToDelimitedString(userRefIds, ","));
        URI uri = builder.build().encode().toUri();
        try {
            ResponseEntity<PageResultResource<UserRefDTO>> usersResponse = javaRestTemplate.exchange(uri, HttpMethod.GET, HttpEntity.EMPTY,
                    new ParameterizedTypeReference<PageResultResource<UserRefDTO>>() {
                    });
            return usersResponse.getBody();
        } catch (CustomRestTemplateException ex) {
            throw new MicroserviceApiException("Error when calling UserAndGroup API to obtain users by IDs: " + userRefIds + ".", ex.getApiSubError());
        } catch (ConstraintViolationException ex) {
            throw new MicroserviceApiException("Error in response when calling user management API to obtain users by IDs: " + userRefIds + ".", ex);
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
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("/roles/users");
        setCommonParams(givenName, familyName, pageable, builder);
        builder.queryParam("roleType", roleType.name());
        URI uri = builder.build().encode().toUri();
        try {
            ResponseEntity<PageResultResource<UserRefDTO>> usersResponse = javaRestTemplate.exchange(uri, HttpMethod.GET, HttpEntity.EMPTY,
                    new ParameterizedTypeReference<PageResultResource<UserRefDTO>>() {
                    });
            return usersResponse.getBody();
        } catch (CustomRestTemplateException ex) {
            throw new MicroserviceApiException("Error when calling UserAndGroup API to obtain users with role " + roleType.name() + ".", ex.getApiSubError());
        } catch (ConstraintViolationException ex) {
            throw new MicroserviceApiException("Error in response when calling user management API to obtain users with role " + roleType.name() + ".", ex);
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
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("/roles/users-not-with-ids");
        setCommonParams(givenName, familyName, pageable, builder);
        builder.queryParam("roleType", roleType.name());
        builder.queryParam("ids", StringUtils.collectionToDelimitedString(userRefIds, ","));
        URI uri = builder.build().encode().toUri();
        try {
            ResponseEntity<PageResultResource<UserRefDTO>> usersResponse = javaRestTemplate.exchange(uri, HttpMethod.GET, HttpEntity.EMPTY,
                    new ParameterizedTypeReference<PageResultResource<UserRefDTO>>() {
                    });
            return usersResponse.getBody();
        } catch (CustomRestTemplateException ex) {
            throw new MicroserviceApiException("Error when calling UserAndGroup API to obtain users with role " + roleType.name() + " and IDs: " + userRefIds + ".", ex.getApiSubError());
        } catch (ConstraintViolationException ex) {
            throw new MicroserviceApiException("Error in response when calling user management API to obtain users with role " + roleType.name() + " and IDs: " + userRefIds + ".", ex);
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

    private void setCommonParams(String givenName, String familyName, Pageable pageable, UriComponentsBuilder builder) {
        if (givenName != null) {
            builder.queryParam("givenName", givenName);
        }
        if (familyName != null) {
            builder.queryParam("familyName", familyName);
        }
        builder.queryParam("page", pageable.getPageNumber());
        builder.queryParam("size", pageable.getPageSize());
    }
}
