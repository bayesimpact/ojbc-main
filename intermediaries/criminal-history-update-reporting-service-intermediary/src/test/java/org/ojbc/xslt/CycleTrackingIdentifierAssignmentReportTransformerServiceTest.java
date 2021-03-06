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
package org.ojbc.xslt;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.transform.sax.SAXSource;

import org.apache.commons.io.FileUtils;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Test;
import org.ojbc.util.camel.helper.OJBUtils;
import org.ojbc.util.xml.XmlUtils;
import org.ojbc.util.xml.XsltTransformer;

public class CycleTrackingIdentifierAssignmentReportTransformerServiceTest {
				
	private XsltTransformer xsltTransformer; 
		
	private Logger logger = Logger.getLogger(CycleTrackingIdentifierAssignmentReportTransformerServiceTest.class.getName());

	@Before
	public void setup() throws Exception {
		
		xsltTransformer = new XsltTransformer();
		
		XMLUnit.setIgnoreWhitespace(true);
    	XMLUnit.setIgnoreAttributeOrder(true);
    	XMLUnit.setIgnoreComments(true);				        		
	}

	@Test
	public void cycleTrackingIdentifierAssignmentReportToNotificationTransform() throws Exception{
				
		String xml = XmlUtils.getRootNodeAsString(
				"src/test/resources/xmlInstances/cycleTrackingIdentifierAssignmentReport/Cycle-Tracking-Identifier-Assignment-Report.xml");
		
		String xslt = FileUtils.readFileToString(new File(
				"src/main/resources/xslt/cycleTrackingIdentifierAssignmentReportToNotifications.xsl"));

		Map<String, Object> paramsMap = new HashMap<String, Object>(1);
		paramsMap.put("systemId", "{http://ojbc.org}ProbationChCyleTrackingID");
		
		transformAndValidate(xslt, xml,"output/notifications/notification.xml", paramsMap);
	}
	
	
	private void transformAndValidate(String xslPath, String inputXmlPath, String expectedXMLPath, Map<String,Object> paramsMap) throws Exception {
						
		String expectedXml = XmlUtils.getRootNodeAsString("src/test/resources/xmlInstances/" + expectedXMLPath);
		
		SAXSource inputXmlSaxSource = OJBUtils.createSaxSource(inputXmlPath);
		
		SAXSource xsltSaxSource = OJBUtils.createSaxSource(xslPath);
		
		String convertedResult = xsltTransformer.transform(inputXmlSaxSource, xsltSaxSource, paramsMap);
		
		logger.info("Converted Result:\n" + convertedResult);

		Diff diff = new Diff(expectedXml, convertedResult);
		
		assertTrue(diff.identical());
	}

}

