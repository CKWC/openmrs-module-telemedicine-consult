
package org.openmrs.module.telemedicineconsult;

import java.io.Serializable;
import org.openmrs.BaseOpenmrsObject;
import org.openmrs.Concept;

public class CCDSectionEntity extends BaseOpenmrsObject implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Integer id;
	
	private Concept concept;
	
	private String category;
	
	private String ccdSectionEntity;
	
	public CCDSectionEntity() {
	}
	
	public String getCcdSectionEntity() {
		return this.ccdSectionEntity;
	}
	
	public void setCcdSectionEntity(String ccdSectionEntity) {
		this.ccdSectionEntity = ccdSectionEntity;
	}
	
	public Concept getConcept() {
		return this.concept;
	}
	
	public void setConcept(Concept concept) {
		this.concept = concept;
	}
	
	public String getCategory() {
		return this.category;
	}
	
	public void setCategory(String category) {
		this.category = category;
	}
	
	public Integer getId() {
		return this.id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
}
