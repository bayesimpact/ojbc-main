package org.ojbc.web.portal.validators;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.ojbc.web.model.subscription.add.SubscriptionAddRequest;

public class ArrestSubscriptionAddValidatorTest {
	
	private ArrestSubscriptionAddValidator validator = new ArrestSubscriptionAddValidator();
	
	@Test
	public void testValidator(){
				
		SubscriptionAddRequest subAddRequest = new SubscriptionAddRequest();
									
		Map<String, String> fieldToErrorMap = validator.getValidationErrorsList(subAddRequest);	
				
		String subTypeError = fieldToErrorMap.get("subscriptionAddRequest.subscriptionType");		
		assertEquals("Subscription type must be specified", subTypeError);
				
		String stateIdError = fieldToErrorMap.get("subscriptionAddRequest.stateId");
		assertEquals("SID must be specified", stateIdError);
		
		String fullNameError =  fieldToErrorMap.get("subscriptionAddRequest.fullName");
		assertEquals("Name must be specified", fullNameError);
		
		String startDateError = fieldToErrorMap.get("subscriptionAddRequest.subscriptionStartDate");
		assertEquals("Start date must be specified", startDateError);		
								
		String emailListError = fieldToErrorMap.get("subscriptionAddRequest.emailList");
		assertEquals("Email Address must be specified", emailListError);				
	}
		
	
	@Test
	public void testValidDates(){
		
		SubscriptionAddRequest subAddRequest = new SubscriptionAddRequest();
		
		Calendar startDateCal = Calendar.getInstance();
		startDateCal.set(2014, 10, 21);
		Date startDate = startDateCal.getTime();
				
		Calendar endDateCal = Calendar.getInstance();
		endDateCal.set(2014, 10, 22);
		Date endDate = endDateCal.getTime();
				
		subAddRequest.setSubscriptionStartDate(startDate);		
		subAddRequest.setSubscriptionEndDate(endDate);			

		Map<String, String> fieldToErrorMap = validator.getValidationErrorsList(subAddRequest);
		
		String endDateError = fieldToErrorMap.get("subscriptionAddRequest.subscriptionEndDate");
		boolean hasEndDateError = StringUtils.isNotBlank(endDateError);
		assertEquals(false, hasEndDateError);						
	}
	
	
	@Test
	public void testEndDateBeforeStartDate(){
		
		SubscriptionAddRequest subAddRequest = new SubscriptionAddRequest();
		
		Calendar startDateCal = Calendar.getInstance();
		startDateCal.set(2014, 10, 21);
		Date startDate = startDateCal.getTime();
				
		Calendar endDateCal = Calendar.getInstance();
		endDateCal.set(2014, 10, 20);
		Date endDate = endDateCal.getTime();
				
		subAddRequest.setSubscriptionStartDate(startDate);		
		subAddRequest.setSubscriptionEndDate(endDate);			

		Map<String, String> fieldToErrorMap = validator.getValidationErrorsList(subAddRequest);
		
		String endDateError = fieldToErrorMap.get("subscriptionAddRequest.subscriptionEndDate");
		assertEquals("End date may not occur before start date", endDateError);		
	}	
	
	
	@Test
	public void testValidatorSuccesses(){
				
		SubscriptionAddRequest subAddReq = new SubscriptionAddRequest();
		
		subAddReq.setSubscriptionType("{http://ojbc.org/wsn/topics}:person/arrest");		
		subAddReq.setStateId("123");		
		subAddReq.setFullName("Homer Simpson");	
		subAddReq.setSubscriptionStartDate(new Date());
		subAddReq.getEmailList().add("hsimpson@gmail.com");
		
		Map<String, String> fieldToErrorMap = validator.getValidationErrorsList(subAddReq);
		
		String subTypeError = fieldToErrorMap.get("subscriptionEditRequest.subscriptionType");		
		boolean hasSubTypeError = StringUtils.isNotBlank(subTypeError);				
		assertEquals(false, hasSubTypeError);
		
		String stateIdError = fieldToErrorMap.get("subscriptionEditRequest.stateId");
		boolean hasStateIdError = StringUtils.isNotBlank(stateIdError);
		assertEquals(false, hasStateIdError);
		
		String fullNameError =  fieldToErrorMap.get("subscriptionEditRequest.fullName");
		boolean hasFullNameError = StringUtils.isNotBlank(fullNameError);
		assertEquals(false, hasFullNameError);
		
		String startDateError = fieldToErrorMap.get("subscriptionEditRequest.subscriptionStartDate");
		boolean hasStartDateError = StringUtils.isNotBlank(startDateError);
		assertEquals(false, hasStartDateError);
		
		String emailListError = fieldToErrorMap.get("subscriptionEditRequest.emailList");
		boolean hasEmailError = StringUtils.isNotBlank(emailListError);
		assertEquals(false, hasEmailError);
	}

}
