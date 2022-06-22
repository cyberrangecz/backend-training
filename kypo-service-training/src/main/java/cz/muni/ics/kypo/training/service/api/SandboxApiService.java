package cz.muni.ics.kypo.training.service.api;

import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.responses.LockedPoolInfo;
import cz.muni.ics.kypo.training.api.responses.PageResultResource;
import cz.muni.ics.kypo.training.api.responses.PoolInfoDTO;
import cz.muni.ics.kypo.training.api.responses.SandboxDefinitionInfo;
import cz.muni.ics.kypo.training.api.responses.SandboxInfo;
import cz.muni.ics.kypo.training.exceptions.CustomWebClientException;
import cz.muni.ics.kypo.training.exceptions.ForbiddenException;
import cz.muni.ics.kypo.training.exceptions.MicroserviceApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

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
     * @return the locked pool info
     */
    public LockedPoolInfo lockPool(Long poolId) {
        try {
            return sandboxServiceWebClient
                    .post()
                    .uri("/pools/{poolId}/locks", poolId)
                    .body(Mono.just("{}"), String.class)
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

    public SandboxInfo getAndLockSandbox(Long poolId) {
        try {
            return sandboxServiceWebClient
                    .get()
                    .uri("/pools/{poolId}/sandboxes/get-and-lock", poolId)
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
     * Get APG variables defined in the sandbox definition of the pool.
     *
     * @param poolId the pool id
     * @return set of APG variables
     */
    public Set<String> getVariablesByPoolId(Long poolId) {
        try {
            return sandboxServiceWebClient
                    .get()
                    .uri("/pools/{poolId}/variables", poolId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Set<String>>() {})
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
}
