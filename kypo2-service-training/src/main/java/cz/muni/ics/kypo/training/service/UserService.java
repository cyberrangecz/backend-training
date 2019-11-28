package cz.muni.ics.kypo.training.service;

import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.enums.RoleType;
import cz.muni.ics.kypo.training.api.responses.PageResultResource;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.persistence.model.UserRef;
import org.springframework.data.domain.Pageable;

import java.util.Set;

/**
 * The interface for User service.
 *
 * @author Dominik Pilar (445537)
 */
public interface UserService {

    /**
     * Finds specific User reference by login
     *
     * @param userRefId of wanted User reference
     * @return {@link UserRef} with corresponding login
     * @throws ServiceLayerException if UserRef was not found
     */
    UserRef getUserByUserRefId(Long userRefId);

    /**
     * Finds specific User reference by login
     *
     * @param userRefId of wanted User reference
     * @return {@link UserRef} with corresponding login
     * @throws ServiceLayerException if UserRef was not found
     */
    UserRefDTO getUserRefDTOByUserRefId(Long userRefId);


    /**
     * Gets users with given user ref ids.
     *
     * @param userRefIds the user ref ids
     * @param pageable pageable parameter with information about pagination.
     * @param givenName optional parameter used for filtration
     * @param familyName optional parameter used for filtration
     * @return the users with given user ref ids
     */
    PageResultResource<UserRefDTO> getUsersRefDTOByGivenUserIds(Set<Long> userRefIds, Pageable pageable, String givenName, String familyName);

    /**
     * Finds all logins of users that have role of designer
     *
     * @param roleType the role type
     * @param pageable pageable parameter with information about pagination.
     * @param givenName optional parameter used for filtration
     * @param familyName optional parameter used for filtration
     * @return list of users with given role
     */
    PageResultResource<UserRefDTO> getUsersByGivenRole(RoleType roleType, Pageable pageable, String givenName, String familyName);

    /**
     * Finds all logins of users that have role of designer
     *
     * @param roleType the role type
     * @param userRefIds ids of the users who should be excluded from the result set.
     * @param pageable the pageable
     * @param givenName optional parameter used for filtration
     * @param familyName optional parameter used for filtration
     * @return list of users with given role
     */
    PageResultResource<UserRefDTO> getUsersByGivenRoleAndNotWithGivenIds(RoleType roleType, Set<Long> userRefIds, Pageable pageable, String givenName, String familyName);

    /**
     * Create new user reference
     * @param userRefToCreate user reference to be created
     * @return created {@link UserRef}
     */
    UserRef createUserRef(UserRef userRefToCreate);
}
