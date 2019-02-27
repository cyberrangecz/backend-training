package cz.muni.ics.kypo.training.annotations.security;


import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Dominik Pilar
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAnyAuthority('ORGANIZER', 'ADMINISTRATOR')")
public @interface IsOrganizerOrAdmin {
}
