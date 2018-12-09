package cz.muni.ics.kypo.training.facade;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.RoleDTO;
import cz.muni.ics.kypo.training.exception.CommonsFacadeException;
import cz.muni.ics.kypo.training.exceptions.CommonsServiceException;
import cz.muni.ics.kypo.training.facade.impl.RoleFacadeImpl;
import cz.muni.ics.kypo.training.mapping.RoleMapper;
import cz.muni.ics.kypo.training.persistence.model.IDMGroupRef;
import cz.muni.ics.kypo.training.persistence.model.Role;
import cz.muni.ics.kypo.training.service.RoleService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
public class RoleFacadeTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private RoleFacade roleFacade;
    @Mock
    private RoleService roleService;
    private Role role1, role2;
    private IDMGroupRef groupRef1;
    private RoleDTO roleDTO;
    private Predicate predicate;
    private Pageable pageable;


    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        roleFacade = new RoleFacadeImpl(roleService, Mappers.getMapper(RoleMapper.class));
        role1 = new Role();
        role1.setId(1L);
        role1.setRoleType("ADMINISTRATOR");

        role2 = new Role();
        role2.setId(2L);
        role2.setRoleType("GUEST");

        roleDTO = new RoleDTO();
        roleDTO.setId(1L);
        roleDTO.setRoleType("ADMINISTRATOR");

        pageable = PageRequest.of(0, 10);
    }

    @Test
    public void testGetById() {
        BDDMockito.given(roleService.getById(1L)).willReturn(role1);
        RoleDTO rDTO = roleFacade.getById(1L);

        assertEquals(roleDTO.getId(), rDTO.getId());
        assertEquals(roleDTO.getRoleType(), rDTO.getRoleType());
    }

    @Test
    public void testGetByIdWithServiceException() {
        BDDMockito.given(roleService.getById(ArgumentMatchers.anyLong())).willThrow(new CommonsServiceException());
        thrown.expect(CommonsFacadeException.class);
        roleFacade.getById(1L);
    }

    @Test
    public void testGetByRoleType() {
        BDDMockito.given(roleService.getByRoleType("ADMINISTRATOR")).willReturn(role1);
        RoleDTO rDTO = roleFacade.getByRoleType("ADMINISTRATOR");

        assertEquals(roleDTO.getId(), rDTO.getId());
        assertEquals(roleDTO.getRoleType(), rDTO.getRoleType());
    }

    @Test
    public void testGetByRoleTypeWithServiceException() {
        BDDMockito.given(roleService.getByRoleType("ADMINISTRATOR")).willThrow(new CommonsServiceException());
        thrown.expect(CommonsFacadeException.class);
        roleFacade.getByRoleType("ADMINISTRATOR");
    }

    @Test
    public void testGetAllRoles() {
        Page<Role> rolesPage = new PageImpl<Role>(Arrays.asList(role1));
        PageResultResource<RoleDTO> pages = new PageResultResource<>();
        pages.setContent(Arrays.asList(roleDTO));

        BDDMockito.given(roleService.getAllRoles(predicate, pageable)).willReturn(rolesPage);
        PageResultResource<RoleDTO> pageResultResource = roleFacade.getAllRoles(predicate, pageable);

        assertEquals(1, pageResultResource.getContent().size());
        assertEquals(roleDTO, pageResultResource.getContent().get(0));
    }

}
