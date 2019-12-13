package com.sun.faces.test.servlet30.dynamic;

import static org.junit.Assert.assertTrue;

import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.HttpHeaders;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomChangeEvent;
import com.gargoylesoftware.htmlunit.html.DomChangeListener;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;

public class Issue4261IT {

	private String webUrl;
	private WebClient webClient;

	@Before
	public void setUp() {
		webUrl = System.getProperty("integration.url");
		webClient = new WebClient();
	}

	@After
	public void tearDown() {
		webClient.close();
	}

	@Test
	public void testAddComponent() throws Exception {
		/*
		 * Make sure the three dynamically added input components are in their proper
		 * place.
		 */
		webClient.addRequestHeader(HttpHeaders.CONTENT_LANGUAGE, Locale.ENGLISH.getISO3Language());
		HtmlPage page = webClient.getPage(webUrl + "faces/add_uirepeat.xhtml");
		assertRowsOrder(page);

		/**
		 * After clicking make sure the added component is still in its proper place.
		 * Also verify the validation required error message appears as the third input
		 * component has required attribute set to true.
		 */
		AjaxUpdateChangeListener ajaxUpdate = new AjaxUpdateChangeListener(4);
		page.addDomChangeListener(ajaxUpdate);

		HtmlSubmitInput button = (HtmlSubmitInput) page.getHtmlElementById("j_idt6:0:button");
		page = button.click();

		ajaxUpdate.await(10, TimeUnit.SECONDS);
		// only check for Validation Start Strings to avoid localization errors
		assertTrue(page.asXml().contains("j_idt6:0:text3: "));
		assertTrue(page.asXml().contains("j_idt6:1:text3: "));
		assertTrue(page.asXml().contains("j_idt6:2:text3: "));
		assertRowsOrder(page);

		ajaxUpdate.reset(3);

		HtmlInput input = (HtmlInput) page.getElementById("j_idt6:0:text3");
		input.setValueAttribute("example text content first row");
		button = (HtmlSubmitInput) page.getElementById("j_idt6:1:button");
		page = button.click();

		ajaxUpdate.await(10, TimeUnit.SECONDS);

		// only check for Validation Start Strings to avoid localization errors
		assertTrue(page.asXml().indexOf("j_idt6:0:text3: ") == -1);
		assertTrue(page.asXml().contains("j_idt6:1:text3: "));
		assertTrue(page.asXml().contains("j_idt6:2:text3: "));
		assertRowsOrder(page);
		assertTrue(page.asXml().indexOf("name=\"j_idt6:0:text3\"") < page.asXml()
				.indexOf("example text content first row"));
		assertTrue(page.asXml().indexOf("example text content first row") < page.asXml()
				.indexOf("name=\"j_idt6:1:j_id1\""));

		input = (HtmlInput) page.getElementById("j_idt6:1:text3");
		input.setValueAttribute("example text content second row");

		ajaxUpdate.reset(3);

		button = (HtmlSubmitInput) page.getElementById("j_idt6:2:button");
		page = button.click();

		ajaxUpdate.await(10, TimeUnit.SECONDS);

		// only check for Validation Start Strings to avoid localization errors
		assertTrue(page.asXml().indexOf("j_idt6:0:text3: ") == -1);
		assertTrue(page.asXml().indexOf("j_idt6:1:text3: ") == -1);
		assertTrue(page.asXml().contains("j_idt6:2:text3: "));
		assertRowsOrder(page);
		assertTrue(page.asXml().indexOf("name=\"j_idt6:1:text3\"") < page.asXml()
				.indexOf("example text content second row"));
		assertTrue(page.asXml().indexOf("example text content second row") < page.asXml()
				.indexOf("name=\"j_idt6:2:j_id1\""));
	}

	private void assertRowsOrder(HtmlPage page) {
		assertTrue(page.asXml().indexOf("name=\"j_idt6:0:j_id1\"") < page.asXml().indexOf("name=\"j_idt6:0:j_id2\""));
		assertTrue(page.asXml().indexOf("name=\"j_idt6:0:j_id2\"") < page.asXml().indexOf("name=\"j_idt6:0:text3\""));
		assertTrue(page.asXml().indexOf("name=\"j_idt6:0:text3\"") < page.asXml().indexOf("name=\"j_idt6:1:j_id1\""));
		assertTrue(page.asXml().indexOf("name=\"j_idt6:1:j_id1\"") < page.asXml().indexOf("name=\"j_idt6:1:j_id2\""));
		assertTrue(page.asXml().indexOf("name=\"j_idt6:1:j_id2\"") < page.asXml().indexOf("name=\"j_idt6:1:text3\""));
		assertTrue(page.asXml().indexOf("name=\"j_idt6:1:text3\"") < page.asXml().indexOf("name=\"j_idt6:2:j_id1\""));
		assertTrue(page.asXml().indexOf("name=\"j_idt6:2:j_id1\"") < page.asXml().indexOf("name=\"j_idt6:2:j_id2\""));
		assertTrue(page.asXml().indexOf("name=\"j_idt6:2:j_id2\"") < page.asXml().indexOf("name=\"j_idt6:2:text3\""));
	}

	private class AjaxUpdateChangeListener implements DomChangeListener {
		CountDownLatch countDownLatch;

		/**
		 * Konstruktor
		 */
		public AjaxUpdateChangeListener(int updateCount) {
			this.countDownLatch = new CountDownLatch(updateCount);
		}

		@Override
		public void nodeDeleted(DomChangeEvent event) {
		}

		@Override
		public void nodeAdded(DomChangeEvent event) {
			// since every dom update is done by first adding the new and then removing the
			// old content. Except for viewstate parameter which only gets added.
			countDownLatch.countDown();
		}

		public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
			return this.countDownLatch.await(timeout, unit);
		}

		public void reset(int updateCount) {
			this.countDownLatch = new CountDownLatch(updateCount);
		}
	}

}