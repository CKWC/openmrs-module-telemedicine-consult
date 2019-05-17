package org.openmrs.module.telemedicineconsult.scheduler.tasks;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Obs;
import org.openmrs.Visit;
import org.openmrs.api.EncounterService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.telemedicineconsult.Consult;
import org.openmrs.module.telemedicineconsult.api.TelemedicineConsultService;
import org.openmrs.scheduler.tasks.AbstractTask;

public class SyncTask extends AbstractTask {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	@Override
	public void execute() {
		log.debug("started....");
		
		try {
			
			TelemedicineConsultService tcs = Context.getService(TelemedicineConsultService.class);
			
			List<Consult> consultsRequests = tcs.getOpenConsults();
			if (!consultsRequests.isEmpty()) {
				
				EncounterService encounterService = Context.getEncounterService();
				VisitService visitService = Context.getVisitService();
				EncounterType encounterType = encounterService.getEncounterType("Telemedicine Consult");
				Concept encounterNoteConcept = Context.getConceptService().getConceptByMapping("162169", "CIEL");
				
				if (encounterType == null || encounterNoteConcept == null) {
					// Can't save results
					return;
				}
				
				StringBuilder json = new StringBuilder();
				
				json.append("[");
				for (Consult consultRequest : consultsRequests) {
					
					if (json.length() != 1) {
						json.append(",");
					}
					json.append("\"");
					json.append(consultRequest.getToken());
					json.append("\"");
				}
				json.append("]");
				
				URL url = new URL("https://portal.connectingkidswithcare.org/api/emr/consult/status");
				
				int responseCode;
				JSONArray resp = post(url, json.toString());
				log.info(resp);
				
				if (resp != null && resp.length() == consultsRequests.size()) {
					// We should always get back an array of the same size as we sent
					
					for (int i = 0; i < resp.length(); i++) {
						JSONArray consults = (JSONArray) resp.get(i);
						Consult consultRequest = consultsRequests.get(i);
						
						for (int j = 0; j < consults.length(); j++) {
							
							JSONObject consult = (JSONObject) consults.get(j);
							int id = consult.getInt("id");
							
							String consultText = consultToNoteText(consult);
							Visit visit = visitService.getVisit(consultRequest.getVisitId());
							if (visit != null) {
								tcs.saveConsultForVisit(consultRequest, id, visit, encounterType, encounterNoteConcept,
								    consultText);
							}
						}
					}
				}
			}
			
		}
		catch (Exception e) {
			log.error("Failed ", e);
		}
	}
	
	private String consultToNoteText(JSONObject consult) throws JSONException {
		
		// 							int patient_id = consult.getInt("patient_id");
		String summary = null;
		String comments = null;
		String recommendation = null;
		String diagnosis = null;
		String background_info = null;
		String qol_wo_treatment = null;
		String qol_w_treatment = null;
		String disclaimer = null;
		String created_at = null;
		String update_at = null;
		String purpose = null;
		String external_id = null;
		int doctor_id = -1;
		String doctor_name = null;
		String doctor_specialty = null;
		
		if (consult.has("summary")) {			
			summary = consult.getString("summary");
		}
		if (consult.has("comments")) {			
			comments = consult.getString("comments");
		}
		if (consult.has("recommendation")) {			
			recommendation = consult.getString("recommendation");
		}
		if (consult.has("diagnosis")) {			
			diagnosis = consult.getString("diagnosis");
		}
		if (consult.has("background_info")) {			
			background_info = consult.getString("background_info");
		}
		if (consult.has("qol_wo_treatment")) {			
			qol_wo_treatment = consult.getString("qol_wo_treatment");
		}
		if (consult.has("qol_w_treatment")) {			
			qol_w_treatment = consult.getString("qol_w_treatment");
		}
		if (consult.has("disclaimer")) {			
			disclaimer = consult.getString("disclaimer");
		}
		if (consult.has("created_at")) {			
			created_at = consult.getString("created_at");
		}
		if (consult.has("update_at")) {			
			update_at = consult.getString("update_at");
		}
		if (consult.has("purpose")) {			
			purpose = consult.getString("purpose");
		}
		if (consult.has("external_id")) {			
			external_id = consult.getString("external_id");
		}

		if (consult.has("doctor")) {			
			JSONObject doctor = (JSONObject) consult.get("doctor");

			if (doctor.has("id")) {
				doctor_id = doctor.getInt("id");
			}

			if (doctor.has("name")) {
				doctor_name = doctor.getString("name");
			}

			if (doctor.has("specialty")) {
				doctor_specialty = doctor.getString("specialty");
			}
		}
		
		StringBuilder consultText = new StringBuilder();
		if (!StringUtils.isEmpty(background_info)) {
			consultText.append("Background Info:\n");
			consultText.append(background_info);
			consultText.append("\n\n");
		}
		if (!StringUtils.isEmpty(background_info)) {
			consultText.append("Diagnosis:\n");
			consultText.append(diagnosis);
			consultText.append("\n\n");
		}
		if (!StringUtils.isEmpty(recommendation)) {
			consultText.append("Recommendation:\n");
			consultText.append(recommendation);
			consultText.append("\n\n");
		}
		if (!StringUtils.isEmpty(comments)) {
			consultText.append("Comments:\n");
			consultText.append(comments);
			consultText.append("\n\n");
		}
		if (!StringUtils.isEmpty(summary)) {
			consultText.append("Summary:\n");
			consultText.append(summary);
			consultText.append("\n\n");
		}
		if (!StringUtils.isEmpty(qol_wo_treatment)) {
			consultText.append("Quality of life without treatment:\n");
			consultText.append(qol_wo_treatment);
			consultText.append("\n\n");
		}
		if (!StringUtils.isEmpty(qol_w_treatment)) {
			consultText.append("Quality of life with treatment:\n");
			consultText.append(qol_w_treatment);
			consultText.append("\n\n");
		}
		consultText.append("Electronically signed by: " + doctor_name);
		if (!StringUtils.isEmpty(doctor_specialty)) {
			consultText.append(" (");
			consultText.append(doctor_specialty);
			consultText.append(")");
		}
		
		// add on on {{Date}}{{/Date}}.
		consultText.append(".\n\n");
		
		if (!StringUtils.isEmpty(disclaimer)) {
			consultText.append(disclaimer);
		}
		
		return consultText.toString();
	}
	
	private JSONArray post(URL url, String tokenJson) {
		BufferedReader rd = null;
		int responseCode = 0;
		JSONArray json = null;
		String response = "";
		
		try {
			byte data[] = tokenJson.getBytes("UTF8");
			
			// Send the data
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Content-Length", String.valueOf(data.length));
			
			connection.getOutputStream().write(data);
			
			// Get the response
			rd = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
			String line;
			while ((line = rd.readLine()) != null) {
				response = String.format("%s%s%n", response, line);
			}
			
			json = new JSONArray(response);
			
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
	
}
