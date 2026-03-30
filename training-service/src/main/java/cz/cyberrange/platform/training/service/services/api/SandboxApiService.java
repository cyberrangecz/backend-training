package cz.cyberrange.platform.training.service.services.api;

import cz.cyberrange.platform.training.api.exceptions.CustomWebClientException;
import cz.cyberrange.platform.training.api.exceptions.ForbiddenException;
import cz.cyberrange.platform.training.api.exceptions.MicroserviceApiException;
import cz.cyberrange.platform.training.api.exceptions.errors.JavaApiError;
import cz.cyberrange.platform.training.api.responses.ActiveSandboxSummaryDTO;
import cz.cyberrange.platform.training.api.responses.LockedPoolInfo;
import cz.cyberrange.platform.training.api.responses.PoolInfoDTO;
import cz.cyberrange.platform.training.api.responses.SandboxDefinitionInfo;
import cz.cyberrange.platform.training.api.responses.SandboxInfo;
import cz.cyberrange.platform.training.api.responses.Variables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * The type Sandbox Api service.
 */
@Service
public class SandboxApiService {

    private static final Logger LOG = LoggerFactory.getLogger(SandboxApiService.class);
    private WebClient sandboxServiceWebClient;

    /**
     * Instantiates a new SandboxApiService service.
     *
     * @param sandboxServiceWebClient the web client
     */
    public SandboxApiService(@Qualifier("sandboxServiceWebClient") WebClient sandboxServiceWebClient) {
        this.sandboxServiceWebClient = sandboxServiceWebClient;
    }

    /**
     * Gets sandbox definition id.
     *
     * @param poolId the pool id
     * @return the sandbox definition id
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
                throw new ForbiddenException("There is no available sandbox definition for particular pool (ID: " + poolId + ").");
            }
            throw new MicroserviceApiException("Error when calling Python API to obtain sandbox definition info for particular pool (ID: " + poolId + ").", ex);
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
            throw new MicroserviceApiException("Currently, it is not possible to lock and assign pool with (ID: " + poolId + ").", ex);
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
            PoolInfoDTO poolInfoDto = sandboxServiceWebClient
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
            if(ex.getStatusCode() != HttpStatus.NOT_FOUND){
                throw new MicroserviceApiException("Currently, it is not possible to unlock a pool (ID: " + poolId + ").", ex);
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
                    .uri("/pools/{poolId}/sandboxes/get-and-lock/{trainingAccessToken}", poolId, trainingAccessToken)
                    .retrieve()
                    .bodyToMono(SandboxInfo.class)
                    .block();
        } catch (CustomWebClientException ex) {
            if (ex.getStatusCode() == HttpStatus.CONFLICT) {
                throw new ForbiddenException("There is no available sandbox, wait a minute and try again or ask organizer to allocate more sandboxes.");
            }
            throw new MicroserviceApiException("Error when calling OpenStack Sandbox Service API to get unlocked sandbox from pool (ID: " + poolId + ").", ex);
        }
    }

    /**
     * Get and lock one sandbox in the pool for managed instance flow.
     * Returns empty when no free sandbox (404 or 409); caller should show stay-on-overview message
     * and must not create an allocation.
     *
     * @param poolId               the pool id
     * @param trainingAccessToken  the training instance access token (pool must be locked with this token)
     * @return sandbox info with id and allocationUnitId, or empty if no free sandbox
     */
    public Optional<SandboxInfo> getAndLockSandboxForManaged(Long poolId, String trainingAccessToken) {
        try {
            SandboxInfo info = sandboxServiceWebClient
                    .get()
                    .uri("/pools/{poolId}/sandboxes/get-and-lock/{trainingAccessToken}", poolId, trainingAccessToken)
                    .retrieve()
                    .bodyToMono(SandboxInfo.class)
                    .block();
            return Optional.ofNullable(info);
        } catch (CustomWebClientException ex) {
            HttpStatus status = ex.getStatusCode();
            if (status == HttpStatus.NOT_FOUND || status == HttpStatus.CONFLICT) {
                return Optional.empty();
            }
            throw new MicroserviceApiException("Error when calling Sandbox Service API to get sandbox from pool (ID: " + poolId + ").", ex);
        }
    }

    /**
     * Release the lock on the sandbox for the given allocation unit.
     * Used when a training run is deleted so the sandbox can be reused in the pool.
     * Idempotent: sandbox-service returns 204 when there is no lock.
     *
     * @param allocationUnitId the allocation unit id (sandbox's allocation_unit_id)
     */
    public void deleteLockForAllocationUnit(Integer allocationUnitId) {
        try {
            sandboxServiceWebClient
                    .delete()
                    .uri("/sandbox-allocation-units/{unitId}/lock", allocationUnitId)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (CustomWebClientException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                return;
            }
            LOG.warn("Failed to delete lock for allocation unit {}: {}", allocationUnitId, ex.getMessage());
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
            throw new MicroserviceApiException("Currently, it is not possible to get variables of the pool (ID: " + poolId + ").", ex);
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
            throw new MicroserviceApiException("Currently, it is not possible to get variables of the sandbox definition (ID: " + sandboxDefinitionId + ").", ex);
        }
    }

    /**
     * List all sandbox allocation units in the given pool (no filter by creator).
     * Used for managed instances: Admin allocates sandboxes in the pool; trainee gets any ready one.
     * Requires Trainee (or Organizer/Admin) permission on sandbox-service.
     */
    public List<ActiveSandboxSummaryDTO> listAllocationUnitsByPoolId(Long poolId) {
        try {
            List<ActiveSandboxSummaryDTO> list = sandboxServiceWebClient
                    .get()
                    .uri("/pools/{poolId}/sandbox-allocation-units", poolId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<ActiveSandboxSummaryDTO>>() {})
                    .block();
            return list != null ? list : Collections.emptyList();
        } catch (CustomWebClientException ex) {
            LOG.warn("Failed to list allocation units for pool {}: {}", poolId, ex.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * List active sandbox allocation units for a creator (OIDC sub).
     * Used for single-sandbox-per-user: enforce at most one active sandbox per trainee.
     */
    public List<ActiveSandboxSummaryDTO> listActiveAllocationUnitsByCreatorSub(String createdBySub) {
        return listAllocationUnitsByCreatorSub(createdBySub, true);
    }

    private static final int LIST_BY_CREATOR_RETRY_DELAY_MS = 500;

    /**
     * List sandbox allocation units for a creator (OIDC sub), optionally active only.
     * When activeOnly is false, returns all units (for ownership lookup when trainee requests cleanup).
     * Retries once on 5xx or connection errors to reduce intermittent failures.
     */
    public List<ActiveSandboxSummaryDTO> listAllocationUnitsByCreatorSub(String createdBySub, boolean activeOnly) {
        String uri = "/sandbox-allocation-units/by-creator?created_by_sub="
                + java.net.URLEncoder.encode(createdBySub, java.nio.charset.StandardCharsets.UTF_8);
        if (activeOnly) {
            uri += "&state=ACTIVE";
        }
        for (int attempt = 0; attempt < 2; attempt++) {
            try {
                List<ActiveSandboxSummaryDTO> list = sandboxServiceWebClient
                        .get()
                        .uri(uri)
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<List<ActiveSandboxSummaryDTO>>() {})
                        .block();
                return list != null ? list : Collections.emptyList();
            } catch (CustomWebClientException ex) {
                HttpStatus status = ex.getStatusCode();
                String msg = ex.getMessage();
                if (msg == null && ex.getApiSubError() != null) {
                    msg = ex.getApiSubError().getMessage();
                }
                if (msg == null) {
                    msg = "status=" + (status != null ? status : "null");
                }
                LOG.warn("Failed to list sandboxes for created_by_sub={} (attempt {}): {}", createdBySub, attempt + 1, msg);
                boolean retryable = status != null && status.is5xxServerError();
                if (!retryable || attempt >= 1) {
                    return Collections.emptyList();
                }
                try {
                    Thread.sleep(LIST_BY_CREATOR_RETRY_DELAY_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return Collections.emptyList();
                }
            }
        }
        return Collections.emptyList();
    }

    /**
     * Get allocation unit by id (to poll for sandbox_id when allocation is in progress).
     *
     * @param unitId allocation unit id
     * @return unit summary or null if not found
     */
    public ActiveSandboxSummaryDTO getAllocationUnitById(Integer unitId) {
        try {
            return sandboxServiceWebClient
                    .get()
                    .uri("/sandbox-allocation-units/{unitId}", unitId)
                    .retrieve()
                    .bodyToMono(ActiveSandboxSummaryDTO.class)
                    .block();
        } catch (CustomWebClientException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                return null;
            }
            throw new MicroserviceApiException("Error when calling Sandbox Service API to get allocation unit " + unitId, ex);
        }
    }

    /**
     * Create one sandbox allocation unit in the pool with the trainee as creator (OIDC sub).
     * Used for single-sandbox-per-user on-demand allocation.
     *
     * @param poolId       pool id
     * @param createdBySub OIDC sub of the trainee
     * @return the created allocation unit info (first element if multiple)
     */
    public ActiveSandboxSummaryDTO createAllocationUnitWithCreator(Long poolId, String createdBySub) {
        try {
            Map<String, String> body = Map.of("created_by_sub", createdBySub != null ? createdBySub : "");
            List<ActiveSandboxSummaryDTO> list = sandboxServiceWebClient
                    .post()
                    .uri("/pools/{poolId}/sandbox-allocation-units?count=1", poolId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<ActiveSandboxSummaryDTO>>() {})
                    .block();
            if (list != null && !list.isEmpty()) {
                return list.get(0);
            }
            throw new MicroserviceApiException(HttpStatus.BAD_GATEWAY, JavaApiError.of("Sandbox service returned no allocation unit for pool " + poolId));
        } catch (CustomWebClientException ex) {
            throw new MicroserviceApiException("Error when calling Sandbox Service API to create allocation unit in pool (ID: " + poolId + ").", ex);
        }
    }

    /**
     * Request cleanup for an allocation unit (used when trainee requests "Remove sandbox").
     * Uses force=true so cleanup succeeds even if sandbox is locked or allocation is not fully finished
     * (same behaviour as admin "Delete" from Pool detail).
     *
     * @param unitId allocation unit id
     */
    public void requestCleanupForAllocationUnit(Integer unitId) {
        try {
            sandboxServiceWebClient
                    .post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/sandbox-allocation-units/{unitId}/cleanup-request")
                            .queryParam("force", "true")
                            .build(unitId))
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (CustomWebClientException ex) {
            throw new MicroserviceApiException("Error when calling Sandbox Service API to request cleanup for unit " + unitId, ex);
        }
    }
}
