"# SpringBootIntro"
======================

Introduction to Spring Restful , Spring JPA , Service Layer , Service domain object , Spring Mvc , Spring Content Negotiation 
==============================================================================================================================

Content Negotiation 
===================

In Java configuration, the strategy can be fully customized using methods on the configurer:


@Configuration
@EnableWebMvc
public class WebConfig extends WebMvcConfigurerAdapter {

  /**
    *  Total customization - see below for explanation.
    */
  @Override
  public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
    configurer.favorPathExtension(false).
            favorParameter(true).
            parameterName("mediaType").
            ignoreAcceptHeader(true).
            useJaf(false).
            defaultContentType(MediaType.APPLICATION_JSON).
            mediaType("xml", MediaType.APPLICATION_XML).
            mediaType("json", MediaType.APPLICATION_JSON);
  }
}

What we did,:
=================
Disabled path extension. Note that favor does not mean use one approach in preference to another, it just enables or disables it. The order of checking is always path extension, parameter, Accept header.
Enable the use of the URL parameter but instead of using the default parameter, format, we will use mediaType instead.
Ignore the Accept header completely. This is often the best approach if most of your clients are actually web-browsers (typically making REST calls via AJAX).
Don't use the JAF, instead specify the media type mappings manually - we only wish to support JSON and XML.



application.properties
======================
#For more logging info of spring
logging.level.root=DEBUG
spring.jpa.hibernate.ddl-auto=none
#Enabling H2 Console
#spring.h2.console.enabled=true
#spring.datasource.initialization-mode=always
#spring.datasource.platform=h2
#spring.datasource.data=classpath:data.sql
spring.jpa.properties.hibernate.hbm2ddl.import_files=data.sql
#For more logging info of spring
logging.level.root=DEBUG



Hibernate not fetching from Second Level Cache if ID column is VARCHAR
======================================================================

Using lazy for properties in Hibernate
=============spring.jpa.hibernate.ddl-auto=none
#Enabling H2 Console
#spring.h2.console.enabled=true
#spring.datasource.initialization-mode=always
#spring.datasource.platform=h2
#spring.datasource.data=classpath:data.sql
spring.jpa.properties.hibernate.hbm2ddl.import_files=data.sql
#For more logging info of spring
logging.level.root=DEBUG===========================

JpaTransactionManager
=====================
Committing JPA transaction on EntityManager

HikariProxyConnection
======================

==>com.zaxxer.hikari.pool.HikariPool        : HikariPool-1 - Pool stats (total=10, active=0, idle=10, waiting=0)


CONDITIONS EVALUATION REPORT
============================


Creating shared instance of singleton bean
===========================================



@Entity
@Table
@Id
@Column
@GeneratedValue
@Repository
@RestController
@RequestMapping
@RequestParam
@PathVariable
@Configuration
@Component
@Service
@Autowired
@RunWith
@WebMvcTest
@MockBean
@EnableWebMvc@SpringBootApplication
@Controller
