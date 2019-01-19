package org.openmrs.module.telemedicineconsult.api.generators;

import org.openhealthtools.mdht.uml.cda.Act;
import org.openhealthtools.mdht.uml.cda.CDAFactory;
import org.openhealthtools.mdht.uml.cda.Entry;
import org.openhealthtools.mdht.uml.cda.EntryRelationship;
import org.openhealthtools.mdht.uml.cda.Observation;
import org.openhealthtools.mdht.uml.cda.StrucDocText;
import org.openhealthtools.mdht.uml.cda.ccd.CCDFactory;
import org.openhealthtools.mdht.uml.cda.ccd.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.ccd.PlanOfCareSection;
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
public class PlanOfCareSectionGenerator {
	
	@Autowired
	private ExportCcdUtils utils;
	
	@Autowired
	private PatientSummaryExportDAO dao;
	
	public ContinuityOfCareDocument buildPlanOfCare(ContinuityOfCareDocument ccd, Patient patient) {
		PlanOfCareSection section = CCDFactory.eINSTANCE.createPlanOfCareSection();
		section.getTemplateIds().add(utils.buildTemplateID("2.16.840.1.113883.10.20.22.2.10"));
		section.setCode(utils.buildCodeCE("18776-5", "2.16.840.1.113883.6.1", "Treatment plan", "LOINC"));
		section.setTitle(utils.buildST("PLAN OF CARE"));
		StringBuffer buffer = new StringBuffer();
		buffer.append(utils.getBorderStart());
		buffer.append("<thead>");
		buffer.append("<tr>");
		buffer.append("<th>Planned Activity</th>");
		buffer.append("<th>Planned Date</th>");
		List<Concept> labResultsList = this.dao.getConceptByCategory("PlanOfCare");
		List<Obs> listOfObservations = new ArrayList();
		Iterator i$ = labResultsList.iterator();
		
		while (true) {
			while (i$.hasNext()) {
				Concept concept = (Concept) i$.next();
				if (concept != null && concept.isSet()) {
					List<Concept> conceptSet = concept.getSetMembers();
					Iterator ii$ = conceptSet.iterator();
					
					while (ii$.hasNext()) {
						Concept conceptSet2 = (Concept) ii$.next();
						listOfObservations.addAll(Context.getObsService().getObservationsByPersonAndConcept(patient,
						    conceptSet2));
					}
				} else {
					listOfObservations.addAll(Context.getObsService().getObservationsByPersonAndConcept(patient, concept));
				}
			}
			
			buffer.append("</tr>");
			buffer.append("</thead>");
			buffer.append("<tbody>");
			i$ = listOfObservations.iterator();
			
			while (i$.hasNext()) {
				Obs obs = (Obs) i$.next();
				buffer.append("<tr>");
				buffer.append("<td><content id = \"" + obs.getConcept().getDisplayString() + "\">"
				        + obs.getConcept().getDisplayString() + "</content></td>");
				int type = obs.getConcept().getDatatype().getId();
				switch (type) {
					case 1:
						buffer.append("<td>" + obs.getValueNumeric() + "</td>");
						break;
					case 2:
						buffer.append("<td>" + obs.getValueCoded().getDisplayString() + "</td>");
						break;
					case 3:
						buffer.append("<td>" + obs.getValueText() + "</td>");
					case 4:
					case 5:
					case 9:
					case 11:
					case 12:
					default:
						break;
					case 6:
						buffer.append("<td>" + utils.format(obs.getValueDate()) + "</td>");
						break;
					case 7:
						buffer.append("<td>" + obs.getValueTime() + "</td>");
						break;
					case 8:
						buffer.append("<td>" + utils.format(obs.getValueDatetime()) + "</td>");
						break;
					case 10:
						buffer.append("<td>" + obs.getValueBoolean() + "</td>");
						break;
					case 13:
						buffer.append("<td>" + obs.getValueComplex() + "</td>");
				}
				
				buffer.append("</tr>");
				Entry labResultEntry = CDAFactory.eINSTANCE.createEntry();
				labResultEntry.setTypeCode(x_ActRelationshipEntry.DRIV);
				Observation observation = CDAFactory.eINSTANCE.createObservation();
				observation.setClassCode(ActClassObservation.OBS);
				observation.setMoodCode(x_ActMoodDocumentObservation.RQO);
				observation.getTemplateIds().add(utils.buildTemplateID("2.16.840.1.113883.10.20.1.25", "", "HITSP C83"));
				observation.getIds().add(utils.buildID(obs.getUuid(), ""));
				observation.setCode(utils.buildConceptCode(obs.getConcept(), "SNOMED", "LOINC"));
				observation.setText(utils.buildEDText("#" + obs.getConcept().getDisplayString()));
				CS statusCode1 = DatatypesFactory.eINSTANCE.createCS();
				statusCode1.setCode("new");
				observation.setStatusCode(statusCode1);
				observation.setEffectiveTime(utils.buildEffectiveTimeinIVL(obs.getObsDatetime(), (Date) null));
				EntryRelationship entryRelationship = CDAFactory.eINSTANCE.createEntryRelationship();
				entryRelationship.setInversionInd(false);
				entryRelationship.setTypeCode(x_ActRelationshipEntryRelationship.REFR);
				Act act = CDAFactory.eINSTANCE.createAct();
				act.setClassCode(x_ActClassDocumentEntryAct.ACT);
				act.setMoodCode(x_DocumentActMood.EVN);
				act.getTemplateIds().add(utils.buildTemplateID("1.3.6.1.4.1.19376.1.5.3.1.4.4.1", "", ""));
				if (obs.getEncounter() != null) {
					act.getIds().add(utils.buildID(obs.getEncounter().getUuid(), ""));
				}
				
				CD code = DatatypesFactory.eINSTANCE.createCD();
				code.setNullFlavor(NullFlavor.UNK);
				act.setCode(code);
				entryRelationship.setAct(act);
				labResultEntry.setObservation(observation);
				section.getEntries().add(labResultEntry);
			}
			
			buffer.append("</tbody>");
			buffer.append("</table>");
			
			utils.createStrucDocText(section, buffer.toString());
			ccd.addSection(section);
			return ccd;
		}
	}
}
