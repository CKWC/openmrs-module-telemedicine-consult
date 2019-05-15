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
@Entity(name = "telemedicineconsult.Consult")
@Table(name = "telemedicineconsult_consult")
public class Consult extends BaseOpenmrsData {
	
	@Id
	@GeneratedValue
	@Column(name = "telemedicineconsult_consult_id")
	private Integer id;
	
	@Basic
	@Column(name = "token", length = 255)
	private String token;
	
	@Basic
	@Column(name = "completed")
	private Boolean completed;
	
	@Basic
	@Column(name = "visit_id")
	private Integer visitId;
	
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
	
	public String getToken() {
		return token;
	}
	
	public void setToken(String token) {
		this.token = token;
	}
	
	public Boolean getCompleted() {
		return completed;
	}
	
	public void setCompleted(boolean completed) {
		this.completed = completed;
	}
	
	public Integer getVisitId() {
		return visitId;
	}
	
	public void setVisitId(Integer visitId) {
		this.visitId = visitId;
	}
}
