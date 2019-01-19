
package org.openmrs.module.telemedicineconsult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ModuleActivator;

public class ExportccdActivator implements ModuleActivator {
	
	protected Log log = LogFactory.getLog(this.getClass());
	
	public ExportccdActivator() {
	}
	
	public void willRefreshContext() {
		this.log.info("Refreshing Export CCD Module");
	}
	
	public void contextRefreshed() {
		this.log.info("Export CCD Module refreshed");
	}
	
	public void willStart() {
		this.log.info("Starting Export CCD Module");
	}
	
	public void started() {
		this.log.info("Export CCD Module started");
	}
	
	public void willStop() {
		this.log.info("Stopping Export CCD Module");
	}
	
	public void stopped() {
		this.log.info("Export CCD Module stopped");
	}
}
