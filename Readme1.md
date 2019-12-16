How to Configure Spring Boot Tomcat
===================================

Spring Boot web applications include a pre-configured, embedded web server by default. 

## Common Embedded Tomcat Configurations
#### 2.1. Server Address And Port

The most common configuration we may wish to change is the port number:
1. server.port=80

If we don't provide the server.port parameter it's set to 8080 by default.

In some cases, we may wish to set a network address to which the server should bind. In other words, we define an IP address where our server will listen:
1. server.address=my_custom_ip

By default, the value is set to 0.0.0.0 which allows connection via all IPv4 addresses. Setting another value, for example, localhost – 127.0.0.1 – will make the server more selective.
#### 2.2. Error Handling

By default, Spring Boot provides a standard error web page. This page is called the Whitelabel. It's enabled by default but if we don't want to display any error information we can  disable it:
1. server.error.whitelabel.enabled=false

The default path to a Whitelabel is /error. We can customize it by setting the server.error.path parameter:
1. server.error.path=/user-error

We can also set properties that will determine which information about the error is presented. For example, we can include the error message and the stack trace:
1. server.error.include-exception=true
2. server.error.include-stacktrace=always

#### 2.3. Server Connections

When running on a low resource container we might like to decrease the CPU and memory load. One way of doing that is to limit the number of simultaneous requests that can be handled by our application. Conversely, we can increase this value to use more available resources to get better performance.

In Spring Boot, we can define the maximum amount of Tomcat worker threads:
1. server.tomcat.max-threads=200

When configuring a web server, it also might be useful to set the server connection timeout. This represents the maximum amount of time the server will wait for the client to make their request after connecting before the connection is closed:
1. server.connection-timeout=5s

We can also define the maximum size of a request header:
1. server.max-http-header-size=8KB

The maximum size of a request body:
1. server.tomcat.max-swallow-size=2MB

Or a maximum size of the whole post request:
1. server.tomcat.max-http-post-size=2MB

#### 2.4. SSL

To enable SSL support in our Spring Boot application we need to set the server.ssl.enabled property to true and define an SSL protocol:
1. server.ssl.enabled=true
2. server.ssl.protocol=TLS

We should also configure the password, type, and path to the key store that holds the certificate:
server.ssl.key-store-password=my_password
server.ssl.key-store-type=keystore_type
server.ssl.key-store=keystore-path

And we must also define the alias that identifies our key in the key store:
server.ssl.key-alias=tomcat

#### 2.5. Tomcat Server Access Logs

Tomcat access logs are very useful when trying to measure page hit counts, user session activity, and so on.

To enable access logs, simply set:
server.tomcat.accesslog.enabled=true

We should also configure other parameters such as directory name, prefix, suffix, and date format appended to log files:
	
server.tomcat.accesslog.directory=logs
server.tomcat.accesslog.file-date-format=yyyy-MM-dd
server.tomcat.accesslog.prefix=access_log
server.tomcat.accesslog.suffix=.log

#### Spring Bean Configuration

SpringBootServletInitializer enables the process used in Servlet 3.0 using web.xml
EnableAutoConfiguration. This will help Spring Boot to automatically identify how to configure Spring, based on the JAR dependencies that we have added. Since spring-boot-starter-web added Tomcat and Spring MVC, the auto-configuration will assume that we are developing a web application and setup Spring accordingly.

@Configuration

@ComponentScan(basePackages = "com.developerstack")

@Import({BeanConfig.class, WebConfig.class})

@EnableAutoConfiguration

public class Application extends SpringBootServletInitializer {

private static Class applicationClass = Application.class;

public static void main(String[] args) {

SpringApplication.run(Application.class, args);

}

@Override

protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {

return application.sources(applicationClass);

}

}


The following configuration is responsible for initializing a Spring-based web application. We have implemented WebApplicationInitializer.java to configure the ServletContext programmatically.

public class ApplicationInitializer implements WebApplicationInitializer {

    public void onStartup(ServletContext container) throws ServletException {

        AnnotationConfigWebApplicationContext ctx = new AnnotationConfigWebApplicationContext();

        ctx.setServletContext(container);

        ServletRegistration.Dynamic servlet = container.addServlet("dispatcher", new DispatcherServlet(ctx));

        servlet.setLoadOnStartup(1);

        servlet.addMapping("/");

    }

}


By default, Spring Boot will serve static content from a directory called /static (or /public or /resources or /META-INF/resources) in the classpath or from the root of the ServletContext. But here we, have defined out custom folder structure for static contents, hence it is required to tell Spring boot about how to render static content.

@Configuration

public class WebConfig extends WebMvcConfigurerAdapter {

    @Override

    public void addResourceHandlers(final ResourceHandlerRegistry registry) {

        registry.addResourceHandler("/*.js/**").addResourceLocations("/ui/static/");

        registry.addResourceHandler("/*.css/**").addResourceLocations("/ui/static/");

    }

}


Now let's define our controller that will serve the response to the client. Here we have hardcoded some user details.

@Controller

public class DashboardController {

    @RequestMapping(value = "/dashboard", method = RequestMethod.GET)

    public ModelAndView dashboard() {

        ModelAndView model = new ModelAndView();

        model.addObject("users", getUsers());

        model.setViewName("dashboard");

        return model;

    }

    private List getUsers() {

        User user = new User();

        user.setEmail("johndoe123@gmail.com");

        user.setName("John Doe");

        user.setAddress("Bangalore, Karnataka");

        User user1 = new User();

        user1.setEmail("amitsingh@yahoo.com");

        user1.setName("Amit Singh");

        user1.setAddress("Chennai, Tamilnadu");

        User user2 = new User();

        user2.setEmail("bipulkumar@gmail.com");

        user2.setName("Bipul Kumar");

        user2.setAddress("Bangalore, Karnataka");

        User user3 = new User();

        user3.setEmail("prakashranjan@gmail.com");

        user3.setName("Prakash Ranjan");

        user3.setAddress("Chennai, Tamilnadu");

        return Arrays.asList(user, user1, user2, user3);

    }

}


#### Client Side

At the client side, there will be a traditional JSP page, which will show the model object using JSTL.
Run Application

There are two ways with which we can run this application.

Either we can define the scope of spring-boot-starter-tomcat as provided and create a .war file with a Maven command and deploy it to a standalone Tomcat or run Application.java as a java application.
