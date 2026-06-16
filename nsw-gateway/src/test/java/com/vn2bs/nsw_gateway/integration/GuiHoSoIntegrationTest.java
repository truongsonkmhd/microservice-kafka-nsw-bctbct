package com.vn2bs.nsw_gateway.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import com.vn2bs.common.domains.OutboxStatus;
import com.vn2bs.common.domains.ThuTuc1.ThuTuc1_GuiHoSo;
import com.vn2bs.common.repositories.OutboxEventRepository;
import com.vn2bs.common.repositories.ThuTuc1.ThuTuc1_GuiHoSoRepository;

@Tag("integration")
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GuiHoSoIntegrationTest {

    @Container
    static MySQLContainer mysql = new MySQLContainer(DockerImageName.parse("mysql:8.0"))
            .withDatabaseName("nsw_adapter")
            .withUsername("root")
            .withPassword("test");

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("apache/kafka-native:3.8.0"));

    @Container
    static GenericContainer<?> minio = new GenericContainer<>(DockerImageName.parse("minio/minio"))
            .withExposedPorts(9000)
            .withEnv("MINIO_ROOT_USER", "minioadmin")
            .withEnv("MINIO_ROOT_PASSWORD", "minioadmin")
            .withCommand("server", "/data");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("minio.url", () -> "http://" + minio.getHost() + ":" + minio.getMappedPort(9000));
        registry.add("spring.liquibase.enabled", () -> "false");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("outbox.publisher.enabled", () -> "false");
        registry.add("minio.orphan-cleanup.enabled", () -> "false");
        registry.add("management.tracing.enabled", () -> "false");
        registry.add("management.zipkin.tracing.export.enabled", () -> "false");
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ThuTuc1_GuiHoSoRepository guiHoSoRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Test
    void guiHoSo_persistsEntityAndOutboxEvent() {
        HttpHeaders jsonHeaders = new HttpHeaders();
        jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> thongTin = new HttpEntity<>("{\"tenNguoiGui\":\"G5 Integration Test\"}", jsonHeaders);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("thongTin", thongTin);

        HttpHeaders multipartHeaders = new HttpHeaders();
        multipartHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/nsw/thu-tuc-1/gui-ho-so",
                new HttpEntity<>(body, multipartHeaders),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("maSoHoSo");

        List<ThuTuc1_GuiHoSo> saved = guiHoSoRepository.findAll();
        assertThat(saved).hasSize(1);
        assertThat(saved.get(0).getTenNguoiGui()).isEqualTo("G5 Integration Test");
        assertThat(saved.get(0).getMaSoHoSo()).startsWith("NSW-");
        assertThat(saved.get(0).getCorrelationId()).isNotBlank();

        assertThat(outboxEventRepository.findAll())
                .anyMatch(e -> e.getStatus() == OutboxStatus.PENDING
                        && e.getAggregateKey().equals(saved.get(0).getMaSoHoSo()));
    }

    @Test
    void guiHoSo_withAttachment_uploadsToMinio() {
        HttpHeaders jsonHeaders = new HttpHeaders();
        jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> thongTin = new HttpEntity<>("{\"tenNguoiGui\":\"G5 With File\"}", jsonHeaders);

        ByteArrayResource fileResource = new ByteArrayResource("sample attachment".getBytes()) {
            @Override
            public String getFilename() {
                return "test.txt";
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("thongTin", thongTin);
        body.add("tepDinhKem", fileResource);

        HttpHeaders multipartHeaders = new HttpHeaders();
        multipartHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/nsw/thu-tuc-1/gui-ho-so",
                new HttpEntity<>(body, multipartHeaders),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ThuTuc1_GuiHoSo entity = guiHoSoRepository.findAll().stream()
                .filter(h -> "G5 With File".equals(h.getTenNguoiGui()))
                .findFirst()
                .orElseThrow();
        assertThat(entity.getTaiLieuDinhKem()).isNotEmpty();
        assertThat(entity.getBucketName()).startsWith("nsw-thutuc1-guihoso");
    }
}
