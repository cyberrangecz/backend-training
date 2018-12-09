package cz.muni.ics.kypo.training.facade;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.api.dto.RoleDTO;
import cz.muni.ics.kypo.training.exception.CommonsFacadeException;
import cz.muni.ics.kypo.training.exceptions.CommonsServiceException;
import cz.muni.ics.kypo.training.facade.impl.IDMGroupRefFacadeImpl;
import cz.muni.ics.kypo.training.mapping.IDMGroupRefMapper;
import cz.muni.ics.kypo.training.mapping.RoleMapper;
import cz.muni.ics.kypo.training.persistence.model.IDMGroupRef;
import cz.muni.ics.kypo.training.persistence.model.Role;
import cz.muni.ics.kypo.training.service.IDMGroupRefService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

@RunWith(SpringRunner.class)
public class IDMGroupRefFacadeTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private IDMGroupRefFacade groupRefFacade;

    @Mock
    private IDMGroupRefService groupRefService;

    private IDMGroupRef groupRef1, groupRef2;
    private Role role1, role2;
    private RoleDTO roleDTO;
    private Predicate predicate;
    private Pageable pageable;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        groupRefFacade = new IDMGroupRefFacadeImpl(groupRefService, Mappers.getMapper(IDMGroupRefMapper.class), Mappers.getMapper(RoleMapper.class));

        role1 = new Role();
        role1.setId(1L);
        role1.setRoleType("ADMINISTRATOR");

        role2 = new Role();
        role2.setId(2L);
        role2.setRoleType("GUEST");

        roleDTO = new RoleDTO();
        roleDTO.setId(1L);
        roleDTO.setRoleType("ADMINISTRATOR");

        groupRef1 = new IDMGroupRef();
        groupRef1.setId(1L);
        groupRef1.setIdmGroupId(1L);


        pageable = PageRequest.of(0, 10);

    }

    @Test
    public void testDeleteIDMGroupRef() {
        groupRefFacade.delete(1L);
        then(groupRefService).should().delete(1L);
    }

    @Test
    public void testAssignRoleToGroup() {
        groupRefFacade.assignRoleToGroup(1L, 1L);
        then(groupRefService).should().assignRoleToGroup(1L, 1L);
    }

    @Test
    public void testAssignRoleToGroupWithServiceException() {
        willThrow(CommonsServiceException.class).given(groupRefService).assignRoleToGroup(1L, 1L);
        thrown.expect(CommonsFacadeException.class);
        groupRefFacade.assignRoleToGroup(1L, 1L);
    }

    @Test
    public void testRemoveRoleFromGroup() {
        groupRefFacade.removeRoleFromGroup(1L, 1L);
        then(groupRefService).should().removeRoleFromGroup(1L, 1L);
    }

    @Test
    public void testRemoveRoleFromGroupWithServiceException() {
        willThrow(CommonsServiceException.class).given(groupRefService).removeRoleFromGroup(1L, 1L);
        thrown.expect(CommonsFacadeException.class);
        groupRefFacade.removeRoleFromGroup(1L, 1L);
    }


    @Test
    public void testGetRolesOfGroups() {
        given(groupRefService.getRolesOfGroups(Arrays.asList(1L, 2L))).willReturn(new HashSet<>(Arrays.asList(role1, role2)));
        Set<RoleDTO> roleDTOS = groupRefFacade.getRolesOfGroups(Arrays.asList(1L, 2L));
        Assert.assertTrue(roleDTOS.contains(roleDTO));
        Assert.assertEquals(2, roleDTOS.size());
    }


}
