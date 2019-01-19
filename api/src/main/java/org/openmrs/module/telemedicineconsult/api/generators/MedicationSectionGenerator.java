package org.openmrs.module.telemedicineconsult.api.generators;

import org.openhealthtools.mdht.uml.cda.CDAFactory;
import org.openhealthtools.mdht.uml.cda.StrucDocText;
import org.openhealthtools.mdht.uml.cda.ccd.CCDFactory;
import org.openhealthtools.mdht.uml.cda.ccd.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.ccd.MedicationsSection;
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
public class MedicationSectionGenerator {
	
	private static final int DRUGS_CONCEPT_ID = 163711;
	
	private static final int DOSE_CONCEPT_ID = 1444;
	
	private static final int DRUG_NAME_CONCEPT_ID = 1282;
	
	private static final int DURATION_CONCEPT_ID = 159368;
	
	private static final int DISPENSE_DATE_CONCEPT_ID = 1276;
	
	@Autowired
	private ExportCcdUtils utils;
	
	public ContinuityOfCareDocument buildMedication(ContinuityOfCareDocument ccd, Patient patient) {
		MedicationsSection medicationSection = CCDFactory.eINSTANCE.createMedicationsSection();
		medicationSection.getTemplateIds().add(utils.buildTemplateID("2.16.840.1.113883.10.20.22.2.1.1"));
		medicationSection.setCode(utils
		        .buildCodeCE("10160-0", "2.16.840.1.113883.6.1", "HISTORY OF MEDICATION USE", "LOINC"));
		medicationSection.setTitle(utils.buildST("MEDICATIONS"));
		
		String content = generateDrugSectionContent(patient);
		SectionOperations.createStrucDocText(medicationSection, content);
		
		ccd.addSection(medicationSection);
		return ccd;
	}
	
	private String generateDrugSectionContent(Patient patient) {
		StringBuilder builder = utils.buildSectionHeader("Medication/Posologie", "Début", "Fin", "Dernier dt. de dispens.",
		    "Dur. (journées)", "Tox", "Int", "Ech", "Inc");
		
		/*
		List<Obs> observations = Context.getObsService().getObservationsByPersonAndConcept(patient,
		Context.getConceptService().getConcept(DRUGS_CONCEPT_ID));
		for (Obs obs : observations) {
		List<Obs> group = Context.getObsService().getObs(obs.getId());
		String dose = "";
		String name = "";
		String days = "-";
		String dispenseDate = "-";
		for (Obs obs2 : group) {
			switch (obs2.getConcept().getId()) {
				case DOSE_CONCEPT_ID:
					dose = obs2.getValueText();
					break;
				case DRUG_NAME_CONCEPT_ID:
					name = obs2.getValueCoded().getDisplayString();
					break;
				case DURATION_CONCEPT_ID:
					days = String.valueOf(obs2.getValueNumeric());
			}
		}
		
		for (Obs obs2 : group) {
			switch (obs2.getConcept().getId()) {
				case DISPENSE_DATE_CONCEPT_ID:
					dispenseDate = utils.format(obs2.getObsDatetime());
			}
		}
		
		builder.append(utils.buildSectionContent(name + " " + dose, utils.format(obs.getObsDatetime()), "-",
		    dispenseDate, days, "[ ]", "[ ]", "[ ]", "[ ]"));
		}
		
		builder.append(utils.buildSectionFooter());
		*/
		
		return builder.toString();
	}
}
