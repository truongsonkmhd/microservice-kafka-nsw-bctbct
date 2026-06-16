package com.vn2bs.bct_gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"outbox.publisher.enabled=false",
		"minio.orphan-cleanup.enabled=false"
})
class BctGatewayApplicationTests {

	@Test
	void contextLoads() {
	}

}
