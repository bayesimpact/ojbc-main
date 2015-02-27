<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:b="http://docs.oasis-open.org/wsn/b-2" xmlns:add="http://www.w3.org/2005/08/addressing"
	xmlns:topics="http://ojbc.org/wsn/topics"
	xmlns:pct="http://ojbc.org/IEPD/Exchange/ProbationCaseTermination/1.0"
	xmlns:pcext="http://ojbc.org/IEPD/Extensions/ProbationCase/1.0"
	xmlns:s="http://niem.gov/niem/structures/2.0"
	xmlns:jxdm41="http://niem.gov/niem/domains/jxdm/4.1"
	xmlns:nc20="http://niem.gov/niem/niem-core/2.0"
	xmlns:um="http://ojbc.org/IEPD/Exchange/UnsubscriptionMessage/1.0"
	xmlns:smext="http://ojbc.org/IEPD/Extensions/Subscription/1.0"
	exclude-result-prefixes="pct pcext s"
	>
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	<xsl:param name="topicExpression"/>
	<xsl:variable name="supervisionSupervisorPersonID" select="/pct:ProbationCaseTermination/pcext:ProbationCase/pcext:Supervision/nc20:SupervisionSupervisor/nc20:EntityPerson/@s:id"/>
	<xsl:variable name="officerID">
		<xsl:choose>
			<xsl:when test="/pct:ProbationCaseTermination/pcext:ProbationCase/nc20:Person[@s:id=/pct:ProbationCaseTermination/pcext:ProbationCase/jxdm41:EnforcementOfficial/nc20:RoleOfPersonReference/@s:ref]/@s:id">
				<xsl:value-of select="/pct:ProbationCaseTermination/pcext:ProbationCase/nc20:Person[@s:id=/pct:ProbationCaseTermination/pcext:ProbationCase/jxdm41:EnforcementOfficial/nc20:RoleOfPersonReference/@s:ref]/@s:id"/>
			</xsl:when>
			<xsl:when test="/pct:ProbationCaseTermination/pcext:ProbationCase/jxdm41:Person[@s:id=/pct:ProbationCaseTermination/pcext:ProbationCase/jxdm41:EnforcementOfficial/nc20:RoleOfPersonReference/@s:ref]/@s:id">
				<xsl:value-of select="/pct:ProbationCaseTermination/pcext:ProbationCase/jxdm41:Person[@s:id=/pct:ProbationCaseTermination/pcext:ProbationCase/jxdm41:EnforcementOfficial/nc20:RoleOfPersonReference/@s:ref]/@s:id"/>
			</xsl:when>
		</xsl:choose>
	</xsl:variable>
	<xsl:variable name="supervisionSupervisorOrganizationID" select="/pct:ProbationCaseTermination/pcext:ProbationCase/pcext:Supervision/nc20:SupervisionSupervisor/nc20:EntityOrganization/@s:id"/>
	
	<xsl:template match="pct:ProbationCaseTermination">
		<b:Unsubscribe>
			<!--You may enter ANY elements at this point-->
			<um:UnsubscriptionMessage>
				<smext:Subject>
					<xsl:apply-templates select="pcext:ProbationCase/pcext:Supervision/pcext:Probationer/nc20:PersonBirthDate"/>
					<xsl:apply-templates select="pcext:ProbationCase/pcext:Supervision/pcext:Probationer/nc20:PersonName" />
					<xsl:apply-templates select="pcext:ProbationCase/pcext:Supervision/pcext:Probationer/jxdm41:PersonAugmentation/jxdm41:PersonStateFingerprintIdentification" />
				</smext:Subject>
				<xsl:apply-templates select="pcext:ProbationCase/nc20:PersonContactInformationAssociation[nc20:PersonReference/@s:ref=$supervisionSupervisorPersonID or nc20:PersonReference/@s:ref=$officerID]"/>
				<xsl:apply-templates select="pcext:ProbationCase/nc20:OrganizationContactInformationAssociation[nc20:OrganizationReference/@s:ref=$supervisionSupervisorOrganizationID]"/>
				<smext:SystemName><xsl:value-of select="pcext:SystemName"/></smext:SystemName>
				<smext:SubscriptionQualifierIdentification>
					<nc20:IdentificationID><xsl:value-of select="pcext:ProbationCase/pcext:Supervision/pcext:SupervisionIdentification/nc20:IdentificationID"/></nc20:IdentificationID>
				</smext:SubscriptionQualifierIdentification>				
			</um:UnsubscriptionMessage>
			<b:TopicExpression
				Dialect="http://docs.oasis-open.org/wsn/t-1/TopicExpression/Concrete"
				><xsl:value-of select="$topicExpression"/></b:TopicExpression>
		</b:Unsubscribe>
	</xsl:template>
	<xsl:template match="nc20:PersonContactInformationAssociation">
		<xsl:variable name="contactID" select="nc20:ContactInformationReference/@s:ref"/>
		<xsl:apply-templates select="../nc20:ContactInformation[@s:id=$contactID]"/>
	</xsl:template>
	<xsl:template match="nc20:OrganizationContactInformationAssociation">
		<xsl:variable name="contactID" select="nc20:ContactInformationReference/@s:ref"/>
		<xsl:apply-templates select="../nc20:ContactInformation[@s:id=$contactID]"/>
	</xsl:template>
	<xsl:template match="nc20:ContactInformation">
		<xsl:apply-templates select="nc20:ContactEmailID"/>
	</xsl:template>
	<xsl:template match="nc20:ContactEmailID">
		<nc20:ContactEmailID>
			<xsl:value-of select="normalize-space(.)"/>
		</nc20:ContactEmailID>
	</xsl:template>
	<xsl:template match="nc20:PersonBirthDate">
		<nc20:PersonBirthDate>
			<nc20:Date><xsl:value-of select="nc20:Date"/></nc20:Date>
		</nc20:PersonBirthDate>
	</xsl:template>
	<xsl:template match="nc20:PersonName">
		<nc20:PersonName>
			<nc20:PersonFullName><xsl:value-of select="nc20:PersonGivenName"/><xsl:text> </xsl:text><xsl:value-of select="nc20:PersonSurName"/></nc20:PersonFullName>
			<nc20:PersonGivenName><xsl:value-of select="nc20:PersonGivenName"/></nc20:PersonGivenName>
			<nc20:PersonSurName><xsl:value-of select="nc20:PersonSurName"/></nc20:PersonSurName>
		</nc20:PersonName>
	</xsl:template>
	<xsl:template match="jxdm41:PersonStateFingerprintIdentification">
		<jxdm41:PersonAugmentation>
			<jxdm41:PersonStateFingerprintIdentification><nc20:IdentificationID><xsl:value-of select="normalize-space(nc20:IdentificationID)"/></nc20:IdentificationID></jxdm41:PersonStateFingerprintIdentification>
		</jxdm41:PersonAugmentation>
	</xsl:template>
</xsl:stylesheet>