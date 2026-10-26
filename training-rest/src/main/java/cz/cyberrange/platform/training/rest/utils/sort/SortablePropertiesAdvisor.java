package cz.cyberrange.platform.training.rest.utils.sort;

import cz.cyberrange.platform.training.service.facade.TrainingRunFacade;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Stream;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Role;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.authorization.method.AuthorizationInterceptorsOrder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerMapping;

/**
 * Rejects a call to a facade method whose {@link Pageable} sorts by a property outside the list
 * that {@link SortableProperties} declares on the controller method serving the current request.
 * The rejection is a {@link ResponseStatusException} with status {@code 400 Bad Request}.
 *
 * <p>The check runs right after the method security of the facade, so a caller who is not
 * authorized is refused before the sort is examined. Calls made outside a request, or for a
 * controller method without {@link SortableProperties}, pass unchecked.
 */
@Component
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class SortablePropertiesAdvisor extends StaticMethodMatcherPointcutAdvisor {

  private static final String FACADE_PACKAGE = TrainingRunFacade.class.getPackageName();

  /** Creates the advisor, ordered directly after the {@code @PreAuthorize} interceptor. */
  public SortablePropertiesAdvisor() {
    setOrder(AuthorizationInterceptorsOrder.PRE_AUTHORIZE.getOrder() + 1);
    setAdvice((MethodInterceptor) SortablePropertiesAdvisor::rejectDisallowedSort);
  }

  @Override
  public boolean matches(Method method, Class<?> targetClass) {
    return method.getDeclaringClass().getPackageName().startsWith(FACADE_PACKAGE)
        && Stream.of(method.getParameterTypes()).anyMatch(Pageable.class::isAssignableFrom);
  }

  private static Object rejectDisallowedSort(MethodInvocation invocation) throws Throwable {
    SortableProperties sortableProperties = sortablePropertiesOfCurrentRequest();
    if (sortableProperties != null) {
      List<String> allowedProperties = sortableProperties.value().properties();
      Stream.of(invocation.getArguments())
          .filter(Pageable.class::isInstance)
          .map(argument -> ((Pageable) argument).getSort())
          .forEach(sort -> rejectDisallowedProperties(sort, allowedProperties));
    }
    return invocation.proceed();
  }

  private static SortableProperties sortablePropertiesOfCurrentRequest() {
    RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
    if (requestAttributes == null) {
      return null;
    }
    Object handler =
        requestAttributes.getAttribute(
            HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
    return handler instanceof HandlerMethod handlerMethod
        ? handlerMethod.getMethodAnnotation(SortableProperties.class)
        : null;
  }

  private static void rejectDisallowedProperties(Sort sort, List<String> allowedProperties) {
    List<String> disallowedProperties =
        sort.stream()
            .map(Sort.Order::getProperty)
            .filter(property -> !allowedProperties.contains(property))
            .toList();
    if (!disallowedProperties.isEmpty()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Cannot sort by %s; the sortable properties are %s."
              .formatted(
                  String.join(", ", disallowedProperties),
                  String.join(", ", allowedProperties.stream().sorted().toList())));
    }
  }
}
