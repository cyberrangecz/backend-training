package cz.muni.ics.kypo.training.persistence.config;

import cz.muni.ics.kypo.training.persistence.model.Role;
import cz.muni.ics.kypo.training.persistence.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Component
@PropertySource("classpath:roles.properties")
public class StartUpRunner implements ApplicationRunner {
    private static final Logger LOG = LoggerFactory.getLogger(StartUpRunner.class);

    @Value("#{'${kypo.commons.roles}'.split(',')}")
    private Set<String> roles;

    private RoleRepository roleRepository;


    @Autowired
    public StartUpRunner(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        loadRoles(roles);

        LOG.info("Roles from external file were loaded and created in DB");
    }


    private void loadRoles(Set<String> roles) {
        roles.forEach(roleString -> {
            Optional<Role> optionalRole = roleRepository.findByRoleType(roleString.toUpperCase());
            if (!optionalRole.isPresent()) {
                Role role = new Role();
                role.setRoleType(roleString.toUpperCase());
                roleRepository.save(role);
                LOG.info("Role {} were added.", roleString);
            } else {
                LOG.info("Role {} already exists in DB", roleString);
            }
        });
    }

}
