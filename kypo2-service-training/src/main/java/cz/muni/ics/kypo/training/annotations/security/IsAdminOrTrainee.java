package cz.muni.ics.kypo.training.annotations.security;


import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The custom annotation <i>@IsAdminOrTrainee<i/>. All methods annotated with this annotation expect the user has a role <strong>ROLE_TRAINING_ADMINISTRATOR<strong/>
 * or <strong>ROLE_TRAINING_ORGANIZER<strong/> or <strong>ROLE_TRAINING_TRAINEE<strong/>.
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAnyAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR, " +
        "T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_TRAINEE)")
public @interface IsAdminOrTrainee {
}
