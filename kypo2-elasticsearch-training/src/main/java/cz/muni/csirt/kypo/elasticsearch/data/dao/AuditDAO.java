package cz.muni.csirt.kypo.elasticsearch.data.dao;

import java.io.IOException;

import cz.muni.csirt.kypo.elasticsearch.AbstractAuditPOJO;

/**
 * 
 * @author Pavel Šeda
 *
 */
public interface AuditDAO {

	/**
	 * Method for saving general class into Elasticsearch under specific index and type. Index is
	 * derived from package and class name lower case, and type is the same expect the class name is
	 * in it's origin e.g. index = cz.muni.csirt.kypo.game.events.wrongflagsubmitted and type =
	 * cz.muni.csirt.kypo.game.events.WrongFlagSubmitted
	 * 
	 * @param pojoClass class saving to Elasticsearch
	 * @throws IOException
	 */
	<T extends AbstractAuditPOJO> void save(T pojoClass) throws IOException;

	/**
	 * Update particular document.
	 * 
	 * @param pojoClass class updating in Elasticsearch
	 * @throws IOException
	 */
	<T extends AbstractAuditPOJO> void update(T pojoClass) throws IOException;

}
