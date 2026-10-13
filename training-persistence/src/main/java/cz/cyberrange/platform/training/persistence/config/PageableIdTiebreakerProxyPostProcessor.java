package cz.cyberrange.platform.training.persistence.config;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.support.RepositoryProxyPostProcessor;

/** Adds the {@link PageableIdTiebreakerInterceptor} to every repository proxy. */
final class PageableIdTiebreakerProxyPostProcessor implements RepositoryProxyPostProcessor {

  @Override
  public void postProcess(ProxyFactory factory, RepositoryInformation repositoryInformation) {
    factory.addAdvice(new PageableIdTiebreakerInterceptor());
  }
}
