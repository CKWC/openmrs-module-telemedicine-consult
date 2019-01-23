package org.openmrs.module.telemedicineconsult.api.db;

import java.util.List;
import org.openmrs.Concept;
import org.openmrs.api.APIException;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.telemedicineconsult.CCDSectionEntity;

public interface PatientSummaryExportDAO {
	
	List<Concept> getConceptByCategory(String var1);
	
	boolean deleteConceptByCategory(CCDSectionEntity var1);
	
	CCDSectionEntity getConceptByCcdSectionEntity(Integer var1, String var2);
	
	CCDSectionEntity saveConceptByCategory(CCDSectionEntity var1) throws DAOException, APIException;
}
