package org.openmrs.module.telemedicineconsult.dao;

import org.junit.Test;
import org.junit.Ignore;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.module.telemedicineconsult.Item;
import org.openmrs.module.telemedicineconsult.api.dao.TelemedicineConsultDao;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * It is an integration test (extends BaseModuleContextSensitiveTest), which verifies DAO methods
 * against the in-memory H2 database. The database is initially loaded with data from
 * standardTestDataset.xml in openmrs-api. All test methods are executed in transactions, which are
 * rolled back by the end of each test method.
 */
public class TelemedicineConsultDaoTest extends BaseModuleContextSensitiveTest {
	
	@Autowired
	TelemedicineConsultDao dao;
	
	@Autowired
	UserService userService;
	
	@Test
	@Ignore("Unignore if you want to make the Item class persistable, see also Item and liquibase.xml")
	public void saveItem_shouldSaveAllPropertiesInDb() {
		//Given
		Item item = new Item();
		item.setDescription("some description");
		item.setOwner(userService.getUser(1));
		
		//When
		dao.saveItem(item);
		
		//Let's clean up the cache to be sure getItemByUuid fetches from DB and not from cache
		Context.flushSession();
		Context.clearSession();
		
		//Then
		Item savedItem = dao.getItemByUuid(item.getUuid());
		
		assertThat(savedItem, hasProperty("uuid", is(item.getUuid())));
		assertThat(savedItem, hasProperty("owner", is(item.getOwner())));
		assertThat(savedItem, hasProperty("description", is(item.getDescription())));
	}
}
