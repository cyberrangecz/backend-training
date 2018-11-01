package cz.muni.ics.kypo.training.facade.impl;

import java.util.Objects;

import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceCreateResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceCreateDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceUpdateDTO;
import cz.muni.ics.kypo.training.exception.FacadeLayerException;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.facade.TrainingInstanceFacade;
import cz.muni.ics.kypo.training.mapping.BeanMapping;
import cz.muni.ics.kypo.training.persistence.model.TrainingInstance;
import cz.muni.ics.kypo.training.service.TrainingInstanceService;

/**
 * @author Pavel Šeda
 *
 */
@Service
@Transactional
public class TrainingInstanceFacadeImpl implements TrainingInstanceFacade {

	private static final Logger LOG = LoggerFactory.getLogger(TrainingInstanceFacadeImpl.class);

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
    LOG.debug("findById({})", id);
    try {
      Objects.requireNonNull(id);
      return beanMapping.mapTo(trainingInstanceService.findById(id), TrainingInstanceDTO.class);
    } catch (ServiceLayerException ex) {
      throw new FacadeLayerException(ex);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public PageResultResource<TrainingInstanceDTO> findAll(Predicate predicate, Pageable pageable) {
    LOG.debug("findAll({},{})", predicate, pageable);
    return beanMapping.mapToPageResultDTO(trainingInstanceService.findAll(predicate, pageable), TrainingInstanceDTO.class);
  }

  @Override
  @Transactional
  public String update(TrainingInstanceUpdateDTO trainingInstance) {
    LOG.debug("update({})",trainingInstance);
    try{
      Objects.requireNonNull(trainingInstance);
      TrainingInstance updatedTrainingInstance = beanMapping.mapTo(trainingInstance, TrainingInstance.class);
			trainingInstanceService.update(updatedTrainingInstance);
			if(!trainingInstance.getKeyword().isEmpty()){
				return trainingInstanceService.generatePassword(updatedTrainingInstance, trainingInstance.getKeyword());
			}
			return null;
		} catch (ServiceLayerException ex){
			throw new FacadeLayerException(ex);
    }
  }

  @Override
  @Transactional
  public TrainingInstanceCreateResponseDTO create(TrainingInstanceCreateDTO trainingInstance) {
    LOG.debug("create({})", trainingInstance);
    try{
      Objects.requireNonNull(trainingInstance);
      TrainingInstance newTI = trainingInstanceService.create(beanMapping.mapTo(trainingInstance, TrainingInstance.class));
      TrainingInstanceCreateResponseDTO newTIDTO = beanMapping.mapTo(newTI, TrainingInstanceCreateResponseDTO.class);
      String newKeyword = trainingInstanceService.generatePassword(newTI, trainingInstance.getKeyword());
      newTIDTO.setKeyword(newKeyword);
      return newTIDTO;
    } catch(ServiceLayerException ex) {
      throw new FacadeLayerException(ex);
    }
  }

  @Override
  @Transactional
  public void delete(Long id) {
    try {
      Objects.requireNonNull(id);
      trainingInstanceService.delete(id);
    } catch(ServiceLayerException ex) {
      throw new FacadeLayerException(ex);
    }
  }

	@Override public ResponseEntity<Void> allocateSandboxes(Long instanceId) {
		LOG.debug("allocateSandboxes({})", instanceId);
		try{
				return trainingInstanceService.allocateSandboxes(instanceId);
		} catch( ServiceLayerException ex){
				throw new FacadeLayerException(ex.getLocalizedMessage());
		}
	}
}
