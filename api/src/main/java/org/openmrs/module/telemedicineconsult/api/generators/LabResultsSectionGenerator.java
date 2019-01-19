package org.openmrs.module.telemedicineconsult.api.generators;

import org.apache.commons.lang3.StringUtils;
import org.openhealthtools.mdht.uml.cda.CDAFactory;
import org.openhealthtools.mdht.uml.cda.Observation;
import org.openhealthtools.mdht.uml.cda.StrucDocText;
import org.openhealthtools.mdht.uml.cda.ccd.CCDFactory;
import org.openhealthtools.mdht.uml.cda.ccd.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.ccd.ResultsSection;
import org.openhealthtools.mdht.uml.cda.operations.SectionOperations;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.telemedicineconsult.api.db.PatientSummaryExportDAO;
import org.openmrs.module.telemedicineconsult.api.utils.ExportCcdUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LabResultsSectionGenerator {
	
	private static final int LAB_RESULTS_CONCEPT_ID = 1271;
	
	@Autowired
	private ExportCcdUtils utils;
	
	@Autowired
	private PatientSummaryExportDAO dao;
	
	public ContinuityOfCareDocument buildLabResults(ContinuityOfCareDocument ccd, Patient patient) {
		ResultsSection section = CCDFactory.eINSTANCE.createResultsSection();
		section.getTemplateIds().add(utils.buildTemplateID("2.16.840.1.113883.10.20.22.2.3.1"));
		section.setCode(utils.buildCodeCE("30954-2", "2.16.840.1.113883.6.1", "RESULTS", "LOINC"));
		section.setTitle(utils.buildST("RESULTS"));
		
		StringBuilder builder = new StringBuilder();
		Concept concept = Context.getConceptService().getConcept(LAB_RESULTS_CONCEPT_ID);
		List<Obs> listOfObservations = utils.extractObservations(patient, concept);
		if (!listOfObservations.isEmpty()) {
			builder.append(utils.buildSectionHeader("Nom du test", "Date", "Résultat"));
			for (Obs obs : listOfObservations) {
				builder.append(utils.buildSectionContent(obs.getValueCoded().getDisplayString(),
				    utils.format(obs.getDateCreated()), getValueOfObs(patient, obs)));
			}
			builder.append(utils.buildSectionFooter());
		}
		
		utils.createStrucDocText(section, builder.toString());
		ccd.addSection(section);
		
		return ccd;
	}
	
	private String getValueOfObs(Patient patient, Obs obs) {
		Obs observation = null;
		List<Obs> relatedObservations = Context.getObsService().getObservationsByPersonAndConcept(patient,
		    Context.getConceptService().getConcept(obs.getValueCoded().getConceptId()));
		for (Obs tmp : relatedObservations) {
			if (tmp.getObsDatetime().equals(obs.getObsDatetime())) {
				observation = tmp;
				break;
			}
		}
		return observation == null ? "-" : extractObsProperValue(observation);
	}
	
	private String extractObsProperValue(Obs observation) {
		if (observation.getValueNumeric() != null) {
			return observation.getValueNumeric().toString();
		} else if (observation.getValueCoded() != null) {
			return observation.getValueCoded().getDisplayString();
		} else if (observation.getValueBoolean() != null) {
			return observation.getValueBoolean() ? "Oui" : "Non";
		} else {
			return "-";
		}
	}
}
