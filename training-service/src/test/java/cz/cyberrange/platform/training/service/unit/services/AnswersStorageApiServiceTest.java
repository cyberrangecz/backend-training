package cz.cyberrange.platform.training.service.unit.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cz.cyberrange.platform.training.api.responses.SandboxAnswersInfo;
import cz.cyberrange.platform.training.service.services.api.AnswersStorageApiService;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

class AnswersStorageApiServiceTest {

  private final List<URI> requestedUris = new ArrayList<>();

  @Test
  @DisplayName("getAnswersBySandboxIds, several pages: reads every page at the capped size")
  void getAnswersBySandboxIds_severalPages_readsEveryPageAtCappedSize() {
    AnswersStorageApiService service = serviceAnsweringPages(3);

    List<SandboxAnswersInfo> answers = service.getAnswersBySandboxIds(List.of("a", "b"));

    assertEquals(
        List.of("sandbox-0", "sandbox-1", "sandbox-2"),
        answers.stream().map(SandboxAnswersInfo::getSandboxRefId).toList());
    assertEquals(3, requestedUris.size());
    for (int page = 0; page < requestedUris.size(); page++) {
      var queryParameters =
          UriComponentsBuilder.fromUri(requestedUris.get(page)).build().getQueryParams();
      assertEquals(String.valueOf(page), queryParameters.getFirst("page"));
      assertEquals("2000", queryParameters.getFirst("size"));
      assertEquals(List.of("a", "b"), queryParameters.get("sandboxRefId"));
    }
  }

  @Test
  @DisplayName(
      "getAnswersByAccessTokenAndUserIds, no matching sandbox: returns empty after one request")
  void getAnswersByAccessTokenAndUserIds_noMatchingSandbox_returnsEmptyAfterOneRequest() {
    AnswersStorageApiService service = serviceAnsweringPages(0);

    List<SandboxAnswersInfo> answers =
        service.getAnswersByAccessTokenAndUserIds("token-1234", List.of(1L, 2L));

    assertEquals(List.of(), answers);
    assertEquals(1, requestedUris.size());
    var queryParameters =
        UriComponentsBuilder.fromUri(requestedUris.getFirst()).build().getQueryParams();
    assertEquals("token-1234", queryParameters.getFirst("accessToken"));
    assertEquals(List.of("1", "2"), queryParameters.get("userId"));
  }

  /**
   * Builds the service over a client whose every sandbox answers request receives the requested
   * page of a result spread over the given number of pages, one sandbox per page.
   */
  private AnswersStorageApiService serviceAnsweringPages(int totalPages) {
    WebClient webClient =
        WebClient.builder()
            .exchangeFunction(
                request -> {
                  requestedUris.add(request.url());
                  int page =
                      Integer.parseInt(
                          UriComponentsBuilder.fromUri(request.url())
                              .build()
                              .getQueryParams()
                              .getFirst("page"));
                  return Mono.just(
                      ClientResponse.create(HttpStatus.OK)
                          .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                          .body(pageJson(page, totalPages))
                          .build());
                })
            .build();
    return new AnswersStorageApiService(webClient);
  }

  private static String pageJson(int page, int totalPages) {
    String content = page < totalPages ? "[{\"sandbox_ref_id\":\"sandbox-" + page + "\"}]" : "[]";
    return "{\"content\":"
        + content
        + ",\"pagination\":{\"number\":"
        + page
        + ",\"number_of_elements\":1,\"size\":2000,\"total_elements\":"
        + totalPages
        + ",\"total_pages\":"
        + totalPages
        + "}}";
  }
}
