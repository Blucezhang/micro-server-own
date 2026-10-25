package com.own.file.storage;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.env.MockEnvironment;
import static org.junit.jupiter.api.Assertions.*;

class FileStorageConfigurationTest {
    @Test
    void rootsComeFromPackagedConfigurationAndCanBeOverriddenWithoutEditingJava() throws Exception {
        var environment = new MockEnvironment();
        new YamlPropertySourceLoader().load("file-runtime", new ClassPathResource("file-runtime.yaml"))
                .forEach(source -> environment.getPropertySources().addLast(source));
        var defaults = Binder.get(environment).bind("own.file.storage", Bindable.of(FileStorageProperties.class)).get();
        assertEquals("/opt/temp", defaults.getTemporaryRoot());
        assertEquals("/opt/real", defaults.getPermanentRoot());
        environment.setProperty("own.file.storage.temporary-root", "/tmp/file-fixture/temporary");
        environment.setProperty("own.file.storage.permanent-root", "/tmp/file-fixture/permanent");
        var overrides = Binder.get(environment).bind("own.file.storage", Bindable.of(FileStorageProperties.class)).get();
        assertEquals("/tmp/file-fixture/temporary", overrides.getTemporaryRoot());
        assertEquals("/tmp/file-fixture/permanent", overrides.getPermanentRoot());
    }
}
