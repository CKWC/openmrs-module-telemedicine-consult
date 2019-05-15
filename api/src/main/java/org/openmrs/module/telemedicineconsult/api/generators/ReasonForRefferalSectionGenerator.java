package org.openmrs.module.telemedicineconsult.api.generators;

import org.openhealthtools.mdht.uml.cda.consol.ConsolFactory;
import org.openhealthtools.mdht.uml.cda.consol.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.consol.ReasonForReferralSection;
import org.openhealthtools.mdht.uml.cda.operations.SectionOperations;
import org.openmrs.Patient;
import org.openmrs.module.telemedicineconsult.api.utils.ExportCcdUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReasonForRefferalSectionGenerator {
	
	@Autowired
	private ExportCcdUtils utils;
	
	public ContinuityOfCareDocument buildReasonForRefferal(ContinuityOfCareDocument ccd, Patient patient, String reason) {
		ReasonForReferralSection section = ConsolFactory.eINSTANCE.createReasonForReferralSection();
		section.getTemplateIds().add(utils.buildTemplateID("1.3.6.1.4.1.19376.1.5.3.1.3.1"));
		section.setCode(utils.buildCodeCE("42349-1", "2.16.840.1.113883.6.1", "REASON FOR REFERRAL", "LOINC"));
		section.setTitle(utils.buildST("REASON FOR REFERRAL"));
		
		String content = "<paragraph>" + reason + "</paragraph>";
		SectionOperations.createStrucDocText(section, content);
		
		ccd.addSection(section);
		return ccd;
	}
}
