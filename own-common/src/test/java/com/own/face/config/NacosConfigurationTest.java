package com.own.face.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import static org.junit.jupiter.api.Assertions.*;

class NacosConfigurationTest {
    @Test
    void checkedInConfigurationsHaveNoDuplicateKeys() throws IOException {
        for (Path file : configurations()) {
            LoaderOptions options = new LoaderOptions();
            options.setAllowDuplicateKeys(false);
            try (var stream = Files.newInputStream(file)) {
                assertDoesNotThrow(() -> new Yaml(new SafeConstructor(options)).load(stream), file.toString());
            }
        }
    }

    @Test
    void settlementKeepsInboxSecurityAndFinancialSettingsTogether() throws IOException {
        Path file = configurationDirectory().resolve("own-settlement-local.yaml");
        try (var stream = Files.newInputStream(file)) {
            Map<?, ?> root = new Yaml().load(stream);
            Map<?, ?> trade = (Map<?, ?>) root.get("trade");
            assertEquals("settlement-order-effects-v1", ((Map<?, ?>) trade.get("inbox")).get("consumer-name"));
            assertNotNull(((Map<?, ?>) trade.get("internal")).get("service-token"));
            assertNotNull(((Map<?, ?>) trade.get("settlement")).get("merchant-rate"));
        }
    }

    private java.util.List<Path> configurations() throws IOException {
        try (var files = Files.list(configurationDirectory())) {
            return files.filter(path -> path.toString().endsWith(".yaml")).sorted().toList();
        }
    }

    @Test
    void orderReminderAndRetentionSettingsUseTheirActualConsumerNamespaces() throws IOException {
        try (var stream = Files.newInputStream(configurationDirectory().resolve("own-order-local.yaml"))) {
            Map<?, ?> root = new Yaml().load(stream);
            Map<?, ?> trade = (Map<?, ?>) root.get("trade");
            assertEquals(48, ((Map<?, ?>) trade.get("after-sale")).get("audit-reminder-hours"));
            assertEquals(180, ((Map<?, ?>) trade.get("security")).get("audit-retention-days"));
            Map<?, ?> outbox = (Map<?, ?>) trade.get("outbox");
            assertFalse(outbox.containsKey("after-sale"));
            assertFalse(outbox.containsKey("security"));
        }
    }

    private Path configurationDirectory() {
        Path root = Path.of("").toAbsolutePath();
        if (!Files.isDirectory(root.resolve("deploy/nacos/config"))) root = root.getParent();
        Path result = root.resolve("deploy/nacos/config");
        assertTrue(Files.isDirectory(result), "repository Nacos configuration directory must exist");
        return result;
    }
}
