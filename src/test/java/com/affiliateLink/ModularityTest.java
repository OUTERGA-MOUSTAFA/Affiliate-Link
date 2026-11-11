package com.affiliateLink;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModularityTest {

    private final ApplicationModules modules = ApplicationModules.of(AffiliateLinkApplication.class);

    @Test
    void noModuleAccessesTheInternalsOfAnotherAndThereIsNoCycle() {
        modules.verify();
    }

    @Test
    void theThreeExpectedModulesAreDetected() {
        assertThat(modules.getModuleByName("common")).isPresent();
        assertThat(modules.getModuleByName("profile")).isPresent();
        assertThat(modules.getModuleByName("offer")).isPresent();
    }
}