package org.openmrs.module.telemedicineconsult.api.generators;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import org.openhealthtools.mdht.uml.cda.AssignedAuthor;
import org.openhealthtools.mdht.uml.cda.AssignedCustodian;
import org.openhealthtools.mdht.uml.cda.AssignedEntity;
import org.openhealthtools.mdht.uml.cda.AssociatedEntity;
import org.openhealthtools.mdht.uml.cda.Author;
import org.openhealthtools.mdht.uml.cda.AuthoringDevice;
import org.openhealthtools.mdht.uml.cda.CDAFactory;
import org.openhealthtools.mdht.uml.cda.Custodian;
import org.openhealthtools.mdht.uml.cda.CustodianOrganization;
import org.openhealthtools.mdht.uml.cda.Entry;
import org.openhealthtools.mdht.uml.cda.InfrastructureRootTypeId;
import org.openhealthtools.mdht.uml.cda.Organization;
import org.openhealthtools.mdht.uml.cda.Participant1;
import org.openhealthtools.mdht.uml.cda.Participant2;
import org.openhealthtools.mdht.uml.cda.ParticipantRole;
import org.openhealthtools.mdht.uml.cda.PatientRole;
import org.openhealthtools.mdht.uml.cda.Performer2;
import org.openhealthtools.mdht.uml.cda.PlayingEntity;
import org.openhealthtools.mdht.uml.cda.StrucDocText;
import org.openhealthtools.mdht.uml.cda.ccd.CCDFactory;
import org.openhealthtools.mdht.uml.cda.ccd.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.ccd.EncountersSection;
import org.openhealthtools.mdht.uml.cda.operations.SectionOperations;
import org.openhealthtools.mdht.uml.hl7.datatypes.AD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.CS;
import org.openhealthtools.mdht.uml.hl7.datatypes.DatatypesFactory;
import org.openhealthtools.mdht.uml.hl7.datatypes.ED;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.ON;
import org.openhealthtools.mdht.uml.hl7.datatypes.PN;
import org.openhealthtools.mdht.uml.hl7.datatypes.SC;
import org.openhealthtools.mdht.uml.hl7.datatypes.ST;
import org.openhealthtools.mdht.uml.hl7.datatypes.TEL;
import org.openhealthtools.mdht.uml.hl7.datatypes.TS;
import org.openhealthtools.mdht.uml.hl7.vocab.ActClass;
import org.openhealthtools.mdht.uml.hl7.vocab.EntityClassRoot;
import org.openhealthtools.mdht.uml.hl7.vocab.NullFlavor;
import org.openhealthtools.mdht.uml.hl7.vocab.ParticipationType;
import org.openhealthtools.mdht.uml.hl7.vocab.RoleClassAssociative;
import org.openhealthtools.mdht.uml.hl7.vocab.RoleClassRoot;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActRelationshipEntry;
import org.openhealthtools.mdht.uml.hl7.vocab.x_DocumentEncounterMood;
import org.openmrs.Concept;
import org.openmrs.ConceptMap;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.Provider;
import org.openmrs.Relationship;
import org.openmrs.api.context.Context;
import org.openmrs.module.telemedicineconsult.api.utils.ExportCcdUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class HeaderGenerator {
	
	@Autowired
	private ExportCcdUtils utils;
	
	public ContinuityOfCareDocument buildHeader(ContinuityOfCareDocument ccd, Patient patient) {
		
		Date d = new Date();
		ccd.setEffectiveTime(utils.buildEffectiveTime(d));
		
		CS realmCode = DatatypesFactory.eINSTANCE.createCS("US");
		ccd.getRealmCodes().clear();
		ccd.getRealmCodes().add(realmCode);
		
		InfrastructureRootTypeId typeId = CDAFactory.eINSTANCE.createInfrastructureRootTypeId();
		typeId.setExtension("POCD_HD000040");
		typeId.setRoot("2.16.840.1.113883.1.3");
		ccd.setTypeId(typeId);
		
		ccd.getTemplateIds().clear();
		ccd.getTemplateIds().add(utils.buildTemplateID("2.16.840.1.113883.10.20.22.1.1"));
		ccd.getTemplateIds().add(utils.buildTemplateID("2.16.840.1.113883.10.20.22.1.2"));
		
		ccd.setCode(utils.buildCodeCE("34133-9", "2.16.840.1.113883.6.1", "Summarization of Episode Note", "LOINC"));
		ccd.setTitle(utils.buildST("Transition of Care/Referral Summary"));
		
		CS languageCode = DatatypesFactory.eINSTANCE.createCS();
		languageCode.setCode("en-US");
		ccd.setLanguageCode(languageCode);
		
		CE confidentialityCode = DatatypesFactory.eINSTANCE.createCE();
		confidentialityCode.setCode("N");
		ccd.setConfidentialityCode(confidentialityCode);
		
		PatientRole patientRole = CDAFactory.eINSTANCE.createPatientRole();
		Set<PersonAddress> addresses = patient.getAddresses();
		
		for (PersonAddress address : addresses) {
			if (address.isPreferred()) {
				AD patientAddress = DatatypesFactory.eINSTANCE.createAD();
				if (address.getAddress1() != null || address.getAddress2() != null) {
					patientAddress.addStreetAddressLine(address.getAddress1() + address.getAddress2());
				}
				if (address.getCityVillage() != null) {
					patientAddress.addCity(address.getCityVillage());
				}
				if (address.getStateProvince() != null) {
					patientAddress.addState(address.getStateProvince());
				}
				if (address.getCountry() != null) {
					patientAddress.addCountry(address.getCountry());
				}
				patientRole.getAddrs().add(patientAddress);
			}
		}
		
		TEL patientTelecom = DatatypesFactory.eINSTANCE.createTEL();
		patientTelecom.setNullFlavor(NullFlavor.UNK);
		patientRole.getTelecoms().add(patientTelecom);
		
		org.openhealthtools.mdht.uml.cda.Patient cdapatient = CDAFactory.eINSTANCE.createPatient();
		patientRole.setPatient(cdapatient);
		
		PN name = DatatypesFactory.eINSTANCE.createPN();
		name.addGiven(patient.getPersonName().getGivenName());
		name.addFamily(patient.getPersonName().getFamilyName());
		cdapatient.getNames().add(name);
		
		CE gender = DatatypesFactory.eINSTANCE.createCE();
		gender.setCode(patient.getGender());
		gender.setCodeSystem("2.16.840.1.113883.5.1");
		cdapatient.setAdministrativeGenderCode(gender);
		
		/*
		PersonAttribute civilStatus = patient.getAttribute("Civil Status");
		if (civilStatus != null) {
			Concept c = Context.getConceptService().getConceptByName(civilStatus.toString());
			Collection<ConceptMap> conceptmapp = c.getConceptMappings();
			
			for (ConceptMap n : conceptmapp) {
				if (n.getSource().getName().equalsIgnoreCase("z")) {
					CE codes = DatatypesFactory.eINSTANCE.createCE();
					codes.setCode(n.getSourceCode());
					codes.setCodeSystem("2.16.840.1.113883.6.96");
					codes.setCodeSystemName(n.getSource().getName());
					codes.setDisplayName(n.getConcept().getDisplayString());
					cdapatient.setMaritalStatusCode(codes);
				}
			}
		}*/
		
		TS dateOfBirth = DatatypesFactory.eINSTANCE.createTS();
		SimpleDateFormat s1 = new SimpleDateFormat("yyyyMMdd");
		Date dobs = patient.getBirthdate();
		String dob = s1.format(dobs);
		dateOfBirth.setValue(dob);
		cdapatient.setBirthTime(dateOfBirth);
		Organization providerOrganization = CDAFactory.eINSTANCE.createOrganization();
		AD providerOrganizationAddress = DatatypesFactory.eINSTANCE.createAD();
		providerOrganizationAddress.addCounty("");
		providerOrganizationAddress.addState("");
		providerOrganization.getAddrs().add(providerOrganizationAddress);
		ON organizationName = DatatypesFactory.eINSTANCE.createON();
		providerOrganization.getNames().add(organizationName);
		TEL providerOrganizationTelecon = DatatypesFactory.eINSTANCE.createTEL();
		providerOrganizationTelecon.setNullFlavor(NullFlavor.UNK);
		providerOrganization.getTelecoms().add(providerOrganizationTelecon);
		patientRole.setProviderOrganization(providerOrganization);
		ccd.addPatientRole(patientRole);
		Author author = CDAFactory.eINSTANCE.createAuthor();
		author.setTime(utils.buildEffectiveTime(d));
		AssignedAuthor assignedAuthor = CDAFactory.eINSTANCE.createAssignedAuthor();
		II authorId = DatatypesFactory.eINSTANCE.createII();
		assignedAuthor.getIds().add(authorId);
		Organization representedOrganization = CDAFactory.eINSTANCE.createOrganization();
		AD representedOrganizationAddress = DatatypesFactory.eINSTANCE.createAD();
		representedOrganizationAddress.addCounty("");
		representedOrganizationAddress.addState("");
		ON implName = DatatypesFactory.eINSTANCE.createON();
		representedOrganization.getNames().add(implName);
		assignedAuthor.getAddrs().add(representedOrganizationAddress);
		assignedAuthor.getTelecoms().add(providerOrganizationTelecon);
		org.openhealthtools.mdht.uml.cda.Person assignedPerson = CDAFactory.eINSTANCE.createPerson();
		PN assignedPersonName = DatatypesFactory.eINSTANCE.createPN();
		assignedPersonName.addText("Auto-generated");
		assignedPerson.getNames().add(assignedPersonName);
		AuthoringDevice authoringDevice = CDAFactory.eINSTANCE.createAuthoringDevice();
		SC authoringDeviceName = DatatypesFactory.eINSTANCE.createSC();
		authoringDeviceName.addText(Context.getAdministrationService().getGlobalProperty("application.name"));
		authoringDevice.setSoftwareName(authoringDeviceName);
		assignedAuthor.setAssignedAuthoringDevice(authoringDevice);
		assignedAuthor.setAssignedPerson(assignedPerson);
		assignedAuthor.setRepresentedOrganization(representedOrganization);
		author.setAssignedAuthor(assignedAuthor);
		ccd.getAuthors().add(author);
		ccd = this.buildEncounters(ccd, patient);
		List<Relationship> relationShips = Context.getPersonService().getRelationshipsByPerson(patient);
		List<Participant1> participantList = new ArrayList(relationShips.size());
		
		II custodianId;
		for (Relationship relationship : relationShips) {
			Participant1 e = CDAFactory.eINSTANCE.createParticipant1();
			e.setTypeCode(ParticipationType.IND);
			II pid1 = DatatypesFactory.eINSTANCE.createII();
			pid1.setAssigningAuthorityName("HITSP/C83");
			pid1.setRoot("2.16.840.1.113883.3.88.11.83.3");
			custodianId = DatatypesFactory.eINSTANCE.createII();
			custodianId.setAssigningAuthorityName("IHE/PCC");
			custodianId.setRoot("1.3.6.1.4.1.19376.1.5.3.1.2.4");
			e.getTemplateIds().add(pid1);
			e.getTemplateIds().add(custodianId);
			IVL_TS time = DatatypesFactory.eINSTANCE.createIVL_TS();
			time.setNullFlavor(NullFlavor.UNK);
			e.setTime(time);
			AssociatedEntity patientRelationShip = CDAFactory.eINSTANCE.createAssociatedEntity();
			patientRelationShip.setClassCode(RoleClassAssociative.PRS);
			CE relationShipCode = DatatypesFactory.eINSTANCE.createCE();
			relationShipCode.setCodeSystemName("Snomed CT");
			relationShipCode.setCodeSystem("2.16.840.1.113883.6.96");
			org.openhealthtools.mdht.uml.cda.Person associatedPerson = CDAFactory.eINSTANCE.createPerson();
			PN associatedPersonName = DatatypesFactory.eINSTANCE.createPN();
			Iterator<PersonAddress> patientAddressIterator = null;
			switch (relationship.getRelationshipType().getId()) {
				case 1:
					relationShipCode.setCode("305450004");
					relationShipCode.setDisplayName("Doctor");
					associatedPersonName.addFamily(relationship.getPersonA().getFamilyName());
					associatedPersonName.addGiven(relationship.getPersonA().getGivenName());
					patientAddressIterator = relationship.getPersonB().getAddresses().iterator();
					break;
				case 2:
					relationShipCode.setCode("375005");
					relationShipCode.setDisplayName("Sibling");
					associatedPersonName.addFamily(relationship.getPersonA().getFamilyName());
					associatedPersonName.addGiven(relationship.getPersonA().getGivenName());
					patientAddressIterator = relationship.getPersonA().getAddresses().iterator();
					break;
				case 3:
					if (patient.getId().equals(relationship.getPersonA().getId())) {
						relationShipCode.setCode("67822003");
						relationShipCode.setDisplayName("Child");
						associatedPersonName.addFamily(relationship.getPersonB().getFamilyName());
						associatedPersonName.addGiven(relationship.getPersonB().getGivenName());
						patientAddressIterator = relationship.getPersonB().getAddresses().iterator();
					} else {
						relationShipCode.setCode("40683002");
						relationShipCode.setDisplayName("Parent");
						associatedPersonName.addFamily(relationship.getPersonA().getFamilyName());
						associatedPersonName.addGiven(relationship.getPersonA().getGivenName());
						patientAddressIterator = relationship.getPersonA().getAddresses().iterator();
					}
					break;
				case 4:
					if (patient.getId().equals(relationship.getPersonA().getId())) {
						if (relationship.getPersonB().getGender().equalsIgnoreCase("M")) {
							relationShipCode.setCode("83559000");
						} else {
							relationShipCode.setCode("34581001");
						}
						
						relationShipCode.setDisplayName("Neice/Nephew");
						associatedPersonName.addFamily(relationship.getPersonB().getFamilyName());
						associatedPersonName.addGiven(relationship.getPersonB().getGivenName());
						patientAddressIterator = relationship.getPersonB().getAddresses().iterator();
					} else {
						if (relationship.getPersonA().getGender().equalsIgnoreCase("M")) {
							relationShipCode.setCode("38048003");
						} else {
							relationShipCode.setCode("25211005");
						}
						
						relationShipCode.setDisplayName("Aunt/Uncle");
						associatedPersonName.addFamily(relationship.getPersonA().getFamilyName());
						associatedPersonName.addGiven(relationship.getPersonA().getGivenName());
						patientAddressIterator = relationship.getPersonB().getAddresses().iterator();
					}
			}
			
			patientRelationShip.setCode(relationShipCode);
			AD associatedPersonAddress = DatatypesFactory.eINSTANCE.createAD();
			if (patientAddressIterator.hasNext()) {
				PersonAddress padd = patientAddressIterator.next();
				associatedPersonAddress.addStreetAddressLine(padd.getAddress1() + padd.getAddress2());
			}
			
			patientRelationShip.getAddrs().add(associatedPersonAddress);
			associatedPerson.getNames().add(associatedPersonName);
			patientRelationShip.setAssociatedPerson(associatedPerson);
			e.setAssociatedEntity(patientRelationShip);
			participantList.add(e);
		}
		
		ccd.getParticipants().addAll(participantList);
		Custodian custodian = CDAFactory.eINSTANCE.createCustodian();
		AssignedCustodian assignedCustodian = CDAFactory.eINSTANCE.createAssignedCustodian();
		CustodianOrganization custodianOrganization = CDAFactory.eINSTANCE.createCustodianOrganization();
		custodianId = DatatypesFactory.eINSTANCE.createII();
		custodianOrganization.getIds().add(custodianId);
		custodianOrganization.setAddr(providerOrganizationAddress);
		custodianOrganization.setName(organizationName);
		custodianOrganization.setTelecom(providerOrganizationTelecon);
		assignedCustodian.setRepresentedCustodianOrganization(custodianOrganization);
		custodian.setAssignedCustodian(assignedCustodian);
		ccd.setCustodian(custodian);
		return ccd;
	}
	
	private ContinuityOfCareDocument buildEncounters(ContinuityOfCareDocument ccd, Patient patient) {
		EncountersSection encounterSection = CCDFactory.eINSTANCE.createEncountersSection();
		ST encounterSectionTitle = DatatypesFactory.eINSTANCE.createST();
		encounterSectionTitle.addText("ENCOUNTERS");
		encounterSection.setTitle(encounterSectionTitle);
		StringBuffer buffer = new StringBuffer();
		buffer.append(utils.getBorderStart());
		buffer.append("<thead>");
		buffer.append("<tr>");
		buffer.append("<th>Date</th>");
		buffer.append("<th>Type</th>");
		buffer.append("</tr>");
		buffer.append("</thead>");
		buffer.append("<tbody>");
		List<Encounter> encounterList = Context.getEncounterService().getEncountersByPatient(patient);
		int i = 0;
		
		for (Iterator i$ = encounterList.iterator(); i$.hasNext(); ++i) {
			Encounter encounter = (Encounter) i$.next();
			
			Date date = encounter.getEncounterDatetime();
			Date dateSixMonthsAgo = new DateTime().minusMonths(6).toDate();
			
			if (date.after(dateSixMonthsAgo)) {
				buffer.append("<tr>");
				buffer.append("<td>" + utils.format(date) + "</td>");
				buffer.append("<td><content id=\"encounterType" + i + " \">" + encounter.getEncounterType().getName()
				        + "</content></td>");
				buffer.append("</tr>");
			}
		}
		
		buffer.append("</tbody>");
		buffer.append("</table>");
		SectionOperations.createStrucDocText(encounterSection, buffer.toString());
		
		encounterSection.getTemplateIds().add(utils.buildTemplateID("2.16.840.1.113883.10.20.22.2.22.1"));
		
		ccd.addSection(encounterSection);
		return ccd;
	}
}
