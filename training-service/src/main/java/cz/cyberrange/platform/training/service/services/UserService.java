package cz.cyberrange.platform.training.service.services;

import cz.cyberrange.platform.training.api.dto.UserRefDTO;
import cz.cyberrange.platform.training.api.enums.RoleType;
import cz.cyberrange.platform.training.api.exceptions.CustomWebClientException;
import cz.cyberrange.platform.training.api.exceptions.EntityErrorDetail;
import cz.cyberrange.platform.training.api.exceptions.EntityNotFoundException;
import cz.cyberrange.platform.training.api.exceptions.MicroserviceApiException;
import cz.cyberrange.platform.training.api.responses.PageResultResource;
import cz.cyberrange.platform.training.persistence.model.UserRef;
import cz.cyberrange.platform.training.persistence.repository.UserRefRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

/** The type User service. */
@Service
public class UserService {

  private final WebClient userManagementServiceWebClient;
  private final UserRefRepository userRefRepository;

  /**
   * Instantiates a new User service.
   *
   * @param userManagementServiceWebClient the rest template
   * @param userRefRepository the user ref repository
   */
  public UserService(
      @Qualifier("userManagementServiceWebClient") WebClient userManagementServiceWebClient,
      UserRefRepository userRefRepository) {
    this.userManagementServiceWebClient = userManagementServiceWebClient;
    this.userRefRepository = userRefRepository;
  }

  /**
   * Finds the locally stored user reference row carrying the given cross-service user reference id.
   *
   * @param userRefId the cross-service user reference id to look the row up by, which is not the
   *     row's own primary key
   * @return the {@link UserRef} row
   * @throws EntityNotFoundException when no row carries that user reference id
   */
  public UserRef getUserByUserRefId(Long userRefId) {
    return userRefRepository
        .findUserByUserRefId(userRefId)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    new EntityErrorDetail(UserRef.class, "id", userRefId.getClass(), userRefId)));
  }

  /**
   * If user reference with given user id does not exist, it is created and returned. Otherwise, the
   * existing one is returned.
   *
   * @param userRefId id of the referenced user
   * @return user reference with given referenced id
   */
  public UserRef createOrGetUserRef(Long userRefId) {
    return userRefRepository.createOrGet(userRefId);
  }

  /**
   * Asks the user-and-group service for the profile of the user carrying the given cross-service
   * user reference id.
   *
   * @param id the cross-service user reference id of the user in question
   * @return that user's profile
   * @throws MicroserviceApiException when the call to the user-and-group service fails
   */
  public UserRefDTO getUserRefDTOByUserRefId(Long id) {
    try {
      return userManagementServiceWebClient
          .get()
          .uri("/users/{id}", id)
          .retrieve()
          .bodyToMono(UserRefDTO.class)
          .block();
    } catch (CustomWebClientException ex) {
      throw new MicroserviceApiException(
          "Error when calling user management service API to obtain info about user(ID: "
              + id
              + ").",
          ex);
    }
  }

  /**
   * Asks the user-and-group service for one page of the users carrying the given cross-service user
   * reference ids.
   *
   * @param userRefIds the cross-service user reference ids to retrieve
   * @param pageable pageable parameter with information about pagination
   * @param givenName restricts the result to users whose given name matches, no restriction when
   *     null
   * @param familyName restricts the result to users whose family name matches, no restriction when
   *     null
   * @return the requested page of users, an empty page without contacting the service when no id is
   *     given
   * @throws MicroserviceApiException when the call to the user-and-group service fails
   */
  public PageResultResource<UserRefDTO> getUsersRefDTOByGivenUserIds(
      List<Long> userRefIds, Pageable pageable, String givenName, String familyName) {
    if (userRefIds.isEmpty()) {
      return new PageResultResource<>(
          Collections.emptyList(),
          new PageResultResource.Pagination(0, 0, pageable.getPageSize(), 0, 0));
    }
    try {
      return userManagementServiceWebClient
          .get()
          .uri(
              uriBuilder -> {
                uriBuilder
                    .path("/users/ids")
                    .queryParam("ids", StringUtils.collectionToDelimitedString(userRefIds, ","));
                this.setCommonParams(givenName, familyName, pageable, uriBuilder);
                return uriBuilder.build();
              })
          .retrieve()
          .bodyToMono(new ParameterizedTypeReference<PageResultResource<UserRefDTO>>() {})
          .block();
    } catch (CustomWebClientException ex) {
      throw new MicroserviceApiException(
          "Error when calling user management service API to obtain users by IDs: "
              + userRefIds
              + ".",
          ex);
    }
  }

  /**
   * Retrieves the users carrying the given cross-service user reference ids in full, walking every
   * page the user-and-group service reports.
   *
   * @param participantsRefIds the cross-service user reference ids to retrieve
   * @return all matching users, unfiltered by name
   * @throws MicroserviceApiException when any of the calls to the user-and-group service fails
   */
  public List<UserRefDTO> getUsersRefDTOByGivenUserIds(List<Long> participantsRefIds) {
    List<UserRefDTO> participants = new ArrayList<>();
    PageResultResource<UserRefDTO> participantsInfo;
    int page = 0;
    do {
      participantsInfo =
          this.getUsersRefDTOByGivenUserIds(
              participantsRefIds, PageRequest.of(page, 999), null, null);
      participants.addAll(participantsInfo.getContent());
      page++;
    } while (page < participantsInfo.getPagination().getTotalPages());
    return participants;
  }

  /**
   * Asks the user-and-group service for one page of the users holding the given role.
   *
   * @param roleType the role its holders are requested for
   * @param pageable pageable parameter with information about pagination
   * @param givenName restricts the result to users whose given name matches, no restriction when
   *     null
   * @param familyName restricts the result to users whose family name matches, no restriction when
   *     null
   * @return the requested page of users holding that role
   * @throws MicroserviceApiException when the call to the user-and-group service fails
   */
  public PageResultResource<UserRefDTO> getUsersByGivenRole(
      RoleType roleType, Pageable pageable, String givenName, String familyName) {
    try {
      return userManagementServiceWebClient
          .get()
          .uri(
              uriBuilder -> {
                uriBuilder.path("/roles/users").queryParam("roleType", roleType.name());
                this.setCommonParams(givenName, familyName, pageable, uriBuilder);
                return uriBuilder.build();
              })
          .retrieve()
          .bodyToMono(new ParameterizedTypeReference<PageResultResource<UserRefDTO>>() {})
          .block();
    } catch (CustomWebClientException ex) {
      throw new MicroserviceApiException(
          "Error when calling user management service API to obtain users with role "
              + roleType.name()
              + ".",
          ex);
    }
  }

  /**
   * Asks the user-and-group service for one page of the users holding the given role, leaving out
   * the named ones.
   *
   * @param roleType the role its holders are requested for
   * @param userRefIds cross-service user reference ids to exclude from the result
   * @param pageable pageable parameter with information about pagination
   * @param givenName restricts the result to users whose given name matches, no restriction when
   *     null
   * @param familyName restricts the result to users whose family name matches, no restriction when
   *     null
   * @return the requested page of the remaining users holding that role
   * @throws MicroserviceApiException when the call to the user-and-group service fails
   */
  public PageResultResource<UserRefDTO> getUsersByGivenRoleAndNotWithGivenIds(
      RoleType roleType,
      Set<Long> userRefIds,
      Pageable pageable,
      String givenName,
      String familyName) {
    try {
      return userManagementServiceWebClient
          .get()
          .uri(
              uriBuilder -> {
                uriBuilder
                    .path("/roles/users-not-with-ids")
                    .queryParam("roleType", roleType.name())
                    .queryParam("ids", StringUtils.collectionToDelimitedString(userRefIds, ","));
                this.setCommonParams(givenName, familyName, pageable, uriBuilder);
                return uriBuilder.build();
              })
          .retrieve()
          .bodyToMono(new ParameterizedTypeReference<PageResultResource<UserRefDTO>>() {})
          .block();
    } catch (CustomWebClientException ex) {
      throw new MicroserviceApiException(
          "Error when calling user management service API to obtain users with role "
              + roleType.name()
              + " and IDs: "
              + userRefIds
              + ".",
          ex);
    }
  }

  /**
   * Asks the user-and-group service for the profile of the user the current request authenticates
   * as.
   *
   * @return that user's profile
   * @throws MicroserviceApiException when the call to the user-and-group service fails
   */
  public UserRefDTO getUserRefFromUserAndGroup() {
    try {
      return userManagementServiceWebClient
          .get()
          .uri("/users/info")
          .retrieve()
          .bodyToMono(UserRefDTO.class)
          .block();
    } catch (CustomWebClientException ex) {
      throw new MicroserviceApiException(
          "Error when calling user management service API to get info about logged in user.", ex);
    }
  }

  /**
   * Appends the pagination query parameters to the request being built, along with whichever name
   * filters were supplied.
   *
   * @param givenName given name filter to append, skipped when null
   * @param familyName family name filter to append, skipped when null
   * @param pageable the pagination to translate into query parameters
   * @param builder the request URI being built
   */
  private void setCommonParams(
      String givenName, String familyName, Pageable pageable, UriBuilder builder) {
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
