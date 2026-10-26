package cz.cyberrange.platform.training.persistence.config;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Appends the {@code id} property as the last sort key of every {@link Pageable} argument passed to
 * a repository method, so rows that tie on every requested key keep one total order across pages.
 */
final class PageableIdTiebreakerInterceptor implements MethodInterceptor {

  private static final String ID_PROPERTY = "id";

  @Override
  public Object invoke(MethodInvocation invocation) throws Throwable {
    Object[] arguments = invocation.getArguments();
    for (int index = 0; index < arguments.length; index++) {
      if (arguments[index] instanceof Pageable pageable) {
        arguments[index] = withIdTiebreaker(pageable);
      }
    }
    return invocation.proceed();
  }

  private static Pageable withIdTiebreaker(Pageable pageable) {
    if (pageable.isUnpaged() || pageable.getSort().getOrderFor(ID_PROPERTY) != null) {
      return pageable;
    }
    return PageRequest.of(
        pageable.getPageNumber(),
        pageable.getPageSize(),
        pageable.getSort().and(Sort.by(ID_PROPERTY)));
  }
}
