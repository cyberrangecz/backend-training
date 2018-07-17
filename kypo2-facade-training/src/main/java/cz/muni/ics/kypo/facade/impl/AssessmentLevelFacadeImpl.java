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

  @Autowired
  public AssessmentLevelFacadeImpl(AssessmentLevelService assessmentLevelService, BeanMapping beanMapping) {
    this.assessmentLevelService = assessmentLevelService;
    this.beanMapping = beanMapping;
  }

  @Override
  @Transactional(readOnly = true)
  public AssessmentLevelDTO findById(long id) {
    LOG.debug("findById({})", id);
    try {
      Objects.requireNonNull(id);
      Optional<AssessmentLevel> assessmentLevel = assessmentLevelService.findById(id);
      AssessmentLevel al = assessmentLevel.orElseThrow(() -> new ServiceLayerException("AssessmentLevel with this id is not found"));
      return beanMapping.mapTo(al, AssessmentLevelDTO.class);
    } catch (NullPointerException ex) {
      throw new FacadeLayerException("Given AssessmentLevel ID is null.");
    } catch (ServiceLayerException ex) {
      throw new FacadeLayerException(ex.getLocalizedMessage());
    }
  }

  @Override
  @Transactional(readOnly = true)
  public PageResultResource<AssessmentLevelDTO> findAll(Predicate predicate, Pageable pageable) {
    LOG.debug("findAll({},{})", predicate, pageable);
    try {
      return beanMapping.mapToPageResultDTO(assessmentLevelService.findAll(predicate, pageable), AssessmentLevelDTO.class);
    } catch (ServiceLayerException ex) {
      throw new FacadeLayerException(ex.getLocalizedMessage());
    }
  }

}
