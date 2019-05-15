package org.openmrs.module.telemedicineconsult.api.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.GenericXMLResourceFactoryImpl;
import org.eclipse.emf.ecore.xml.type.AnyType;
import org.eclipse.emf.ecore.xml.type.XMLTypeDocumentRoot;
import org.openhealthtools.mdht.uml.cda.CDAFactory;
import org.openhealthtools.mdht.uml.cda.Section;
import org.openhealthtools.mdht.uml.cda.StrucDocText;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.DatatypesFactory;
import org.openhealthtools.mdht.uml.hl7.datatypes.ED;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVXB_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.ST;
import org.openhealthtools.mdht.uml.hl7.datatypes.TS;
import org.openmrs.Concept;
import org.openmrs.ConceptMap;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Component
public class ExportCcdUtils {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	SimpleDateFormat date = new SimpleDateFormat("dd/MM/yyyy");
	
	SimpleDateFormat dateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss (zzz)");
	
	public Comparator<String> descendingDateComparator = new Comparator<String>() {
		
		@Override
		public int compare(String s1, String s2) {
			Date d1, d2;
			try {
				d1 = parse(s1);
				d2 = parse(s2);
			}
			catch (ParseException e) {
				return 0;
			}
			return -1 * d1.compareTo(d2);
		}
	};
	
	public List<Obs> extractObservations(Patient patient, Concept concept) {
		List<Obs> listOfObservations = new ArrayList<Obs>();
		if (concept != null && concept.isSet()) {
			for (Concept conceptSet : concept.getSetMembers()) {
				listOfObservations.addAll(Context.getObsService().getObservationsByPersonAndConcept(patient, conceptSet));
			}
		} else {
			listOfObservations.addAll(Context.getObsService().getObservationsByPersonAndConcept(patient, concept));
		}
		return listOfObservations;
	}
	
	public String buildSubsection(Patient patient, int conceptId, String sectionHeader) {
		StringBuilder builder = new StringBuilder();
		Concept concept = Context.getConceptService().getConcept(conceptId);
		List<Obs> listOfObservations = extractObservations(patient, concept);
		if (!listOfObservations.isEmpty()) {
			builder.append(buildSectionHeader(sectionHeader));
			Set<String> rows = new HashSet<String>();
			for (Obs obs : listOfObservations) {
				rows.add(buildRow(obs));
			}
			for (String row : rows) {
				builder.append(row);
			}
			builder.append(buildSectionFooter());
		}
		return builder.toString();
	}
	
	public String buildRow(Obs obs) {
		StringBuilder builder = new StringBuilder();
		if (obs.getValueNumeric() != null) {
			String conceptName = obs.getConcept().getDisplayString();
			String value = obs.getValueNumeric().toString();
			builder.append(buildSectionContent(conceptName, value));
			builder.append(buildEmptyLine());
			
		} else if (obs.getValueDatetime() != null) {
			String conceptName = obs.getConcept().getDisplayString();
			String value = obs.getValueDatetime().toString();
			builder.append(buildSectionContent(conceptName, value));
			builder.append(buildEmptyLine());
		} else if (obs.getValueCoded() != null) {
			builder.append(buildSectionContent(obs.getValueCoded().getDisplayString()));
			builder.append(buildEmptyLine());
		}
		return builder.toString();
	}
	
	public String format(Date date) {
		return this.date.format(date);
	}
	
	public String formatWithTime(Date date) {
		return this.dateTime.format(date);
	}
	
	public Date parse(String dateText) throws ParseException {
		return date.parse(dateText);
	}
	
	public ST buildST(String title) {
		ST displayTitle = DatatypesFactory.eINSTANCE.createST();
		displayTitle.addText(title);
		return displayTitle;
	}
	
	public II buildID(String root, String extension) {
		II id = DatatypesFactory.eINSTANCE.createII();
		id.setRoot(root);
		id.setExtension(extension);
		return id;
	}
	
	public CE buildConceptCode(Concept c, String... source) {
		Collection<ConceptMap> conceptMap = c.getConceptMappings();
		CE codes = DatatypesFactory.eINSTANCE.createCE();
		Iterator i$ = conceptMap.iterator();
		
		while (i$.hasNext()) {
			ConceptMap n = (ConceptMap) i$.next();
			if (n.getConcept().getName().getName().contains("SNOMED")) {
				codes.setCodeSystem("2.16.840.1.113883.6.96");
			} else if (n.getConcept().getName().getName().contains("LOINC")) {
				codes.setCodeSystem("2.16.840.1.113883.6.1");
			} else if (n.getConcept().getName().getName().contains("RxNorm")) {
				codes.setCodeSystem("2.16.840.1.113883.6.88");
			} else if (!n.getConcept().getName().getName().contains("C4")
			        && !n.getConcept().getName().getName().contains("CPT-4")) {
				if (!n.getConcept().getName().getName().contains("C5")
				        && !n.getConcept().getName().getName().contains("CPT-5")) {
					if (!n.getConcept().getName().getName().contains("I9")
					        && !n.getConcept().getName().getName().contains("ICD9")) {
						if (!n.getConcept().getName().getName().contains("I10")
						        && !n.getConcept().getName().getName().contains("ICD10")) {
							if (!n.getConcept().getName().getName().contains("C2")
							        && !n.getConcept().getName().getName().contains("CPT-2")) {
								if (n.getConcept().getName().getName().contains("FDDX")) {
									codes.setCodeSystem("2.16.840.1.113883.6.63");
								} else if (n.getConcept().getName().getName().contains("MEDCIN")) {
									codes.setCodeSystem("2.16.840.1.113883.6.26");
								}
							} else {
								codes.setCodeSystem("2.16.840.1.113883.6.13");
							}
						} else {
							codes.setCodeSystem("2.16.840.1.113883.6.3");
						}
					} else {
						codes.setCodeSystem("2.16.840.1.113883.6.42");
					}
				} else {
					codes.setCodeSystem("2.16.840.1.113883.6.82");
				}
			} else {
				codes.setCodeSystem("2.16.840.1.113883.6.12");
			}
			
			// codes.setCode(n.getSourceCode());
			codes.setCodeSystemName(n.getConcept().getName().getName());
			codes.setDisplayName(n.getConcept().getDisplayString());
		}
		
		return codes;
	}
	
	public CD buildCode(String code, String codeSystem, String displayString, String codeSystemName) {
		CD e = DatatypesFactory.eINSTANCE.createCD();
		e.setCode(code);
		e.setCodeSystem(codeSystem);
		e.setDisplayName(displayString);
		e.setCodeSystemName(codeSystemName);
		return e;
	}
	
	public CE buildCodeCE(String code, String codeSystem, String displayString, String codeSystemName) {
		CE e = DatatypesFactory.eINSTANCE.createCE();
		e.setCode(code);
		e.setCodeSystem(codeSystem);
		e.setDisplayName(displayString);
		e.setCodeSystemName(codeSystemName);
		return e;
	}
	
	public ED buildEDText(String value) {
		ED text = DatatypesFactory.eINSTANCE.createED();
		text.addText("<reference value=\"" + value + "\"/>");
		return text;
	}
	
	public II buildTemplateID(String root) {
		II templateID = DatatypesFactory.eINSTANCE.createII();
		templateID.setRoot(root);
		return templateID;
	}
	
	public II buildTemplateID(String root, String extension) {
		II templateID = DatatypesFactory.eINSTANCE.createII();
		templateID.setRoot(root);
		templateID.setExtension(extension);
		return templateID;
	}
	
	public II buildTemplateID(String root, String extension, String assigningAuthorityName) {
		II templateID = DatatypesFactory.eINSTANCE.createII();
		templateID.setAssigningAuthorityName(assigningAuthorityName);
		templateID.setRoot(root);
		templateID.setExtension(extension);
		return templateID;
	}
	
	public TS buildEffectiveTime(Date d) {
		TS effectiveTime = DatatypesFactory.eINSTANCE.createTS();
		SimpleDateFormat s = new SimpleDateFormat("dd/MM/yyyy hh:mm");
		String creationDate = s.format(d);
		String timeOffset = d.getTimezoneOffset() + "";
		timeOffset = timeOffset.replace("-", "-0");
		effectiveTime.setValue(creationDate + timeOffset);
		return effectiveTime;
	}
	
	public IVL_TS buildEffectiveTimeinIVL(Date d, Date d1) {
		IVL_TS effectiveTime = DatatypesFactory.eINSTANCE.createIVL_TS();
		String creationDate = this.date.format(d);
		IVXB_TS low = DatatypesFactory.eINSTANCE.createIVXB_TS();
		low.setValue(creationDate);
		effectiveTime.setLow(low);
		IVXB_TS high = DatatypesFactory.eINSTANCE.createIVXB_TS();
		if (d1 != null) {
			high.setValue(this.date.format(d1));
		}
		
		effectiveTime.setHigh(high);
		return effectiveTime;
	}
	
	public StringBuilder buildSectionHeader(String... elements) {
		StringBuilder builder = new StringBuilder();
		
		builder.append(getBorderStart());
		builder.append("<thead>");
		builder.append("<tr>");
		
		for (String element : elements) {
			builder.append("<th>").append(element).append("</th>");
		}
		
		builder.append("</tr>");
		builder.append("</thead>");
		builder.append("<tbody>");
		
		return builder;
	}
	
	public String htmlString(String value) {
		return value.replaceAll("\n", "<br />");
	}
	
	public String buildSectionContent(String... elements) {
		StringBuilder builder = new StringBuilder();
		
		builder.append("<tr>");
		
		for (String element : elements) {
			String htmlValue = htmlString(element);
			
			builder.append("<td>").append(htmlValue).append("</td>");
		}
		
		builder.append("</tr>");
		
		return builder.toString();
	}
	
	public String buildSectionFooter() {
		StringBuilder builder = new StringBuilder();
		
		builder.append("</tbody>");
		builder.append("</table>");
		
		return builder.toString();
	}
	
	public String getBorderStart() {
		return "<table>";
	}
	
	public String buildEmptyLine() {
		StringBuilder builder = new StringBuilder();
		builder.append("<br />");
		return builder.toString();
	}
	
	public String buildTitle(String title) {
		StringBuilder builder = new StringBuilder();
		return builder.toString();
	}
	
	public String buildSubTitle(String subTitle) {
		StringBuilder builder = new StringBuilder();
		builder.append("<paragraph>" + subTitle + "</paragraph>");
		return builder.toString();
	}
	
	public StrucDocText createStrucDocText(Section section, String xmlString) {
		StrucDocText text = null;
		try {
			Resource.Factory factory = new GenericXMLResourceFactoryImpl();
			XMLResource resource = (XMLResource) factory.createResource(null);
			resource.load(new URIConverter.ReadableInputStream("<text>" + xmlString + "</text>"), null);
			XMLTypeDocumentRoot root = (XMLTypeDocumentRoot) resource.getContents().get(0);
			AnyType value = (AnyType) root.getMixed().getValue(0);
			text = CDAFactory.eINSTANCE.createStrucDocText();
			text.getMixed().addAll(value.getMixed());
			section.setText(text);
		}
		catch (Exception e) {
			log.fatal(e);
			log.fatal(e);
			log.fatal(e);
			log.error(xmlString);
		}
		return text;
	}
	
}
