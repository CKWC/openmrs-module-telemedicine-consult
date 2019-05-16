package org.openmrs.module.telemedicineconsult;

import org.openmrs.BaseOpenmrsData;
import org.openmrs.User;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Please note that the corresponding table schema is created in liquibase.xml.
 */
@Entity(name = "telemedicineconsult.ConsultNote")
@Table(name = "telemedicineconsult_consult_note")
public class ConsultNote extends BaseOpenmrsData {
	
	@Id
	@GeneratedValue
	@Column(name = "telemedicineconsult_consult_note_id")
	private Integer id;
	
	@Basic
	@Column(name = "external_consult_id")
	private Integer externalConsultId;
	
	@Basic
	@Column(name = "obs_id")
	private Integer obsId;
	
	@Override
	public Integer getId() {
		return id;
	}
	
	@Override
	public void setId(Integer id) {
		this.id = id;
	}
	
	@Override
	public String getUuid() {
		return super.getUuid();
	}
	
	@Override
	public void setUuid(String uuid) {
		super.setUuid(uuid);
	}
	
	public Integer getExternalConsultId() {
		return externalConsultId;
	}
	
	public void setExternalConsultId(Integer externalConsultId) {
		this.externalConsultId = externalConsultId;
	}
	
	public Integer getObsId() {
		return obsId;
	}
	
	public void setObsId(Integer obsId) {
		this.obsId = obsId;
	}
}
