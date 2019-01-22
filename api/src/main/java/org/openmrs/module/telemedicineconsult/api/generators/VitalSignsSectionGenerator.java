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
	
	private static final String CONCEPT_SOURCE = "CIEL";
	
	private static final String WEIGHT_CONCEPT_CODE = "5089";
	
	private static final String SKIN_EXAM_CONCEPT_CODE = "1120";
	
	private static final String LYMPH_NODES_EXAM_CONCEPT_CODE = "1121";
	
	private static final String HEENT_EXAM_CONCEPT_CODE = "1122";
	
	private static final String CHEST_EXAM_CONCEPT_CODE = "1123";
	
	private static final String CARDIAC_EXAM_CONCEPT_CODE = "1124";
	
	private static final String ABDOMINAL_EXAM_CONCEPT_CODE = "1125";
	
	private static final String UROGENITAL_EXAM_CONCEPT_CODE = "1126";
	
	private static final String EXTREMITY_EXAM_CONCEPT_CODE = "1127";
	
	private static final String MUSCULOSKELETAL_EXAM_CONCEPT_CODE = "1128";
	
	private static final String NEUROLOGIC_EXAM_CONCEPT_CODE = "1129";
	
	private static final String PSYCHIATRIC_EXAM_CONCEPT_CODE = "1130";
	
	private static final String MOUTH_EXAM_CONCEPT_CODE = "163308";
	
	private static final String EYE_EXAM_CONCEPT_CODE = "163309";
	
	private static final String NOSE_EXAM_CONCEPT_CODE = "163336";
	
	private static final String EAR_EXAM_CONCEPT_CODE = "163337";
	
	private static final String ABNORMAL_CONCEPT_CODE = "1116";
	
	private static final String PROBLEMS_LIST_CONCEPT_CODE = "1284";
	
	private Concept WEIGHT_CONCEPT = null;
	
	private Concept ABNORMAL_CONCEPT = null;
	
	@Autowired
	private ExportCcdUtils utils;
	
	@Autowired
	private PatientSummaryExportDAO dao;
	
	private void loadConcepts() {
		if (WEIGHT_CONCEPT == null) {
			WEIGHT_CONCEPT = Context.getConceptService().getConceptByMapping(WEIGHT_CONCEPT_CODE, CONCEPT_SOURCE);
		}
		
		if (ABNORMAL_CONCEPT == null) {
			ABNORMAL_CONCEPT = Context.getConceptService().getConceptByMapping(ABNORMAL_CONCEPT_CODE, CONCEPT_SOURCE);
		}
	}
	
	public ContinuityOfCareDocument buildVitalSigns(ContinuityOfCareDocument ccd, Patient patient) {
		loadConcepts();
		
		VitalSignsSection section = CCDFactory.eINSTANCE.createVitalSignsSection();
		
		section.getTemplateIds().add(utils.buildTemplateID("2.16.840.1.113883.10.20.22.2.4"));
		section.setCode(utils.buildCodeCE("8716-3", "2.16.840.1.113883.6.1", "VITAL SIGNS", "LOINC"));
		section.setTitle(utils.buildST("VITAL SIGNS"));
		
		StringBuilder builder = new StringBuilder();
		
		builder.append(buildWeightSection(patient));
		
		builder.append(buildConclusionSection(patient));
		
		builder.append(buildProblemListSection(patient));
		log.error(builder.toString());
		
		utils.createStrucDocText(section, builder.toString());
		ccd.addSection(section);
		return ccd;
	}
	
	private String buildProblemListSection(Patient patient) {
		StringBuilder builder = new StringBuilder();
		SortedMap<String, List<String>> otherConclusions = new TreeMap<String, List<String>>(utils.descendingDateComparator);
		Concept concept = Context.getConceptService().getConcept(PROBLEMS_LIST_CONCEPT_CODE);
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
			builder.append(utils.buildSubTitle("Other conlusions:"));
			builder.append(utils.buildSectionHeader());
			for (Map.Entry<String, List<String>> conclusion : otherConclusions.entrySet()) {
				builder.append(utils.buildSectionContent(conclusion.getKey(),
				    Arrays.toString(new HashSet(conclusion.getValue()).toArray()).replace("[", "").replace("]", "")));
			}
			builder.append(utils.buildSectionFooter());
		}
		return builder.toString();
	}
	
	private String buildConclusionSection(Patient patient) {
		StringBuilder builder = new StringBuilder();
		/*
		 * SortedMap<String, List<String>> conclusions = new TreeMap<String,
		 * List<String>>(utils.descendingDateComparator); for (int i =
		 * EXAM_CONCEPT_START_ID; i < EXAM_CONCEPT_END_ID; i++) {
		 * extractConclusions(patient, conclusions, i); }
		 * 
		 * extractConclusions(patient, conclusions, MOUTH_EXAM_CONCEPT_ID);
		 * extractConclusions(patient, conclusions, EYE_EXAM_CONCEPT_ID);
		 * extractConclusions(patient, conclusions, NOSE_EXAM_CONCEPT_ID);
		 * extractConclusions(patient, conclusions, EAR_EXAM_CONCEPT_ID);
		 * 
		 * if (!conclusions.isEmpty()) {
		 * builder.append(utils.buildSubTitle("Conclusions d'examen clinique"));
		 * builder.append(utils.buildSectionHeader("Date de visite",
		 * "Résultats anormals")); for (Map.Entry<String, List<String>> conclusion :
		 * conclusions.entrySet()) {
		 * builder.append(utils.buildSectionContent(conclusion.getKey(),
		 * Arrays.toString(conclusion.getValue().toArray()).replace("[",
		 * "").replace("]", ""))); } builder.append(utils.buildSectionFooter()); }
		 */
		return builder.toString();
	}
	
	private String buildWeightSection(Patient patient) {
		StringBuilder builder = new StringBuilder();
		ConceptNumeric weight = Context.getConceptService().getConceptNumeric(WEIGHT_CONCEPT.getId());
		
		List<Obs> listOfObservations = utils.extractObservations(patient, WEIGHT_CONCEPT);
		if (!listOfObservations.isEmpty()) {
			builder.append(utils.buildSubTitle("Weight History"));
			builder.append(utils.buildSectionHeader("Weight", "Unit", "Date"));
			for (Obs obs : listOfObservations) {
				String value = obs.getValueNumeric().toString();
				builder.append(utils.buildSectionContent(value, weight.getUnits(), utils.format(obs.getDateCreated())));
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
			if (obs.getValueCoded() == ABNORMAL_CONCEPT) {
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
