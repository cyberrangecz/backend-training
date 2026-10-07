package cz.cyberrange.platform.training.service.services.api;

import cz.cyberrange.platform.training.api.exceptions.CustomWebClientException;
import cz.cyberrange.platform.training.api.exceptions.MicroserviceApiException;
import cz.cyberrange.platform.training.api.responses.PageResultResource;
import cz.cyberrange.platform.training.api.responses.SandboxAnswersInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

/**
 * Client for the answer-storage microservice, which holds the generated variable answers for
 * sandboxes. Each method issues blocking HTTP calls and rewraps a failing response, surfaced by the
 * underlying {@code WebClient} as a {@link CustomWebClientException}, into a {@link
 * MicroserviceApiException} carrying a message naming the call that failed.
 */
@Service
public class AnswersStorageApiService {

  private static final String SANDBOXES_PATH = "/sandboxes";
  private static final int MAX_PAGE_SIZE = 2000;
  private static final ParameterizedTypeReference<PageResultResource<SandboxAnswersInfo>>
      SANDBOX_ANSWERS_PAGE_TYPE = new ParameterizedTypeReference<>() {};

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
          .accept(MediaType.TEXT_PLAIN)
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
          .accept(MediaType.TEXT_PLAIN)
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
   * Get all answers generated for the given cloud sandboxes, reading every page answer storage
   * holds for them.
   *
   * @param sandboxIds ids of the sandboxes.
   * @return the answers of every matching sandbox
   * @throws MicroserviceApiException error with specific message when calling answers storage
   *     microservice.
   */
  public List<SandboxAnswersInfo> getAnswersBySandboxIds(List<String> sandboxIds) {
    try {
      return getEverySandboxAnswersPage(
          uriBuilder -> uriBuilder.queryParam("sandboxRefId", sandboxIds));
    } catch (CustomWebClientException ex) {
      throw new MicroserviceApiException(
          "Error when calling Answers Storage API to get correct answers for sandboxes (IDs: "
              + sandboxIds
              + ").",
          ex);
    }
  }

  /**
   * Get all answers generated for the local sandboxes by the users IDs and specific access token,
   * reading every page answer storage holds for them.
   *
   * @param accessToken token of the training instance
   * @param userIds ids of the users.
   * @return the answers of every matching sandbox
   * @throws MicroserviceApiException error with specific message when calling answers storage
   *     microservice.
   */
  public List<SandboxAnswersInfo> getAnswersByAccessTokenAndUserIds(
      String accessToken, List<Long> userIds) {
    try {
      return getEverySandboxAnswersPage(
          uriBuilder ->
              uriBuilder.queryParam("accessToken", accessToken).queryParam("userId", userIds));
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

  /**
   * Requests the sandbox answers matching the given filter page by page, each page as large as
   * answer storage allows, until the last page has been read.
   *
   * @param filter adds the filtering query parameters to the request URI
   * @return the answers of every page, in page order
   * @throws CustomWebClientException when a page request fails
   */
  private List<SandboxAnswersInfo> getEverySandboxAnswersPage(UnaryOperator<UriBuilder> filter) {
    List<SandboxAnswersInfo> answers = new ArrayList<>();
    int pageNumber = 0;
    int totalPages;
    do {
      int requestedPage = pageNumber;
      PageResultResource<SandboxAnswersInfo> page =
          answersStorageWebClient
              .get()
              .uri(
                  uriBuilder ->
                      filter
                          .apply(uriBuilder.path(SANDBOXES_PATH))
                          .queryParam("page", requestedPage)
                          .queryParam("size", MAX_PAGE_SIZE)
                          .build())
              .retrieve()
              .bodyToMono(SANDBOX_ANSWERS_PAGE_TYPE)
              .blockOptional()
              .orElseThrow();
      answers.addAll(page.getContent());
      totalPages = page.getPagination().getTotalPages();
      pageNumber++;
    } while (pageNumber < totalPages);
    return answers;
  }
}
