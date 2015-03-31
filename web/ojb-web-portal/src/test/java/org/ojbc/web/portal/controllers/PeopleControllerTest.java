package org.ojbc.web.portal.controllers;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.ojbc.web.DetailsQueryInterface;
import org.ojbc.web.PersonSearchInterface;
import org.ojbc.web.SearchFieldMetadata;
import org.ojbc.web.model.person.query.DetailsRequest;
import org.ojbc.web.model.person.search.PersonSearchRequest;
import org.ojbc.web.portal.controllers.config.PeopleControllerConfigInterface;
import org.ojbc.web.portal.controllers.dto.PersonSearchCommand;
import org.ojbc.web.portal.controllers.helpers.PersonSearchCommandUtils;
import org.ojbc.web.portal.controllers.helpers.PersonSearchType;
import org.ojbc.web.portal.controllers.helpers.SimpleSearchParser;
import org.ojbc.web.portal.controllers.helpers.UserSession;
import org.ojbc.web.portal.services.SamlService;
import org.ojbc.web.portal.services.SearchResultConverter;
import org.ojbc.web.portal.validators.PersonSearchCommandValidator;
import org.springframework.validation.BindingResult;

public class PeopleControllerTest {
	private PeopleController unit;
	private PersonSearchCommand personSearchCommand;
	private PersonSearchInterface personSearchInterface;
	private Map<String, Object> model;
	private String federatedQueryId = "dafdasf-324rfdsaf-vddf";
	private PersonSearchCommandValidator personSearchCommandValidator;
	private PersonSearchCommandUtils personSearchCommandUtils;
	private SearchResultConverter searchResultConverter;
	private SimpleSearchParser simpleSearchParser;
	private ArgumentCaptor<Map> paramsCaptor;

	private BindingResult errors;
	private DetailsQueryInterface detailsQueryInterface;

	private UserSession userSession;
	private HttpServletRequest servletRequest;
	private SamlService samlService;
	

	@Before
	public void setup() {
		personSearchCommand = new PersonSearchCommand();
		model = new HashMap<String, Object>();
		personSearchInterface = mock(PersonSearchInterface.class);
		personSearchCommandValidator = mock(PersonSearchCommandValidator.class);
		personSearchCommandUtils = mock(PersonSearchCommandUtils.class);
		errors = mock(BindingResult.class);
		searchResultConverter = mock(SearchResultConverter.class);
		detailsQueryInterface = mock(DetailsQueryInterface.class);
		userSession = mock(UserSession.class);
		simpleSearchParser = mock(SimpleSearchParser.class);
		servletRequest = mock(HttpServletRequest.class);
		samlService = mock(SamlService.class);
		
		paramsCaptor = ArgumentCaptor.forClass(Map.class);
		
		unit = new PeopleController() {
			@Override
			String getFederatedQueryId() {
				return federatedQueryId;
			}
		};

		unit.personSearchCommandUtils = personSearchCommandUtils;
		unit.personSearchCommandValidator = personSearchCommandValidator;
		unit.searchResultConverter = searchResultConverter;
		unit.userSession = userSession;
		unit.simpleSearchParser = simpleSearchParser;
		unit.samlService = samlService;
		unit.personSearchForm="people/_searchForm";
		unit.personSearchResultPage="people/_searchResult";
		
		unit.config = new PeopleControllerConfigInterface() {
			
			@Override
			public PersonSearchInterface getPersonSearchBean() {
				return personSearchInterface;
			}
			
			@Override
			public DetailsQueryInterface getDetailsQueryBean() {
				return detailsQueryInterface;
			}
		};
	}

	@Test
	public void searchFromSetsActiveTab() {
		unit.searchForm(false, "someTabName", model);

		assertThat((String) model.get("activeSearchTab"), Matchers.is("someTabName"));
	}

	@Test
	public void searchFormReturnsCorrectViewNameAndInitialData() {
		String viewName = unit.searchForm(false, null, model);

		assertThat(viewName, is("people/_searchForm"));

		PersonSearchCommand initialState = (PersonSearchCommand) model.get("personSearchCommand");
		assertThat(initialState.getAdvanceSearch().getPersonGivenNameMetaData(), is(SearchFieldMetadata.StartsWith));
		assertThat(initialState.getAdvanceSearch().getPersonSurNameMetaData(), is(SearchFieldMetadata.StartsWith));
	}

	@Test
	public void searchFormUsesNewFromWhenResetIsSetToTrueAndMostRecentSearchIsReset() {
		String viewName = unit.searchForm(true, null, model);

		assertThat(viewName, is("people/_searchForm"));

		PersonSearchCommand initialState = (PersonSearchCommand) model.get("personSearchCommand");
		assertThat(initialState.getAdvanceSearch().getPersonGivenNameMetaData(), is(SearchFieldMetadata.StartsWith));
		assertThat(initialState.getAdvanceSearch().getPersonSurNameMetaData(), is(SearchFieldMetadata.StartsWith));
		verify(userSession).setMostRecentSearch(Mockito.any(PersonSearchCommand.class));
	}

	@Test
	public void searchFormUsesMostRecentSearchFromSession() {

		PersonSearchCommand mostRecentSearch = new PersonSearchCommand();
		when(userSession.getMostRecentSearch()).thenReturn(mostRecentSearch);

		unit.searchForm(false, null, model);

		assertSame(model.get("personSearchCommand"), mostRecentSearch);
	}

	@Test
	public void advanceSearchSetsSearchToSession() throws Exception {
		
		PersonSearchRequest newPersonSearchRequest = makePersonSearchRequest();
		personSearchCommand.setAdvanceSearch(newPersonSearchRequest);
		personSearchCommand.setSearchType(PersonSearchType.ADVANCED);
		when(personSearchCommandUtils.getPersonSearchRequest(any(PersonSearchCommand.class))).thenReturn(newPersonSearchRequest);
		unit.advanceSearch( personSearchCommand, errors, model);

		verify(userSession).setMostRecentSearch(personSearchCommand);
	}
	
	private PersonSearchRequest makePersonSearchRequest() {
		PersonSearchRequest newPersonSearchRequest = new PersonSearchRequest();
		newPersonSearchRequest.setOnBehalfOf("onBehalfOf");
		newPersonSearchRequest.setPurpose("purpose");
		
		return newPersonSearchRequest;
	}

	@Test
	public void advanceSearchSuccess() throws Exception {
		PersonSearchRequest advanceSearch = new PersonSearchRequest();
		personSearchCommand.setAdvanceSearch(advanceSearch);
		personSearchCommand.setSearchType(PersonSearchType.ADVANCED);

		PersonSearchRequest newPersonSearchRequest = makePersonSearchRequest();
		when(errors.hasErrors()).thenReturn(false);
		when(personSearchCommandUtils.getPersonSearchRequest(personSearchCommand)).thenReturn(newPersonSearchRequest);
		when(personSearchInterface.invokePersonSearchRequest(newPersonSearchRequest, federatedQueryId, null))
		        .thenReturn("some xml");
		when(searchResultConverter.convertPersonSearchResult(eq("some xml"),paramsCaptor.capture())).thenReturn("some html");

		String expectedView = unit.advanceSearch(personSearchCommand, errors, model);

		verify(personSearchCommandValidator).validate(personSearchCommand, errors);
		verify(userSession).setMostRecentSearchResult("some xml");
		verify(userSession).setMostRecentSearchType(PersonSearchType.ADVANCED);

		assertThat(expectedView, Matchers.is("people/_searchResult"));
		assertThat((String) model.get("searchContent"), is("some html"));
		assertThat((String) model.get("activeSearchTab"), is ("advancedSearchTab"));
		
		assertThat((Integer)paramsCaptor.getValue().get("rows"),is(PeopleController.ROWS_PER_PAGE));
		assertThat((String)paramsCaptor.getValue().get("hrefBase"),is(PeopleController.PAGINATE_URL));
		assertThat((String)paramsCaptor.getValue().get("onBehalfOf"),is("onBehalfOf"));
		assertThat((String)paramsCaptor.getValue().get("purpose"),is("purpose"));
	}

	@Test
	public void advanceSearchFails() throws Exception {
		PersonSearchRequest advanceSearch = new PersonSearchRequest();
		personSearchCommand.setAdvanceSearch(advanceSearch);
        personSearchCommand.setSearchType(PersonSearchType.ADVANCED);

		when(errors.hasErrors()).thenReturn(true);
		when(personSearchInterface.invokePersonSearchRequest(advanceSearch, federatedQueryId, null)).thenReturn(
		        "some xml");

		String expectedView = unit.advanceSearch( personSearchCommand, errors, model);

		verify(userSession).setMostRecentSearchResult(null);
		assertThat(expectedView, Matchers.is("people/_searchForm"));
		assertThat((BindingResult) model.get("errors"), Matchers.is(errors));
		assertThat((String) model.get("activeSearchTab"), Matchers.is("advancedSearchTab"));
	}

	@Test
	public void getFederatedQueryId() {
		PeopleController unit = new PeopleController();
		String federatedQueryId1 = unit.getFederatedQueryId();
		String federatedQueryId2 = unit.getFederatedQueryId();
		assertThat(federatedQueryId1, Matchers.notNullValue());

		assertThat(federatedQueryId1.equals(federatedQueryId2), is(false));
	}

	@Test
	public void searchDetails() throws Throwable {
		DetailsRequest detailsRequest = new DetailsRequest();

		when(detailsQueryInterface.invokeRequest(detailsRequest, federatedQueryId, null))
		        .thenReturn("some details xml");
		when(searchResultConverter.convertDetailSearchResult("some details xml", "mySystem")).thenReturn(
		        "converted details xml");

		String expectedView = unit.searchDetails(servletRequest, "mySystem", detailsRequest, model);

		assertThat(expectedView, is("people/_searchDetails"));
		assertThat((String) model.get("searchContent"), is("converted details xml"));
	}
	
	@Test
	public void searchDetailsExceptionReturnsErrorView() throws Exception {
		DetailsRequest detailsRequest = new DetailsRequest();

		when(detailsQueryInterface.invokeRequest(detailsRequest, federatedQueryId, null))
		        .thenThrow(new RuntimeException());
		
		String expectedView = unit.searchDetails(servletRequest, "mySystem", detailsRequest, model);
		
		assertThat(expectedView, is("common/_searchDetailsError"));
	}

	@Test
	public void incidentDetails() throws Throwable {
		DetailsRequest detailsRequest = new DetailsRequest();
		
		when(detailsQueryInterface.invokeRequest(detailsRequest, federatedQueryId, null))
		.thenReturn("some details xml");
		when(searchResultConverter.convertDetailSearchResult("some details xml", "mySystem")).thenReturn(
				"converted details xml");
		
		String expectedView = unit.incidentDetails(servletRequest, "mySystem", detailsRequest, model);
		
		assertThat(expectedView, is("people/_incidentDetails"));
		assertThat((String) model.get("searchContent"), is("converted details xml"));
		
	}

	@Test
	public void simpleSearchSuccess() throws Exception {
		PersonSearchRequest personSearchRequest = new PersonSearchRequest();
		when(simpleSearchParser.validateAndParseSimpleSearch(personSearchCommand, errors)).thenReturn(
		        personSearchRequest);
		when(errors.hasErrors()).thenReturn(false);

		when(personSearchInterface.invokePersonSearchRequest(personSearchRequest, federatedQueryId, null)).thenReturn(
		        "some search result");
		when(searchResultConverter.convertPersonSearchResult(eq("some search result"),paramsCaptor.capture())).thenReturn(
		        "some converted search result");

		String expectedView = unit.simpleSearch(personSearchCommand, errors, model);

		assertThat(expectedView, is("people/_searchResult"));
		assertThat((String) model.get("searchContent"), is("some converted search result"));
		assertThat((String) model.get("activeSearchTab"), is ("simpleSearchTab"));
		
		verify(userSession).setMostRecentSearch(personSearchCommand);
		verify(userSession).setMostRecentSearchResult("some search result");
		verify(userSession).setMostRecentSearchType(PersonSearchType.SIMPLE);
		
		assertThat((Integer)paramsCaptor.getValue().get("rows"),is(PeopleController.ROWS_PER_PAGE));
		assertThat((String)paramsCaptor.getValue().get("hrefBase"),is(PeopleController.PAGINATE_URL));
	}
	
	

	@Test
	public void simpleSearchFail() throws Exception {
		PersonSearchRequest personSearchRequest = new PersonSearchRequest();
		when(simpleSearchParser.validateAndParseSimpleSearch(personSearchCommand, errors)).thenReturn(
		        personSearchRequest);
		when(errors.hasErrors()).thenReturn(true);

		String expectedView = unit.simpleSearch( personSearchCommand, errors, model);

		verify(userSession).setMostRecentSearchResult(null);
		assertThat(expectedView, is("people/_searchForm"));
		assertThat((String) model.get("activeSearchTab"), is("simpleSearchTab"));
		assertThat((BindingResult) model.get("errors"), is(errors));
	}
	
	@Test
	public void systemsToQueryReturnsValueFromMap() {
		Map<String, String> hashMap = new HashMap<String, String>();
		unit.systemsToQuery_people = hashMap;
		assertSame(hashMap, unit.getSystemsToQuery());
	}
	
	@Test
	public void systemsToQueryDisabledReturnsValueFromMap() {
		Map<String, String> hashMap = new HashMap<String, String>();
		unit.systemsToQuery_people_disabled = hashMap;
		assertSame(hashMap, unit.getSystemsToQueryDisabled());
	}

//	@Test
//	public void filter() throws Exception {
//		PersonFilterCommand personFilterCommand = new PersonFilterCommand();
//		
//		when(userSession.getMostRecentSearchResult()).thenReturn("xml from yogesh");
//		
//		when(searchResultConverter.filterXml("xml from yogesh", personFilterCommand)).thenReturn("filtered xml");
//		
//		when(searchResultConverter.convertPersonSearchResult(eq("filtered xml"), paramsCaptor.capture())).thenReturn("converted to html");
//		
//		String viewName = unit.filter(personFilterCommand, errors, model);
//		
//		assertThat((String)model.get("searchContent"), is("converted to html"));
//		
//		
//		assertThat(viewName, is("people/_searchResult"));
//	}
}