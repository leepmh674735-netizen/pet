package com.pet.backend.walk;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class KmaGridConverterTest {

	@Test
	void 서울시청_좌표는_격자_60_127로_변환된다() {
		KmaGridConverter.Grid grid = KmaGridConverter.toGrid(37.5665, 126.9780);

		assertThat(grid.nx()).isEqualTo(60);
		assertThat(grid.ny()).isEqualTo(127);

	}
}
