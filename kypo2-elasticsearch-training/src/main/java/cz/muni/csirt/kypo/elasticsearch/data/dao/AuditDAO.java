package cz.muni.csirt.kypo.elasticsearch.data.dao;

import cz.muni.csirt.kypo.elasticsearch.AbstractAuditPOJO;
import cz.muni.csirt.kypo.elasticsearch.data.exceptions.ElasticsearchTrainingDataLayerException;

import java.io.IOException;

/**
 * @author Pavel Šeda
 */
public interface AuditDAO {

    /**
     * Method for saving general class into Elasticsearch under specific index and type. Index is
     * derived from package and class name lower case, and type is the same expect the class name is
     * in it's origin
     *
     * @param pojoClass class saving to Elasticsearch
     * @throws IOException
     * @throws ElasticsearchTrainingDataLayerException
     */
    <T extends AbstractAuditPOJO> void save(T pojoClass) throws IOException;

    /**
     * Update particular document.
     *
     * @param pojoClass class updating in Elasticsearch
     * @throws IOException
     * @throws ElasticsearchTrainingDataLayerException
     */
    <T extends AbstractAuditPOJO> void update(T pojoClass) throws IOException;

}
