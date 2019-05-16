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
				
				URL url = new URL("https://staging.connectingkidswithcare.org/api/emr/consult/status");
				
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
		String summary = consult.getString("summary");
		String comments = consult.getString("comments");
		String recommendation = consult.getString("recommendation");
		String diagnosis = consult.getString("diagnosis");
		String background_info = consult.getString("background_info");
		String qol_wo_treatment = consult.getString("qol_wo_treatment");
		String qol_w_treatment = consult.getString("qol_w_treatment");
		String disclaimer = consult.getString("disclaimer");
		String created_at = consult.getString("created_at");
		String update_at = consult.getString("update_at");
		String purpose = consult.getString("purpose");
		String external_id = consult.getString("external_id");
		
		JSONObject doctor = (JSONObject) consult.get("doctor");
		int doctor_id = doctor.getInt("id");
		String doctor_name = doctor.getString("name");
		String doctor_specialty = doctor.getString("specialty");
		
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
