package cz.muni.ics.kypo.service.impl;

import cz.muni.ics.kypo.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.model.AssessmentLevel;
import cz.muni.ics.kypo.repository.AssessmentLevelRepository;
import cz.muni.ics.kypo.service.AssessmentLevelService;
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

    private static Logger log = LoggerFactory.getLogger(AssessmentLevelServiceImpl.class.getName());

    private AssessmentLevelRepository assessmentLevelRepository;

    @Autowired
    public AssessmentLevelServiceImpl(AssessmentLevelRepository assessmentLevelRepository) {
        this.assessmentLevelRepository = assessmentLevelRepository;
    }

    @Override
    public Optional<AssessmentLevel> findById(long id) {
        Assert.notNull(id, "Input id must not be null");
        try {
            log.info("Getting assessment level with id: " + id);
            return assessmentLevelRepository.findById(id);
        } catch (HibernateException ex) {
            log.info("Error while loading assessment level with id: " + id);
            throw new ServiceLayerException(ex.getLocalizedMessage());
        }
    }

    @Override
    public Page<AssessmentLevel> findAll(Pageable pageable) {
        try {
            return assessmentLevelRepository.findAll(pageable);
        } catch (HibernateException ex) {
            throw new ServiceLayerException(ex.getLocalizedMessage());
        }
    }

    @Override
    public Optional<AssessmentLevel> create(AssessmentLevel assessmentLevel)  {
        Assert.notNull(assessmentLevel, "Input assessment level must not be null");
        AssessmentLevel aL = assessmentLevelRepository.save(assessmentLevel);
        log.info("Assessment level with id: " + aL.getId() + " created.");
        return Optional.of(aL);
    }

    @Override
    public Optional<AssessmentLevel> update(AssessmentLevel assessmentLevel)  {
        Assert.notNull(assessmentLevel, "Input assessment level must not be null.");
        AssessmentLevel aL = assessmentLevelRepository.saveAndFlush(assessmentLevel);
        log.info("Assessment level with id: " + aL.getId() + "updated." );
        return Optional.of(aL);
    }

    @Override
    public void delete(AssessmentLevel assessmentLevel) {
        Assert.notNull(assessmentLevel, "Input assessment level must not be null.");
        assessmentLevelRepository.delete(assessmentLevel);
        log.info("Assessment level with id: "  + assessmentLevel.getId() + " was deleted.   ");
    }

}
