package cz.muni.ics.kypo.training.facade.impl;

import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.types.Predicate;

import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.TrainingRunDTO;
import cz.muni.ics.kypo.training.exception.FacadeLayerException;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.facade.TrainingRunFacade;
import cz.muni.ics.kypo.training.mapping.BeanMapping;
import cz.muni.ics.kypo.training.model.TrainingRun;
import cz.muni.ics.kypo.training.service.TrainingRunService;

/**
 * @author Pavel Šeda
 *
 */
@Service
@Transactional
public class TrainingRunFacadeImpl implements TrainingRunFacade {

  private static final Logger LOG = LoggerFactory.getLogger(TrainingRunFacadeImpl.class);

  private TrainingRunService trainingRunService;
  private BeanMapping beanMapping;

  @Autowired
  public TrainingRunFacadeImpl(TrainingRunService trainingRunService, BeanMapping beanMapping) {
    this.trainingRunService = trainingRunService;
    this.beanMapping = beanMapping;
  }

  @Override
  @Transactional(readOnly = true)
  public TrainingRunDTO findById(long id) {
    LOG.debug("findById({})", id);
    try {
      Objects.requireNonNull(id);
      Optional<TrainingRun> trainingRun = trainingRunService.findById(id);
      TrainingRun tr = trainingRun.orElseThrow(() -> new ServiceLayerException("TrainingRun with this id is not found"));
      return beanMapping.mapTo(tr, TrainingRunDTO.class);
    } catch (NullPointerException ex) {
      throw new FacadeLayerException("Given TrainingRun ID is null.");
    } catch (ServiceLayerException ex) {
      throw new FacadeLayerException(ex.getLocalizedMessage());
    }
  }

  @Override
  @Transactional(readOnly = true)
  public PageResultResource<TrainingRunDTO> findAll(Predicate predicate, Pageable pageable) {
    LOG.debug("findAll({},{})", predicate, pageable);
    try {
      return beanMapping.mapToPageResultDTO(trainingRunService.findAll(predicate, pageable), TrainingRunDTO.class);
    } catch (ServiceLayerException ex) {
      throw new FacadeLayerException(ex.getLocalizedMessage());
    }
  }

}
