package org.ojbc.xslt;

import java.io.File;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ojbc.util.camel.helper.OJBUtils;
import org.ojbc.util.xml.XsltTransformer;

public class ArrestReportingTransformerServiceTest {
			
	private XsltTransformer xsltTransformer;
	
	private static final Log log = LogFactory.getLog(ArrestReportingTransformerServiceTest.class);
	
	@Before
	public void setup() throws ParserConfigurationException{

		XMLUnit.setIgnoreWhitespace(true);
    	XMLUnit.setIgnoreAttributeOrder(true);
    	XMLUnit.setIgnoreComments(true);
    	XMLUnit.setXSLTVersion("2.0");
		
		xsltTransformer = new XsltTransformer();		
	}	

	@Test
	public void arrestReportTest() throws Exception{		
		
		DetailedDiff detailedDiff = runTransform("src/test/resources/xmlInstances/arrestReport/Arrest_Report.xml",
				"src/main/resources/xslt/arrestReportToNotifications.xsl",
				"src/test/resources/xmlInstances/output/notifications/Arrest_Notification.xml");
        
		List<Difference> differenceList = detailedDiff.getAllDifferences();
        
        Assert.assertEquals(detailedDiff.toString(), 0, differenceList.size());						
	}

	@Test
	public void arrestReportTestTelphoneIndividualFields() throws Exception{		
		
		DetailedDiff detailedDiff = runTransform("src/test/resources/xmlInstances/arrestReport/Arrest_Report_Telephone_Individual_Fields.xml",
				"src/main/resources/xslt/arrestReportToNotifications.xsl",
				"src/test/resources/xmlInstances/output/notifications/Arrest_Notification_Telephone_Individual_Fields.xml");
        
		List<Difference> differenceList = detailedDiff.getAllDifferences();
        
        Assert.assertEquals(detailedDiff.toString(), 0, differenceList.size());						
	}

	@Test
	public void arrestReportNoNamespaceTest() throws Exception{		
		
		DetailedDiff detailedDiff = runTransform("src/test/resources/xmlInstances/arrestReport/ArrestReport-With-No-namespace-prefixes.xml",
				"src/main/resources/xslt/arrestReportToNotifications.xsl",
				"src/test/resources/xmlInstances/output/notifications/ArrestReport-With-No-namespace-prefixes-output.xml");
        
		List<Difference> differenceList = detailedDiff.getAllDifferences();
        
        Assert.assertEquals(detailedDiff.toString(), 0, differenceList.size());						
	}
	
	
	private DetailedDiff runTransform(String inputFileClasspath, String xsltClasspath, String expectedOutputFileClasspath) throws Exception{

		File inputFile = new File(inputFileClasspath);		
		String inputXml = FileUtils.readFileToString(inputFile);
		SAXSource inputSaxSource = OJBUtils.createSaxSource(inputXml);
		
		File expectedOutputFile = new File(expectedOutputFileClasspath);
		String expectedXml = FileUtils.readFileToString(expectedOutputFile);		
		
		File xsltFile = new File(xsltClasspath);
		StreamSource xsltSaxSource = new StreamSource(xsltFile);
		
		String actualTransformedResultXml = xsltTransformer.transform(inputSaxSource, xsltSaxSource, null);
		
		log.debug("Transformed Result: " + actualTransformedResultXml);
				
		DetailedDiff detailedDiff = new DetailedDiff(XMLUnit.compareXML(expectedXml, actualTransformedResultXml));	
		
		return detailedDiff;
		
	}
	
}