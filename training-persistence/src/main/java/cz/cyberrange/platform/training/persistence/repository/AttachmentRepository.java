package cz.cyberrange.platform.training.persistence.repository;

import cz.cyberrange.platform.training.persistence.model.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

/** The JPA repository interface to manage {@link Attachment} instances */
@Repository
public interface AttachmentRepository
    extends JpaRepository<Attachment, Long>, QuerydslPredicateExecutor<Attachment> {}
