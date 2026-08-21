package com.pet.backend.walk;

public enum RiskLevel {

	SAFE,
	CAUTION,
	DANGER,
	SEVERE;
	
   static RiskLevel from(double asphaltTemp) {
	   if (asphaltTemp < WalkWeatherConstants.RISK_CAUTION_THRESHOLD) {
		   return SAFE;
	   }
	   if (asphaltTemp < WalkWeatherConstants.RISK_DANGER_THRESHOLD) {
		   return CAUTION;
	   }
	   if (asphaltTemp < WalkWeatherConstants.RISK_SEVERE_THRESHOLD) {
		   return DANGER;
	   }
	   return SEVERE;
   }
}
