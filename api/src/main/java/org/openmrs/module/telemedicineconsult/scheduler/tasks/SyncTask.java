package org.openmrs.module.telemedicineconsult.scheduler.tasks;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.openmrs.scheduler.tasks.AbstractTask;

public class SyncTask extends AbstractTask {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	@Override
	public void execute() {
		log.error("started....");
		
		try {
			// TODO
		}
		catch (Exception e) {
			log.error("Failed ", e);
		}
	}
}
