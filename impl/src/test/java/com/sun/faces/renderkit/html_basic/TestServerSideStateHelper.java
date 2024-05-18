package com.sun.faces.renderkit.html_basic;

import static jakarta.faces.render.ResponseStateManager.VIEW_STATE_PARAM;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.sun.faces.junit.JUnitFacesTestCaseBase;
import com.sun.faces.renderkit.ServerSideStateHelper;

public class TestServerSideStateHelper extends JUnitFacesTestCaseBase {

	private void prepareViewStateParam(String viewStateParam) {
		externalContext.setRequestParameterMap(Map.of(VIEW_STATE_PARAM, viewStateParam));
	}

	@Test
	void testViewStateParam1() {
		prepareViewStateParam("1");
		assertDoesNotThrow(() -> new ServerSideStateHelper().getState(facesContext, null));
	}

	@Test
	void testViewStateParam2() {
		prepareViewStateParam("-1");
		assertDoesNotThrow(() -> new ServerSideStateHelper().getState(facesContext, null));
	}

	@Test
	void testViewStateParam3() {
		prepareViewStateParam("");
		assertDoesNotThrow(() -> new ServerSideStateHelper().getState(facesContext, null));
	}

	@Test
	void testViewStateParam4() {
		prepareViewStateParam("1:");
		assertDoesNotThrow(() -> new ServerSideStateHelper().getState(facesContext, null));
	}

	@Test
	void testViewStateParam5() {
		prepareViewStateParam(":");
		assertDoesNotThrow(() -> new ServerSideStateHelper().getState(facesContext, null));
	}

	@Test
	void testViewStateParam6() {
		prepareViewStateParam(":1");
		assertDoesNotThrow(() -> new ServerSideStateHelper().getState(facesContext, null));
	}

	@Test
	void testViewStateParam7() {
		prepareViewStateParam("1:1");
		assertDoesNotThrow(() -> new ServerSideStateHelper().getState(facesContext, null));
	}

	@Test
	void testViewStateParam8() {
		prepareViewStateParam("stateless");
		assertDoesNotThrow(() -> new ServerSideStateHelper().getState(facesContext, null));
	}
	
}
