package cz.muni.ics.kypo.facade.impl;

import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.types.Predicate;

import cz.muni.ics.kypo.api.PageResultResource;
import cz.muni.ics.kypo.api.dto.TrainingDefinitionDTO;
import cz.muni.ics.kypo.exception.FacadeLayerException;
import cz.muni.ics.kypo.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.facade.TrainingDefinitionFacade;
import cz.muni.ics.kypo.mapping.BeanMapping;
import cz.muni.ics.kypo.model.TrainingDefinition;
import cz.muni.ics.kypo.service.TrainingDefinitionService;

/**
 * @author Pavel Šeda
 *
 */
@Service
@Transactional
public class TrainingDefinitionFacadeImpl implements TrainingDefinitionFacade {

  private TrainingDefinitionService trainingDefinitionService;
  private BeanMapping beanMapping;

  @Autowired
  public TrainingDefinitionFacadeImpl(TrainingDefinitionService trainingDefinitionService, BeanMapping beanMapping) {
    this.trainingDefinitionService = trainingDefinitionService;
    this.beanMapping = beanMapping;
  }

  @Override
  @Transactional(readOnly = true)
  public TrainingDefinitionDTO findById(long id) {
    try {
      Objects.requireNonNull(id);
      Optional<TrainingDefinition> trainingDef = trainingDefinitionService.findById(id);
      TrainingDefinition td = trainingDef.orElseThrow(() -> new ServiceLayerException("TrainingDefinition with this id is not found"));
      return beanMapping.mapTo(td, TrainingDefinitionDTO.class);
    } catch (NullPointerException ex) {
      throw new FacadeLayerException("Given TrainingDefinition ID is null.");
    } catch (ServiceLayerException ex) {
      throw new FacadeLayerException(ex.getLocalizedMessage());
    }
  }

  @Override
  @Transactional(readOnly = true)
  public PageResultResource<TrainingDefinitionDTO> findAll(Predicate predicate, Pageable pageable) {
    try {
      return beanMapping.mapToPageResultDTO(trainingDefinitionService.findAll(predicate, pageable), TrainingDefinitionDTO.class);
    } catch (ServiceLayerException ex) {
      throw new FacadeLayerException(ex.getLocalizedMessage());
    }
  }

}
