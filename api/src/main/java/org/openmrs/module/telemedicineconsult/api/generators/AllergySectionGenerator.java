package org.openmrs.module.telemedicineconsult.api.generators;

import org.openhealthtools.mdht.uml.cda.Act;
import org.openhealthtools.mdht.uml.cda.CDAFactory;
import org.openhealthtools.mdht.uml.cda.Entry;
import org.openhealthtools.mdht.uml.cda.EntryRelationship;
import org.openhealthtools.mdht.uml.cda.Observation;
import org.openhealthtools.mdht.uml.cda.Participant2;
import org.openhealthtools.mdht.uml.cda.ParticipantRole;
import org.openhealthtools.mdht.uml.cda.PlayingEntity;
import org.openhealthtools.mdht.uml.cda.StrucDocText;
import org.openhealthtools.mdht.uml.cda.ccd.AlertsSection;
import org.openhealthtools.mdht.uml.cda.ccd.CCDFactory;
import org.openhealthtools.mdht.uml.cda.ccd.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.operations.SectionOperations;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.CS;
import org.openhealthtools.mdht.uml.hl7.datatypes.DatatypesFactory;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.datatypes.ST;
import org.openhealthtools.mdht.uml.hl7.vocab.ActClassObservation;
import org.openhealthtools.mdht.uml.hl7.vocab.EntityClassRoot;
import org.openhealthtools.mdht.uml.hl7.vocab.NullFlavor;
import org.openhealthtools.mdht.uml.hl7.vocab.ParticipationType;
import org.openhealthtools.mdht.uml.hl7.vocab.RoleClassRoot;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActClassDocumentEntryAct;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActMoodDocumentObservation;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActRelationshipEntry;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActRelationshipEntryRelationship;
import org.openhealthtools.mdht.uml.hl7.vocab.x_DocumentActMood;
import org.openmrs.Allergies;
import org.openmrs.Allergy;
import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.telemedicineconsult.api.utils.ExportCcdUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

@Component
public class AllergySectionGenerator {
	
	@Autowired
	private ExportCcdUtils utils;
	
	public ContinuityOfCareDocument buildAllergies(ContinuityOfCareDocument ccd, Patient patient) {
		AlertsSection allergySection = CCDFactory.eINSTANCE.createAlertsSection();
		allergySection.getTemplateIds().add(
		    utils.buildTemplateID("2.16.840.1.113883.3.88.11.83.102", (String) null, "HITSP/C83"));
		II allergySectionTemplateID1 = DatatypesFactory.eINSTANCE.createII();
		allergySectionTemplateID1.setRoot("1.3.6.1.4.1.19376.1.5.3.1.3.13");
		allergySectionTemplateID1.setAssigningAuthorityName("IHE PCC");
		allergySection.getTemplateIds().add(allergySectionTemplateID1);
		II allergySectionTemplateID2 = DatatypesFactory.eINSTANCE.createII();
		allergySectionTemplateID2.setRoot("2.16.840.1.113883.10.20.1.2");
		allergySectionTemplateID2.setAssigningAuthorityName("HL7 CCD");
		allergySection.getTemplateIds().add(allergySectionTemplateID2);
		CE allergySectionCode = DatatypesFactory.eINSTANCE.createCE();
		allergySectionCode.setCode("48765-2");
		allergySectionCode.setCodeSystem("2.16.840.1.113883.6.1");
		allergySectionCode.setCodeSystemName("LOINC");
		allergySectionCode.setDisplayName("Allergies, adverse reactions, alerts");
		allergySection.setCode(allergySectionCode);
		ST allergySectionTitle = DatatypesFactory.eINSTANCE.createST();
		allergySectionTitle.addText("ALLERGIES");
		allergySection.setTitle(allergySectionTitle);
		StringBuffer buffer = new StringBuffer();
		buffer.append(utils.getBorderStart());
		buffer.append("<thead>");
		buffer.append("<tr>");
		buffer.append("<th>Substance</th>");
		buffer.append("<th>Reaction</th>");
		buffer.append("<th>Date</th>");
		buffer.append("</tr>");
		buffer.append("</thead>");
		buffer.append("<tbody>");
		PatientService patientService = Context.getPatientService();
		Allergies patientAllergyList = patientService.getAllergies(patient);
		List<Entry> allergyEntryList = new ArrayList();
		
		for (int i = 0; i < patientAllergyList.size(); ++i) {
			Allergy patientAllergy = patientAllergyList.get(i);
			
			buffer.append("<tr>");
			buffer.append("<td><content id=\"allergy" + i + " \">" + patientAllergy.getAllergen().getNonCodedAllergen()
			        + "</content></td>");
			buffer.append("<td><content id=\"reaction" + i + " \">" + patientAllergy.getReactionNonCoded()
			        + "</content></td>");
			Date date = patientAllergy.getDateCreated();
			buffer.append("<td>" + utils.format(date) + "</td>");
			buffer.append("</tr>");
			Entry allergyEntry = CDAFactory.eINSTANCE.createEntry();
			allergyEntry.setTypeCode(x_ActRelationshipEntry.DRIV);
			Act allergyAct = CDAFactory.eINSTANCE.createAct();
			allergyAct.setClassCode(x_ActClassDocumentEntryAct.ACT);
			allergyAct.setMoodCode(x_DocumentActMood.EVN);
			allergyEntry.setAct(allergyAct);
			II allergyTemplateID = DatatypesFactory.eINSTANCE.createII();
			allergyTemplateID.setRoot("2.16.840.1.113883.3.88.11.83.6");
			allergyTemplateID.setAssigningAuthorityName("HITSP C83");
			allergyAct.getTemplateIds().add(allergyTemplateID);
			II allergyTemplateID1 = DatatypesFactory.eINSTANCE.createII();
			allergyTemplateID1.setRoot("2.16.840.1.113883.10.20.1.27");
			allergyTemplateID1.setAssigningAuthorityName("CCD");
			allergyAct.getTemplateIds().add(allergyTemplateID1);
			II allergyTemplateID2 = DatatypesFactory.eINSTANCE.createII();
			allergyTemplateID2.setRoot("1.3.6.1.4.1.19376.1.5.3.1.4.5.1");
			allergyTemplateID2.setAssigningAuthorityName("IHE PCC");
			allergyAct.getTemplateIds().add(allergyTemplateID2);
			allergyAct.getIds().add(utils.buildID(patientAllergy.getUuid(), ""));
			CD alleryActivityCode = DatatypesFactory.eINSTANCE.createCD();
			alleryActivityCode.setNullFlavor(NullFlavor.NA);
			allergyAct.setCode(alleryActivityCode);
			CS c3 = DatatypesFactory.eINSTANCE.createCS();
			c3.setCode("active");
			allergyAct.setStatusCode(c3);
			
			/*
			allergyAct.setEffectiveTime(utils.buildEffectiveTimeinIVL(patientAllergy.getStartDate(),
			    patientAllergy.getEndDate()));
			EntryRelationship allergyReactionEntry = CDAFactory.eINSTANCE.createEntryRelationship();
			allergyReactionEntry.setTypeCode(x_ActRelationshipEntryRelationship.SUBJ);
			allergyReactionEntry.setInversionInd(false);
			allergyAct.getEntryRelationships().add(allergyReactionEntry);
			Observation allergyReactionObservation = CDAFactory.eINSTANCE.createObservation();
			allergyReactionObservation.setClassCode(ActClassObservation.OBS);
			allergyReactionObservation.setMoodCode(x_ActMoodDocumentObservation.EVN);
			II allergyReactionTemplateID = DatatypesFactory.eINSTANCE.createII();
			allergyReactionTemplateID.setRoot("1.3.6.1.4.1.19376.1.5.3.1.4.6");
			allergyReactionTemplateID.setAssigningAuthorityName("IHE PCC");
			allergyReactionObservation.getTemplateIds().add(allergyReactionTemplateID);
			II allergyReactionTemplateID1 = DatatypesFactory.eINSTANCE.createII();
			allergyReactionTemplateID1.setRoot("1.3.6.1.4.1.19376.1.5.3.1.4.5");
			allergyReactionTemplateID1.setAssigningAuthorityName("IHE PCC");
			allergyReactionObservation.getTemplateIds().add(allergyReactionTemplateID1);
			II allergyReactionTemplateID2 = DatatypesFactory.eINSTANCE.createII();
			allergyReactionTemplateID2.setRoot("2.16.840.1.113883.10.20.1.18");
			allergyReactionTemplateID2.setAssigningAuthorityName("CCD");
			allergyReactionObservation.getTemplateIds().add(allergyReactionTemplateID2);
			allergyReactionObservation.getIds().add(utils.buildID(patientAllergy.getReaction().getUuid(), ""));
			allergyReactionObservation.setCode(utils.buildCode("59037007", "2.16.840.1.113883.6.96", "Drug Intolerance",
			    "SNOMED-CT"));
			allergyReactionObservation.setText(utils.buildEDText("#reaction" + i));
			allergyReactionObservation.setEffectiveTime(utils.buildEffectiveTimeinIVL(patientAllergy.getStartDate(), null));
			allergyReactionObservation.getValues().add(utils.buildConceptCode(patientAllergy.getAllergen()));
			CS c = DatatypesFactory.eINSTANCE.createCS();
			c.setCode("completed");
			allergyReactionObservation.setStatusCode(c);
			allergyReactionEntry.setObservation(allergyReactionObservation);
			Participant2 allergySubstance = CDAFactory.eINSTANCE.createParticipant2();
			allergySubstance.setTypeCode(ParticipationType.CSM);
			ParticipantRole alleryParticipantRole = CDAFactory.eINSTANCE.createParticipantRole();
			alleryParticipantRole.setClassCode(RoleClassRoot.MANU);
			PlayingEntity allergyPlayingEntity = CDAFactory.eINSTANCE.createPlayingEntity();
			allergyPlayingEntity.setClassCode(EntityClassRoot.MMAT);
			CE c1 = utils.buildConceptCode(patientAllergy.getAllergen());
			c1.setOriginalText(utils.buildEDText("#allergy" + i));
			allergyPlayingEntity.setCode(c1);
			alleryParticipantRole.setPlayingEntity(allergyPlayingEntity);
			allergySubstance.setParticipantRole(alleryParticipantRole);
			allergyReactionObservation.getParticipants().add(allergySubstance);
			EntryRelationship allergyStatusEntry = CDAFactory.eINSTANCE.createEntryRelationship();
			allergyStatusEntry.setTypeCode(x_ActRelationshipEntryRelationship.REFR);
			Observation statusObservation = CDAFactory.eINSTANCE.createObservation();
			statusObservation.setClassCode(ActClassObservation.OBS);
			statusObservation.setMoodCode(x_ActMoodDocumentObservation.EVN);
			statusObservation.getTemplateIds().add(utils.buildTemplateID("2.16.840.1.113883.10.20.1.39", null, null));
			statusObservation.setCode(utils.buildCode("33999-4", "2.16.840.1.113883.6.1", "Status", "LOINC"));
			CS c2 = DatatypesFactory.eINSTANCE.createCS();
			c2.setCode("completed");
			statusObservation.setStatusCode(c2);
			Concept alleryStatus = Context.getConceptService().getConceptByName(patientAllergy.getSeverity().getName().getName());
			statusObservation.getValues().add(utils.buildConceptCode(alleryStatus));
			allergyStatusEntry.setObservation(statusObservation);
			allergyReactionObservation.getEntryRelationships().add(allergyStatusEntry);
			*/
			
			allergyEntryList.add(allergyEntry);
		}
		
		buffer.append("</tbody>");
		buffer.append("</table>");
		
		utils.createStrucDocText(allergySection, buffer.toString());
		allergySection.getEntries().addAll(allergyEntryList);
		ccd.addSection(allergySection);
		return ccd;
	}
}
