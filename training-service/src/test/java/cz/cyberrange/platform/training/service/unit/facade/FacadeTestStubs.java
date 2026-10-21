package cz.cyberrange.platform.training.service.unit.facade;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willAnswer;

import cz.cyberrange.platform.training.api.dto.UserRefDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.InstanceCountHolder;
import cz.cyberrange.platform.training.service.services.TrainingDefinitionService;
import cz.cyberrange.platform.training.service.services.UserService;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Stubs of service calls shared by the facade unit tests. */
final class FacadeTestStubs {

  private FacadeTestStubs() {}

  /**
   * Makes the mocked service set on each holder the instance count given for its definition, or
   * zero for a definition not listed.
   *
   * @param trainingDefinitionService the mocked service to stub
   * @param instanceCountsByDefinitionId instance count of each listed definition id
   */
  static void stubInstanceCounts(
      TrainingDefinitionService trainingDefinitionService,
      Map<Long, Long> instanceCountsByDefinitionId) {
    willAnswer(
            invocation -> {
              Collection<InstanceCountHolder> holders = invocation.getArgument(0);
              holders.forEach(
                  holder ->
                      holder.setInstanceCount(
                          instanceCountsByDefinitionId.getOrDefault(holder.getId(), 0L)));
              return null;
            })
        .given(trainingDefinitionService)
        .completeInstanceCounts(any());
  }

  /**
   * Makes the mocked service answer the given users to a full lookup of exactly the given user
   * reference ids, in any order.
   *
   * @param userService the mocked service to stub
   * @param userRefIds the user reference ids the lookup asks for
   * @param users the users the lookup answers
   */
  static void stubUserLookup(
      UserService userService, Collection<Long> userRefIds, List<UserRefDTO> users) {
    Set<Long> expectedUserRefIds = Set.copyOf(userRefIds);
    given(
            userService.getUsersRefDTOByGivenUserIds(
                argThat(
                    (List<Long> requestedIds) ->
                        requestedIds != null
                            && new HashSet<>(requestedIds).equals(expectedUserRefIds))))
        .willReturn(users);
  }
}
