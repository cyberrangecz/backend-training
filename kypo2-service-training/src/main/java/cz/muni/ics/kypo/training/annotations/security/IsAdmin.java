package cz.muni.ics.kypo.training.annotations.security;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The custom annotation <i>@IsAdmin<i/>. All methods annotated with this annotation expect the user has a role <strong>ROLE_TRAINING_ADMINISTRATOR<strong/>.
 *
 * @author Dominik Pilar
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)")
public @interface IsAdmin {
}
