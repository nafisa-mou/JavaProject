package com.bloodlink.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;

/**
 * EmailConfiguration - Configure Thymeleaf template engine for email generation
 * 
 * Features:
 * - HTML email template processing
 * - CSS inlining for better email client support
 * - Context-based variable interpolation
 * 
 * OOP: Configuration pattern - Separates email configuration from business logic
 */
@Configuration
@RequiredArgsConstructor
public class EmailConfiguration {

    /**
     * Configure Thymeleaf template resolver for email templates
     */
    @Bean
    public SpringResourceTemplateResolver emailTemplateResolver() {
        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
        resolver.setPrefix("classpath:/templates/emails/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(true);
        return resolver;
    }

    /**
     * Configure Thymeleaf template engine
     */
    @Bean
    public TemplateEngine emailTemplateEngine(SpringResourceTemplateResolver emailTemplateResolver) {
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.addTemplateResolver(emailTemplateResolver);
        engine.setEnableSpringELCompiler(true);
        return engine;
    }
}
