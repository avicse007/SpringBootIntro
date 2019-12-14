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

ConnectionPool Implementation
==============================
Now that we have two separate configs for our transactional and processing pools, let's initialize them. Once again, we are not using DI and instead are using the enum singleton pattern for lazy initialized singletons. Feel free to use DI in your own implementations. We now have two different pools with different configs that each lazily wire themselves up on demand.

public class ConnectionPools {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionPools.class);

    /*

     * Normally we would be using the app config but since this is an example

     * we will be using a localized example config.

     */

    private static final Config conf = new Configs.Builder()

                                                  .withResource("examples/hikaricp/pools.conf")

                                                  .build();

    /*

     *  This pool is made for short quick transactions that the web application uses.

     *  Using enum singleton pattern for lazy singletons

     */

    private enum Transactional {

        INSTANCE(ConnectionPool.getDataSourceFromConfig(conf.getConfig("pools.transactional"), Metrics.registry(), HealthChecks.getHealthCheckRegistry()));

        private final DataSource dataSource;

        private Transactional(DataSource dataSource) {

            this.dataSource = dataSource;

        }

        public DataSource getDataSource() {

            return dataSource;

        }

    }

    public static DataSource getTransactional() {

        return Transactional.INSTANCE.getDataSource();

    }

    /*

     *  This pool is designed for longer running transactions / bulk inserts / background jobs

     *  Basically if you have any multithreading or long running background jobs

     *  you do not want to starve the main applications connection pool.

     *

     *  EX.

     *  You have an endpoint that needs to insert 1000 db records

     *  This will queue up all the connections in the pool

     *

     *  While this is happening a user tries to log into the site.

     *  If you use the same pool they may be blocked until the bulk insert is done

     *  By splitting pools you can give transactional queries a much higher chance to

     *  run while the other pool is backed up.

     */

    private enum Processing {

        INSTANCE(ConnectionPool.getDataSourceFromConfig(conf.getConfig("pools.processing"), Metrics.registry(), HealthChecks.getHealthCheckRegistry()));

        private final DataSource dataSource;

        private Processing(DataSource dataSource) {

            this.dataSource = dataSource;

        }

        public DataSource getDataSource() {

            return dataSource;

        }

    }

    public static DataSource getProcessing() {

        return Processing.INSTANCE.getDataSource();

    }

    public static void main(String[] args) {

        logger.debug("starting");

        DataSource processing = ConnectionPools.getProcessing();

        logger.debug("processing started");

        DataSource transactional = ConnectionPools.getTransactional();

        logger.debug("transactional started");

        logger.debug("done");

    }

}

ConnectionPool Implementation Log
==================================

Notice how the config properly inherited its values and each config is lazily loaded.

2017-02-08 22:43:04.428 [main] INFO  com.stubbornjava.common.Configs - Loading configs first row is highest priority, second row is fallback and so on

2017-02-08 22:43:04.428 [main] INFO  com.stubbornjava.common.Configs - examples/hikaricp/pools.conf

2017-02-08 22:43:04.439 [main] DEBUG com.stubbornjava.common.Configs - Logging properties. Make sure sensitive data such as passwords or secrets are not logged!

2017-02-08 22:43:04.439 [main] DEBUG com.stubbornjava.common.Configs - {

    "pools" : {

        "default" : {

            "cachePrepStmts" : true,

            "jdbcUrl" : "jdbc:hsqldb:mem:testdb",

            "maximumPoolSize" : 10,

            "minimumIdle" : 2,

            "password" : "",

            "prepStmtCacheSize" : 256,

            "prepStmtCacheSqlLimit" : 2048,

            "useServerPrepStmts" : true,

            "username" : "SA"

        },

        "processing" : {

            "cachePrepStmts" : true,

            "jdbcUrl" : "jdbc:hsqldb:mem:testdb",

            "maximumPoolSize" : 30,

            "minimumIdle" : 2,

            "password" : "",

            "poolName" : "processing",

            "prepStmtCacheSize" : 256,

            "prepStmtCacheSqlLimit" : 2048,

            "useServerPrepStmts" : true,

            "username" : "SA"

        },

        "transactional" : {

            "cachePrepStmts" : true,

            "jdbcUrl" : "jdbc:hsqldb:mem:testdb",

            "maximumPoolSize" : 10,

            "minimumIdle" : 2,

            "password" : "",

            "poolName" : "transactional",

            "prepStmtCacheSize" : 256,

            "prepStmtCacheSqlLimit" : 2048,

            "useServerPrepStmts" : true,

            "username" : "SA"

        }

    }

}

2017-02-08 22:43:04.440 [main] DEBUG c.s.e.hikaricp.ConnectionPools - starting

2017-02-08 22:43:04.525 [main] DEBUG com.zaxxer.hikari.HikariConfig - processing - configuration:

2017-02-08 22:43:04.539 [main] DEBUG com.zaxxer.hikari.HikariConfig - allowPoolSuspension.............false

2017-02-08 22:43:04.539 [main] DEBUG com.zaxxer.hikari.HikariConfig - autoCommit......................true

2017-02-08 22:43:04.539 [main] DEBUG com.zaxxer.hikari.HikariConfig - catalog.........................none

2017-02-08 22:43:04.539 [main] DEBUG com.zaxxer.hikari.HikariConfig - connectionInitSql...............none

2017-02-08 22:43:04.539 [main] DEBUG com.zaxxer.hikari.HikariConfig - connectionTestQuery.............none

2017-02-08 22:43:04.540 [main] DEBUG com.zaxxer.hikari.HikariConfig - connectionTimeout...............30000

2017-02-08 22:43:04.540 [main] DEBUG com.zaxxer.hikari.HikariConfig - dataSource......................none

2017-02-08 22:43:04.540 [main] DEBUG com.zaxxer.hikari.HikariConfig - dataSourceClassName.............none

2017-02-08 22:43:04.540 [main] DEBUG com.zaxxer.hikari.HikariConfig - dataSourceJNDI..................none

2017-02-08 22:43:04.543 [main] DEBUG com.zaxxer.hikari.HikariConfig - dataSourceProperties............{password=<masked>, prepStmtCacheSqlLimit=2048, cachePrepStmts=true, useServerPrepStmts=true, prepStmtCacheSize=256}

2017-02-08 22:43:04.543 [main] DEBUG com.zaxxer.hikari.HikariConfig - driverClassName.................none

2017-02-08 22:43:04.543 [main] DEBUG com.zaxxer.hikari.HikariConfig - healthCheckProperties...........{}

2017-02-08 22:43:04.544 [main] DEBUG com.zaxxer.hikari.HikariConfig - healthCheckRegistry.............com.codahale.metrics.health.HealthCheckRegistry@34123d65

2017-02-08 22:43:04.544 [main] DEBUG com.zaxxer.hikari.HikariConfig - idleTimeout.....................600000

2017-02-08 22:43:04.544 [main] DEBUG com.zaxxer.hikari.HikariConfig - initializationFailFast..........true

2017-02-08 22:43:04.544 [main] DEBUG com.zaxxer.hikari.HikariConfig - initializationFailTimeout.......1

2017-02-08 22:43:04.545 [main] DEBUG com.zaxxer.hikari.HikariConfig - isolateInternalQueries..........false

2017-02-08 22:43:04.545 [main] DEBUG com.zaxxer.hikari.HikariConfig - jdbc4ConnectionTest.............false

2017-02-08 22:43:04.545 [main] DEBUG com.zaxxer.hikari.HikariConfig - jdbcUrl........................."jdbc:hsqldb:mem:testdb"

2017-02-08 22:43:04.546 [main] DEBUG com.zaxxer.hikari.HikariConfig - leakDetectionThreshold..........0

2017-02-08 22:43:04.546 [main] DEBUG com.zaxxer.hikari.HikariConfig - maxLifetime.....................1800000

2017-02-08 22:43:04.546 [main] DEBUG com.zaxxer.hikari.HikariConfig - maximumPoolSize.................30

2017-02-08 22:43:04.546 [main] DEBUG com.zaxxer.hikari.HikariConfig - metricRegistry..................com.codahale.metrics.MetricRegistry@59474f18

2017-02-08 22:43:04.546 [main] DEBUG com.zaxxer.hikari.HikariConfig - metricsTrackerFactory...........none

2017-02-08 22:43:04.547 [main] DEBUG com.zaxxer.hikari.HikariConfig - minimumIdle.....................2

2017-02-08 22:43:04.547 [main] DEBUG com.zaxxer.hikari.HikariConfig - password........................<masked>

2017-02-08 22:43:04.547 [main] DEBUG com.zaxxer.hikari.HikariConfig - poolName........................"processing"

2017-02-08 22:43:04.547 [main] DEBUG com.zaxxer.hikari.HikariConfig - readOnly........................false

2017-02-08 22:43:04.547 [main] DEBUG com.zaxxer.hikari.HikariConfig - registerMbeans..................false

2017-02-08 22:43:04.547 [main] DEBUG com.zaxxer.hikari.HikariConfig - scheduledExecutor...............none

2017-02-08 22:43:04.548 [main] DEBUG com.zaxxer.hikari.HikariConfig - scheduledExecutorService........internal

2017-02-08 22:43:04.548 [main] DEBUG com.zaxxer.hikari.HikariConfig - threadFactory...................internal

2017-02-08 22:43:04.548 [main] DEBUG com.zaxxer.hikari.HikariConfig - transactionIsolation............default

2017-02-08 22:43:04.548 [main] DEBUG com.zaxxer.hikari.HikariConfig - username........................"SA"

2017-02-08 22:43:04.548 [main] DEBUG com.zaxxer.hikari.HikariConfig - validationTimeout...............5000

2017-02-08 22:43:04.551 [main] INFO  com.zaxxer.hikari.HikariDataSource - processing - Starting...

2017-02-08 22:43:05.084 [main] INFO  com.zaxxer.hikari.pool.PoolBase - processing - Driver does not support get/set network timeout for connections. (feature not supported)

2017-02-08 22:43:05.089 [main] DEBUG com.zaxxer.hikari.pool.HikariPool - processing - Added connection org.hsqldb.jdbc.JDBCConnection@66982506

2017-02-08 22:43:05.109 [main] INFO  com.zaxxer.hikari.HikariDataSource - processing - Start completed.

2017-02-08 22:43:05.110 [main] DEBUG c.s.e.hikaricp.ConnectionPools - processing started

2017-02-08 22:43:05.112 [main] DEBUG com.zaxxer.hikari.HikariConfig - transactional - configuration:

2017-02-08 22:43:05.115 [main] DEBUG com.zaxxer.hikari.HikariConfig - allowPoolSuspension.............false

2017-02-08 22:43:05.115 [main] DEBUG com.zaxxer.hikari.HikariConfig - autoCommit......................true

2017-02-08 22:43:05.115 [main] DEBUG com.zaxxer.hikari.HikariConfig - catalog.........................none

2017-02-08 22:43:05.115 [main] DEBUG com.zaxxer.hikari.HikariConfig - connectionInitSql...............none

2017-02-08 22:43:05.115 [main] DEBUG com.zaxxer.hikari.HikariConfig - connectionTestQuery.............none

2017-02-08 22:43:05.116 [main] DEBUG com.zaxxer.hikari.HikariConfig - connectionTimeout...............30000

2017-02-08 22:43:05.116 [main] DEBUG com.zaxxer.hikari.HikariConfig - dataSource......................none

2017-02-08 22:43:05.116 [main] DEBUG com.zaxxer.hikari.HikariConfig - dataSourceClassName.............none

2017-02-08 22:43:05.116 [main] DEBUG com.zaxxer.hikari.HikariConfig - dataSourceJNDI..................none

2017-02-08 22:43:05.116 [main] DEBUG com.zaxxer.hikari.HikariConfig - dataSourceProperties............{password=<masked>, prepStmtCacheSqlLimit=2048, cachePrepStmts=true, useServerPrepStmts=true, prepStmtCacheSize=256}

2017-02-08 22:43:05.116 [main] DEBUG com.zaxxer.hikari.HikariConfig - driverClassName.................none

2017-02-08 22:43:05.116 [main] DEBUG com.zaxxer.hikari.HikariConfig - healthCheckProperties...........{}

2017-02-08 22:43:05.117 [main] DEBUG com.zaxxer.hikari.HikariConfig - healthCheckRegistry.............com.codahale.metrics.health.HealthCheckRegistry@34123d65

2017-02-08 22:43:05.117 [main] DEBUG com.zaxxer.hikari.HikariConfig - idleTimeout.....................600000

2017-02-08 22:43:05.117 [main] DEBUG com.zaxxer.hikari.HikariConfig - initializationFailFast..........true

2017-02-08 22:43:05.117 [main] DEBUG com.zaxxer.hikari.HikariConfig - initializationFailTimeout.......1

2017-02-08 22:43:05.117 [main] DEBUG com.zaxxer.hikari.HikariConfig - isolateInternalQueries..........false

2017-02-08 22:43:05.118 [main] DEBUG com.zaxxer.hikari.HikariConfig - jdbc4ConnectionTest.............false

2017-02-08 22:43:05.118 [main] DEBUG com.zaxxer.hikari.HikariConfig - jdbcUrl........................."jdbc:hsqldb:mem:testdb"

2017-02-08 22:43:05.118 [main] DEBUG com.zaxxer.hikari.HikariConfig - leakDetectionThreshold..........0

2017-02-08 22:43:05.118 [main] DEBUG com.zaxxer.hikari.HikariConfig - maxLifetime.....................1800000

2017-02-08 22:43:05.118 [main] DEBUG com.zaxxer.hikari.HikariConfig - maximumPoolSize.................10

2017-02-08 22:43:05.119 [main] DEBUG com.zaxxer.hikari.HikariConfig - metricRegistry..................com.codahale.metrics.MetricRegistry@59474f18

2017-02-08 22:43:05.119 [main] DEBUG com.zaxxer.hikari.HikariConfig - metricsTrackerFactory...........none

2017-02-08 22:43:05.119 [main] DEBUG com.zaxxer.hikari.HikariConfig - minimumIdle.....................2

2017-02-08 22:43:05.119 [main] DEBUG com.zaxxer.hikari.HikariConfig - password........................<masked>

2017-02-08 22:43:05.119 [main] DEBUG com.zaxxer.hikari.HikariConfig - poolName........................"transactional"

2017-02-08 22:43:05.119 [main] DEBUG com.zaxxer.hikari.HikariConfig - readOnly........................false

2017-02-08 22:43:05.120 [main] DEBUG com.zaxxer.hikari.HikariConfig - registerMbeans..................false

2017-02-08 22:43:05.120 [main] DEBUG com.zaxxer.hikari.HikariConfig - scheduledExecutor...............none

2017-02-08 22:43:05.120 [main] DEBUG com.zaxxer.hikari.HikariConfig - scheduledExecutorService........internal

2017-02-08 22:43:05.120 [main] DEBUG com.zaxxer.hikari.HikariConfig - threadFactory...................internal

2017-02-08 22:43:05.120 [main] DEBUG com.zaxxer.hikari.HikariConfig - transactionIsolation............default

2017-02-08 22:43:05.120 [main] DEBUG com.zaxxer.hikari.HikariConfig - username........................"SA"

2017-02-08 22:43:05.120 [main] DEBUG com.zaxxer.hikari.HikariConfig - validationTimeout...............5000

2017-02-08 22:43:05.121 [main] INFO  com.zaxxer.hikari.HikariDataSource - transactional - Starting...

2017-02-08 22:43:05.121 [main] INFO  com.zaxxer.hikari.pool.PoolBase - transactional - Driver does not support get/set network timeout for connections. (feature not supported)

2017-02-08 22:43:05.122 [main] DEBUG com.zaxxer.hikari.pool.HikariPool - transactional - Added connection org.hsqldb.jdbc.JDBCConnection@69c81773

2017-02-08 22:43:05.123 [main] INFO  com.zaxxer.hikari.HikariDataSource - transactional - Start completed.

2017-02-08 22:43:05.123 [main] DEBUG c.s.e.hikaricp.ConnectionPools - transactional started

2017-02-08 22:43:05.123 [main] DEBUG c.s.e.hikaricp.ConnectionPools - done


com.zaxxer.hikari.pool.HikariPool        : HikariPool-1 - Pool stats (total=10, active=0, idle=10, waiting=0)

## HikariProxyConnection

The proxies delegate to the real driver classes. Some proxies, like the one for ResultSet, only intercept a few methods. Without the code generation, the proxy would have to implement all 50+ methods which simply delegate to the wrapped instance.

Code generation, based on reflection, also means that nothing needs to be done when a new JDK version introduces new JDBC methods to existing interfaces.


Auto Configuration 
====================

Simply put, the Spring Boot autoconfiguration represents a way to automatically configure a Spring application based on the dependencies that are present on the classpath.

Spring Boot looks at a) Frameworks available on the CLASSPATH b) Existing configuration for the application. Based on these, Spring Boot provides basic configuration needed to configure the application with these frameworks. This is called Auto Configuration.

## Spring Boot Auto Configuration in action.

When we run StudentServicesApplication.java as a Java Application, you will see a few important things in the log.

Mapping servlet: 'dispatcherServlet' to [/]

Mapped "{[/error]}" onto public org.springframework.http.ResponseEntity<java.util.Map<java.lang.String, java.lang.Object>> org.springframework.boot.autoconfigure.web.BasicErrorController.error(javax.servlet.http.HttpServletRequest)

Mapped URL path [/webjars/**] onto handler of type [class org.springframework.web.servlet.resource.ResourceHttpRequestHandler]

Above log statements are good examples of Spring Boot Auto Configuration in action.

As soon as we added in Spring Boot Starter Web as a dependency in our project, Spring Boot Autoconfiguration sees that Spring MVC is on the classpath. It autoconfigures dispatcherServlet, a default error page and webjars.

If you add Spring Boot Data JPA Starter, you will see that Spring Boot Auto Configuration auto configures a datasource and an Entity Manager.

## Where is Spring Boot Auto Configuration implemented?

All auto configuration logic is implemented in spring-boot-autoconfigure.jar. All auto configuration logic for mvc, data, jms and other frameworks is present in a single jar.  

Other important file inside spring-boot-autoconfigure.jar is /META-INF/spring.factories. This file lists all the auto configuration classes that should be enabled under the EnableAutoConfiguration key. A few of the important auto configurations are listed below.

org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
org.springframework.boot.autoconfigure.aop.AopAutoConfiguration,\
org.springframework.boot.autoconfigure.MessageSourceAutoConfiguration,\
org.springframework.boot.autoconfigure.PropertyPlaceholderAutoConfiguration,\
org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration,\
org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,\
org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration,\
org.springframework.boot.autoconfigure.jdbc.JndiDataSourceAutoConfiguration,\
org.springframework.boot.autoconfigure.jdbc.XADataSourceAutoConfiguration,\
org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration,\
org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration,\
org.springframework.boot.autoconfigure.security.SecurityFilterAutoConfiguration,\
org.springframework.boot.autoconfigure.web.DispatcherServletAutoConfiguration,\
org.springframework.boot.autoconfigure.web.EmbeddedServletContainerAutoConfiguration,\
org.springframework.boot.autoconfigure.web.ErrorMvcAutoConfiguration,\


### Example Auto Configuration

We will take a look at DataSourceAutoConfiguration.

Typically all Auto Configuration classes look at other classes available in the classpath. If specific classes are available in the classpath, then configuration for that functionality is enabled through auto configuration. Annotations like @ConditionalOnClass, @ConditionalOnMissingBean help in providing these features!

#### @ConditionalOnClass({ DataSource.class, EmbeddedDatabaseType.class }) : This configuration is enabled only when these classes are available in the classpath.

@Configuration
@ConditionalOnClass({ DataSource.class, EmbeddedDatabaseType.class })
@EnableConfigurationProperties(DataSourceProperties.class)
@Import({ Registrar.class, DataSourcePoolMetadataProvidersConfiguration.class })
public class DataSourceAutoConfiguration {

#### @ConditionalOnMissingBean : This bean is configured only if there is no other bean configured with the same name.

@Bean
@ConditionalOnMissingBean
public DataSourceInitializer dataSourceInitializer() {
	return new DataSourceInitializer();
}

Embedded Database is configured only if there are no beans of type DataSource.class or XADataSource.class already configured.

@Conditional(EmbeddedDatabaseCondition.class)
@ConditionalOnMissingBean({ DataSource.class, XADataSource.class })
@Import(EmbeddedDataSourceConfiguration.class)
protected static class EmbeddedDatabaseConfiguration {
}

### Debugging Auto Configuration

There are two ways you can debug and find more information about auto configuration.

   ##### Turning on debug logging
   #### Using Spring Boot Actuator

##### Debug Logging

You can turn debug logging by adding a simple property value to application.properties. In the example below, we are turning on Debug level for all logging from org.springframework package (and sub packages).

###### logging.level.org.springframework: DEBUG

When you restart the application, you would see an auto configuration report printed in the log. Similar to what you see below, a report is produced including all the auto configuration classes. The report separates the positive matches from negative matches. It will show why a specific bean is auto configured and also why something is not auto configured.

=========================
AUTO-CONFIGURATION REPORT
=========================

Positive matches:
-----------------
DispatcherServletAutoConfiguration matched
 - @ConditionalOnClass classes found: org.springframework.web.servlet.DispatcherServlet (OnClassCondition)
 - found web application StandardServletEnvironment (OnWebApplicationCondition)


Negative matches:
-----------------
ActiveMQAutoConfiguration did not match
 - required @ConditionalOnClass classes not found: javax.jms.ConnectionFactory,org.apache.activemq.ActiveMQConnectionFactory (OnClassCondition)

AopAutoConfiguration.CglibAutoProxyConfiguration did not match
 - @ConditionalOnProperty missing required properties spring.aop.proxy-target-class (OnPropertyCondition)


#### Spring Boot Actuator

Other way to debug auto configuration is to add spring boot actuator to your project. We will also add in HAL Browser to make things easy.

<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-actuator</artifactId>
</dependency>

<dependency>
	<groupId>org.springframework.data</groupId>
	<artifactId>spring-data-rest-hal-browser</artifactId>
</dependency>


HAL Browser auto configuration http://localhost:8080/actuator/#http://localhost:8080/autoconfig would show the details of all the beans which are auto configured and those which are not.

#### Disabling Auto-Configuration Classes

If we wanted to exclude the auto-configuration from being loaded, we could add the @EnableAutoConfiguration annotation with exclude or excludeName attribute to a configuration class:
	
@Configuration
@EnableAutoConfiguration(
  exclude={MySQLAutoconfiguration.class})
public class AutoconfigurationApplication {
    //...
}

Another option to disable specific auto-configurations is by setting the spring.autoconfigure.exclude property:
	
spring.autoconfigure.exclude=com.baeldung.autoconfiguration.MySQLAutoconfiguration



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
