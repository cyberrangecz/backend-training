package cz.muni.ics.kypo.training.service.impl;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.model.AssessmentLevel;
import cz.muni.ics.kypo.training.repository.AssessmentLevelRepository;
import cz.muni.ics.kypo.training.service.AssessmentLevelService;
import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;


import java.util.Optional;

@Service
public class AssessmentLevelServiceImpl implements AssessmentLevelService {


    private static Logger LOG = LoggerFactory.getLogger(AssessmentLevelServiceImpl.class.getName());

    private AssessmentLevelRepository assessmentLevelRepository;

    @Autowired
    public AssessmentLevelServiceImpl(AssessmentLevelRepository assessmentLevelRepository) {
        this.assessmentLevelRepository = assessmentLevelRepository;
    }

    @Override
    public Optional<AssessmentLevel> findById(long id) {
        LOG.debug("findById({})", id);
        try {
            return assessmentLevelRepository.findById(id);
        } catch (HibernateException ex) {
            throw new ServiceLayerException(ex.getLocalizedMessage());
        }
    }

    @Override
    public Page<AssessmentLevel> findAll(Predicate predicate, Pageable pageable) {
        LOG.debug("findAll({},{})", predicate, pageable);
        try {
            return assessmentLevelRepository.findAll(predicate, pageable);
        } catch (HibernateException ex) {
            throw new ServiceLayerException(ex.getLocalizedMessage());
        }
    }
}
