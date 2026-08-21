package com.pet.backend.prediction;

public interface DiseasePredictionClient {

	DiseasePrediction predict(Long petId);
}
