//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.openmrs.module.telemedicineconsult.api.db.hibernate;

import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.ConstraintViolationException;
import org.openmrs.Concept;
import org.openmrs.api.APIException;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.telemedicineconsult.CCDSectionEntity;
import org.openmrs.module.telemedicineconsult.api.db.PatientSummaryExportDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HibernatePatientSummaryExportDAO implements PatientSummaryExportDAO {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	@Autowired
	private SessionFactory sessionFactory;
	
	public HibernatePatientSummaryExportDAO() {
	}
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	public SessionFactory getSessionFactory() {
		return this.sessionFactory;
	}
	
	public CCDSectionEntity saveConceptByCategory(CCDSectionEntity e) throws DAOException, APIException {
		try {
			this.sessionFactory.getCurrentSession().save(e);
			return e;
		}
		catch (ConstraintViolationException var3) {
			throw new APIException("Concept Already Exists");
		}
	}
	
	public List<Concept> getConceptByCategory(String category) {
		Criteria c = this.sessionFactory.getCurrentSession().createCriteria(CCDSectionEntity.class);
		ProjectionList projList = Projections.projectionList();
		projList.add(Projections.property("concept"));
		c.setProjection(projList);
		c.add(Restrictions.eq("category", category)).list();
		List<Concept> l = c.list();
		return l;
	}
	
	public boolean deleteConceptByCategory(CCDSectionEntity e) {
		this.sessionFactory.getCurrentSession().delete(e);
		return true;
	}
	
	public CCDSectionEntity getConceptByCcdSectionEntity(Integer conceptId, String category) {
		return (CCDSectionEntity) this.sessionFactory.getCurrentSession().createCriteria(CCDSectionEntity.class)
		        .add(Restrictions.eq("ccdSectionEntity", conceptId + category)).list().get(0);
	}
}
