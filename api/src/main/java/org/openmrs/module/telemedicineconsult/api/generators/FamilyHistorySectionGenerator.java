package org.openmrs.module.telemedicineconsult.api.generators;

import org.openhealthtools.mdht.uml.cda.CDAFactory;

import org.openhealthtools.mdht.uml.cda.StrucDocText;

import org.openhealthtools.mdht.uml.cda.consol.ConsolFactory;
import org.openhealthtools.mdht.uml.cda.consol.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.consol.FamilyHistorySection;
import org.openhealthtools.mdht.uml.cda.operations.SectionOperations;
import org.openmrs.Patient;
import org.openmrs.Person;
import java.util.Date;
import org.openmrs.module.telemedicineconsult.api.utils.ExportCcdUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FamilyHistorySectionGenerator {
	
	public static final String BIRTHPLACE = "Birthplace";
	
	public static final String TELEPHONE_NUMBER = "Telephone Number";
	
	public static final String CIVIL_STATUS = "Civil Status";
	
	public static final String FIRST_NAME_OF_MOTHER = "First Name of Mother";
	
	@Autowired
	private ExportCcdUtils utils;
	
	public ContinuityOfCareDocument buildFamilyHistory(ContinuityOfCareDocument ccd, Patient patient) {
		FamilyHistorySection section = ConsolFactory.eINSTANCE.createFamilyHistorySection();
		section.getTemplateIds().add(utils.buildTemplateID("2.16.840.1.113883.10.20.22.2.15"));
		section.setCode(utils.buildCodeCE("10157-6", "2.16.840.1.113883.6.1", "Family History", "LOINC"));
		section.setTitle(utils.buildST("FAMILY HISTORY"));
		StringBuilder builder = new StringBuilder();
		
		Person person = patient.getPerson();
		
		StringBuilder nameRow = new StringBuilder();
		nameRow.append(person.getGivenName());
		if (person.getMiddleName() != null) {
			nameRow.append(" ");
			nameRow.append(patient.getPerson().getMiddleName());
		}
		nameRow.append(", ");
		nameRow.append(person.getFamilyName());
		builder.append(utils.buildSectionHeader());
		builder.append(String.format("<tr><td>%s</td><td>Summary of the patient for:<br />%s</td></tr>",
		    utils.formatWithTime(new Date()), nameRow.toString()));
		builder.append(utils.buildSectionFooter());
		builder.append(utils.buildTitle("Medical record summary"));
		builder.append(utils.buildSubTitle("Demographic information"));
		
		builder.append(utils.buildSectionHeader());
		
		builder.append(utils.buildSectionContent("Name:", nameRow.toString()));
		
		if (person.getPersonAddress() != null) {
			builder.append(utils.buildSectionContent("Address:", person.getPersonAddress().getAddress1() == null ? "-"
			        : person.getPersonAddress().getAddress1()));
			builder.append(utils.buildSectionContent("District:",
			    person.getPersonAddress().getCountyDistrict() == null ? "-" : person.getPersonAddress().getCountyDistrict()));
			builder.append(utils.buildSectionContent("City:", person.getPersonAddress().getCityVillage() == null ? "-"
			        : person.getPersonAddress().getCityVillage()));
		}
		
		builder.append(utils.buildSectionContent("Place of birth:", person.getAttribute(BIRTHPLACE) == null ? "-" : person
		        .getAttribute(BIRTHPLACE).getValue()));
		builder.append(utils.buildSectionContent("Phone:", person.getAttribute(TELEPHONE_NUMBER) == null ? "-" : person
		        .getAttribute(TELEPHONE_NUMBER).getValue()));
		builder.append(utils.buildSectionContent("Gender:", person.getGender() == null ? "-" : person.getGender()));
		builder.append(utils.buildSectionContent("Civil Status:", person.getAttribute(CIVIL_STATUS) == null ? "-" : person
		        .getAttribute(CIVIL_STATUS).getValue()));
		String birthdate = person.getBirthdateEstimated() == true ? "~" : "";
		birthdate += person.getBirthdate().toString() == null ? "-" : utils.format(person.getBirthdate());
		builder.append(utils.buildSectionContent("Date of Birth:", birthdate));
		builder.append(utils.buildSectionContent("First name of the mother:",
		    person.getAttribute(FIRST_NAME_OF_MOTHER) == null ? "-" : person.getAttribute(FIRST_NAME_OF_MOTHER).getValue()));
		
		builder.append(utils.buildSectionFooter());
		
		utils.createStrucDocText(section, builder.toString());
		ccd.addSection(section);
		return ccd;
	}
}
