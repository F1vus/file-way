package net.f1v.fileway.config;

import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.runtime.KieContainer;
import org.kie.internal.io.ResourceFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DroolsConfig {

    private static final String drl_file_path = "rules/fileway/file-acces.drl";

    private static final KieServices kieServices = KieServices.Factory.get();

    @Bean
    public KieContainer kieContainer() {
        KieFileSystem kfs = kieServices.newKieFileSystem();
        kfs.write(ResourceFactory.newClassPathResource(drl_file_path));
        KieBuilder kieBuilder = kieServices.newKieBuilder(kfs).buildAll();
        KieModule kieModule = kieBuilder.getKieModule();
        return kieServices.newKieContainer(kieModule.getReleaseId());
    }
}
