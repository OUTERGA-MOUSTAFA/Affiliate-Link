package com.affiliateLink.profile;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

import com.affiliateLink.AffiliateLinkApplication;

public class ModularityTest {
    static ApplicationModules modules = ApplicationModules.of(AffiliateLinkApplication.class);

    @Test
    void verifiesModularStructure() {
        modules.forEach(System.out::println);
        modules.verify();// loh exception ila khafti architicture dyale modulith ola bash nshef modules
                         // .forEach(System.out::println); li katlehem modulith
    }
}
