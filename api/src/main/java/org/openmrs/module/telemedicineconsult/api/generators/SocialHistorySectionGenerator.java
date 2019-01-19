package org.openmrs.module.telemedicineconsult.api.generators;

import org.openhealthtools.mdht.uml.cda.CDAFactory;
import org.openhealthtools.mdht.uml.cda.StrucDocText;
import org.openhealthtools.mdht.uml.cda.ccd.CCDFactory;
import org.openhealthtools.mdht.uml.cda.ccd.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.ccd.SocialHistorySection;
import org.openhealthtools.mdht.uml.cda.operations.SectionOperations;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.telemedicineconsult.api.db.PatientSummaryExportDAO;
import org.openmrs.module.telemedicineconsult.api.utils.ExportCcdUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class SocialHistorySectionGenerator {
	
	private static final int METHOD_OD_FAMILY_PLANNING_CONCEPT_ID = 374;
	
	private static final int METHOD_OD_HIV_EXPOSURE_CONCEPT_ID = 1061;
	
	private static final int POINT_OF_HIV_TESTING_CONCEPT_ID = 159936;
	
	private static final int TUBERCULOSIS_DISEASE_STATUS_CONCEPT_ID = 1659;
	
	private static final int OBSTETRIC_HISTORY_ID = 160076;
	
	private static final int ARV_ID = 5356;
	
	@Autowired
	private ExportCcdUtils utils;
	
	@Autowired
	private PatientSummaryExportDAO dao;
	
	public ContinuityOfCareDocument buildSocialHistory(ContinuityOfCareDocument ccd, Patient patient) {
		SocialHistorySection section = CCDFactory.eINSTANCE.createSocialHistorySection();
		section.getTemplateIds().add(utils.buildTemplateID("2.16.840.1.113883.10.20.22.2.17"));
		section.setCode(utils.buildCodeCE("29762-2", "2.16.840.1.113883.6.1", "Social History", "LOINC"));
		section.setTitle(utils.buildST("SOCIAL HISTORY"));
		
		StringBuilder builder = new StringBuilder();
		
		Concept pointOfHiv = Context.getConceptService().getConcept(POINT_OF_HIV_TESTING_CONCEPT_ID);
		List<Obs> listOfObservations = utils.extractObservations(patient, pointOfHiv);
		if (!listOfObservations.isEmpty()) {
			builder.append(utils.buildSectionHeader(pointOfHiv.getDisplayString()));
			Set<String> rows = new HashSet<String>();
			for (Obs obs : listOfObservations) {
				rows.add(utils.buildRow(obs));
			}
			for (String row : rows) {
				builder.append(row);
			}
		}
		
		builder.append(utils.buildSubsection(patient, METHOD_OD_HIV_EXPOSURE_CONCEPT_ID, "Mode probable de "
		        + "transmission"));
		builder.append(utils.buildSubsection(patient, OBSTETRIC_HISTORY_ID, "Antécédents Obstétriques et Grossesse"));
		builder.append(utils.buildSubsection(patient, METHOD_OD_FAMILY_PLANNING_CONCEPT_ID, "Planning familial"));
		builder.append(utils.buildSubsection(patient, TUBERCULOSIS_DISEASE_STATUS_CONCEPT_ID, "Statut de TB"));
		builder.append(utils.buildSubsection(patient, ARV_ID, "Eligibilité Médical aux ARV"));
		
		builder.append(utils.buildSectionFooter());
		
		utils.createStrucDocText(section, builder.toString());
		ccd.addSection(section);
		return ccd;
	}
	
}
