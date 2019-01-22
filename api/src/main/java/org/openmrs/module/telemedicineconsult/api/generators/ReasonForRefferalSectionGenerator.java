package org.openmrs.module.telemedicineconsult.api.generators;

import org.openhealthtools.mdht.uml.cda.CDAFactory;
import org.openhealthtools.mdht.uml.cda.StrucDocText;
import org.openhealthtools.mdht.uml.cda.ccd.CCDFactory;
import org.openhealthtools.mdht.uml.cda.ccd.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.ccd.MedicationsSection;
import org.openhealthtools.mdht.uml.cda.ccd.ProblemSection;
import org.openhealthtools.mdht.uml.cda.operations.SectionOperations;
import org.openhealthtools.mdht.uml.cda.operations.StrucDocTextOperations;
import org.openhealthtools.mdht.uml.cda.operations.StructuredBodyOperations;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.telemedicineconsult.api.utils.ExportCcdUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReasonForRefferalSectionGenerator {
	
	@Autowired
	private ExportCcdUtils utils;
	
	public ContinuityOfCareDocument buildReasonForRefferal(ContinuityOfCareDocument ccd, Patient patient, String reason) {
		ProblemSection section = CCDFactory.eINSTANCE.createProblemSection();
		section.getTemplateIds().add(utils.buildTemplateID("1.3.6.1.4.1.19376.1.5.3.1.3.1"));
		section.setCode(utils.buildCodeCE("42349-1", "2.16.840.1.113883.6.1", "REASON FOR REFERRAL", "LOINC"));
		section.setTitle(utils.buildST("REASON FOR REFERRAL"));
		
		String content = "<paragraph>" + reason + "</paragraph>";
		SectionOperations.createStrucDocText(section, content);
		
		ccd.addSection(section);
		return ccd;
	}
}
