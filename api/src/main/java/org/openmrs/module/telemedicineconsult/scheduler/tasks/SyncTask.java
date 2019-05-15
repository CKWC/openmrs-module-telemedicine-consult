package org.openmrs.module.telemedicineconsult.scheduler.tasks;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.openmrs.api.context.Context;
import org.openmrs.module.telemedicineconsult.Consult;
import org.openmrs.module.telemedicineconsult.api.TelemedicineConsultService;
import org.openmrs.scheduler.tasks.AbstractTask;

public class SyncTask extends AbstractTask {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	@Override
	public void execute() {
		log.error("started2....");
		
		try {
			
			TelemedicineConsultService tcs = Context.getService(TelemedicineConsultService.class);
			
			List<Consult> consults = tcs.getOpenConsults();
			if (!consults.isEmpty()) {
				
				StringBuilder json = new StringBuilder();
				
				json.append("[");
				for (Consult consult : consults) {
					
					if (json.length() != 1) {
						json.append(",");
					}
					json.append("\"");
					json.append(consult.getToken());
					json.append("\"");
				}
				json.append("]");
				
				URL url = new URL("https://staging.connectingkidswithcare.org/api/emr/consult/status");
				
				int responseCode;
				JSONArray resp = post(url, json.toString());
				log.fatal(resp);
			}
			
		}
		catch (Exception e) {
			log.error("Failed ", e);
		}
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
