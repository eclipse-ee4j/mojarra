package com.sun.faces.renderkit.html_basic;

import static com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter.CompressViewState;
import static com.sun.faces.junit.Serialization.withSerialVersionUID;
import static jakarta.faces.render.ResponseStateManager.VIEW_STATE_PARAM;
import static java.util.Collections.emptyEnumeration;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;

import com.sun.faces.renderkit.ServerSideStateHelper;

import jakarta.faces.application.StateManager;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.PartialViewContext;
import jakarta.servlet.ServletContext;

public class TestServerSideStateHelper {

	private FacesContext mockedFacesContext;
	private ExternalContext mockedExternalContext;
	private MockedStatic<FacesContext> mockedStaticFacesContext;
	private final Map<String, Object> sessionMap = new HashMap<>();

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

	/**
	 * A component state is read back as written, whether or not the server state is serialized.
	 */
	@ParameterizedTest
	@ValueSource(booleans = { false, true })
	void componentStateRoundTrips(boolean serializeServerState) throws Exception {
		prepareSession(serializeServerState);
		Object[] state = { null, new HashMap<>(Map.of("form", "state")) };

		assertArrayEquals(state, roundTrip(state));
	}

	/**
	 * A view without component state keeps the session entry of its view, so it is read back as a state without component state, and not as a missing
	 * state which would expire the view, whether or not the server state is serialized.
	 */
	@ParameterizedTest
	@ValueSource(booleans = { false, true })
	void nullComponentStateRoundTrips(boolean serializeServerState) throws Exception {
		prepareSession(serializeServerState);
		Object[] state = { null, null };

		assertArrayEquals(state, roundTrip(state));
	}

	/**
	 * A view whose session entry was evicted has expired.
	 */
	@Test
	void evictedViewHasNoState() throws Exception {
		prepareSession(false);
		String id = write(new Object[] { null, null });
		viewStates(id).remove(viewStateId(id));

		prepareViewStateParam(id);

		assertNull(new ServerSideStateHelper().getState(mockedFacesContext, null));
	}

	/**
	 * A serialized session entry which holds a class in another serialized form, such as one saved by another version, expires the view.
	 */
	@Test
	void stateInAnotherSerializedFormExpiresView() throws Exception {
		prepareSession(true);
		ServletContext servletContext = (ServletContext) mockedExternalContext.getContext();
		when(servletContext.getInitParameter(CompressViewState.getQualifiedName())).thenReturn("false");
		String id = write(new Object[] { null, new HashMap<>(Map.of("form", new Versioned())) });
		Object[] entry = (Object[]) viewStates(id).get(viewStateId(id));
		entry[1] = withSerialVersionUID((byte[]) entry[1], Versioned.class.getName(), Versioned.serialVersionUID + 1);

		prepareViewStateParam(id);

		assertNull(new ServerSideStateHelper().getState(mockedFacesContext, null));
	}

	private void prepareSession(boolean serializeServerState) {
		ServletContext servletContext = (ServletContext) mockedExternalContext.getContext();
		when(servletContext.getInitParameter(StateManager.SERIALIZE_SERVER_STATE_PARAM_NAME)).thenReturn(String.valueOf(serializeServerState));
		when(mockedExternalContext.getSession(anyBoolean())).thenReturn(new Object());
		when(mockedExternalContext.getSessionMap()).thenReturn(sessionMap);
		when(mockedFacesContext.getAttributes()).thenReturn(new HashMap<>());
		when(mockedFacesContext.getPartialViewContext()).thenReturn(mock(PartialViewContext.class));
	}

	private Object[] roundTrip(Object[] state) throws IOException {
		prepareViewStateParam(write(state));
		return (Object[]) new ServerSideStateHelper().getState(mockedFacesContext, null);
	}

	private String write(Object[] state) throws IOException {
		StringBuilder id = new StringBuilder();
		new ServerSideStateHelper().writeState(mockedFacesContext, state, id);
		when(mockedFacesContext.getAttributes()).thenReturn(new HashMap<>());
		return id.toString();
	}

	/**
	 * Returns the session entries of the logical view of the view with the given id, which is the logical view id and the actual view id separated by a colon.
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Object> viewStates(String id) {
		Map<String, Map<String, Object>> logicalViews = (Map<String, Map<String, Object>>) sessionMap.get(ServerSideStateHelper.LOGICAL_VIEW_MAP);
		return logicalViews.get(id.substring(0, id.indexOf(':')));
	}

	private static String viewStateId(String id) {
		return id.substring(id.indexOf(':') + 1);
	}

	private static final class Versioned implements Serializable {

		private static final long serialVersionUID = 1L;
	}

}
