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

public class RemoteReferralFragmentController {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	public Redirect get(FragmentModel model, @RequestParam("patientId") Integer patientId,
	        @RequestParam(value = "returnUrl", required = false) String returnUrl,
	        @SpringBean("patientService") PatientService service,
	        @SpringBean("adminService") AdministrationService adminService) {
		
		Patient patient = service.getPatient(patientId);
		
		if (patient == null || patient.isVoided() || patient.isPersonVoided()) {
			return new Redirect("coreapps", "patientdashboard/deletedPatient", "patientId=" + patientId.toString());
		}
		
		ImplementationId impl = adminService.getImplementationId();
		model.addAttribute("implementationId", impl);
		model.addAttribute("returnUrl", returnUrl);
		
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
		User u = Context.getAuthenticatedUser();
		Patient patient = patientService.getPatient(patientId);
		
		telemedicineConsultService.remoteReferral(impl, u, patient, reason, specialtyId);
	}
	
}
