package com.northstar.crm.config;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

@EnableWs
@Configuration
public class WebServiceConfig extends WsConfigurerAdapter {

  @Bean
  public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(
      ApplicationContext applicationContext) {
    MessageDispatcherServlet servlet = new MessageDispatcherServlet();
    servlet.setApplicationContext(applicationContext);
    servlet.setTransformWsdlLocations(true);
    // TODO: confirm mapping "/ws/*"
    return new ServletRegistrationBean<>(servlet, "/ws/*");
  }

  @Bean(name = "customers")
  public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema customersSchema) {
    DefaultWsdl11Definition wsdl = new DefaultWsdl11Definition();
    // TODO: setPortTypeName("CustomersPort")
    // TODO: setLocationUri("/ws")
    // TODO: setTargetNamespace("http://northstar.com/crm/customers")
    // TODO: setSchema(customersSchema)
    return wsdl;
  }

  @Bean
  public XsdSchema customersSchema() {
    return new SimpleXsdSchema(new ClassPathResource("customer.xsd"));
  }
}
