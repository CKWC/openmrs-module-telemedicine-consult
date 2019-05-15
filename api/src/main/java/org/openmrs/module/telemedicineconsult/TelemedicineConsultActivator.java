package org.openmrs.module.telemedicineconsult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleFactory;
import org.openmrs.module.dataexchange.DataImporter;
import org.openmrs.module.emrapi.utils.MetadataUtil;

/**
 * This class contains the logic that is run every time this module is either started or shutdown
 */
public class TelemedicineConsultActivator extends BaseModuleActivator {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	/**
	 * @see #started()
	 */
	public void started() {
		
		try {
			installConcepts();
			installMetadata();
			
			log.info("Started Telemedicine Consult");
		}
		catch (Exception e) {
			Module mod = ModuleFactory.getModuleById("telemedicineconsult");
			ModuleFactory.stopModule(mod);
			throw new RuntimeException("failed to setup the module ", e);
		}
	}
	
	/**
	 * @see #shutdown()
	 */
	public void shutdown() {
		log.info("Shutdown Telemedicine Consult");
	}
	
	private void installConcepts() throws Exception {
		
		DataImporter dataImporter = Context.getRegisteredComponent("dataImporter", DataImporter.class);
		
		log.info("Installing telemedicine concepts");
		dataImporter.importData("consult-concepts.xml");
		log.info("telemedicine concepts installed");
	}
	
	private void installMetadata() throws Exception {
		
		log.info("Installing standard metadata using the packages.xml file");
		MetadataUtil.setupStandardMetadata(getClass().getClassLoader());
		log.info("Standard metadata installed");
	}
}
