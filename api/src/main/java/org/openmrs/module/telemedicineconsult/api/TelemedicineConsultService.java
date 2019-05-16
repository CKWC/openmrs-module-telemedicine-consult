package org.openmrs.module.telemedicineconsult.api;

import java.util.List;

import org.apache.commons.lang.NullArgumentException;
import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.ImplementationId;
import org.openmrs.User;
import org.openmrs.Visit;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.telemedicineconsult.Consult;
import org.openmrs.module.telemedicineconsult.ConsultNote;
import org.springframework.transaction.annotation.Transactional;

/**
 * The main service of this module, which is exposed for other modules. See
 * moduleApplicationContext.xml on how it is wired up.
 */
public interface TelemedicineConsultService extends OpenmrsService {
	
	@Authorized()
	@Transactional(readOnly = true)
	Consult getConsultByUuid(String uuid) throws APIException;
	
	@Authorized()
	@Transactional(readOnly = true)
	List<Consult> getOpenConsults() throws APIException;
	
	@Transactional
	Consult remoteReferral(ImplementationId impl, User u, Visit visit, String reason, Integer specialtyId)
	        throws NullArgumentException;
	
	@Transactional
	void saveConsultForVisit(Consult consultRequest, int consultId, Visit visit, EncounterType encounterType,
	        Concept noteConcept, String consultText) throws NullArgumentException;
	
}
