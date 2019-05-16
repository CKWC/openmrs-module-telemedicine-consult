package org.openmrs.module.telemedicineconsult.fragment.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.ImplementationId;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.Visit;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.VisitService;
import org.openmrs.module.telemedicineconsult.api.TelemedicineConsultService;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.openmrs.ui.framework.page.Redirect;
import org.springframework.web.bind.annotation.RequestParam;
import org.openmrs.api.context.Context;

public class RemoteReferralFragmentController {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	public Redirect get(FragmentModel model, @RequestParam("patientId") String patientId,
	        @RequestParam("visitId") String visitId, @RequestParam(value = "returnUrl", required = false) String returnUrl,
	        @SpringBean("visitService") VisitService visitService,
	        @SpringBean("adminService") AdministrationService adminService) {
		
		Visit visit = visitService.getVisitByUuid(visitId);
		
		if (visit == null || visit.isVoided()) {
			return new Redirect("coreapps", "clinicianfacing/patient.page", "patientId=" + patientId);
		}
		
		ImplementationId impl = adminService.getImplementationId();
		model.addAttribute("implementationId", impl);
		model.addAttribute("returnUrl", returnUrl);
		
		return null;
	}
	
	public void submit(
	        @RequestParam(value = "patientId", required = false) String patientId,
	        @RequestParam(value = "visitId", required = false) String visitId,
	        @RequestParam(value = "reason", required = false) String reason,
	        @RequestParam(value = "specialty", required = false) Integer specialtyId,
	        @SpringBean("visitService") VisitService visitService,
	        @SpringBean("adminService") AdministrationService adminService,
	        @SpringBean("telemedicineconsult.TelemedicineConsultService") TelemedicineConsultService telemedicineConsultService,
	        FragmentModel model) {
		
		ImplementationId impl = adminService.getImplementationId();
		User u = Context.getAuthenticatedUser();
		Visit visit = visitService.getVisitByUuid(visitId);
		
		telemedicineConsultService.remoteReferral(impl, u, visit, reason, specialtyId);
	}
	
}
