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

This transaction manager is appropriate for applications that use a single JPA EntityManagerFactory for transactional data access. JTA (usually through JtaTransactionManager) is necessary for accessing multiple transactional resources within the same transaction. Note that you need to configure your JPA provider accordingly in order to make it participate in JTA transactions.


## What transaction manager to use

The org.springframework.transaction.PlatformTransactionManager interface is the key abstraction in the Spring API providing essential methods for controlling transaction operations at run-time: begin, commit and rollback.

PlatformTransactionManager interface, its implementations

   ## JtaTransactionManager -----> JTA
   ## DataSourceTransactionManager -----> JDBC
   ## JpaTransactionManager ------> JPA
   ## HibernateTransactionManager ------> Hibernate

it depand on your requirment which moudle of spring your are using





Database Connection Pooling in Java With HikariCP 
==================================================
Connection pooling is a technique used to improve performance in applications with dynamic database-driven content. Opening and closing database connections may not seem like a costly expense, but it can add up rather quickly. Let's assume it takes 5ms to establish a connection and 5ms to execute your query (completely made up numbers), 50% of the time is establishing the connection. Extend this to thousands or tens of thousands of requests and there is a lot of wasted network time. Connection pools are essentially a cache of open database connections. Once you open and use a database connection instead of closing it you add it back to the pool. When you go to fetch a new connection, if there is one available in the pool, it will use that connection instead of establishing another.

## Why Use a Connection Pool?

Constantly opening and closing connections can be expensive. Cache and reuse.
    When activity spikes you can limit the number of connections to the database. This will force code to block until a connection is available. This is especially helpful in distributed environments.
    Split out common operations into multiple pools. For instance, you can have a pool designated for OLAP connections and a pool for OLTP connections each with different configurations.


##  HikariCP

HikariCP is a very fast lightweight Java connection pool. The API and overall codebase are relatively small (a good thing) and highly optimized. It also does not cut corners for performance like many other Java connection pool implementations. The Wiki is highly informative and dives really deep. If you are not as interested in the deep dives, you should at least read and watch the video on connection pool sizing.

## Creating Connection pools

Let's create two connections pools one for OLTP (named transactional) queries and one for OLAP (named processing). We want them split so we can have a queue of reporting queries back up but allow critical transactional queries to still get priority (this is up to the database of course, but we can help a bit). We can also easily configure different timeouts or transaction isolation levels. For now we just change their names and pool sizes.

## Configuring the Pools

HikariCP offers several options for configuring the pool. Since we are fans of roll your own and already created our own Typesafe Configuration, we will reuse that. Notice we are using some of Typesafe's configuration inheritance.

pools {

    default {

        jdbcUrl = "jdbc:hsqldb:mem:testdb"

        maximumPoolSize = 10

        minimumIdle = 2

        username = "SA"

        password = ""

        cachePrepStmts = true

        prepStmtCacheSize = 256

        prepStmtCacheSqlLimit = 2048

        useServerPrepStmts = true

    }

    // This syntax inherits the config from pools.default.

    // We can then override or add additional properties.

    transactional = ${pools.default} {

        poolName = "transactional"

    }

    processing = ${pools.default} {

        poolName = "processing"

        maximumPoolSize = 30

    }

}


## ConnectionPool Factory

Since we don't need any additional state a static factory method passing our config, MetricRegistry and HealthCheckRegistry are sufficient. Once again, Dropwizard Metrics makes an appearance hooking into our connection pool now. This will provide us with some very useful pool stats in the future.

public class ConnectionPool {

    private ConnectionPool() {

    }

    /*

     * Expects a config in the following format

     *

     * poolName = "test pool"

     * jdbcUrl = ""

     * maximumPoolSize = 10

     * minimumIdle = 2

     * username = ""

     * password = ""

     * cachePrepStmts = true

     * prepStmtCacheSize = 256

     * prepStmtCacheSqlLimit = 2048

     * useServerPrepStmts = true

     *

     * Let HikariCP bleed out here on purpose

     */

    public static HikariDataSource getDataSourceFromConfig(

        Config conf

        , MetricRegistry metricRegistry

        , HealthCheckRegistry healthCheckRegistry) {

        HikariConfig jdbcConfig = new HikariConfig();

        jdbcConfig.setPoolName(conf.getString("poolName"));

        jdbcConfig.setMaximumPoolSize(conf.getInt("maximumPoolSize"));

        jdbcConfig.setMinimumIdle(conf.getInt("minimumIdle"));

        jdbcConfig.setJdbcUrl(conf.getString("jdbcUrl"));

        jdbcConfig.setUsername(conf.getString("username"));

        jdbcConfig.setPassword(conf.getString("password"));

        jdbcConfig.addDataSourceProperty("cachePrepStmts", conf.getBoolean("cachePrepStmts"));

        jdbcConfig.addDataSourceProperty("prepStmtCacheSize", conf.getInt("prepStmtCacheSize"));

        jdbcConfig.addDataSourceProperty("prepStmtCacheSqlLimit", conf.getInt("prepStmtCacheSqlLimit"));

        jdbcConfig.addDataSourceProperty("useServerPrepStmts", conf.getBoolean("useServerPrepStmts"));

        // Add HealthCheck

        jdbcConfig.setHealthCheckRegistry(healthCheckRegistry);

        // Add Metrics

        jdbcConfig.setMetricRegistry(metricRegistry);

        return new HikariDataSource(jdbcConfig);

    }

}
com.zaxxer.hikari.pool.HikariPool        : HikariPool-1 - Pool stats (total=10, active=0, idle=10, waiting=0)

## HikariProxyConnection

The proxies delegate to the real driver classes. Some proxies, like the one for ResultSet, only intercept a few methods. Without the code generation, the proxy would have to implement all 50+ methods which simply delegate to the wrapped instance.

Code generation, based on reflection, also means that nothing needs to be done when a new JDK version introduces new JDBC methods to existing interfaces.


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
