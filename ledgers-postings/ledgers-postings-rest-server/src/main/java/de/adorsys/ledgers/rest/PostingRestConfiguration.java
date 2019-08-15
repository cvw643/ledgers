package de.adorsys.ledgers.rest;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = {PostingRestBasePackage.class})
public class PostingRestConfiguration {
}
