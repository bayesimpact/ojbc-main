<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:er-ext="http://nij.gov/IEPD/Extensions/EntityResolutionExtensions/1.0"
    xmlns:entityMergeRequest="http://nij.gov/IEPD/Exchange/EntityMergeRequestMessage/1.0"
    xmlns:ext="http://ojbc.org/IEPD/Extensions/PersonSearchResults/1.0"
    xmlns:exchange="http://ojbc.org/IEPD/Exchange/PersonSearchResults/1.0"
    xmlns:psr="http://ojbc.org/IEPD/Extensions/PersonSearchResults/1.0"
    xmlns:psr-ext="http://ojbc.org/IEPD/Extensions/PersonSearchResults/1.0"
    xmlns:s="http://niem.gov/niem/structures/2.0"
    xmlns:nc="http://niem.gov/niem/niem-core/2.0"
    xmlns:intel="http://niem.gov/niem/domains/intelligence/2.1" 
    xmlns:jxdm="http://niem.gov/niem/domains/jxdm/4.1"
    exclude-result-prefixes="xs"
    version="2.0">
    
    <xsl:output omit-xml-declaration="yes"/>
    <xsl:output indent="yes" method="xml"/>
    
    <!-- Camel will inject these parameter since it is declared as a Camel header -->
    <!-- See: http://camel.apache.org/xslt.html -->
    <xsl:param name="erAttributeParametersFilePath"/>
    <xsl:param name="entityResolutionRecordThreshold"/>
    
    <xsl:variable name="attributeParameters" select="document($erAttributeParametersFilePath)"/>
    
    <xsl:template match="/OJBAggregateResponseWrapper">
    <entityMergeRequest:EntityMergeRequestMessage >
        <entityMergeRequest:MergeParameters>
        
        	<xsl:copy-of select="$attributeParameters"/>
        
        	<er-ext:EntityResolutionConfiguration>
        		<er-ext:RecordLimit><xsl:value-of select="$entityResolutionRecordThreshold"/></er-ext:RecordLimit>
        	</er-ext:EntityResolutionConfiguration>
        
            <er-ext:EntityContainer>
                
            <xsl:apply-templates select="exchange:PersonSearchResults/ext:PersonSearchResult"/>
                
            </er-ext:EntityContainer>
        </entityMergeRequest:MergeParameters>
    </entityMergeRequest:EntityMergeRequestMessage>   
    </xsl:template>    
    
    <xsl:template match="ext:PersonSearchResult">
        <xsl:call-template name="PersonSearchResult"/>
    </xsl:template>
 
    <xsl:template name="PersonSearchResult">
        <er-ext:Entity>
        	<xsl:attribute name="id" namespace="http://niem.gov/niem/structures/2.0"><xsl:value-of select="generate-id()"/></xsl:attribute>
            <ext:PersonSearchResult>
                <xsl:copy-of select="node()" copy-namespaces="no"/>
            </ext:PersonSearchResult>    
        </er-ext:Entity>    
    </xsl:template>
    
</xsl:stylesheet>