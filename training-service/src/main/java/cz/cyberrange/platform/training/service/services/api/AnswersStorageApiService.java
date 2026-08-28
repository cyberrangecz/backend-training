package cz.cyberrange.platform.training.service.services.api;

import cz.cyberrange.platform.training.api.exceptions.CustomWebClientException;
import cz.cyberrange.platform.training.api.exceptions.MicroserviceApiException;
import cz.cyberrange.platform.training.api.responses.PageResultResource;
import cz.cyberrange.platform.training.api.responses.SandboxAnswersInfo;
import java.util.List;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Client for the answer-storage microservice, which holds the generated variable answers for
 * sandboxes. Each method issues one blocking HTTP call and rewraps a failing response, surfaced by
 * the underlying {@code WebClient} as a {@link CustomWebClientException}, into a {@link
 * MicroserviceApiException} carrying a message naming the call that failed.
 */
@Service
public class AnswersStorageApiService {

  private final WebClient answersStorageWebClient;

  public AnswersStorageApiService(WebClient answersStorageWebClient) {
    this.answersStorageWebClient = answersStorageWebClient;
  }

  /**
   * Returns the correct answer stored for one variable of a cloud sandbox.
   *
   * @param sandboxId the sandbox's reference id
   * @param answerVariableName the variable's identifier
   * @return the answer text
   * @throws MicroserviceApiException when the answer-storage call fails
   */
  public String getCorrectAnswerByCloudSandboxIdAndVariableName(
      String sandboxId, String answerVariableName) {
    try {
      return answersStorageWebClient
          .get()
          .uri("/sandboxes/{sandboxId}/answers/{answerVariableName}", sandboxId, answerVariableName)
          .retrieve()
          .bodyToMono(String.class)
          .block();
    } catch (CustomWebClientException ex) {
      throw new MicroserviceApiException(
          "Error when calling Answers Storage API to get correct answer (Identifier: "
              + answerVariableName
              + ") for sandbox (ID: "
              + sandboxId
              + ").",
          ex);
    }
  }

  /**
   * Returns the correct answer stored for one variable of a local sandbox, addressed by the
   * training instance's access token and the owning user's cross-service user reference id.
   *
   * @param accessToken the training instance's access token
   * @param userId the sandbox owner's user reference id
   * @param answerVariableName the variable's identifier
   * @return the answer text
   * @throws MicroserviceApiException when the answer-storage call fails
   */
  public String getCorrectAnswerByLocalSandboxIdAndVariableName(
      String accessToken, Long userId, String answerVariableName) {
    try {
      return answersStorageWebClient
          .get()
          .uri(
              "/sandboxes/access-tokens/{accessToken}/users/{userId}/answers/{answerVariableName}",
              accessToken,
              userId,
              answerVariableName)
          .retrieve()
          .bodyToMono(String.class)
          .block();
    } catch (CustomWebClientException ex) {
      throw new MicroserviceApiException(
          "Error when calling Answers Storage API to get correct answer (Identifier: "
              + answerVariableName
              + ") "
              + "for sandbox (Access Token: "
              + accessToken
              + ", User ID: "
              + userId
              + ").",
          ex);
    }
  }

  /**
   * Returns every answer generated for a cloud sandbox.
   *
   * @param sandboxId the sandbox's reference id
   * @return the sandbox's answers
   * @throws MicroserviceApiException when the answer-storage call fails
   */
  public SandboxAnswersInfo getAnswersBySandboxId(String sandboxId) {
    try {
      return answersStorageWebClient
          .get()
          .uri("/sandboxes/{sandboxId}/answers", sandboxId)
          .retrieve()
          .bodyToMono(SandboxAnswersInfo.class)
          .block();
    } catch (CustomWebClientException ex) {
      throw new MicroserviceApiException(
          "Error when calling Answers Storage API to get correct answers for cloud sandbox (ID: "
              + sandboxId
              + ").",
          ex);
    }
  }

  /**
   * Returns every answer generated for a local sandbox, addressed by the training instance's access
   * token and the owning user's cross-service user reference id.
   *
   * @param accessToken the training instance's access token
   * @param userId the sandbox owner's user reference id
   * @return the sandbox's answers
   * @throws MicroserviceApiException when the answer-storage call fails
   */
  public SandboxAnswersInfo getAnswersByAccessTokenAndUserId(String accessToken, Long userId) {
    try {
      return answersStorageWebClient
          .get()
          .uri("/sandboxes/access-tokens/{accessToken}/users/{userId}", accessToken, userId)
          .retrieve()
          .bodyToMono(SandboxAnswersInfo.class)
          .block();
    } catch (CustomWebClientException ex) {
      throw new MicroserviceApiException(
          "Error when calling Answers Storage API to get correct answers for local sandbox (accessToken: "
              + accessToken
              + ", userID: "
              + userId
              + ").",
          ex);
    }
  }

  /**
   * Get all answers generated for the given cloud sandboxes.
   *
   * @param sandboxIds ids of the sandboxes.
   * @throws MicroserviceApiException error with specific message when calling answers storage
   *     microservice.
   */
  public PageResultResource<SandboxAnswersInfo> getAnswersBySandboxIds(List<String> sandboxIds) {
    try {
      return answersStorageWebClient
          .get()
          .uri(
              uriBuilder ->
                  uriBuilder
                      .path("/sandboxes")
                      .queryParam("sandboxRefId", sandboxIds)
                      .queryParam("page", 0)
                      .queryParam("size", Integer.MAX_VALUE)
                      .build())
          .retrieve()
          .bodyToMono(new ParameterizedTypeReference<PageResultResource<SandboxAnswersInfo>>() {})
          .block();
    } catch (CustomWebClientException ex) {
      throw new MicroserviceApiException(
          "Error when calling Answers Storage API to get correct answers for sandboxes (IDs: "
              + sandboxIds
              + ").",
          ex);
    }
  }

  /**
   * Get all answers generated for the local sandboxes by the users IDs and specific access token.
   *
   * @param accessToken token of the training instance
   * @param userIds ids of the users.
   * @throws MicroserviceApiException error with specific message when calling answers storage
   *     microservice.
   */
  public PageResultResource<SandboxAnswersInfo> getAnswersByAccessTokenAndUserIds(
      String accessToken, List<Long> userIds) {
    try {
      return answersStorageWebClient
          .get()
          .uri(
              uriBuilder ->
                  uriBuilder
                      .path("/sandboxes")
                      .queryParam("accessToken", accessToken)
                      .queryParam("userId", userIds)
                      .queryParam("page", 0)
                      .queryParam("size", Integer.MAX_VALUE)
                      .build())
          .retrieve()
          .bodyToMono(new ParameterizedTypeReference<PageResultResource<SandboxAnswersInfo>>() {})
          .block();
    } catch (CustomWebClientException ex) {
      throw new MicroserviceApiException(
          "Error when calling Answers Storage API to get correct answers for local sandboxes (accessToken: "
              + accessToken
              + ", userIDs: "
              + userIds
              + ").",
          ex);
    }
  }
}
