package org.openmrs.module.telemedicineconsult;

import java.util.Map;

import org.junit.Test;
import org.openmrs.module.Extension;
import org.openmrs.module.telemedicineconsult.extension.html.AdminList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * This test validates the AdminList extension class
 */
public class AdminListExtensionTest {
	
	/**
	 * Get the links for the extension class
	 */
	@Test
	public void testValidatesLinks() {
		AdminList ext = new AdminList();
		
		Map<String, String> links = ext.getLinks();
		
		assertThat(links, is(notNullValue()));
		assertThat(links.size(), is(not(0)));
	}
	
	/**
	 * Check the media type of this extension class
	 */
	@Test
	public void testMediaTypeIsHtml() {
		AdminList ext = new AdminList();
		
		assertThat(ext.getMediaType(), is(Extension.MEDIA_TYPE.html));
	}
	
}
