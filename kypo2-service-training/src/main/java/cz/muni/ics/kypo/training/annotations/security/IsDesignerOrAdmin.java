package cz.muni.ics.kypo.training.annotations.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.security.access.prepost.PreAuthorize;

/**
 * The custom annotation <i>@IsDesignerOrAdmin<i/>. All methods annotated with this annotation expect the user has a role <strong>ROLE_TRAINING_ADMINISTRATOR<strong/>
 * or <strong>ROLE_TRAINING_DESIGNER<strong/>.
 *
 * @author Pavel Seda
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAnyAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR, " +
        "T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_DESIGNER)")
public @interface IsDesignerOrAdmin {
}
