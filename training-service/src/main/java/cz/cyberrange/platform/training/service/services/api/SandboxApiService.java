package cz.cyberrange.platform.training.service.services.api;

import cz.cyberrange.platform.training.api.exceptions.CustomWebClientException;
import cz.cyberrange.platform.training.api.exceptions.ForbiddenException;
import cz.cyberrange.platform.training.api.exceptions.MicroserviceApiException;
import cz.cyberrange.platform.training.api.exceptions.errors.PythonApiError;
import cz.cyberrange.platform.training.api.responses.AllocationUnitInfo;
import cz.cyberrange.platform.training.api.responses.LockedPoolInfo;
import cz.cyberrange.platform.training.api.responses.PoolInfoDTO;
import cz.cyberrange.platform.training.api.responses.SandboxDefinitionInfo;
import cz.cyberrange.platform.training.api.responses.SandboxInfo;
import cz.cyberrange.platform.training.api.responses.Variables;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/** The type Sandbox Api service */
@Service
public class SandboxApiService {

  private static final Logger LOG = LoggerFactory.getLogger(SandboxApiService.class);

  /** Header carrying the access token of the training whose pool a trainee allocates in */
  private static final String TRAINING_ACCESS_TOKEN_HEADER = "X-Training-Access-Token";

  private WebClient sandboxServiceWebClient;

  /**
   * Instantiates a new SandboxApiService service.
   *
   * @param sandboxServiceWebClient the web client
   */
  public SandboxApiService(
      @Qualifier("sandboxServiceWebClient") WebClient sandboxServiceWebClient) {
    this.sandboxServiceWebClient = sandboxServiceWebClient;
  }

  /**
   * Retrieves the sandbox definition backing a pool by calling the sandbox service.
   *
   * @param poolId the pool id
   * @return the sandbox definition of the given pool
   * @throws ForbiddenException if the sandbox service reports no available sandbox definition for
   *     the pool
   * @throws MicroserviceApiException if the sandbox service call fails for any other reason
   */
  public SandboxDefinitionInfo getSandboxDefinitionId(Long poolId) {
    try {
      return sandboxServiceWebClient
          .get()
          .uri("/pools/{poolId}/definition", poolId)
          .retrieve()
          .bodyToMono(SandboxDefinitionInfo.class)
          .block();
    } catch (CustomWebClientException ex) {
      if (ex.getStatusCode() == HttpStatus.CONFLICT) {
        throw new ForbiddenException(
            "There is no available sandbox definition for particular pool (ID: " + poolId + ").");
      }
      throw new MicroserviceApiException(
          "Error when calling Python API to obtain sandbox definition info for particular pool (ID: "
              + poolId
              + ").",
          ex);
    }
  }

  /**
   * Lock pool locked pool info.
   *
   * @param poolId the pool id
   * @param accessToken the training access token
   * @return the locked pool info
   */
  public LockedPoolInfo lockPool(Long poolId, String accessToken) {
    try {
      String requestBody = String.format("{\"training_access_token\": \"%s\"}", accessToken);

      return sandboxServiceWebClient
          .post()
          .uri("/pools/{poolId}/locks", poolId)
          .body(Mono.just(requestBody), String.class)
          .retrieve()
          .bodyToMono(LockedPoolInfo.class)
          .block();
    } catch (CustomWebClientException ex) {
      throw new MicroserviceApiException(
          "Currently, it is not possible to lock and assign pool with (ID: " + poolId + ").", ex);
    }
  }

  /**
   * Unlock pool.
   *
   * @param poolId the pool id
   */
  public void unlockPool(Long poolId) {
    try {
      // get lock id from pool
      PoolInfoDTO poolInfoDto =
          sandboxServiceWebClient
              .get()
              .uri("/pools/{poolId}", poolId)
              .retrieve()
              .bodyToMono(PoolInfoDTO.class)
              .block();
      // unlock pool
      if (poolInfoDto != null && poolInfoDto.getLockId() != null) {
        sandboxServiceWebClient
            .delete()
            .uri("/pools/{poolId}/locks/{lockId}", poolId, poolInfoDto.getLockId())
            .retrieve()
            .bodyToMono(Void.class)
            .block();
      }
    } catch (CustomWebClientException ex) {
      if (ex.getStatusCode() != HttpStatus.NOT_FOUND) {
        throw new MicroserviceApiException(
            "Currently, it is not possible to unlock a pool (ID: " + poolId + ").", ex);
      }
    }
  }

  /**
   * Get and lock sandbox.
   *
   * @param poolId the pool id
   * @param trainingAccessToken the training access token
   * @return the sandbox info
   */
  public SandboxInfo getAndLockSandbox(Long poolId, String trainingAccessToken) {
    try {
      return sandboxServiceWebClient
          .get()
          .uri(
              "/pools/{poolId}/sandboxes/get-and-lock/{trainingAccessToken}",
              poolId,
              trainingAccessToken)
          .retrieve()
          .bodyToMono(SandboxInfo.class)
          .block();
    } catch (CustomWebClientException ex) {
      if (ex.getStatusCode() == HttpStatus.CONFLICT) {
        throw new ForbiddenException(
            "There is no available sandbox, wait a minute and try again or ask organizer to allocate more sandboxes.");
      }
      throw new MicroserviceApiException(
          "Error when calling Sandbox Service API to get unlocked sandbox from pool (ID: "
              + poolId
              + ").",
          ex);
    }
  }

  /**
   * Get APG variables defined in the sandbox definition of the pool.
   *
   * @param poolId the pool id
   * @return set of APG variables
   */
  public Variables getVariablesByPoolId(Long poolId) {
    try {
      return sandboxServiceWebClient
          .get()
          .uri("/pools/{poolId}/variables", poolId)
          .retrieve()
          .bodyToMono(Variables.class)
          .block();
    } catch (CustomWebClientException ex) {
      throw new MicroserviceApiException(
          "Currently, it is not possible to get variables of the pool (ID: " + poolId + ").", ex);
    }
  }

  /**
   * Get APG variables defined in the sandbox definition.
   *
   * @param sandboxDefinitionId the sandbox definition id
   * @return set of APG variables
   */
  public Set<String> getVariablesBySandboxDefinitionId(Long sandboxDefinitionId) {
    try {
      return sandboxServiceWebClient
          .get()
          .uri("/definitions/{sandboxDefinitionId}/variables", sandboxDefinitionId)
          .retrieve()
          .bodyToMono(new ParameterizedTypeReference<Set<String>>() {})
          .block();
    } catch (CustomWebClientException ex) {
      throw new MicroserviceApiException(
          "Currently, it is not possible to get variables of the sandbox definition (ID: "
              + sandboxDefinitionId
              + ").",
          ex);
    }
  }

  /**
   * Allocates one sandbox in the given pool for the logged in user, who becomes its owner. The pool
   * must be locked for the training instance with the given access token.
   *
   * @param poolId the pool id
   * @param trainingAccessToken the access token of the training instance holding the pool
   * @return the new allocation unit; empty when the user already has an active sandbox in the pool
   * @throws MicroserviceApiException when the call to the sandbox service fails for another reason
   */
  public Optional<AllocationUnitInfo> allocateSandboxForTrainee(
      Long poolId, String trainingAccessToken) {
    try {
      List<AllocationUnitInfo> units =
          sandboxServiceWebClient
              .post()
              .uri("/pools/{poolId}/sandbox-allocation-units?count=1", poolId)
              .header(TRAINING_ACCESS_TOKEN_HEADER, trainingAccessToken)
              .retrieve()
              .bodyToMono(new ParameterizedTypeReference<List<AllocationUnitInfo>>() {})
              .block();
      if (units == null || units.isEmpty()) {
        throw new MicroserviceApiException(
            HttpStatus.BAD_GATEWAY,
            PythonApiError.of("The sandbox service allocated no sandbox in pool " + poolId + "."));
      }
      return Optional.of(units.get(0));
    } catch (CustomWebClientException ex) {
      if (ex.getStatusCode() == HttpStatus.CONFLICT) {
        return Optional.empty();
      }
      throw new MicroserviceApiException(
          "Currently, it is not possible to allocate a sandbox in the pool (ID: " + poolId + ").",
          ex);
    }
  }

  /**
   * Gets an allocation unit. A trainee may get only the units they allocated.
   *
   * @param unitId the allocation unit id
   * @return the allocation unit; empty when it does not exist, e.g. because it was cleaned up
   * @throws MicroserviceApiException when the call to the sandbox service fails for another reason,
   *     with status 403 when the logged in user may not see the unit
   */
  public Optional<AllocationUnitInfo> findAllocationUnit(Integer unitId) {
    try {
      return Optional.ofNullable(
          sandboxServiceWebClient
              .get()
              .uri("/sandbox-allocation-units/{unitId}", unitId)
              .retrieve()
              .bodyToMono(AllocationUnitInfo.class)
              .block());
    } catch (CustomWebClientException ex) {
      if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
        return Optional.empty();
      }
      throw new MicroserviceApiException(
          "Currently, it is not possible to get the allocation unit (ID: " + unitId + ").", ex);
    }
  }

  /**
   * Lists every allocation unit the user with the given OIDC subject allocated for themselves, in
   * any state.
   *
   * @param createdBySub the OIDC subject of the user, who must be the logged in user unless that
   *     user organizes trainings
   * @return the user's allocation units
   * @throws MicroserviceApiException when the call to the sandbox service fails
   */
  public List<AllocationUnitInfo> findAllocationUnitsCreatedBy(String createdBySub) {
    try {
      List<AllocationUnitInfo> units =
          sandboxServiceWebClient
              .get()
              .uri(
                  uriBuilder ->
                      uriBuilder
                          .path("/sandbox-allocation-units/by-creator")
                          .queryParam("created_by_sub", "{createdBySub}")
                          .build(createdBySub))
              .retrieve()
              .bodyToMono(new ParameterizedTypeReference<List<AllocationUnitInfo>>() {})
              .block();
      return units == null ? List.of() : units;
    } catch (CustomWebClientException ex) {
      throw new MicroserviceApiException(
          "Currently, it is not possible to list the sandboxes the user allocated.", ex);
    }
  }

  /**
   * Asks the sandbox service to remove the sandbox of an allocation unit, cancelling its allocation
   * if it is still running. Retries a removal that failed and does nothing while one is running.
   *
   * @param unitId the allocation unit id
   * @return false when the unit does not exist any more
   * @throws MicroserviceApiException when the call to the sandbox service fails for another reason,
   *     e.g. with status 400 while the first stage of the allocation is running
   */
  public boolean requestCleanup(Integer unitId) {
    try {
      sandboxServiceWebClient
          .post()
          .uri("/sandbox-allocation-units/{unitId}/cleanup-request?force=true", unitId)
          .retrieve()
          .toBodilessEntity()
          .block();
      return true;
    } catch (CustomWebClientException ex) {
      if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
        return false;
      }
      throw new MicroserviceApiException(
          "Currently, it is not possible to remove the sandbox of the allocation unit (ID: "
              + unitId
              + ").",
          ex);
    }
  }
}
