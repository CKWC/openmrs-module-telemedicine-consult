package org.openmrs.module.telemedicineconsult;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.User;
import org.openmrs.api.UserService;
import org.openmrs.module.telemedicineconsult.Item;
import org.openmrs.module.telemedicineconsult.api.dao.TelemedicineConsultDao;
import org.openmrs.module.telemedicineconsult.api.impl.TelemedicineConsultServiceImpl;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * This is a unit test, which verifies logic in TelemedicineConsultService. It doesn't extend
 * BaseModuleContextSensitiveTest, thus it is run without the in-memory DB and Spring context.
 */
public class TelemedicineConsultServiceTest {
	
	@InjectMocks
	TelemedicineConsultServiceImpl basicModuleService;
	
	@Mock
	TelemedicineConsultDao dao;
	
	@Mock
	UserService userService;
	
	@Before
	public void setupMocks() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void saveItem_shouldSetOwnerIfNotSet() {
		//Given
		Item item = new Item();
		item.setDescription("some description");
		
		when(dao.saveItem(item)).thenReturn(item);
		
		User user = new User();
		when(userService.getUser(1)).thenReturn(user);
		
		//When
		basicModuleService.saveItem(item);
		
		//Then
		assertThat(item, hasProperty("owner", is(user)));
	}
}
