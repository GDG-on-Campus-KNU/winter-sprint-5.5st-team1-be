package com.gdg.sprint.team1;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import software.amazon.awssdk.services.s3.S3Client;

@SpringBootTest
class Team1ApplicationTests {

	@MockBean
	S3Client s3Client;

	@Test
	void contextLoads() {
	}

}
