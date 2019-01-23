package org.openmrs.module.telemedicineconsult.api;

import org.apache.commons.lang.NullArgumentException;
import org.openmrs.ImplementationId;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.telemedicineconsult.TelemedicineConsultConfig;
import org.openmrs.module.telemedicineconsult.Consult;
import org.springframework.transaction.annotation.Transactional;

/**
 * The main service of this module, which is exposed for other modules. See
 * moduleApplicationContext.xml on how it is wired up.
 */
public interface TelemedicineConsultService extends OpenmrsService {
	
	/**
	 * Returns an item by uuid. It can be called by any authenticated user. It is fetched in read
	 * only transaction.
	 * 
	 * @param uuid
	 * @return
	 * @throws APIException
	 */
	@Authorized()
	@Transactional(readOnly = true)
	Consult getConsultByUuid(String uuid) throws APIException;
	
	@Transactional
	Consult remoteReferral(ImplementationId impl, User u, Patient patient, String reason, Integer specialtyId)
	        throws NullArgumentException;
}
