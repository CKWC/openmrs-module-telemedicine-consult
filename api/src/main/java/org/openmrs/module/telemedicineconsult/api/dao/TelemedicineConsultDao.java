package org.openmrs.module.telemedicineconsult.api.dao;

import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.openmrs.api.db.hibernate.DbSession;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.telemedicineconsult.Consult;
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
		return (List<Consult>) getSession().createCriteria(Consult.class)
		        .add(Restrictions.or(Restrictions.eq("completed", false), Restrictions.isNull("completed"))).list();
	}
	
	public Consult saveConsult(Consult item) {
		getSession().saveOrUpdate(item);
		return item;
	}
}
