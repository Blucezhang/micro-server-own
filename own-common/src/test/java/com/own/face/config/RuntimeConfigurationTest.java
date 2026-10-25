package com.own.face.config;

import com.own.face.trade.config.ServiceEndpoint;
import com.own.face.trade.config.TradeHttpProperties;
import com.own.face.trade.config.TradeRecoveryProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mock.env.MockEnvironment;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import static org.junit.jupiter.api.Assertions.*;

class RuntimeConfigurationTest {
    @Test
    void everyApplicationImportsSharedDefaultsThenLocalDefaultsThenNacos() throws IOException {
        int applications = 0;
        try (var modules = Files.list(repository())) {
            for (Path module : modules.filter(path -> path.getFileName().toString().startsWith("own-")).toList()) {
                Path resources = module.resolve("src/main/resources");
                Path application = resources.resolve("application.yml");
                if (!Files.exists(application)) continue;
                applications++;
                Map<?, ?> spring = (Map<?, ?>) readYaml(application).get("spring");
                List<?> imports = (List<?>) ((Map<?, ?>) spring.get("config")).get("import");
                assertEquals("classpath:trade-runtime.yaml", imports.get(0), module.toString());
                assertTrue(imports.get(imports.size() - 1).toString().startsWith("optional:nacos:"));
                assertNotNull(spring.get("cloud"), "cloud must not be swallowed by the import list");
                for (Object entry : imports) {
                    if (!entry.toString().startsWith("classpath:")) continue;
                    String file = entry.toString().substring("classpath:".length());
                    Path source = file.equals("trade-runtime.yaml")
                            ? repository().resolve("own-common/src/main/resources").resolve(file) : resources.resolve(file);
                    assertTrue(Files.exists(source), source.toString());
                    readYaml(source); // Also rejects duplicate keys in new runtime resources.
                }
            }
        }
        assertEquals(10, applications, "all executable modules must be covered");
    }

    @Test
    void packagedOrderAndSettlementDefaultsBindWithoutJavaFallbacks() throws IOException {
        for (String service : List.of("order", "settlement")) {
            MockEnvironment environment = environment(service);
            TradeHttpProperties http = Binder.get(environment).bind("trade.http", Bindable.of(TradeHttpProperties.class)).get();
            TradeRecoveryProperties recovery = Binder.get(environment).bind("trade.recovery", Bindable.of(TradeRecoveryProperties.class)).get();
            assertEquals(3000, http.connectTimeoutMillis());
            assertEquals(service.equals("order") ? 10000 : 30000, http.readTimeoutMillis());
            assertEquals(8, recovery.maxAttempts());
            assertEquals(30000, recovery.retryDelayMillis());
            assertEquals(30000, recovery.scanDelayMillis());
        }
    }

    @Test
    void deploymentValuesOverridePackagedDefaultsInBoundBeans() throws IOException {
        MockEnvironment environment = environment("order");
        environment.getPropertySources().addFirst(new MapPropertySource("deployment", Map.of(
                "trade.http.connect-timeout-millis", 987,
                "trade.http.read-timeout-millis", 4321,
                "trade.recovery.max-attempts", 3,
                "trade.recovery.retry-delay-millis", 2000,
                "trade.recovery.scan-delay-millis", 5000)));
        new ApplicationContextRunner().withInitializer(context -> context.setEnvironment(environment))
                .withUserConfiguration(BoundConfiguration.class).run(context -> {
                    assertNull(context.getStartupFailure());
                    assertEquals(new TradeHttpProperties(987, 4321), context.getBean(TradeHttpProperties.class));
                    assertEquals(new TradeRecoveryProperties(3, 2000, 5000), context.getBean(TradeRecoveryProperties.class));
                });
    }

    @Test
    void invalidConfigurationFailsStartupInsteadOfDisablingTimeoutsOrSpinningWorkers() throws IOException {
        for (Map<String, Object> invalid : List.<Map<String, Object>>of(
                Map.of("trade.http.connect-timeout-millis", 0),
                Map.of("trade.http.read-timeout-millis", 60001),
                Map.of("trade.recovery.max-attempts", 0),
                Map.of("trade.recovery.retry-delay-millis", 999),
                Map.of("trade.recovery.scan-delay-millis", 86400001))) {
            MockEnvironment environment = environment("order");
            environment.getPropertySources().addFirst(new MapPropertySource("invalid-deployment", invalid));
            new ApplicationContextRunner().withInitializer(context -> context.setEnvironment(environment))
                    .withUserConfiguration(BoundConfiguration.class)
                    .run(context -> assertNotNull(context.getStartupFailure(), invalid.toString()));
        }
    }

    @Test
    void serviceOriginsAreValidatedWithoutMakingNetworkRequests() {
        assertEquals("http://own-inventory", ServiceEndpoint.requireRoot("http://own-inventory/"));
        assertEquals("https://inventory.test:8443", ServiceEndpoint.requireRoot("https://inventory.test:8443"));
        for (String invalid : List.of("", "ftp://inventory", "http://user:secret@inventory", "http://inventory/api",
                "http://inventory?token=secret", "http://inventory#fragment", "http://inventory:0", "http://inventory:65536")) {
            assertThrows(IllegalArgumentException.class, () -> ServiceEndpoint.requireRoot(invalid), invalid);
        }
        assertThrows(IllegalArgumentException.class, () -> ServiceEndpoint.requireRoot(null));
    }

    @Test
    void jwtLifetimeSettingsAreInjectedIntoTheSingleProductionConstructor() throws IOException {
        MockEnvironment environment = environment("order");
        environment.getPropertySources().addFirst(new MapPropertySource("deployment-jwt", Map.of(
                "trade.security.jwt.secret", "01234567890123456789012345678901",
                "trade.security.jwt.expiration-seconds", 123,
                "trade.security.jwt.refresh-expiration-seconds", 456)));
        new ApplicationContextRunner().withInitializer(context -> context.setEnvironment(environment))
                .withBean(com.fasterxml.jackson.databind.ObjectMapper.class)
                .withBean(com.own.face.security.JwtTokenService.class)
                .run(context -> {
                    assertNull(context.getStartupFailure());
                    var service = context.getBean(com.own.face.security.JwtTokenService.class);
                    long now = System.currentTimeMillis();
                    assertTrue(Math.abs(service.expiresAt().getTime() - now - 123000L) < 1000);
                    assertTrue(Math.abs(service.refreshExpiresAt().getTime() - now - 456000L) < 1000);
                });
    }

    @Test
    void productionPlaceholdersDoNotReintroduceNonEmptyRuntimeDefaults() throws IOException {
        var inlineDefault = java.util.regex.Pattern.compile("\\$\\{[\\w.-]+:[^{}]+}");
        try (var modules = Files.list(repository())) {
            for (Path module : modules.filter(path -> path.getFileName().toString().startsWith("own-")).toList()) {
                Path sources = module.resolve("src/main/java");
                if (!Files.isDirectory(sources)) continue;
                try (var files = Files.walk(sources)) {
                    for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
                        assertFalse(inlineDefault.matcher(Files.readString(file)).find(),
                                "move runtime defaults to the owning YAML: " + file);
                    }
                }
            }
        }
    }

    private MockEnvironment environment(String service) throws IOException {
        MockEnvironment environment = new MockEnvironment();
        Path root = repository();
        for (Path resource : List.of(root.resolve("own-common/src/main/resources/trade-runtime.yaml"),
                root.resolve("own-" + service + "/src/main/resources/" + service + "-runtime.yaml"))) {
            new YamlPropertySourceLoader().load(resource.toString(), new FileSystemResource(resource))
                    .forEach(source -> environment.getPropertySources().addFirst(source));
        }
        return environment;
    }

    private Map<?, ?> readYaml(Path path) throws IOException {
        LoaderOptions options = new LoaderOptions();
        options.setAllowDuplicateKeys(false);
        try (var stream = Files.newInputStream(path)) {
            return new Yaml(new SafeConstructor(options)).load(stream);
        }
    }

    private Path repository() {
        Path root = Path.of("").toAbsolutePath();
        return Files.isDirectory(root.resolve("own-common")) ? root : root.getParent();
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties({TradeHttpProperties.class, TradeRecoveryProperties.class})
    static class BoundConfiguration { }
}
