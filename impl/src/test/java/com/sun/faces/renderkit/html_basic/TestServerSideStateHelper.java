package com.sun.faces.renderkit.html_basic;

import static jakarta.faces.render.ResponseStateManager.VIEW_STATE_PARAM;
import static java.util.Collections.emptyEnumeration;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import com.sun.faces.renderkit.ServerSideStateHelper;

import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.ServletContext;

public class TestServerSideStateHelper {

	private FacesContext mockedFacesContext;
	private ExternalContext mockedExternalContext;
	private MockedStatic<FacesContext> mockedStaticFacesContext;

	@BeforeEach
	public void setup() {
		ServletContext mockedServletContext = mock(ServletContext.class);
		when(mockedServletContext.getInitParameterNames()).thenReturn(emptyEnumeration());
		mockedExternalContext = mock(ExternalContext.class);
		when(mockedExternalContext.getContext()).thenReturn(mockedServletContext);
		mockedFacesContext = mock(FacesContext.class);
		when(mockedFacesContext.getExternalContext()).thenReturn(mockedExternalContext);
		when(mockedFacesContext.getViewRoot()).thenReturn(new UIViewRoot());
		mockedStaticFacesContext = mockStatic(FacesContext.class);
		mockedStaticFacesContext.when(FacesContext::getCurrentInstance).thenReturn(mockedFacesContext);
	}

	@AfterEach
	public void teardown() {
		mockedStaticFacesContext.close();
	}

	private void prepareViewStateParam(String viewStateParam) {
		when(mockedExternalContext.getRequestParameterMap()).thenReturn(Map.of(VIEW_STATE_PARAM, viewStateParam));
	}

	@Test
	void testViewStateParam1() {
		prepareViewStateParam("1");
		assertDoesNotThrow(() -> new ServerSideStateHelper().getState(mockedFacesContext, null));
	}

	@Test
	void testViewStateParam2() {
		prepareViewStateParam("-1");
		assertDoesNotThrow(() -> new ServerSideStateHelper().getState(mockedFacesContext, null));
	}

	@Test
	void testViewStateParam3() {
		prepareViewStateParam("");
		assertDoesNotThrow(() -> new ServerSideStateHelper().getState(mockedFacesContext, null));
	}

	@Test
	void testViewStateParam4() {
		prepareViewStateParam("1:");
		assertDoesNotThrow(() -> new ServerSideStateHelper().getState(mockedFacesContext, null));
	}

	@Test
	void testViewStateParam5() {
		prepareViewStateParam(":");
		assertDoesNotThrow(() -> new ServerSideStateHelper().getState(mockedFacesContext, null));
	}

	@Test
	void testViewStateParam6() {
		prepareViewStateParam(":1");
		assertDoesNotThrow(() -> new ServerSideStateHelper().getState(mockedFacesContext, null));
	}

	@Test
	void testViewStateParam7() {
		prepareViewStateParam("1:1");
		assertDoesNotThrow(() -> new ServerSideStateHelper().getState(mockedFacesContext, null));
	}

	@Test
	void testViewStateParam8() {
		prepareViewStateParam("stateless");
		assertDoesNotThrow(() -> new ServerSideStateHelper().getState(mockedFacesContext, null));
	}

}
