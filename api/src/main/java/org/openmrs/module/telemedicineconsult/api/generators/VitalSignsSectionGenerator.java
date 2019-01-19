package org.openmrs.module.telemedicineconsult.api.generators;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openhealthtools.mdht.uml.cda.ccd.CCDFactory;
import org.openhealthtools.mdht.uml.cda.ccd.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.ccd.VitalSignsSection;
import org.openhealthtools.mdht.uml.cda.operations.SectionOperations;
import org.openmrs.Concept;
import org.openmrs.ConceptNumeric;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.telemedicineconsult.api.db.PatientSummaryExportDAO;
import org.openmrs.module.telemedicineconsult.api.utils.ExportCcdUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Component
public class VitalSignsSectionGenerator {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	private static final int WEIGHT_CONCEPT_ID = 5089;
	
	private static final int CD4_VALUE_CONCEPT_ID = 159375;
	
	private static final int EXAM_CONCEPT_START_ID = 1120;
	
	private static final int EXAM_CONCEPT_END_ID = 1129;
	
	private static final int MOUTH_EXAM_CONCEPT_ID = 163308;
	
	private static final int EYE_EXAM_CONCEPT_ID = 163309;
	
	private static final int NOSE_EXAM_CONCEPT_ID = 163336;
	
	private static final int EAR_EXAM_CONCEPT_ID = 163337;
	
	public static final int ABNORMAL_CONCEPT_ID = 1116;
	
	public static final int AUTRES_CONCLUSION_CONCEPT_ID = 1284;
	
	@Autowired
	private ExportCcdUtils utils;
	
	@Autowired
	private PatientSummaryExportDAO dao;
	
	public ContinuityOfCareDocument buildVitalSigns(ContinuityOfCareDocument ccd, Patient patient) {
		VitalSignsSection section = CCDFactory.eINSTANCE.createVitalSignsSection();
		
		section.getTemplateIds().add(utils.buildTemplateID("2.16.840.1.113883.10.20.22.2.4"));
		section.setCode(utils.buildCodeCE("8716-3", "2.16.840.1.113883.6.1", "VITAL SIGNS", "LOINC"));
		section.setTitle(utils.buildST("VITAL SIGNS"));
		
		StringBuilder builder = new StringBuilder();
		
		builder.append(buildWeightSection(patient));
		
		builder.append(buildCd4Section(patient));
		
		builder.append(buildConclusionSection(patient));
		
		builder.append(buildOtherConclusionSection(patient));
		log.error(builder.toString());
		
		utils.createStrucDocText(section, builder.toString());
		ccd.addSection(section);
		return ccd;
	}
	
	private String buildOtherConclusionSection(Patient patient) {
		StringBuilder builder = new StringBuilder();
		SortedMap<String, List<String>> otherConclusions = new TreeMap<String, List<String>>(utils.descendingDateComparator);
		Concept concept = Context.getConceptService().getConcept(AUTRES_CONCLUSION_CONCEPT_ID);
		List<Obs> listOfObservations = utils.extractObservations(patient, concept);
		
		for (Obs obs : listOfObservations) {
			String conclusionValue = obs.getValueCoded().getDisplayString();
			if (obs.getComment() != null) {
				conclusionValue += String.format(" (%s)", obs.getComment());
			}
			
			if (otherConclusions.get(utils.format(obs.getDateCreated())) == null) {
				List<String> concepts = new ArrayList<String>();
				
				concepts.add(conclusionValue);
				otherConclusions.put(utils.format(obs.getDateCreated()), concepts);
			} else {
				otherConclusions.get(utils.format(obs.getDateCreated())).add(conclusionValue);
			}
		}
		
		if (!otherConclusions.isEmpty()) {
			builder.append(utils.buildSubTitle("Autres conlusions:"));
			builder.append(utils.buildSectionHeader());
			for (Map.Entry<String, List<String>> conclusion : otherConclusions.entrySet()) {
				builder.append(utils.buildSectionContent(conclusion.getKey(),
				    Arrays.toString(new HashSet(conclusion.getValue()).toArray()).replace("[", "").replace("]", "")));
			}
			builder.append(utils.buildSectionFooter());
		}
		return builder.toString();
	}
	
	private String buildCd4Section(Patient patient) {
		StringBuilder builder = new StringBuilder();
		ConceptNumeric cd4 = Context.getConceptService().getConceptNumeric(CD4_VALUE_CONCEPT_ID);
		List<Obs> listOfObservations = utils.extractObservations(patient, cd4);
		if (!listOfObservations.isEmpty()) {
			builder.append(utils.buildSubTitle("Historique de poids"));
			builder.append(utils.buildSectionHeader("Résultat", "Unité", "Date"));
			for (Obs obs : listOfObservations) {
				String value = obs.getValueNumeric().toString();
				builder.append(utils.buildSectionContent(value, "/mm3", utils.format(obs.getDateCreated())));
			}
			builder.append(utils.buildSectionFooter());
		}
		return builder.toString();
	}
	
	private String buildConclusionSection(Patient patient) {
		StringBuilder builder = new StringBuilder();
		SortedMap<String, List<String>> conclusions = new TreeMap<String, List<String>>(utils.descendingDateComparator);
		for (int i = EXAM_CONCEPT_START_ID; i < EXAM_CONCEPT_END_ID; i++) {
			extractConclusions(patient, conclusions, i);
		}
		
		extractConclusions(patient, conclusions, MOUTH_EXAM_CONCEPT_ID);
		extractConclusions(patient, conclusions, EYE_EXAM_CONCEPT_ID);
		extractConclusions(patient, conclusions, NOSE_EXAM_CONCEPT_ID);
		extractConclusions(patient, conclusions, EAR_EXAM_CONCEPT_ID);
		
		if (!conclusions.isEmpty()) {
			builder.append(utils.buildSubTitle("Conclusions d'examen clinique"));
			builder.append(utils.buildSectionHeader("Date de visite", "Résultats anormals"));
			for (Map.Entry<String, List<String>> conclusion : conclusions.entrySet()) {
				builder.append(utils.buildSectionContent(conclusion.getKey(),
				    Arrays.toString(conclusion.getValue().toArray()).replace("[", "").replace("]", "")));
			}
			builder.append(utils.buildSectionFooter());
		}
		return builder.toString();
	}
	
	private String buildWeightSection(Patient patient) {
		StringBuilder builder = new StringBuilder();
		Concept weight = Context.getConceptService().getConceptByMapping("27113001", "SNOMED CT");
		ConceptNumeric weightn = Context.getConceptService().getConceptNumeric(weight.getId());
		
		List<Obs> listOfObservations = utils.extractObservations(patient, weight);
		if (!listOfObservations.isEmpty()) {
			builder.append(utils.buildSubTitle("Weight History"));
			builder.append(utils.buildSectionHeader("Weight", "Unit", "Date"));
			for (Obs obs : listOfObservations) {
				String value = obs.getValueNumeric().toString();
				builder.append(utils.buildSectionContent(value, weightn.getUnits(), utils.format(obs.getDateCreated())));
			}
			builder.append(utils.buildSectionFooter());
		}
		return builder.toString();
	}
	
	private void extractConclusions(Patient patient, SortedMap<String, List<String>> conclusions, int i) {
		List<Obs> listOfObservations;
		Concept examConcept = Context.getConceptService().getConcept(i);
		listOfObservations = utils.extractObservations(patient, examConcept);
		for (Obs obs : listOfObservations) {
			if (obs.getValueCoded().getConceptId() == ABNORMAL_CONCEPT_ID) {
				if (conclusions.get(utils.format(obs.getDateCreated())) == null) {
					List<String> concepts = new ArrayList<String>();
					concepts.add(examConcept.getDisplayString());
					conclusions.put(utils.format(obs.getDateCreated()), concepts);
				} else {
					conclusions.get(utils.format(obs.getDateCreated())).add(examConcept.getDisplayString());
				}
			}
		}
	}
	
}
