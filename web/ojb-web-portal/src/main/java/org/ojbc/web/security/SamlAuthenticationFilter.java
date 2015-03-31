package org.ojbc.web.security;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.ojbc.util.camel.security.saml.SAMLTokenUtils;
import org.ojbc.util.model.saml.SamlAttribute;
import org.ojbc.util.xml.XmlUtils;
import org.ojbc.web.portal.services.SamlService;
import org.opensaml.xml.signature.SignatureConstants;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.w3c.dom.Element;

/**
 * @author Haiqi Wei
 */
public class SamlAuthenticationFilter extends AbstractPreAuthenticatedProcessingFilter {
    
    private SamlService samlService;
    
    private Boolean allowQueriesWithoutSAMLToken;

    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        Element samlAssertion = this.extractSAMLAssertion(request);

        String federationId = null;
        if ( samlAssertion != null) {
           try {
            federationId = XmlUtils.xPathStringSearch(samlAssertion,
                        "/saml2:Assertion/saml2:AttributeStatement[1]/"
                        + "saml2:Attribute[@Name='gfipm:2.0:user:FederationId']/saml2:AttributeValue");
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } 

        }
        return federationId;
    }

    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        
        Element samlAssertion = extractSAMLAssertion(request); 
        
        request.setAttribute("samlAssertion", samlAssertion);
        return samlAssertion;
    }

    private Element extractSAMLAssertion(HttpServletRequest request) {
        
        Element samlAssertion = null; 
        
        try {
            samlAssertion = getSamlService().getSamlAssertion(request);
        }
        catch(Exception e) {
            e.printStackTrace(); 
        }
        
        // TODO The following if block is for the development. Will comment out later. 
        if (samlAssertion == null && isAllowQueriesWithoutSAMLToken()) {
            try {
                Map<String, String> customAttributes = new HashMap<String, String>();
                customAttributes.put("gfipm:2.0:user:FederationId", "HIJIS:IDP:HCJDC:USER:haiqi");
//                customAttributes.put(SamlAttribute.FederationId.getAttibuteName(), "HIJIS:IDP:HCJDC:USER:demouser4");
                customAttributes.put(SamlAttribute.EmployerORI.getAttibuteName(), "1234567890");
//                customAttributes.put("gfipm:2.0:user:EmployerORI", "H00000001");
                
                samlAssertion = SAMLTokenUtils.createStaticAssertionAsElement("http://ojbc.org/ADS/AssertionDelegationService", 
                        SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS, 
                        SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1, true, true, customAttributes);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return samlAssertion;
    }

    public Boolean isAllowQueriesWithoutSAMLToken() {
        return allowQueriesWithoutSAMLToken;
    }

    public void setAllowQueriesWithoutSAMLToken(Boolean allowQueriesWithoutSAMLToken) {
        this.allowQueriesWithoutSAMLToken = allowQueriesWithoutSAMLToken;
    }

    public SamlService getSamlService() {
        return samlService;
    }

    public void setSamlService(SamlService samlService) {
        this.samlService = samlService;
    }

    

}