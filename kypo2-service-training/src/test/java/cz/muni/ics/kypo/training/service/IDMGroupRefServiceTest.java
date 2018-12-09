package cz.muni.ics.kypo.training.service;

import cz.muni.ics.kypo.training.exceptions.CommonsServiceException;
import cz.muni.ics.kypo.training.persistence.model.IDMGroupRef;
import cz.muni.ics.kypo.training.persistence.model.Role;
import cz.muni.ics.kypo.training.persistence.repository.IDMGroupRefRepository;
import cz.muni.ics.kypo.training.persistence.repository.RoleRepository;
import cz.muni.ics.kypo.training.service.impl.IDMGroupRefServiceImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@RunWith(SpringJUnit4ClassRunner.class)
public class IDMGroupRefServiceTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private IDMGroupRefService groupRefService;

    @Mock
    private IDMGroupRefRepository groupRefRepository;

    @Mock
    private RoleRepository roleRepository;

    private IDMGroupRef groupRef1, groupRef2;
    private Role role1, role2;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        groupRefService = new IDMGroupRefServiceImpl(groupRefRepository, roleRepository);

        role1 = new Role();
        role1.setId(1L);
        role1.setRoleType("ADMINISTRATOR");

        role2 = new Role();
        role2.setId(2L);
        role2.setRoleType("GUEST");

        groupRef1 = new IDMGroupRef();
        groupRef1.setId(1L);
        groupRef1.setIdmGroupId(5L);

        groupRef2 = new IDMGroupRef();
        groupRef2.setId(2L);
        groupRef2.setIdmGroupId(2L);
    }

    @Test
    public void testDeleteIDMGroupRef() {
        given(groupRefRepository.findByIdmGroupId(1L)).willReturn(Optional.of(groupRef1));
        groupRefService.delete(1L);
        then(groupRefRepository).should().delete(groupRef1);
    }

    @Test
    public void testAssignRoleToGroup() {
        given(roleRepository.findById(ArgumentMatchers.anyLong())).willReturn(Optional.of(role1));
        given(groupRefRepository.findByIdmGroupId(ArgumentMatchers.anyLong())).willReturn(Optional.of(groupRef1));
        groupRef1.addRole(role1);
        groupRefService.assignRoleToGroup(role1.getId(), 1L);

        Assert.assertEquals(1, groupRef1.getRoles().size());
        Assert.assertTrue(groupRef1.getRoles().contains(role1));

    }

    @Test
    public void testAssignRoleToGroupWithRoleNotFound() {
        thrown.expect(CommonsServiceException.class);
        thrown.expectMessage("Input role with id " + role1.getId() + " cannot be found");
        given(roleRepository.findById(1L)).willReturn(Optional.empty());
        groupRefService.assignRoleToGroup(1L, 1L);

    }

    @Test
    public void testAssignRoleToGroupWithGroupRefNotFound() {
        given(roleRepository.findById(1L)).willReturn(Optional.of(role1));
        given(groupRefRepository.findByIdmGroupId(ArgumentMatchers.anyLong())).willReturn(Optional.empty());
        groupRefService.assignRoleToGroup(1L, 1L);

        then(groupRefRepository).should().save(any(IDMGroupRef.class));

    }

    @Test
    public void testRemoveRoleFromGroup() {
        given(roleRepository.findById(ArgumentMatchers.anyLong())).willReturn(Optional.of(role1));
        groupRef2.addRole(role2);
        given(groupRefRepository.findByIdmGroupId(ArgumentMatchers.anyLong())).willReturn(Optional.of(groupRef2));
        groupRefService.removeRoleFromGroup(1L, 2L);

        Assert.assertEquals(1, groupRef2.getRoles().size());
        Assert.assertEquals(role2, groupRef2.getRoles().iterator().next());
    }

    @Test
    public void testRemoveRoleFromGroupAndDelete() {
        given(roleRepository.findById(ArgumentMatchers.anyLong())).willReturn(Optional.of(role1));
        given(groupRefRepository.findByIdmGroupId(ArgumentMatchers.anyLong())).willReturn(Optional.of(groupRef2));
        groupRef2.removeRole(role1);
        groupRefService.removeRoleFromGroup(1L, 2L);

        then(groupRefRepository).should().delete(groupRef2);
    }

    @Test
    public void testRemoveRoleFromGroupWithRoleNotFound() {
        thrown.expect(CommonsServiceException.class);
        thrown.expectMessage("Input role with id " + role1.getId() + " cannot be found");
        given(roleRepository.findById(1L)).willReturn(Optional.empty());
        groupRefService.removeRoleFromGroup(1L, 1L);

    }

    @Test
    public void testRemoveRoleFromGroupWithGroupRefNotFound() {
        thrown.expectMessage("Idm group with id: " + 1L + " cannot be found.");
        thrown.expect(CommonsServiceException.class);
        given(roleRepository.findById(1L)).willReturn(Optional.of(role1));
        given(groupRefRepository.findByIdmGroupId(ArgumentMatchers.anyLong())).willReturn(Optional.empty());
        groupRefService.removeRoleFromGroup(1L, 1L);
    }

    @Test
    public void testGetRolesOfGroups() {
        groupRef1.addRole(role2);
        groupRef2.addRole(role1);
        given(groupRefRepository.findByIdmGroupId(1L)).willReturn(Optional.of(groupRef1));
        given(groupRefRepository.findByIdmGroupId(2L)).willReturn(Optional.of(groupRef2));
        Set<Role> roles = groupRefService.getRolesOfGroups(Arrays.asList(1L, 2L));

        Assert.assertTrue(roles.contains(role1));
        Assert.assertTrue(roles.contains(role2));
        Assert.assertTrue(roles.size() == 2);
    }

}
