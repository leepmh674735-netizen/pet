package com.pet.backend.skin;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkinPredictionDto {
	
	@JsonProperty("class_index")
	@JsonAlias({"class_index", "classIndex"})
	private Integer classIndex;
	
	@JsonProperty("class_name")
	@JsonAlias({"class_name", "className"})
	private String className;
	
	private Double confidence;

}
