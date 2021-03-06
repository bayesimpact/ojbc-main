/*
 * Unless explicitly acquired and licensed from Licensor under another license, the contents of
 * this file are subject to the Reciprocal Public License ("RPL") Version 1.5, or subsequent
 * versions as allowed by the RPL, and You may not copy or use this file in either source code
 * or executable form, except in compliance with the terms and conditions of the RPL
 *
 * All software distributed under the RPL is provided strictly on an "AS IS" basis, WITHOUT
 * WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED, AND LICENSOR HEREBY DISCLAIMS ALL SUCH
 * WARRANTIES, INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE, QUIET ENJOYMENT, OR NON-INFRINGEMENT. See the RPL for specific language
 * governing rights and limitations under the RPL.
 *
 * http://opensource.org/licenses/RPL-1.5
 *
 * Copyright 2012-2015 Open Justice Broker Consortium
 */
package org.ojbc.web.portal.controllers;

import static org.ojbc.web.security.SecurityContextUtils.getSamlToken;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.ojbc.web.SearchFieldMetadata;
import org.ojbc.web.model.person.query.DetailsRequest;
import org.ojbc.web.model.person.search.PersonSearchRequest;
import org.ojbc.web.portal.controllers.config.PeopleControllerConfigInterface;
import org.ojbc.web.portal.controllers.dto.PersonFilterCommand;
import org.ojbc.web.portal.controllers.dto.PersonSearchCommand;
import org.ojbc.web.portal.controllers.helpers.DateTimePropertyEditor;
import org.ojbc.web.portal.controllers.helpers.PersonSearchCommandUtils;
import org.ojbc.web.portal.controllers.helpers.PersonSearchType;
import org.ojbc.web.portal.controllers.helpers.SimpleSearchParser;
import org.ojbc.web.portal.controllers.helpers.UserSession;
import org.ojbc.web.portal.services.SamlService;
import org.ojbc.web.portal.services.SearchResultConverter;
import org.ojbc.web.portal.validators.PersonFilterCommandValidator;
import org.ojbc.web.portal.validators.PersonSearchCommandValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.w3c.dom.Element;


@Controller
@RequestMapping("/people/*")
@SessionAttributes({"personSearchCommand", "juvenileHistoryDetailResponses"})
public class PeopleController {
	public static final String PAGINATE_URL = "../people/paginate";

	public static final int ROWS_PER_PAGE = 50;

    @Value("${personSearchForm:people/_searchForm}")
    String personSearchForm;
    
    @Value("${personSearchResultPage:people/_searchResult}")
    String personSearchResultPage; 	
    
    @Resource
	PeopleControllerConfigInterface config;

	@Resource
	Map<String, String> races;

	@Resource
	Map<String, String> genders;

	@Resource
	Map<String, String> eyeColors;

	@Resource
	Map<String, String> hairColors;
	
	@Value("#{getObject('placements')}")
    Map<String, String> placements;
    
    @Value("#{getObject('states')}")
    Map<String, String> states;
    
	
	@Resource
	Map<String, String> systemsToQuery_people;
	
	@Resource
	Map<String, String> systemsToQuery_people_disabled;

	@Resource
	PersonSearchCommandValidator personSearchCommandValidator;

	@Resource
	PersonFilterCommandValidator personFilterCommandValidator;	

	@Resource
	PersonSearchCommandUtils personSearchCommandUtils;

	@Resource
	SearchResultConverter searchResultConverter;

	@Resource
	UserSession userSession;

	@Resource
	SimpleSearchParser simpleSearchParser;
	
	@Resource
	SamlService samlService;

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(DateTime.class, new DateTimePropertyEditor());
	}

	@RequestMapping(value = "searchForm", method = RequestMethod.GET)
	public String searchForm(@RequestParam(value = "resetForm", required = false) boolean resetForm,
	        @RequestParam(value = "activeSearchTab", defaultValue = "simpleSearchTab") String activeSearchTab,
	        Map<String, Object> model) {

		if (resetForm || userSession.getMostRecentSearch() == null) {
			PersonSearchCommand personSearchCommand = new PersonSearchCommand();
			setInitialState(personSearchCommand);
			userSession.setMostRecentSearch(personSearchCommand);
			model.put("personSearchCommand", personSearchCommand);
		} else {
			model.put("personSearchCommand", userSession.getMostRecentSearch());
		}

		model.put("activeSearchTab", activeSearchTab);

		return personSearchForm;
	}


	@RequestMapping(value = "simpleSearch", method = RequestMethod.POST)
	public String simpleSearch(@ModelAttribute("personSearchCommand") PersonSearchCommand personSearchCommand,
	        BindingResult errors, Map<String, Object> model) throws Exception {
		
		userSession.setMostRecentSearch(personSearchCommand);
		userSession.setMostRecentSearchType(PersonSearchType.SIMPLE);

		PersonSearchRequest personSearchRequest = simpleSearchParser.validateAndParseSimpleSearch(personSearchCommand,
		        errors);

		model.put("activeSearchTab", "simpleSearchTab");

		if (errors.hasErrors()) {
			model.put("errors", errors);
			userSession.setMostRecentSearchResult(null);
			return personSearchForm;
		}

		return performSearchAndReturnResults(model, personSearchRequest); 
	}

	private void populateModelActiveSearchTabFromSession(Map<String, Object> model) {
		PersonSearchType mostRecentSearchType = userSession.getMostRecentSearchType();
		if (PersonSearchType.ADVANCED.equals(mostRecentSearchType)) {
			model.put("activeSearchTab", "advancedSearchTab");
		} else {
			model.put("activeSearchTab", "simpleSearchTab");
		}
	}
	
	String getMostRecentSearchPurpose() {
		return userSession.getMostRecentSearch().getAdvanceSearch().getPurpose();
	}
	
	String getMostRecentSearchOnBehalfOf() {
		return userSession.getMostRecentSearch().getAdvanceSearch().getOnBehalfOf();
	}

	//http://host/ojbc_web_portal/people/filter post
	@RequestMapping(value="filter", method = RequestMethod.POST)
	public String filter(@ModelAttribute("personFilterCommand") PersonFilterCommand personFilterCommand, 
			BindingResult errors, Map<String, Object> model) {
		
		String filterInput;

		personFilterCommandValidator.validate(personFilterCommand, errors);		
				
		if(errors.hasErrors()){
			model.put("errors", errors);
			return "people/_searchFilter";
		}

		//we do not wish to re-filter on filtered results - we always filter on results from a non-filtered search
		if (userSession.getSavedMostRecentSearchResult() == null) {
			userSession.setSavedMostRecentSearchResult(userSession.getMostRecentSearchResult());
			filterInput = userSession.getMostRecentSearchResult();
		} else {
			filterInput = userSession.getSavedMostRecentSearchResult();;
		}
		
		//call a service to filter
		String filteredResult = searchResultConverter.filterXml(filterInput, personFilterCommand);

		//pass the filter results to our xml to html transform
		String htmlResult = searchResultConverter.convertPersonSearchResult(filteredResult, getParams(getMostRecentSearchPurpose(), getMostRecentSearchOnBehalfOf()));
		
		//saving filtered results allows pagination to function:
		userSession.setMostRecentSearchResult(filteredResult);	
		
		populateModelActiveSearchTabFromSession(model);
		
		//put it in the model
		model.put("searchContent", htmlResult);
		return personSearchResultPage;
	}
	
    @RequestMapping(value="clearFilter", method = RequestMethod.GET)
    public String clearFilter( Map<String, Object> model ) {
        
        //reset the mostRecentSearchResult. 
        if (userSession.getSavedMostRecentSearchResult() != null) {
            userSession.setMostRecentSearchResult(userSession.getSavedMostRecentSearchResult()); 
        } 
                
        String htmlResult = searchResultConverter.convertPersonSearchResult(
                userSession.getMostRecentSearchResult(), 
                getParams(getMostRecentSearchPurpose(), getMostRecentSearchOnBehalfOf()));
        
        populateModelActiveSearchTabFromSession(model);
        
        //put it in the model
        model.put("searchContent", htmlResult);
        return personSearchResultPage;
    }
	
	@RequestMapping(value = "advanceSearch", method = RequestMethod.POST)
	public String advanceSearch(@ModelAttribute("personSearchCommand") PersonSearchCommand personSearchCommand,
	        BindingResult errors, Map<String, Object> model) throws Exception {
		userSession.setMostRecentSearch(personSearchCommand);
		userSession.setMostRecentSearchType(personSearchCommand.getSearchType());

		personSearchCommandValidator.validate(personSearchCommand, errors);

        model.put("activeSearchTab", personSearchCommand.getSearchType().name().toLowerCase() + "SearchTab");

		if (errors.hasErrors()) {
			model.put("errors", errors);
			userSession.setMostRecentSearchResult(null);
			return personSearchForm;
		}

		PersonSearchRequest personSearchRequest = personSearchCommandUtils.getPersonSearchRequest(personSearchCommand);

		return performSearchAndReturnResults(model, personSearchRequest);
	}

	@RequestMapping(value = "searchDetails", method = RequestMethod.GET)
	public String searchDetails(HttpServletRequest request, @RequestParam String systemName,
	        @ModelAttribute("detailsRequest") DetailsRequest detailsRequest, Map<String, Object> model) {
		try {
			processDetailRequest(request, systemName, detailsRequest, model);
			return "people/_searchDetails";
		} catch (Exception ex) {
			ex.printStackTrace();
			return "common/_searchDetailsError";
		}
	}

	@RequestMapping(value = "incidentDetails", method = RequestMethod.GET)
	public String incidentDetails(HttpServletRequest request, @RequestParam String systemName,
	        @ModelAttribute("detailsRequest") DetailsRequest detailsRequest, Map<String, Object> model)
	        throws Exception {
		processDetailRequest(request, systemName, detailsRequest, model);

		return "people/_incidentDetails";
	}
	
    @ModelAttribute
    public void setupModelAttributes(Model model) {
        model.addAttribute("placements", placements);
        model.addAttribute("states", states);
    }
    
	@ModelAttribute("races")
	public Map<String, String> getRaces() {
		return races;
	}

	@ModelAttribute("genders")
	public Map<String, String> getGenders() {
		return genders;
	}

	@ModelAttribute("eyeColors")
	public Map<String, String> getEyeColors() {
		return eyeColors;
	}

	@ModelAttribute("hairColors")
	public Map<String, String> getHairColors() {
		return hairColors;
	}
	
	@ModelAttribute("systemsToQuery")
	public Map<String, String> getSystemsToQuery() {
		return systemsToQuery_people;
	}
	
	@ModelAttribute("systemsToQuery_disabled")
	public Map<String, String> getSystemsToQueryDisabled() {
		return systemsToQuery_people_disabled;
	}

	String getFederatedQueryId() {
		return UUID.randomUUID().toString();
	}

	private void setInitialState(PersonSearchCommand personSearchCommand) {
		personSearchCommand.getAdvanceSearch().setPersonGivenNameMetaData(SearchFieldMetadata.StartsWith);
		personSearchCommand.getAdvanceSearch().setPersonSurNameMetaData(SearchFieldMetadata.StartsWith);
		
	}

	private void processDetailRequest(HttpServletRequest request, String systemName, DetailsRequest detailsRequest, Map<String, Object> model)
			throws Exception {
	    String searchContent = null; 
		if (detailsRequest.isJuvenileDetailRequest() ) {
		    PersonSearchDetailResponses juvenileHistoryDetailResponses = 
		            (PersonSearchDetailResponses) model.get("juvenileHistoryDetailResponses"); 
		    if (juvenileHistoryDetailResponses != null) {
		        searchContent = juvenileHistoryDetailResponses.getDetailResponse(detailsRequest); 
		    }
		    else {
		        juvenileHistoryDetailResponses = new PersonSearchDetailResponses(detailsRequest); 
		    }
		    
		    if (searchContent == null) {
		        searchContent = getDetailResultViaWebService(systemName, detailsRequest); 
		        juvenileHistoryDetailResponses.cacheDetailResponse(detailsRequest, searchContent);
		        model.put("juvenileHistoryDetailResponses", juvenileHistoryDetailResponses);
		    }
		    
		}
		else {
            searchContent = getDetailResultViaWebService(systemName, detailsRequest); 
		}
		
	     String convertedContent = searchResultConverter.convertDetailSearchResult(searchContent, systemName, detailsRequest.getActiveAccordionId());
         model.put("searchContent", convertedContent); 

	}


    private String getDetailResultViaWebService(String systemName, DetailsRequest detailsRequest) throws Exception {
        String searchContent = config.getDetailsQueryBean().invokeRequest(detailsRequest, getFederatedQueryId(), 
		        (Element)getSamlToken());
        return searchContent;
    }

	private Map<String, Object> getParams(String purpose, String onBehalfOf) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("rows", ROWS_PER_PAGE);
		params.put("hrefBase", PAGINATE_URL);
		params.put("purpose", purpose);
		params.put("onBehalfOf", onBehalfOf);
		return params;
	}

	private String performSearchAndReturnResults(Map<String, Object> model, PersonSearchRequest personSearchRequest)
			throws Exception {
		
		if ("On behalf of".equals(personSearchRequest.getOnBehalfOf())) {
			personSearchRequest.setOnBehalfOf(StringUtils.EMPTY);
		}
		
		String searchContent = config.getPersonSearchBean().invokePersonSearchRequest(personSearchRequest,
				getFederatedQueryId(), getSamlToken());
		
		userSession.setMostRecentSearchResult(searchContent);
		userSession.setSavedMostRecentSearchResult(null);
		String convertPersonSearchResult = searchResultConverter.convertPersonSearchResult(searchContent,getParams( personSearchRequest.getPurpose(), personSearchRequest.getOnBehalfOf()));
		model.put("searchContent", convertPersonSearchResult);
		
		return personSearchResultPage;
	}
}
