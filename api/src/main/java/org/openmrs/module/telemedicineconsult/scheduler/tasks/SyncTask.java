package org.openmrs.module.telemedicineconsult.scheduler.tasks;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.telemedicineconsult.Consult;
import org.openmrs.module.telemedicineconsult.api.TelemedicineConsultService;
import org.openmrs.scheduler.tasks.AbstractTask;

public class SyncTask extends AbstractTask {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	@Override
	public void execute() {
		log.error("started....");
		
		TelemedicineConsultService tcs = Context.getService(TelemedicineConsultService.class);
		
		@SuppressWarnings("unchecked")
		List<Consult> consults = tcs.getOpenConsults();
		
		log.error("started...." + consults.get(0).getToken());
		log.error("started...." + consults.get(1).getToken());
		
		try {
			// TODO
		}
		catch (Exception e) {
			log.error("Failed ", e);
		}
	}
}
