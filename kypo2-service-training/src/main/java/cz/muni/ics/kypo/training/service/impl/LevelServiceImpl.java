package cz.muni.ics.kypo.training.service.impl;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.model.AbstractLevel;
import cz.muni.ics.kypo.training.repository.AbstractLevelRepository;
import cz.muni.ics.kypo.training.service.LevelService;
import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 *
 * @author Boris Jaduš
 *
 */
@Service
public class LevelServiceImpl implements LevelService {

		private static final Logger LOG = LoggerFactory.getLogger(LevelServiceImpl.class);

		private AbstractLevelRepository abstractLevelRepository;

		@Autowired
		public LevelServiceImpl(AbstractLevelRepository abstractLevelRepository) {
				this.abstractLevelRepository = abstractLevelRepository;
		}

		@Override
		public Optional<AbstractLevel> findById(long id) {
				LOG.debug("findById({})", id);
				try {
						return abstractLevelRepository.findById(id);
				} catch (HibernateException ex){
						throw new ServiceLayerException(ex.getLocalizedMessage());
				}
		}

		@Override
		public Page<AbstractLevel> findAll(Predicate predicate, Pageable pageable) {
				LOG.debug("findAll({}, {})", predicate, pageable);
				try {
						return abstractLevelRepository.findAll(predicate, pageable);
				} catch (HibernateException ex){
						throw new ServiceLayerException(ex.getLocalizedMessage());
				}
		}
}
