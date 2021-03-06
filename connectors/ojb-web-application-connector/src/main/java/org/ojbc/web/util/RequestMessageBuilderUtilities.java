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
package org.ojbc.web.util;


import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ojbc.util.camel.helper.OJBUtils;
import org.ojbc.util.helper.NIEMXMLUtils;
import org.ojbc.util.helper.OJBCXMLUtils;
import org.ojbc.util.xml.OjbcNamespaceContext;
import org.ojbc.util.xml.XmlUtils;
import org.ojbc.web.OJBCWebServiceURIs;
import org.ojbc.web.SearchFieldMetadata;
import org.ojbc.web.model.firearm.search.FirearmSearchRequest;
import org.ojbc.web.model.firearm.search.FirearmSearchRequestDomUtils;
import org.ojbc.web.model.incident.search.IncidentSearchRequest;
import org.ojbc.web.model.incident.search.IncidentSearchRequestDomUtils;
import org.ojbc.web.model.person.query.DetailsRequest;
import org.ojbc.web.model.person.search.PersonSearchRequest;
import org.ojbc.web.model.person.search.PersonSearchRequestDomUtils;
import org.ojbc.web.model.subscription.Subscription;
import org.ojbc.web.model.vehicle.search.VehicleSearchRequest;
import org.ojbc.web.model.vehicle.search.VehicleSearchRequestDomUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class contains methods to convert POJOs to XML request documents or to 
 * take a few string arguments and make an XML request document out of them.
 *
 */
public class RequestMessageBuilderUtilities {

	private static final Log log = LogFactory.getLog( RequestMessageBuilderUtilities.class );
	
	private static final OjbcNamespaceContext OJBC_NAMESPACE_CONTEXT = new OjbcNamespaceContext();
	
	private static final String TOPIC_EXPRESSION_DIALECT = "http://docs.oasis-open.org/wsn/t-1/TopicExpression/Concrete";
	
	public static String createPersonQueryRequest(DetailsRequest detailsRequest)
	{
		StringBuffer sb = new StringBuffer();
		
		sb.append("<pqr:PersonRecordRequest xmlns:pqr=\"" + OjbcNamespaceContext.NS_PERSON_QUERY_REQUEST + "\" xmlns:nc20=\"" + OjbcNamespaceContext.NS_NC + "\">");
		sb.append("    <pqr:PersonRecordRequestIdentification >");
		sb.append("        <nc20:IdentificationID>" + detailsRequest.getIdentificationID() + "</nc20:IdentificationID>");
		sb.append("        <nc20:IdentificationSourceText>" + detailsRequest.getIdentificationSourceText() + "</nc20:IdentificationSourceText>");
		sb.append("    </pqr:PersonRecordRequestIdentification>");
				
		sb.append("</pqr:PersonRecordRequest>");
		
		return sb.toString();
	}

	public static String createPersonToIncidentQueryRequest(String identificationID, String identificationSourceText)
	{
		StringBuffer sb = new StringBuffer();
		
		sb.append("<exchange:IncidentPersonSearchRequest");
		sb.append("	xmlns:exchange=\"http://ojbc.org/IEPD/Exchange/IncidentSearchRequest/1.0\"");
		sb.append("	xmlns:ext=\"http://ojbc.org/IEPD/Extensions/IncidentSearchRequest/1.0\"");
		sb.append("	xmlns:nc=\"http://niem.gov/niem/niem-core/2.0\">");
		sb.append("	<nc:Person>");
		sb.append("		<nc:PersonOtherIdentification>");
		sb.append("			<nc:IdentificationID>" + identificationID + "</nc:IdentificationID>");
		sb.append("		</nc:PersonOtherIdentification>");
		sb.append("	</nc:Person>");
		sb.append("	<ext:SourceSystemNameText>" + identificationSourceText + "</ext:SourceSystemNameText>");
		sb.append("</exchange:IncidentPersonSearchRequest>");

		
		return sb.toString();
	}

	public static String createVehicleToIncidentQueryRequest(String identificationID, String identificationSourceText)
	{
		StringBuffer sb = new StringBuffer();
		
		sb.append("<exchange:IncidentVehicleSearchRequest");
		sb.append("	xmlns:exchange=\"http://ojbc.org/IEPD/Exchange/IncidentSearchRequest/1.0\"");
		sb.append("	xmlns:nc=\"http://niem.gov/niem/niem-core/2.0\"");
		sb.append("	xmlns:extVehicle=\"http://ojbc.org/IEPD/Extensions/IncidentVehicleSearchRequest/1.0\"");
		sb.append("	xmlns:ext=\"http://ojbc.org/IEPD/Extensions/IncidentSearchRequest/1.0\">");
		sb.append("		<extVehicle:Vehicle>");
		sb.append("			<extVehicle:VehicleSystemIdentification>");
		sb.append("				<nc:IdentificationID>" + identificationID + "</nc:IdentificationID>");
		sb.append("			</extVehicle:VehicleSystemIdentification>");
		sb.append("		</extVehicle:Vehicle>");
		sb.append("		<ext:SourceSystemNameText>" + identificationSourceText + "</ext:SourceSystemNameText>");
		sb.append("</exchange:IncidentVehicleSearchRequest>");


		
		return sb.toString();
	}
	
	public static String createIncidentReportRequest(String identificationID, String identificationSourceText, String categoryCode)
	{
		StringBuffer sb = new StringBuffer();
		
		sb.append("<exchange:IncidentIdentifierIncidentReportRequest"); 
		sb.append("	xmlns:exchange=\"http://ojbc.org/IEPD/Exchange/IncidentReportRequest/1.0\""); 
		sb.append("	xmlns:extension=\"http://ojbc.org/IEPD/Extensions/IncidentReportRequest/1.0\""); 
		sb.append("	xmlns:nc=\"http://niem.gov/niem/niem-core/2.0\">");
		sb.append("	<extension:Incident>");
		sb.append("	    <nc:ActivityIdentification>");
		sb.append("	    	<nc:IdentificationID>"+ identificationID + "</nc:IdentificationID>");
		sb.append("	    </nc:ActivityIdentification>");
		sb.append("	    <extension:IncidentCategoryCode>" + categoryCode + "</extension:IncidentCategoryCode>");
		sb.append("	</extension:Incident>");
		sb.append(" <extension:SourceSystemNameText>" + identificationSourceText + "</extension:SourceSystemNameText>");
		sb.append("</exchange:IncidentIdentifierIncidentReportRequest>");

		
		return sb.toString();
	}
	
	public static Document createPersonSearchRequest(PersonSearchRequest psr) throws Exception
	{
		Document doc = OJBCXMLUtils.createDocument();
		
		Element personSearchRequestElement = NIEMXMLUtils.createPersonsSearchRequestElement(doc, "SM003");

		Element personElement = PersonSearchRequestDomUtils.createPersonsElement(doc, psr);
		
		personSearchRequestElement.appendChild(personElement);
		doc.appendChild(personSearchRequestElement);
		
		if (psr.getSourceSystems()==null || psr.getSourceSystems().size()==0)
		{
			throw new IllegalStateException("No source systems specified");
		}	
		
		for (String sourceSystemName : psr.getSourceSystems())
		{
			Element sourceSystemNameElement = NIEMXMLUtils.createSourceSystemElement(doc, OjbcNamespaceContext.NS_PERSON_SEARCH_REQUEST_EXT, sourceSystemName);
			personSearchRequestElement.appendChild(sourceSystemNameElement);
		}	
		
		if (psr.getPersonSurNameMetaData() == SearchFieldMetadata.ExactMatch || psr.getPersonGivenNameMetaData() == SearchFieldMetadata.ExactMatch )
		{
			Element exactMatchMetaData = NIEMXMLUtils.createSearchMetaData(doc, OjbcNamespaceContext.NS_PERSON_SEARCH_REQUEST_EXT, SearchFieldMetadata.ExactMatch);
			personSearchRequestElement.appendChild(exactMatchMetaData);
		}	

		if (psr.getPersonSurNameMetaData() == SearchFieldMetadata.StartsWith || psr.getPersonGivenNameMetaData() == SearchFieldMetadata.StartsWith )
		{
			Element startsWithMetaData = NIEMXMLUtils.createSearchMetaData(doc, OjbcNamespaceContext.NS_PERSON_SEARCH_REQUEST_EXT, SearchFieldMetadata.StartsWith);
			personSearchRequestElement.appendChild(startsWithMetaData);
		}	
		
		if (StringUtils.isNotBlank(psr.getOnBehalfOf()) || StringUtils.isNotBlank(psr.getPurpose()))
		{
			Element auditMetaData = NIEMXMLUtils.createSearchMetaDataPurposeOnBehalfOf(doc, OjbcNamespaceContext.NS_PERSON_SEARCH_REQUEST_EXT, "OBO3", psr.getOnBehalfOf(), psr.getPurpose());
			personSearchRequestElement.appendChild(auditMetaData);
		}	

		log.debug("Person Search Request Document: " + OJBUtils.getStringFromDocument(doc));
		
		return doc;
	}
	
	public static Document createVehicleSearchRequest(VehicleSearchRequest vsr) throws Exception
	{
		Document doc = OJBCXMLUtils.createDocument();
		
		Element vehicleSearchRequestElement = VehicleSearchRequestDomUtils.createVehicleSearchRequestElement(doc);

		Element vehicleElment = VehicleSearchRequestDomUtils.createVehicleElement(doc, vsr);

		vehicleSearchRequestElement.appendChild(vehicleElment);
		doc.appendChild(vehicleSearchRequestElement);		
		
		if (vsr.getSourceSystems()==null || vsr.getSourceSystems().size()==0)
		{
			throw new IllegalStateException("No source systems specified");
		}	

		for (String sourceSystemName : vsr.getSourceSystems())
		{
			Element sourceSystemNameElement = NIEMXMLUtils.createSourceSystemElement(doc, OjbcNamespaceContext.NS_VEHICLE_SEARCH_REQUEST_EXT, sourceSystemName);
			vehicleSearchRequestElement.appendChild(sourceSystemNameElement);
		}	

		log.debug("Vehicle Search Request Document: " + OJBUtils.getStringFromDocument(doc));
		
		return doc;
	}
	
	public static Document createIncidentSearchRequest(IncidentSearchRequest isr, String cityTownCodelistNamespace, String cityTownCodelistElementName) throws Exception {
		Document doc = OJBCXMLUtils.createDocument();
		
		Element incidentSearchRequestElement = IncidentSearchRequestDomUtils.createIncidentSearchRequestElement(doc);
		
		Element incidentElement = IncidentSearchRequestDomUtils.createIncidentElement(doc, isr);
		
		Element locationElement = IncidentSearchRequestDomUtils.createLocationElement(doc, isr, cityTownCodelistNamespace, cityTownCodelistElementName);
		
		Element activityLocationAssociation = NIEMXMLUtils.createActivityLocationAssociationElement(doc, "I001", "L001");
		
		incidentSearchRequestElement.appendChild(incidentElement);
		incidentSearchRequestElement.appendChild(locationElement);
		incidentSearchRequestElement.appendChild(activityLocationAssociation);

		if (isr.getSourceSystems()==null || isr.getSourceSystems().size()==0)
		{
			throw new IllegalStateException("No source systems specified");
		}	

		for (String sourceSystemName : isr.getSourceSystems())
		{
			Element sourceSystemNameElement = NIEMXMLUtils.createSourceSystemElement(doc, OjbcNamespaceContext.NS_INCIDENT_SEARCH_REQUEST_EXT, sourceSystemName);
			incidentSearchRequestElement.appendChild(sourceSystemNameElement);
		}	
		
		if (StringUtils.isNotBlank(isr.getOnBehalfOf()) || StringUtils.isNotBlank(isr.getPurpose()))
		{
			Element auditMetaData = NIEMXMLUtils.createSearchMetaDataPurposeOnBehalfOf(doc, OjbcNamespaceContext.NS_INCIDENT_SEARCH_REQUEST_EXT, "SM003", isr.getOnBehalfOf(), isr.getPurpose());
			incidentSearchRequestElement.appendChild(auditMetaData);
		}	
		
		doc.appendChild(incidentSearchRequestElement);
		
		return doc;
	}
	
	public static Document createFirearmSearchRequest(FirearmSearchRequest fsr) throws Exception {
		Document doc = OJBCXMLUtils.createDocument();
		
		Element firearmSearchRequestElement = FirearmSearchRequestDomUtils.createFirearmSearchRequestElement(doc);
		
		Element firearm = FirearmSearchRequestDomUtils.createFirearmElement(doc, fsr);
		firearmSearchRequestElement.appendChild(firearm);
		
		Element firearmItemRegistration = NIEMXMLUtils.createFirearmItemRegistration(doc, "REGISTRATION", fsr.getFirearmRegistrationNumber(), fsr.getFirearmCounty(), fsr.getFirearmCurrentRegOnly());
		firearmSearchRequestElement.appendChild(firearmItemRegistration);
	
		Element propertyRegistrationAssociation = NIEMXMLUtils.createPropertyRegistrationAssociationElement(doc, "REGISTRATION", "FIREARM");
		firearmSearchRequestElement.appendChild(propertyRegistrationAssociation);
		
		if (fsr.getSourceSystems()==null || fsr.getSourceSystems().size()==0)
		{
			throw new IllegalStateException("No source systems specified");
		}	

		for (String sourceSystemName : fsr.getSourceSystems())
		{
			Element sourceSystemNameElement = NIEMXMLUtils.createSourceSystemElement(doc, OjbcNamespaceContext.NS_FIREARM_SEARCH_REQUEST_EXT, sourceSystemName);
			firearmSearchRequestElement.appendChild(sourceSystemNameElement);
		}	
		
		Element searchhMetaData = NIEMXMLUtils.createFirearmSearchMetaData(doc, OjbcNamespaceContext.NS_FIREARM_SEARCH_REQUEST_EXT, fsr.getFirearmSerialNumberMetaData());
		firearmSearchRequestElement.appendChild(searchhMetaData);
		
		if (StringUtils.isNotBlank(fsr.getOnBehalfOf()) || StringUtils.isNotBlank(fsr.getPurpose()))
		{
			Element auditMetaData = NIEMXMLUtils.createSearchMetaDataPurposeOnBehalfOf(doc, OjbcNamespaceContext.NS_FIREARM_SEARCH_REQUEST_EXT, "SM003", fsr.getOnBehalfOf(), fsr.getPurpose());
			firearmSearchRequestElement.appendChild(auditMetaData);
		}	
		
		doc.appendChild(firearmSearchRequestElement);
		
		return doc;
	}

	public static String createFirearmQueryRequest(
			DetailsRequest firearmQueryRequest) {
		
		String requestIdSrcTxt = firearmQueryRequest.getIdentificationSourceText().trim();
		
		String rootNode = null;
		String idNode = null;
		
		if(requestIdSrcTxt.contains(OJBCWebServiceURIs.FIREARMS_QUERY_REQUEST_BY_PERSON)){
			
			rootNode = "PersonFirearmRegistrationRequest";
			idNode = "PersonFirearmRegistrationIdentification";
			
		}else if(requestIdSrcTxt.contains(OJBCWebServiceURIs.FIREARMS_QUERY_REQUEST_BY_FIREARM)){
			rootNode = "FirearmRegistrationRequest";
			idNode = "FirearmRegistrationIdentification";
			
		}
		
		
		StringBuffer sb = new StringBuffer();
		
		sb.append("<exchange:" + rootNode);
		sb.append("	xmlns:exchange=\"http://ojbc.org/IEPD/Exchange/FirearmRegistrationQueryRequest/1.0\""); 
		sb.append("	xmlns:extension=\"http://ojbc.org/IEPD/Extension/FirearmRegistrationQueryRequest/1.0\""); 
		sb.append("	xmlns:nc=\"http://niem.gov/niem/niem-core/2.0\">");
		sb.append("    <extension:" + idNode + ">");
		sb.append("        <nc:IdentificationID>" + firearmQueryRequest.getIdentificationID() + "</nc:IdentificationID>");
		sb.append("        <nc:IdentificationSourceText>" + firearmQueryRequest.getIdentificationSourceText() + "</nc:IdentificationSourceText>");
		sb.append("    </extension:" + idNode + ">");
		
		if (StringUtils.isNotBlank(firearmQueryRequest.getOnBehalfOf()) || StringUtils.isNotBlank(firearmQueryRequest.getPurpose()))
		{
			if (StringUtils.isNotBlank(firearmQueryRequest.getOnBehalfOf()))
			{
				log.debug("Add on behalf to message here");
			}	
					
			if (StringUtils.isNotBlank(firearmQueryRequest.getPurpose()))
			{
				log.debug("Add purpose to message here");
			}	
		}	
		
		sb.append("</exchange:" + rootNode + ">");
		
		return sb.toString();
	}
	
	public static Document createSubscriptionSearchRequest() throws Exception {
		Document doc = OJBCXMLUtils.createDocument();

        Element root = doc.createElementNS(OjbcNamespaceContext.NS_SUBSCRIPTION_SEARCH_REQUEST, "SubscriptionSearchRequest");
        doc.appendChild(root);
        root.setPrefix(OjbcNamespaceContext.NS_PREFIX_SUBSCRIPTION_SEARCH_REQUEST);
        
        OJBC_NAMESPACE_CONTEXT.populateRootNamespaceDeclarations(root);
		
		return doc;

	}

	public static Document createSubscriptionQueryRequest(
			DetailsRequest subscriptionQueryRequest) throws Exception {
		Document doc = OJBCXMLUtils.createDocument();

        Element root = doc.createElementNS(OjbcNamespaceContext.NS_SUBSCRIPTION_QUERY_REQUEST, "SubscriptionQueryRequest");
        doc.appendChild(root);
        root.setPrefix(OjbcNamespaceContext.NS_PREFIX_SUBSCRIPTION_QUERY_REQUEST);
        
        Element subIdentification = XmlUtils.appendElement(root, OjbcNamespaceContext.NS_SUBSCRIPTION_QUERY_REQUEST_EXT, "SubscriptionIdentification");
        
        Element identificationId = XmlUtils.appendElement(subIdentification, OjbcNamespaceContext.NS_NC, "IdentificationID");
        identificationId.setTextContent(subscriptionQueryRequest.getIdentificationID());
        
        Element identificationSourceText = XmlUtils.appendElement(subIdentification, OjbcNamespaceContext.NS_NC, "IdentificationSourceText");
        identificationSourceText.setTextContent(subscriptionQueryRequest.getIdentificationSourceText());

        OJBC_NAMESPACE_CONTEXT.populateRootNamespaceDeclarations(root);

		return doc;
	}

	public static Document createUnubscriptionRequest(String subscriptionIdentificationId, String topic) throws Exception{
		
		Document doc = OJBCXMLUtils.createDocument();
        Element root = doc.createElementNS(OjbcNamespaceContext.NS_B2, "Unsubscribe");
        doc.appendChild(root);
        root.setPrefix(OjbcNamespaceContext.NS_PREFIX_B2);
		
        Element unsubscriptionMessage = XmlUtils.appendElement(root, OjbcNamespaceContext.NS_UNBSUB_MSG_EXCHANGE, "UnsubscriptionMessage");
        
        Element subscriptionIdentification = XmlUtils.appendElement(unsubscriptionMessage, OjbcNamespaceContext.NS_SUB_MSG_EXT, "SubscriptionIdentification");
        
        Element identificationID = XmlUtils.appendElement(subscriptionIdentification, OjbcNamespaceContext.NS_NC, "IdentificationID");
        identificationID.setTextContent(subscriptionIdentificationId);
        
		Element topicExpNode = XmlUtils.appendElement(root, OjbcNamespaceContext.NS_B2, "TopicExpression");		
		XmlUtils.addAttribute(topicExpNode, null, "Dialect", TOPIC_EXPRESSION_DIALECT);
		
		topicExpNode.setTextContent(topic);
                
		return doc;
	}
	
	
	public static Document createValidateSubscriptionRequest(String subscriptionId, String topic) throws Exception{
		
		Document doc = OJBCXMLUtils.createDocument();		
		Element rootElement = doc.createElementNS(OjbcNamespaceContext.NS_B2, "Validate");
		doc.appendChild(rootElement);
		rootElement.setPrefix(OjbcNamespaceContext.NS_PREFIX_B2);
		
		Element subValidMsgElement = XmlUtils.appendElement(rootElement, OjbcNamespaceContext.NS_SUB_VALID_MESSAGE, "SubscriptionValidationMessage");
		
		Element subIdElement = XmlUtils.appendElement(subValidMsgElement, OjbcNamespaceContext.NS_SUB_MSG_EXT, "SubscriptionIdentification");
		
		Element identificationIDElement = XmlUtils.appendElement(subIdElement, OjbcNamespaceContext.NS_NC, "IdentificationID");
		
		identificationIDElement.setTextContent(subscriptionId);
						
		Element topicElement = XmlUtils.appendElement(rootElement, OjbcNamespaceContext.NS_B2, "TopicExpression");
		XmlUtils.addAttribute(topicElement, null, "Dialect", TOPIC_EXPRESSION_DIALECT);
		
		topicElement.setTextContent(topic);
						
		return doc;
	}
	

	public static Document createSubscriptionRequest(Subscription subscription) throws Exception{

		SubscriptionDocumentBuilder subscriptionDocumentBuilder = new SubscriptionDocumentBuilder();		
		Document subAddReqDoc = subscriptionDocumentBuilder.buildSubscribeDoc(subscription);		
        XmlUtils.printNode(subAddReqDoc);
        
		return subAddReqDoc;
	}

    public static Document createPolicyAcknowledgementRecordingRequest() throws Exception {
        Document doc = OJBCXMLUtils.createDocument();       
        Element rootElement = doc.createElementNS(OjbcNamespaceContext.NS_ACKNOWLEGEMENT_RECORDING_REQUEST_EXCHANGE, 
                OjbcNamespaceContext.NS_PREFIX_ACKNOWLEGEMENT_RECORDING_REQUEST_EXCHANGE 
                +":AcknowledgementRecordingRequestForAllPolicies");
        doc.appendChild(rootElement);
        
        rootElement.setAttribute("xmlns:" + OjbcNamespaceContext.NS_PREFIX_ACKNOWLEGEMENT_RECORDING_REQUEST_EXCHANGE, 
                OjbcNamespaceContext.NS_ACKNOWLEGEMENT_RECORDING_REQUEST_EXCHANGE);
        rootElement.setAttribute("xmlns:"+ OjbcNamespaceContext.NS_PREFIX_ACKNOWLEGEMENT_RECORDING_REQUEST_EXT, 
                OjbcNamespaceContext.NS_ACKNOWLEGEMENT_RECORDING_REQUEST_EXT);
        rootElement.setAttribute("xmlns:"+ OjbcNamespaceContext.NS_PREFIX_NC_30, OjbcNamespaceContext.NS_NC_30);
        
        Element documentCreationDate = XmlUtils.appendElement(rootElement, OjbcNamespaceContext.NS_NC_30, "DocumentCreationDate");
        Element date = XmlUtils.appendElement(documentCreationDate, OjbcNamespaceContext.NS_NC_30, "Date"); 
        date.setTextContent(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        
        Element policyAcknowledgementIndicator = XmlUtils.appendElement(rootElement, 
                OjbcNamespaceContext.NS_ACKNOWLEGEMENT_RECORDING_REQUEST_EXT, 
                "PolicyAcknowledgementIndicator");
        policyAcknowledgementIndicator.setTextContent("true");
        return doc;
    }
    
    public static Document createJuvenileQueryRequest(String identificationID, String identificationSourceText) throws Exception
    {
    	Document document = OJBCXMLUtils.createDocument();
    	
        Element root = document.createElementNS(OjbcNamespaceContext.NS_JUVENILE_HISTORY_QUERY_REQUEST_DOC, "JuvenileHistoryQuery");
        document.appendChild(root);
        root.setPrefix(OjbcNamespaceContext.NS_PREFIX_JUVENILE_HISTORY_QUERY_REQUEST_DOC);
        
        OJBC_NAMESPACE_CONTEXT.populateRootNamespaceDeclarations(root);
		
        Element juvenileHistoryQueryCriteria = XmlUtils.appendElement(root, OjbcNamespaceContext.NS_JUVENILE_HISTORY_EXT, "JuvenileHistoryQueryCriteria");
        
        Element juvenileInformationRecordID = XmlUtils.appendElement(juvenileHistoryQueryCriteria, OjbcNamespaceContext.NS_JUVENILE_HISTORY_EXT, "JuvenileInformationRecordID");
        
        Element identificationIDElement = XmlUtils.appendElement(juvenileInformationRecordID, OjbcNamespaceContext.NS_NC_30, "IdentificationID");
        identificationIDElement.setTextContent(identificationID);

        Element identificationSourceTextElement = XmlUtils.appendElement(juvenileInformationRecordID, OjbcNamespaceContext.NS_NC_30, "IdentificationSourceText");
        identificationSourceTextElement.setTextContent(identificationSourceText);
        
		return document;
    	
    }
    

    public static Document createPolicyBasedAccessControlRequest(Element samlToken, String requestedResource) throws Exception {
        Document document = OJBCXMLUtils.createDocument();       
        Element rootElement = document.createElementNS(OjbcNamespaceContext.NS_ACCESS_CONTROL_REQUEST, 
                OjbcNamespaceContext.NS_PREFIX_ACCESS_CONTROL_REQUEST 
                +":IdentityBasedAccessControlRequest");
        document.appendChild(rootElement); 
        rootElement.setAttribute("xmlns:" + OjbcNamespaceContext.NS_PREFIX_ACCESS_CONTROL_REQUEST, 
                OjbcNamespaceContext.NS_ACCESS_CONTROL_REQUEST);
        rootElement.setAttribute("xmlns:"+ OjbcNamespaceContext.NS_PREFIX_ACCESS_CONTROL_REQUEST_EXT, 
                OjbcNamespaceContext.NS_ACCESS_CONTROL_REQUEST_EXT);
        rootElement.setAttribute("xmlns:"+ OjbcNamespaceContext.NS_PREFIX_PROXY_XSD_30, 
                OjbcNamespaceContext.NS_PROXY_XSD_30);
        rootElement.setAttribute("xmlns:"+ OjbcNamespaceContext.NS_PREFIX_STRUCTURES_30, 
                OjbcNamespaceContext.NS_STRUCTURES_30);
        rootElement.setAttribute("xmlns:"+ OjbcNamespaceContext.NS_PREFIX_NC_30, OjbcNamespaceContext.NS_NC_30);
        rootElement.setAttribute("xmlns:"+ OjbcNamespaceContext.NS_PREFIX_JXDM_50, OjbcNamespaceContext.NS_JXDM_50);
        
        Element personIdentity = XmlUtils.appendElement(rootElement, 
                OjbcNamespaceContext.NS_ACCESS_CONTROL_REQUEST_EXT, "PersonIdentity"); 
        
        Element identityPersonRepresentation = XmlUtils.appendElement(personIdentity, 
                OjbcNamespaceContext.NS_NC_30, "IdentityPersonRepresentation"); 
        XmlUtils.addAttribute(identityPersonRepresentation, 
                OjbcNamespaceContext.NS_STRUCTURES_30, "id", "P001"); 
        Element personName = XmlUtils.appendElement(identityPersonRepresentation, 
                OjbcNamespaceContext.NS_NC_30, "PersonName"); 
        Element personGivenName = XmlUtils.appendElement(personName, 
                OjbcNamespaceContext.NS_NC_30, "PersonGivenName");
        String userFirstName = XmlUtils.xPathStringSearch(samlToken,
                "/saml2:Assertion/saml2:AttributeStatement[1]/"
                + "saml2:Attribute[@Name='gfipm:2.0:user:GivenName']/saml2:AttributeValue"); 
        personGivenName.setTextContent(userFirstName);
        
        Element personSurName = XmlUtils.appendElement(personName, 
                OjbcNamespaceContext.NS_NC_30, "PersonSurName");
        String userLastName = XmlUtils.xPathStringSearch(samlToken,
                "/saml2:Assertion/saml2:AttributeStatement[1]/"
                + "saml2:Attribute[@Name='gfipm:2.0:user:SurName']/saml2:AttributeValue"); 
        personSurName.setTextContent(userLastName);
        
        Element identityIdentification = XmlUtils.appendElement(personIdentity, 
                OjbcNamespaceContext.NS_ACCESS_CONTROL_REQUEST_EXT, "IdentityIdentification");
        Element identificationID = XmlUtils.appendElement(identityIdentification, 
                OjbcNamespaceContext.NS_NC_30, "IdentificationID");
        String federationId = XmlUtils.xPathStringSearch(samlToken,
                "/saml2:Assertion/saml2:AttributeStatement[1]/"
                + "saml2:Attribute[@Name='gfipm:2.0:user:FederationId']/saml2:AttributeValue"); 
        identificationID.setTextContent(federationId);
        
        Element federatedQueryUserIndicator = XmlUtils.appendElement(personIdentity, 
                OjbcNamespaceContext.NS_ACCESS_CONTROL_REQUEST_EXT, "FederatedQueryUserIndicator");
        String federatedQueryUserIndicatorValue = XmlUtils.xPathStringSearch(samlToken,
                "/saml2:Assertion/saml2:AttributeStatement[1]/"
                + "saml2:Attribute[@Name='gfipm:ext:user:FederatedQueryUserIndicator']/saml2:AttributeValue"); 
        federatedQueryUserIndicator.setTextContent(federatedQueryUserIndicatorValue);
        
        Element contactInformationAssociation = XmlUtils.appendElement(personIdentity, 
                OjbcNamespaceContext.NS_NC_30, "ContactInformationAssociation");
        Element contactEntity = XmlUtils.appendElement(contactInformationAssociation, 
                OjbcNamespaceContext.NS_NC_30, "ContactEntity");
        Element entityPerson = XmlUtils.appendElement(contactEntity, 
                OjbcNamespaceContext.NS_NC_30, "EntityPerson");
        XmlUtils.addAttribute(entityPerson, OjbcNamespaceContext.NS_STRUCTURES_30, "ref", "P001");
        
        Element contactInformation = XmlUtils.appendElement(contactInformationAssociation, 
                OjbcNamespaceContext.NS_NC_30, "ContactInformation");
        Element contactEmailID = XmlUtils.appendElement(contactInformation, 
                OjbcNamespaceContext.NS_NC_30, "ContactEmailID");
        String email = XmlUtils.xPathStringSearch(samlToken,
                "/saml2:Assertion/saml2:AttributeStatement[1]/"
                + "saml2:Attribute[@Name='gfipm:2.0:user:EmailAddressText']/saml2:AttributeValue");
        contactEmailID.setTextContent(email);
        
        Element contactTelephoneNumber = XmlUtils.appendElement(contactInformation, 
                OjbcNamespaceContext.NS_NC_30, "ContactTelephoneNumber");
        Element fullTelephoneNumber = XmlUtils.appendElement(contactTelephoneNumber, 
                OjbcNamespaceContext.NS_NC_30, "FullTelephoneNumber");
        Element telephoneNumberFullID = XmlUtils.appendElement(fullTelephoneNumber, 
                OjbcNamespaceContext.NS_NC_30, "TelephoneNumberFullID");
        String telephoneNumber = XmlUtils.xPathStringSearch(samlToken,
                "/saml2:Assertion/saml2:AttributeStatement[1]/"
                + "saml2:Attribute[@Name='gfipm:2.0:user:TelephoneNumber']/saml2:AttributeValue"); 
        telephoneNumberFullID.setTextContent(telephoneNumber);
        
        Element personEmploymentAssociation = XmlUtils.appendElement(personIdentity, 
                OjbcNamespaceContext.NS_NC_30, "PersonEmploymentAssociation"); 
        Element employee  = XmlUtils.appendElement(personEmploymentAssociation, 
                OjbcNamespaceContext.NS_NC_30, "Employee"); 
        XmlUtils.addAttribute(employee, OjbcNamespaceContext.NS_STRUCTURES_30, "ref", "P001");
        
        Element employer = XmlUtils.appendElement(personEmploymentAssociation, 
                OjbcNamespaceContext.NS_NC_30, "Employer"); 
        Element entityOrganization = XmlUtils.appendElement(employer, 
                OjbcNamespaceContext.NS_NC_30, "EntityOrganization"); 
        Element organizationName = XmlUtils.appendElement(entityOrganization, 
                OjbcNamespaceContext.NS_NC_30, "OrganizationName"); 
        String userAgency = XmlUtils.xPathStringSearch(samlToken,
                "/saml2:Assertion/saml2:AttributeStatement[1]/"
                + "saml2:Attribute[@Name='gfipm:2.0:user:EmployerName']/saml2:AttributeValue"); 
        organizationName.setTextContent(userAgency);
        
        Element organizationUnitName = XmlUtils.appendElement(entityOrganization, 
                OjbcNamespaceContext.NS_NC_30, "OrganizationUnitName"); 
        String employerSubUnitName = XmlUtils.xPathStringSearch(samlToken, 
                "/saml2:Assertion/saml2:AttributeStatement[1]/"
                + "saml2:Attribute[@Name='gfipm:2.0:user:EmployerSubUnitName']/saml2:AttributeValue");  
        organizationUnitName.setTextContent(employerSubUnitName);
        
        Element organizationAugmentation = XmlUtils.appendElement(entityOrganization, 
                OjbcNamespaceContext.NS_JXDM_50, "OrganizationAugmentation"); 
        Element organizationORIIdentification = XmlUtils.appendElement(organizationAugmentation, 
                OjbcNamespaceContext.NS_JXDM_50, "OrganizationORIIdentification"); 
        Element oriIdentificationID = XmlUtils.appendElement(organizationORIIdentification, 
                OjbcNamespaceContext.NS_NC_30, "IdentificationID"); 
        String ori = XmlUtils.xPathStringSearch(samlToken, 
                "/saml2:Assertion/saml2:AttributeStatement[1]/"
                + "saml2:Attribute[@Name='gfipm:2.0:user:EmployerORI']/saml2:AttributeValue");  
        oriIdentificationID.setTextContent(ori);

        Element criminalJusticeOrganizationIndicator = XmlUtils.appendElement(entityOrganization, 
                OjbcNamespaceContext.NS_ACCESS_CONTROL_REQUEST_EXT, "CriminalJusticeOrganizationIndicator"); 
        String criminalJusticeOrganizationIndicatorValue = XmlUtils.xPathStringSearch(samlToken, 
                "/saml2:Assertion/saml2:AttributeStatement[1]/"
                + "saml2:Attribute[@Name='gfipm:ext:user:CriminalJusticeEmployerIndicator']/saml2:AttributeValue"); 
        criminalJusticeOrganizationIndicator.setTextContent(criminalJusticeOrganizationIndicatorValue);

        Element lawEnforcementOrganizationIndicator = XmlUtils.appendElement(entityOrganization, 
                OjbcNamespaceContext.NS_ACCESS_CONTROL_REQUEST_EXT, "LawEnforcementOrganizationIndicator"); 
        String lawEnforcementOrganizationIndicatorValue = XmlUtils.xPathStringSearch(samlToken, 
                "/saml2:Assertion/saml2:AttributeStatement[1]/"
                        + "saml2:Attribute[@Name='gfipm:ext:user:LawEnforcementEmployerIndicator']/saml2:AttributeValue");
        lawEnforcementOrganizationIndicator.setTextContent(lawEnforcementOrganizationIndicatorValue);
        
        Element employeePositionName  = XmlUtils.appendElement(personEmploymentAssociation, 
                OjbcNamespaceContext.NS_NC_30, "EmployeePositionName"); 
        String employeePositionNameValue = XmlUtils.xPathStringSearch(samlToken, 
                "/saml2:Assertion/saml2:AttributeStatement[1]/"
                + "saml2:Attribute[@Name='gfipm:2.0:user:EmployeePositionName']/saml2:AttributeValue");
        employeePositionName.setTextContent(employeePositionNameValue);
        
        Element requestedResourceURI = XmlUtils.appendElement(rootElement, 
                OjbcNamespaceContext.NS_ACCESS_CONTROL_REQUEST_EXT, "RequestedResourceURI"); 
        requestedResourceURI.setTextContent(requestedResource);

        log.debug("\nCreated Request:\n" + OJBUtils.getStringFromDocument(document));
        return document;
    }	
    
}
