package cz.muni.ics.kypo.facade.impl;

import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.types.Predicate;

import cz.muni.ics.kypo.api.PageResultResource;
import cz.muni.ics.kypo.api.dto.AssessmentLevelDTO;
import cz.muni.ics.kypo.exception.FacadeLayerException;
import cz.muni.ics.kypo.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.facade.AssessmentLevelFacade;
import cz.muni.ics.kypo.mapping.BeanMapping;
import cz.muni.ics.kypo.model.AssessmentLevel;
import cz.muni.ics.kypo.service.AssessmentLevelService;
import org.springframework.util.Assert;

import javax.validation.constraints.Null;

/**
 * @author Pavel Šeda
 *
 */
@Service
@Transactional
public class AssessmentLevelFacadeImpl implements AssessmentLevelFacade {

  private static final Logger LOG = LoggerFactory.getLogger(AssessmentLevelFacadeImpl.class);

  private AssessmentLevelService assessmentLevelService;
  private BeanMapping beanMapping;
  private Logger log = LoggerFactory.getLogger(AssessmentLevelFacadeImpl.class);


  @Autowired
  public AssessmentLevelFacadeImpl(AssessmentLevelService assessmentLevelService, BeanMapping beanMapping) {
    this.assessmentLevelService = assessmentLevelService;
    this.beanMapping = beanMapping;
  }

  @Override
  @Transactional(readOnly = true)

  public AssessmentLevelDTO findById(Long id) throws FacadeLayerException {

    try {
      Objects.requireNonNull(id);
      Optional<AssessmentLevel> assessmentLevel = assessmentLevelService.findById(id);
      AssessmentLevel al = assessmentLevel.orElseThrow(() -> new ServiceLayerException("AssessmentLevel with this id is not found"));
      return beanMapping.mapTo(al, AssessmentLevelDTO.class);
    } catch (NullPointerException ex) {
      throw new FacadeLayerException();
    } catch (ServiceLayerException ex) {
      throw new FacadeLayerException(ex.getLocalizedMessage());
    }
  }

  @Override
  @Transactional(readOnly = true)

  public PageResultResource<AssessmentLevelDTO> findAll(Pageable pageable) {
    try {
      return beanMapping.mapToPageResultDTO(assessmentLevelService.findAll(pageable), AssessmentLevelDTO.class);
    } catch (ServiceLayerException ex) {
      throw new FacadeLayerException(ex.getLocalizedMessage());
    }
  }

  @Override
  @Transactional
  public AssessmentLevelDTO create(AssessmentLevel al) {
    try {
      Objects.requireNonNull(al);
      Optional<AssessmentLevel> assessmentLevel = assessmentLevelService.create(al);
      AssessmentLevel createdAL = assessmentLevel.orElseThrow(() -> new ServiceLayerException());
      return beanMapping.mapTo(createdAL, AssessmentLevelDTO.class);
    }catch (NullPointerException| ServiceLayerException ex ) {
      throw new FacadeLayerException(ex.getLocalizedMessage());
    }
  }

  @Override
  @Transactional
  public AssessmentLevelDTO update(AssessmentLevel al) {
    try {
        Objects.requireNonNull(al);
        Optional<AssessmentLevel> assessmentLevel = assessmentLevelService.update(al);
        AssessmentLevel updatedAL = assessmentLevel.orElseThrow(() -> new ServiceLayerException());
        return beanMapping.mapTo(updatedAL, AssessmentLevelDTO.class);
    } catch (NullPointerException|ServiceLayerException ex) {
        throw new FacadeLayerException(ex.getLocalizedMessage());
    }
  }

  @Override
  @Transactional
  public void delete(Long id) {
      Optional<AssessmentLevel> assessmentLevel = null;
      try {
          Objects.requireNonNull(id);
          assessmentLevel = assessmentLevelService.findById(id);
          AssessmentLevel al = assessmentLevel.orElseThrow(() -> new ServiceLayerException("AssessmentLevel with this id is not found"));
          assessmentLevelService.delete(al);
    } catch (NullPointerException ex) {
          throw new FacadeLayerException("Assessment level with null id cannot be deleted.");
      } catch (ServiceLayerException ex) {
          throw new FacadeLayerException(ex.getLocalizedMessage());
      }
  }
}
