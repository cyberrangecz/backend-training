package cz.muni.ics.kypo.facade.impl;

import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.types.Predicate;

import cz.muni.ics.kypo.dto.TrainingRunDTO;
import cz.muni.ics.kypo.exception.FacadeLayerException;
import cz.muni.ics.kypo.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.facade.TrainingRunFacade;
import cz.muni.ics.kypo.mapping.BeanMapping;
import cz.muni.ics.kypo.model.TrainingRun;
import cz.muni.ics.kypo.service.TrainingRunService;

/**
 * @author Pavel Šeda
 *
 */
@Service
@Transactional
public class TrainingRunFacadeImpl implements TrainingRunFacade {

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
  public Page<TrainingRunDTO> findAll(Predicate predicate, Pageable pageable) {
    try {
      return beanMapping.mapTo(trainingRunService.findAll(predicate, pageable), TrainingRunDTO.class);
    } catch (ServiceLayerException ex) {
      throw new FacadeLayerException(ex.getLocalizedMessage());
    }
  }

}
