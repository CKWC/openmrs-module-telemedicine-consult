/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.telemedicineconsult.fragment.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.ImplementationId;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PatientService;
import org.openmrs.module.telemedicineconsult.api.TelemedicineConsultService;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.openmrs.ui.framework.page.Redirect;
import org.springframework.web.bind.annotation.RequestParam;
import org.openmrs.api.context.Context;

/**
 *  * Controller for a fragment that shows all users  
 */
public class RemoteReferralFragmentController {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	public Redirect get(FragmentModel model, @RequestParam("patientId") Integer patientId,
	        @SpringBean("patientService") PatientService service,
	        @SpringBean("adminService") AdministrationService adminService) {
		
		Patient patient = service.getPatient(patientId);
		
		if (patient == null || patient.isVoided() || patient.isPersonVoided()) {
			return new Redirect("coreapps", "patientdashboard/deletedPatient", "patientId=" + patientId.toString());
		}

		ImplementationId impl = adminService.getImplementationId();
		model.addAttribute("implementationId", impl);
		
		return null;
	}
	
	public void submit(
	        @RequestParam(value = "patientId", required = false) Integer patientId,
	        @RequestParam(value = "reason", required = false) String reason,
	        @RequestParam(value = "specialty", required = false) Integer specialtyId,
	        @SpringBean("patientService") PatientService patientService,
	        @SpringBean("adminService") AdministrationService adminService,
	        @SpringBean("telemedicineconsult.TelemedicineConsultService") TelemedicineConsultService telemedicineConsultService,
	        FragmentModel model) {
		
		ImplementationId impl = adminService.getImplementationId();
		log.error(impl);
		log.error(impl.getImplementationId());
		log.error(impl.getName());
		
		// log.error("1");
		// telemedicineConsultService.produceCCD("0f441e43-3db8-41c9-9625-9aa7c57db8ac");
		
		User u = Context.getAuthenticatedUser();
		
		Patient patient = patientService.getPatient(patientId);
		log.error(patient);
		
		log.error("2");
		telemedicineConsultService.remoteReferral(impl, u, patient);
		
		/*
		log.error("3");
		telemedicineConsultService.produceCCD("7");
		
		log.error("3");
		telemedicineConsultService.produceCCD("10003P");
		
		log.error("4");
		telemedicineConsultService.produceCCD("8");
		
		log.error("5");
		telemedicineConsultService.produceCCD("cc1bdd75-9918-498e-b4e2-ddf3a80696c9");
		*/
		
		log.error(reason);
		log.error(reason);
		log.error(specialtyId);
		
		log.error(model);
	}
	
}
