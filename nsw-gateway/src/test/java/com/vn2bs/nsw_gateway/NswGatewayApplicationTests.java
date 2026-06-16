package com.vn2bs.nsw_gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"outbox.publisher.enabled=false",
		"minio.orphan-cleanup.enabled=false"
})
class NswGatewayApplicationTests {

	@Test
	void contextLoads() {
	}

}
