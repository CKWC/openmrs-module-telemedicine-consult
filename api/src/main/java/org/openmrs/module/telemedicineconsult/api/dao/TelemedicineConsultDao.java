package org.openmrs.module.telemedicineconsult.api.dao;

import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.openmrs.api.db.hibernate.DbSession;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.telemedicineconsult.Consult;
import org.openmrs.module.telemedicineconsult.ConsultNote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("telemedicineconsult.TelemedicineConsultDao")
public class TelemedicineConsultDao {
	
	@Autowired
	DbSessionFactory sessionFactory;
	
	private DbSession getSession() {
		return sessionFactory.getCurrentSession();
	}
	
	public Consult getConsultByUuid(String uuid) {
		return (Consult) getSession().createCriteria(Consult.class).add(Restrictions.eq("uuid", uuid)).uniqueResult();
	}
	
	public List<Consult> getOpenConsults() {
		return (List<Consult>) getSession().createCriteria(Consult.class).add(Restrictions.eq("completed", false)).list();
	}
	
	public ConsultNote getConsultNoteByExternalId(Integer externalId) {
		return (ConsultNote) getSession().createCriteria(ConsultNote.class)
		        .add(Restrictions.eq("externalConsultId", externalId)).uniqueResult();
	}
	
	public Consult saveConsult(Consult item) {
		getSession().saveOrUpdate(item);
		return item;
	}
	
	public ConsultNote saveConsultNote(ConsultNote item) {
		getSession().saveOrUpdate(item);
		return item;
	}
}
