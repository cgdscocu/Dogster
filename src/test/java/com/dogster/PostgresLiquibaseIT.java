package com.dogster;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class PostgresLiquibaseIT {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("dogster_test")
            .withUsername("dogster")
            .withPassword("dogster");

    @DynamicPropertySource
    static void registerPostgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("dogster.storage.root-dir", () -> "target/test-uploads/dogster-postgres");
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void liquibaseMigrationsRunAgainstRealPostgres() {
        Integer usersTableCount = jdbcTemplate.queryForObject(
                """
                        select count(*)
                        from information_schema.tables
                        where table_schema = 'public'
                          and table_name = 'users'
                        """,
                Integer.class
        );
        Integer changelogCount = jdbcTemplate.queryForObject("select count(*) from databasechangelog", Integer.class);

        assertThat(usersTableCount).isEqualTo(1);
        assertThat(changelogCount).isGreaterThanOrEqualTo(7);
    }
}
