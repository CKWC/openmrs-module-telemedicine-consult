package org.openmrs.module.telemedicineconsult.api.generators;

import org.openhealthtools.mdht.uml.cda.Act;
import org.openhealthtools.mdht.uml.cda.CDAFactory;
import org.openhealthtools.mdht.uml.cda.Entry;
import org.openhealthtools.mdht.uml.cda.EntryRelationship;
import org.openhealthtools.mdht.uml.cda.Observation;
import org.openhealthtools.mdht.uml.cda.StrucDocText;
import org.openhealthtools.mdht.uml.cda.consol.AssessmentSection;
import org.openhealthtools.mdht.uml.cda.consol.ConsolFactory;
import org.openhealthtools.mdht.uml.cda.consol.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.consol.HospitalConsultationsSection;
import org.openhealthtools.mdht.uml.cda.operations.SectionOperations;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CS;
import org.openhealthtools.mdht.uml.hl7.datatypes.DatatypesFactory;
import org.openhealthtools.mdht.uml.hl7.vocab.ActClassObservation;
import org.openhealthtools.mdht.uml.hl7.vocab.NullFlavor;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActClassDocumentEntryAct;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActMoodDocumentObservation;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActRelationshipEntry;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActRelationshipEntryRelationship;
import org.openhealthtools.mdht.uml.hl7.vocab.x_DocumentActMood;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.telemedicineconsult.api.db.PatientSummaryExportDAO;
import org.openmrs.module.telemedicineconsult.api.utils.ExportCcdUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Component
public class EncounterSectionsGenerator {
	
	@Autowired
	private ExportCcdUtils utils;
	
	@Autowired
	private PatientSummaryExportDAO dao;
	
	private static final String CONCEPT_SOURCE = "CIEL";
	
	private static final String ENCOUNTER_NOTE_CONCEPT_CODE = "162169";
	
	private static final String VISIT_DIAGNOSES_CONCEPT_CODE = "159947";
	
	private Concept ENCOUNTER_NOTE_CONCEPT = null;
	
	private Concept VISIT_DIAGNOSES_CONCEPT = null;
	
	private void loadConcepts() {
		if (ENCOUNTER_NOTE_CONCEPT == null) {
			ENCOUNTER_NOTE_CONCEPT = Context.getConceptService().getConceptByMapping(ENCOUNTER_NOTE_CONCEPT_CODE,
			    CONCEPT_SOURCE);
		}
		
		if (VISIT_DIAGNOSES_CONCEPT == null) {
			VISIT_DIAGNOSES_CONCEPT = Context.getConceptService().getConceptByMapping(VISIT_DIAGNOSES_CONCEPT_CODE,
			    CONCEPT_SOURCE);
		}
	}
	
	public ContinuityOfCareDocument buildClinicalNotes(ContinuityOfCareDocument ccd, Patient patient) {
		loadConcepts();
		
		HospitalConsultationsSection section = ConsolFactory.eINSTANCE.createHospitalConsultationsSection();
		section.getTemplateIds().add(utils.buildTemplateID("2.16.840.1.113883.10.20.22.2.65", "2016-11-01"));
		section.setCode(utils.buildCodeCE("34109-9", "2.16.840.1.113883.6.1", "Clinical note", "LOINC"));
		section.setTitle(utils.buildST("CLINICAL NOTES"));
		
		StringBuffer builder = new StringBuffer();
		List<Obs> listOfObservations = utils.extractObservations(patient, ENCOUNTER_NOTE_CONCEPT);
		if (!listOfObservations.isEmpty()) {
			builder.append(utils.getBorderStart());
			builder.append("<thead>");
			builder.append("<tr>");
			builder.append("<th>Date</th>");
			builder.append("<th>Note</th>");
			builder.append("</tr>");
			builder.append("</thead>");
			builder.append("<tbody>");
			
			for (Obs obs : listOfObservations) {
				String value = obs.getValueText();
				builder.append(utils.buildSectionContent(utils.format(obs.getDateCreated()), value));
			}
			builder.append(utils.buildSectionFooter());
		}
		
		utils.createStrucDocText(section, builder.toString());
		ccd.addSection(section);
		return ccd;
	}
	
	public ContinuityOfCareDocument buildAssessments(ContinuityOfCareDocument ccd, Patient patient) {
		loadConcepts();
		
		AssessmentSection section = ConsolFactory.eINSTANCE.createAssessmentSection();
		section.getTemplateIds().add(utils.buildTemplateID("2.16.840.1.113883.10.20.22.2.8"));
		section.setCode(utils.buildCodeCE("51848-0", "2.16.840.1.113883.6.1", "Assessment", "LOINC"));
		section.setTitle(utils.buildST("ASSESSMENT"));
		
		List<Obs> listOfObservations = utils.extractObservations(patient, VISIT_DIAGNOSES_CONCEPT);
		if (!listOfObservations.isEmpty()) {
			StringBuffer builder = new StringBuffer();
			
			builder.append("<list listType=\"ordered\">");
			for (Obs obs : listOfObservations) {
				String value = obs.getValueText();
				if (value != null) {
					builder.append("<item>" + value + "</item>");
				}
			}
			builder.append("</list>");
			
			utils.createStrucDocText(section, builder.toString());
		}
		
		ccd.addSection(section);
		return ccd;
	}
}
