package org.ojbc.web.portal.validators;

import org.ojbc.web.portal.controllers.dto.PersonFilterCommand;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

@Service
public class PersonFilterCommandValidator {
	
	public void validate(PersonFilterCommand PersonFilterCommand, BindingResult errors) {
		
		if(hasAgeRange(PersonFilterCommand) ){
			Integer startAge = PersonFilterCommand.getFilterAgeRangeStart();
			Integer endAge = PersonFilterCommand.getFilterAgeRangeEnd();
			
			if(startAge != null  && startAge < 0){
				errors.rejectValue("filterAgeRangeStart","startAgeInvalid", "from age in invalid");
			}			
			if(startAge == null  && endAge!=null){
				errors.rejectValue("filterAgeRangeStart","startAgeMissing", "from age missing");
			}	
			if(startAge != null  && endAge==null){
				errors.rejectValue("filterAgeRangeStart","endAgeMissing", "to age missing");
			}							
			if(startAge!= null  && endAge!=null && startAge > endAge ){
				errors.rejectValue("filterAgeRangeStart","endAgeBeforeStart", "from age must be after to age");				
			}			
		}
		
		if(hasHeightRange(PersonFilterCommand) ){
			Integer heightFeet = PersonFilterCommand.getFilterHeightInFeet();
			Integer heightInches = PersonFilterCommand.getFilterHeightInInches();
			Integer heightTolerance = PersonFilterCommand.getFilterHeightTolerance();

			if(heightFeet == null  || heightFeet < 1){
				errors.rejectValue("filterHeightInFeet","filterHeightInFeetInvalid", "height in feet is invalid");
			}	
			if(heightInches == null  || heightInches < 0 ||  heightInches > 11 ){
				errors.rejectValue("filterHeightInFeet","filterHeightInInchesInvalid", "height in inches is invalid");
			}
			if(heightTolerance == null  || heightTolerance < 0){
				errors.rejectValue("filterHeightInFeet","filterHeightToleranceInvalid", "height +/- is invalid");
			}									
			if(heightFeet!=null  && heightInches!=null && heightTolerance!=null ){
				if (  ((heightFeet * 12) + heightInches - heightTolerance) < 1 ) {
					errors.rejectValue("filterHeightInFeet","filterHeightToleranceExcessive", "height +/- is excessive");					
				}				
			}			
		}
		
		if(hasWeightRange(PersonFilterCommand) ){
			Integer weight = PersonFilterCommand.getFilterWeight();
			Integer weightTolerance = PersonFilterCommand.getFilterWeightTolerance();
									
			if(weight == null  || weight < 1){
				errors.rejectValue("filterWeight","filterWeightInvalid", "weight is invalid");
			}	
			if(weightTolerance == null  || weightTolerance < 0){
				errors.rejectValue("filterWeight","filterWeightToleranceInvalid", "weight +/- is invalid");
			}									
			if(weight!=null   && weightTolerance!=null ){
				if ( weight - weightTolerance  < 1 ) {
					errors.rejectValue("filterWeight","filterWeightToleranceExcessive", "weight +/- is excessive");					
				}				
			}			
		}	
		
	}
	
	

	private boolean hasAgeRange(PersonFilterCommand PersonFilterCommand) {
		return PersonFilterCommand.getFilterAgeRangeStart() != null || PersonFilterCommand.getFilterAgeRangeEnd() != null;
	}
	private boolean hasHeightRange(PersonFilterCommand PersonFilterCommand) {
		return PersonFilterCommand.getFilterHeightInFeet() != null || 
				PersonFilterCommand.getFilterHeightInInches() != null ||
				PersonFilterCommand.getFilterHeightTolerance() != null;
	}	
	private boolean hasWeightRange(PersonFilterCommand PersonFilterCommand) {
		return PersonFilterCommand.getFilterWeight() != null || 
				PersonFilterCommand.getFilterWeightTolerance() != null;
	}	
}