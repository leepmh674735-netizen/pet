package com.pet.backend.pet;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

public record PetSaveRequest(
	
		@NotBlank(message = "이름은 필수 입니다.")
		@Size(max = 50, message = "이름은 50자 이하여야 합니다.")
		String name,
		
		@Size(max = 50, message = "품종은 50자 이하여야 합니다.")
		String breed,
		
		@PastOrPresent(message = "생년월일은 미래 날짜일 수 없습니다.")
		LocalDate birthDate
		
) {

}
