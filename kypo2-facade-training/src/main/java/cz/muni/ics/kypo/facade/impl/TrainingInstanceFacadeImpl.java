package cz.muni.ics.kypo.facade.impl;

import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.types.Predicate;

import cz.muni.ics.kypo.dto.TrainingInstanceDTO;
import cz.muni.ics.kypo.exception.FacadeLayerException;
import cz.muni.ics.kypo.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.facade.TrainingInstanceFacade;
import cz.muni.ics.kypo.mapping.BeanMapping;
import cz.muni.ics.kypo.model.TrainingInstance;
import cz.muni.ics.kypo.service.TrainingInstanceService;

/**
 * @author Pavel Šeda
 *
 */
@Service
@Transactional
public class TrainingInstanceFacadeImpl implements TrainingInstanceFacade {

  private TrainingInstanceService trainingInstanceService;
  private BeanMapping beanMapping;

  @Autowired
  public TrainingInstanceFacadeImpl(TrainingInstanceService trainingInstanceService, BeanMapping beanMapping) {
    this.trainingInstanceService = trainingInstanceService;
    this.beanMapping = beanMapping;
  }

  @Override
  @Transactional(readOnly = true)
  public TrainingInstanceDTO findById(long id) {
    try {
      Objects.requireNonNull(id);
      Optional<TrainingInstance> trainingInstance = trainingInstanceService.findById(id);
      TrainingInstance ti = trainingInstance.orElseThrow(() -> new ServiceLayerException("TrainingInstance with this id is not found"));
      return beanMapping.mapTo(ti, TrainingInstanceDTO.class);
    } catch (NullPointerException ex) {
      throw new FacadeLayerException("Given TrainingInstance ID is null.");
    } catch (ServiceLayerException ex) {
      throw new FacadeLayerException(ex.getLocalizedMessage());
    }
  }

  @Override
  @Transactional(readOnly = true)
  public Page<TrainingInstanceDTO> findAll(Predicate predicate, Pageable pageable) {
    try {
      return beanMapping.mapTo(trainingInstanceService.findAll(predicate, pageable), TrainingInstanceDTO.class);
    } catch (ServiceLayerException ex) {
      throw new FacadeLayerException(ex.getLocalizedMessage());
    }
  }

}
