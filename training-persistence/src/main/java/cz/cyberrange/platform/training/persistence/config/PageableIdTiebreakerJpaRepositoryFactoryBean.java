package cz.cyberrange.platform.training.persistence.config;

import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

/**
 * Repository factory bean whose repositories append the {@code id} property to the sort of every
 * {@link org.springframework.data.domain.Pageable} they receive.
 *
 * @param <R> the repository type
 * @param <T> the entity type
 * @param <I> the entity id type
 */
public class PageableIdTiebreakerJpaRepositoryFactoryBean<R extends Repository<T, I>, T, I>
    extends JpaRepositoryFactoryBean<R, T, I> {

  /**
   * Creates the factory bean for the given repository interface.
   *
   * @param repositoryInterface the repository interface to create a factory for
   */
  public PageableIdTiebreakerJpaRepositoryFactoryBean(Class<? extends R> repositoryInterface) {
    super(repositoryInterface);
  }

  @Override
  protected RepositoryFactorySupport createRepositoryFactory(EntityManager entityManager) {
    RepositoryFactorySupport factory = super.createRepositoryFactory(entityManager);
    factory.addRepositoryProxyPostProcessor(new PageableIdTiebreakerProxyPostProcessor());
    return factory;
  }
}
