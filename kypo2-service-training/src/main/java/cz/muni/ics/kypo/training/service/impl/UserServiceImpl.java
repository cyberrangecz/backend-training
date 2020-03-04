package cz.muni.ics.kypo.training.service.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import cz.muni.ics.kypo.training.annotations.transactions.TransactionalWO;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.enums.RoleType;
import cz.muni.ics.kypo.training.api.responses.PageResultResource;
import cz.muni.ics.kypo.training.exceptions.*;
import cz.muni.ics.kypo.training.exceptions.errors.JavaApiError;
import cz.muni.ics.kypo.training.persistence.model.UserRef;
import cz.muni.ics.kypo.training.persistence.repository.UserRefRepository;
import cz.muni.ics.kypo.training.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.*;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);

    @Value("${user-and-group-server.uri}")
    private String userAndGroupURI;
    private RestTemplate restTemplate;
    private UserRefRepository userRefRepository;

    public UserServiceImpl(RestTemplate restTemplate, UserRefRepository userRefRepository) {
        this.restTemplate = restTemplate;
        this.userRefRepository = userRefRepository;
    }

    @Override
    public UserRef getUserByUserRefId(Long userRefId) {
        Objects.requireNonNull(userRefId, "UserRef ID must not be null.");
        return userRefRepository.findUserByUserRefId(userRefId).orElseThrow(
                () -> new EntityNotFoundException(new EntityErrorDetail(UserRef.class, "id", userRefId.getClass(), userRefId, "UserRef not found.")));
    }

    @Override
    public UserRefDTO getUserRefDTOByUserRefId(Long id) {
        Objects.requireNonNull(id, "UserRef ID must not be null.");
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        try {
            ResponseEntity<UserRefDTO> userInfoResponseEntity = restTemplate.exchange(userAndGroupURI + "/users/" + id, HttpMethod.GET, new HttpEntity<>(httpHeaders), UserRefDTO.class);
            return userInfoResponseEntity.getBody();
        } catch (HttpClientErrorException ex) {
            try {
                throw new MicroserviceApiException("Error when calling UserAndGroup API to obtain info about user(ID: " + id + ")", convertJsonBytesToObject(ex.getResponseBodyAsString(), JavaApiError.class));
            } catch (IOException ex1) {
                throw new InternalServerErrorException("Unable to parse ApiError when calling UserAndGroup API");
            }
        }
    }

    @Override
    public PageResultResource<UserRefDTO> getUsersRefDTOByGivenUserIds(Set<Long> userRefIds, Pageable pageable, String givenName, String familyName) {
        Objects.requireNonNull(userRefIds, "UserRef IDs must not be null.");
        if(userRefIds.isEmpty()) {
            return new PageResultResource<>(Collections.emptyList(), new PageResultResource.Pagination(0, 0, pageable.getPageSize(), 0, 0 ));
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(userAndGroupURI + "/users/ids");
        if(givenName != null) {
            builder.queryParam("givenName", givenName);
        }
        if(familyName != null) {
            builder.queryParam("familyName", familyName);
        }
        builder.queryParam("ids", StringUtils.collectionToDelimitedString(userRefIds, ","));
        builder.queryParam("page", pageable.getPageNumber());
        builder.queryParam("size", pageable.getPageSize());
        URI uri = builder.build().encode().toUri();
        try {
            ResponseEntity<PageResultResource<UserRefDTO>> usersResponse = restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(httpHeaders),
                    new ParameterizedTypeReference<PageResultResource<UserRefDTO>>() {
                    });
            return (Objects.requireNonNull(usersResponse.getBody()));
        } catch (HttpClientErrorException ex) {
            try {
                throw new MicroserviceApiException("Error when calling UserAndGroup API to obtain users by IDs: " + userRefIds, convertJsonBytesToObject(ex.getResponseBodyAsString(), JavaApiError.class));
            } catch (IOException ex1) {
                throw new InternalServerErrorException("Unable to parse ApiError when calling UserAndGroup API");
            }
        }
    }

    @Override
    public PageResultResource<UserRefDTO> getUsersByGivenRole(RoleType roleType, Pageable pageable, String givenName, String familyName) {
        Objects.requireNonNull(roleType, "Role type must not be null.");
        HttpHeaders httpHeaders = new HttpHeaders();
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(userAndGroupURI + "/roles/users");
        if(givenName != null) {
            builder.queryParam("givenName", givenName);
        }
        if(familyName != null) {
            builder.queryParam("familyName", familyName);
        }
        builder.queryParam("roleType", roleType.name());
        builder.queryParam("page", pageable.getPageNumber());
        builder.queryParam("size", pageable.getPageSize());
        URI uri = builder.build().encode().toUri();
        try {
            ResponseEntity<PageResultResource<UserRefDTO>> usersResponse = restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(httpHeaders),
                    new ParameterizedTypeReference<PageResultResource<UserRefDTO>>() {
                    });
            return Objects.requireNonNull(usersResponse.getBody());

        } catch (HttpClientErrorException ex) {
            try {
                throw new MicroserviceApiException("Error when calling UserAndGroup API to obtain users with role " + roleType.name(), convertJsonBytesToObject(ex.getResponseBodyAsString(), JavaApiError.class));
            } catch (IOException ex1) {
                throw new InternalServerErrorException("Unable to parse ApiError when calling UserAndGroup API");
            }
        }
    }

    @Override
    public PageResultResource<UserRefDTO> getUsersByGivenRoleAndNotWithGivenIds(RoleType roleType, Set<Long> userRefIds, Pageable pageable, String givenName, String familyName) {
        Objects.requireNonNull(roleType, "Role type must not be null.");
        Objects.requireNonNull(userRefIds, "UserRef IDs must not be null.");
        HttpHeaders httpHeaders = new HttpHeaders();
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(userAndGroupURI + "/roles/users-not-with-ids");
        if(givenName != null) {
            builder.queryParam("givenName", givenName);
        }
        if(familyName != null) {
            builder.queryParam("familyName", familyName);
        }
        builder.queryParam("roleType", roleType.name());
        builder.queryParam("ids", StringUtils.collectionToDelimitedString(userRefIds, ","));
        builder.queryParam("page", pageable.getPageNumber());
        builder.queryParam("size", pageable.getPageSize());
        URI uri = builder.build().encode().toUri();
        try {
            ResponseEntity<PageResultResource<UserRefDTO>> usersResponse = restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(httpHeaders),
                    new ParameterizedTypeReference<PageResultResource<UserRefDTO>>() {
                    });
            return Objects.requireNonNull(usersResponse.getBody());

        } catch (HttpClientErrorException ex) {
            try {
                throw new MicroserviceApiException("Error when calling UserAndGroup API to obtain users with role " + roleType.name() + " and IDs:", convertJsonBytesToObject(ex.getResponseBodyAsString(), JavaApiError.class));
            } catch (IOException ex1) {
                throw new InternalServerErrorException("Unable to parse ApiError when calling UserAndGroup API");
            }
        }
    }

    @Override
    @TransactionalWO
    public UserRef createUserRef(UserRef userRefToCreate) {
        Objects.requireNonNull(userRefToCreate, "UserRef must not be null.");
        UserRef userRef = userRefRepository.save(userRefToCreate);
        LOG.info("User ref with user_ref_id: {} created.", userRef.getUserRefId());
        return userRef;
    }

    private static <T> T convertJsonBytesToObject(String object, Class<T> objectClass) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.registerModule( new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper.readValue(object, objectClass);
    }
}
