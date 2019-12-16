package com.avinash.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@EnableWebMvc
public class WebConfig extends WebMvcConfigurerAdapter {

	@Override
	public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
	    configurer.favorPathExtension(false).
	            favorParameter(true).
	            parameterName("mediaType").
	            ignoreAcceptHeader(false).
	            useJaf(false).
	            defaultContentType(MediaType.APPLICATION_XML).
	            mediaType("xml", MediaType.APPLICATION_XML).
	            mediaType("json", MediaType.APPLICATION_JSON).
	            mediaType("pdf", MediaType.APPLICATION_PDF).
	            mediaType("html", MediaType.TEXT_HTML);
	  }
}
