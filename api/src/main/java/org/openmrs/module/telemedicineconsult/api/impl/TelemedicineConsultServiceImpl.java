package org.openmrs.module.telemedicineconsult.api.impl;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.openhealthtools.mdht.uml.cda.ccd.CCDFactory;
import org.openhealthtools.mdht.uml.cda.ccd.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openmrs.ImplementationId;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.telemedicineconsult.api.generators.AllergySectionGenerator;
import org.openmrs.module.telemedicineconsult.api.generators.FamilyHistorySectionGenerator;
import org.openmrs.module.telemedicineconsult.api.generators.HeaderGenerator;
import org.openmrs.module.telemedicineconsult.api.generators.ImmunizationsSectionGenerator;
import org.openmrs.module.telemedicineconsult.api.generators.LabResultsSectionGenerator;
import org.openmrs.module.telemedicineconsult.api.generators.MedicationSectionGenerator;
import org.openmrs.module.telemedicineconsult.api.generators.PlanOfCareSectionGenerator;
import org.openmrs.module.telemedicineconsult.api.generators.ProblemsSectionGenerator;
import org.openmrs.module.telemedicineconsult.api.generators.ReasonForRefferalSectionGenerator;
import org.openmrs.module.telemedicineconsult.api.generators.SocialHistorySectionGenerator;
import org.openmrs.module.telemedicineconsult.api.generators.VitalSignsSectionGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.openmrs.module.telemedicineconsult.Consult;
import org.openmrs.module.telemedicineconsult.api.TelemedicineConsultService;
import org.openmrs.module.telemedicineconsult.api.dao.TelemedicineConsultDao;

public class TelemedicineConsultServiceImpl extends BaseOpenmrsService implements TelemedicineConsultService {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	TelemedicineConsultDao dao;
	
	@Autowired
	private AllergySectionGenerator allergySectionGenerator;
	
	@Autowired
	private FamilyHistorySectionGenerator familyHistorySectionGenerator;
	
	@Autowired
	private LabResultsSectionGenerator labResultsSectionGenerator;
	
	@Autowired
	private MedicationSectionGenerator medicationSectionGenerator;
	
	@Autowired
	private PlanOfCareSectionGenerator planOfCareSectionGenerator;
	
	@Autowired
	private ProblemsSectionGenerator problemsSectionGenerator;
	
	@Autowired
	private SocialHistorySectionGenerator socialHistorySectionGenerator;
	
	@Autowired
	private VitalSignsSectionGenerator vitalSignsSectionGenerator;
	
	@Autowired
	private ImmunizationsSectionGenerator immunizationsSectionGenerator;
	
	@Autowired
	private ReasonForRefferalSectionGenerator reasonForRefferalSectionGenerator;
	
	@Autowired
	private HeaderGenerator headerGenerator;
	
	UserService userService;
	
	/**
	 * Injected in moduleApplicationContext.xml
	 */
	public void setDao(TelemedicineConsultDao dao) {
		this.dao = dao;
	}
	
	/**
	 * Injected in moduleApplicationContext.xml
	 */
	public void setUserService(UserService userService) {
		this.userService = userService;
	}
	
	@Override
	public Consult getConsultByUuid(String uuid) throws APIException {
		return dao.getConsultByUuid(uuid);
	}
	
	private ContinuityOfCareDocument produceCCD(Patient patient, User u, String reason) {
		ContinuityOfCareDocument ccd = CCDFactory.eINSTANCE.createContinuityOfCareDocument();
		
		ccd = headerGenerator.buildHeader(ccd, patient, u);
		ccd = allergySectionGenerator.buildAllergies(ccd, patient);
		ccd = problemsSectionGenerator.buildProblems(ccd, patient);
		ccd = medicationSectionGenerator.buildMedication(ccd, patient);
		ccd = vitalSignsSectionGenerator.buildVitalSigns(ccd, patient);
		ccd = socialHistorySectionGenerator.buildSocialHistory(ccd, patient);
		ccd = reasonForRefferalSectionGenerator.buildReasonForRefferal(ccd, patient, reason);
		ccd = immunizationsSectionGenerator.buildImmunizations(ccd, patient);
		ccd = labResultsSectionGenerator.buildLabResults(ccd, patient);
		ccd = planOfCareSectionGenerator.buildPlanOfCare(ccd, patient);
		ccd = familyHistorySectionGenerator.buildFamilyHistory(ccd, patient);
		
		return ccd;
	}
	
	public Consult remoteReferral(ImplementationId impl, User u, Patient patient, String reason, Integer specialtyId)
	        throws NullArgumentException {
		
		if (impl == null)
			throw new NullArgumentException("impl");
		if (u == null)
			throw new NullArgumentException("u");
		if (patient == null)
			throw new NullArgumentException("patient");
		
		try {
			ContinuityOfCareDocument ccd = produceCCD(patient, u, reason);
			if (ccd != null) {
				URL url = new URL("https://staging.connectingkidswithcare.org/api/emr/consult");
				Map<String, String> parameters = new HashMap<String, String>();
				parameters.put("first_name", u.getGivenName());
				parameters.put("last_name", u.getFamilyName());
				parameters.put("email", u.getUuid());
				
				parameters.put("name", impl.getName());
				parameters.put("external_id", impl.getImplementationId());
				parameters.put("external_source", "openmrs");
				parameters.put("specialty_id", specialtyId + "");
				
				String fileName = patient.getId().toString() + ".xml";
				saveToStream(ccd, fileName);
				
				int responseCode;
				JSONObject resp = post(ccd, url, parameters, fileName);
				log.fatal(resp);
				
				String token = resp.getString("token");
				
				Consult consult = new Consult();
				consult.setCreator(u);
				consult.setToken(token);
				
				dao.saveConsult(consult);
				return consult;
			}
		}
		catch (JSONException e) {
			
		}
		catch (MalformedURLException e) {
			//
		}
		
		return null;
	}
	
	private JSONObject post(ContinuityOfCareDocument ccd, URL url, Map<String, String> parameters, String fileName) {
		BufferedReader rd = null;
		int responseCode = 0;
		JSONObject json = null;
		String response = "";
		StringBuilder data = new StringBuilder();
		
		try {
			boolean first = true;
			// Construct data
			for (Map.Entry<String, String> entry : parameters.entrySet()) {
				
				// skip over invalid post variables
				if (entry.getKey() == null || entry.getValue() == null) {
					continue;
				}
				
				if (first == false) {
					data.append("&");
				}
				first = false;
				
				// finally, setup the actual post string
				data.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
				data.append("=");
				data.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
			}
			
			url = new URL(url.toString() + "?" + data.toString());
			
			ByteArrayOutputStream ms = new ByteArrayOutputStream();
			CDAUtil.save(ccd, ms);
			
			// Send the data
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/xml");
			connection.setRequestProperty("Content-Length", String.valueOf(ms.size()));
			
			ms.writeTo(connection.getOutputStream());
			
			// Get the response
			rd = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
			String line;
			while ((line = rd.readLine()) != null) {
				response = String.format("%s%s%n", response, line);
			}
			
			json = new JSONObject(response);
			
			responseCode = connection.getResponseCode();
		}
		catch (Exception e) {
			log.warn("Exception while posting to : " + url, e);
			log.warn("Reponse from server was: " + response);
		}
		finally {
			if (rd != null) {
				try {
					rd.close();
				}
				catch (Exception e) { /* pass */
				}
			}
		}
		
		return json;
	}
	
	private void saveToStream(ContinuityOfCareDocument ccd, String fileName) {
		FileOutputStream fos = null;
		File file;
		try {
			// Specify the file path here
			file = new File(fileName);
			fos = new FileOutputStream(file);
			
			if (!file.exists()) {
				file.createNewFile();
			}
			
			log.warn("saving to: " + fileName);
			CDAUtil.save(ccd, fos);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				if (fos != null) {
					fos.close();
				}
			}
			catch (IOException ioe) {
				// ...
			}
		}
	}
}
