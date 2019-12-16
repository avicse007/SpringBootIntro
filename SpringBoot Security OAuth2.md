Spring Boot OAuth2 
==================

 ## Spring Boot Security - Introduction to OAuth2
OAuth (Open Authorization) is a simple way to publish and interact with protected data.
It is an open standard for token-based authentication and authorization on the Internet. It allows an end user's account information to be 
used by third-party services, such as Facebook, without exposing the user's password.
The OAuth specification describes five grants for acquiring an access token:

    Authorization code grant
    Implicit grant
    Resource owner credentials grant
    Client credentials grant
    Refresh token gran
    
 ## What actually is OAuth?
Consider the use case of Quora. Go to Quora.com.
If you are a new user you need to signup. You can signup using google or facebook account. When doing so you are authorizing Google or Facebook 
to allow quora to access you profile info with Quora. This authorizing is done using OAuth. Here you have in no way shared your credentials
with Quora.

In the above example of Quora, we have 3 actors-

     #### Resource Owner - This is the user who wants to sign up using Quora.
     #### Client Application - This will be Quora
     #### Resource Server - This will be Gmail or Facebook.
     #### Authorization Server - The resource server hosts the protected user accounts, and the authorization server 
     verifies the identity of the user then issues access tokens to the application.
The resource owner will then using OAuth authorize the resource server to share data with the client application.
The client application must first register with the authorization server associated with the resource server. This is usually a one-time task.
Once registered, the registration remains valid, unless the client application registration is revoked. At registration the client application
is assigned a client ID and a client secret (password) by the authorization server. The client ID and secret is unique to the client
application on that authorization server.     

Similar to the above flow we will be developing our own client application and Resource Server. Using OAuth the Resource server will then 
share the data with the client application. Also we will be assuming that the client is already registered with the Resource Server and
has been assigned a unique client id and secret key.

     #### Spring Boot Client Application - We already have a unique client id -'javainuse' and secret key - 'secret'.
     We need to import data from Resource Server.
     #### Resource Server - Using OAuth we configure authorization server. It already has the unique key configured for recognizing our 
     client application
     
  ## OAuth2 Roles: There are four roles that can be applied on OAuth2:

    #### Resource Owner: The owner of the resource — this is pretty self-explanatory.
    #### Resource Server: This serves resources that are protected by the OAuth2 token.
    #### Client: The application accessing the resource server.
    #### Authorization Server:  This is the server issuing access tokens to the client after successfully authenticating the resource 
    owner and obtaining authorization.

## Create an OAuth 2.0 Server

#### Start by going to the Spring Initializr and creating a new project with the following settings:

    Change project type from Maven to Gradle.
    Change the Group to com.okta.spring.
    Change the Artifact to AuthorizationServerApplication.
    Add one dependency: Web.

#### Spring Initializr

Download the project and copy it somewhere that makes sense on your hard drive. In this tutorial you’re going to create three different projects, so you might want to create a parent directory, something like SpringBootOAuth somewhere.

You need to add one dependency to the build.gradle file:

implementation 'org.springframework.security.oauth:spring-security-oauth2:2.3.3.RELEASE'

This adds in Spring’s OAuth goodness.

#### Update the src/main/resources/application.properties to match:

server.port=8081
server.servlet.context-path=/auth
user.oauth.clientId=R2dpxQ3vPrtfgF72
user.oauth.clientSecret=fDw7Mpkk5czHNuSRtmhGmAGL42CaxQB9
user.oauth.redirectUris=http://localhost:8082/login/oauth2/code/
user.oauth.user.username=Andrew
user.oauth.user.password=abcd

This sets the server port, servlet context path, and some default values for the in-memory, ad hoc generated tokens the server is going to return to the client, as well as for our user’s username and password. In production, you would need to have a bit more of a sophisticated back end for a real authentication server without the hard-coded redirect URIs and usernames and passwords.

#### Update the AuthorizationServerApplication class to add @EnableResourceServer:

package com.okta.spring.AuthorizationServerApplication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;

@SpringBootApplication
@EnableResourceServer
public class AuthorizationServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthorizationServerApplication.class, args);
    }
}

#### Create a new class AuthServerConfig in the same package as your application class 
com.okta.spring.AuthorizationServerApplication under src/main/java (from now on please create Java classes in src/main/java/com/okta/spring/AuthorizationServerApplication). This Spring configuration class enables and configures an OAuth authorization server.

package com.okta.spring.AuthorizationServerApplication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;

@Configuration
@EnableAuthorizationServer
public class AuthServerConfig extends AuthorizationServerConfigurerAdapter {

    @Value("${user.oauth.clientId}")
    private String ClientID;
    @Value("${user.oauth.clientSecret}")
    private String ClientSecret;
    @Value("${user.oauth.redirectUris}")
    private String RedirectURLs;

   private final PasswordEncoder passwordEncoder;
    
    public AuthServerConfig(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void configure(
        AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
        oauthServer.tokenKeyAccess("permitAll()")
            .checkTokenAccess("isAuthenticated()");
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory()
            .withClient(ClientID)
            .secret(passwordEncoder.encode(ClientSecret))
            .authorizedGrantTypes("authorization_code")
            .scopes("user_info")
            .autoApprove(true)
            .redirectUris(RedirectURLs);
    }
}

#### The AuthServerConfig class is the class that will create and return our JSON web tokens when the client properly authenticates.

#### Create a SecurityConfiguration class:

package com.okta.spring.AuthorizationServerApplication;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@Order(1)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Value("${user.oauth.user.username}")
    private String username;
    @Value("${user.oauth.user.password}")
    private String password;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.requestMatchers()
            .antMatchers("/login", "/oauth/authorize")
            .and()
            .authorizeRequests()
            .anyRequest().authenticated()
            .and()
            .formLogin().permitAll();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
            .withUser(username)
            .password(passwordEncoder().encode(password))
            .roles("USER");
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

The SecurityConfiguration class is the class that actually authenticates requests to your authorization server. Notice near the top where it’s pulling in the username and password from the application.properties file.

#### Lastly, create a Java class called UserController:

package com.okta.spring.AuthorizationServerApplication;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
public class UserController {

    @GetMapping("/user/me")
    public Principal user(Principal principal) {
        return principal;
    }
}

This file allows the client apps to find out more about the users that authenticate with the server.

That’s your authorization server! Not too bad. Spring Boot makes it pretty easy. Four files and a few properties. In a little bit you’ll make it even simpler with Okta, but for the moment, move on to creating a client app you can use to test the auth server.

Start the authorization server:

./gradlew bootRun

Wait a bit for it to finish running. The terminal should end with something like this:

...
2019-02-23 19:06:49.122  INFO 54333 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  :
Tomcat started on port(s): 8081 (http) with context path '/auth  '
2019-02-23 19:06:49.128  INFO 54333 --- [           main] c.o.s.A.AuthorizationServerApplication Started AuthorizationServerApplication in 3.502 seconds (JVM running for 3.945)

NOTE: If you get an error about JAXB (java.lang.ClassNotFoundException: javax.xml.bind.JAXBException), it’s because you’re using Java 11. 
To fix this, add JAXB to your build.gradle.

implementation 'org.glassfish.jaxb:jaxb-runtime'

## Build Your Client App

##### Back to Spring Initializr. Create a new project with the following settings:

    Project type should be Gradle (not Maven).
    Group: com.okta.spring.
    Artifact: SpringBootOAuthClient.
    Add three dependencies: Web, Thymeleaf, OAuth2 Client.

## Create Client App

Download the project, copy it to its final resting place, and unpack it.

This time you need to add the following dependency to your build.gradle file:

implementation 'org.thymeleaf.extras:thymeleaf-extras-springsecurity5:3.0.4.RELEASE'

Rename the src/main/resources/application.properties to application.yml and update it to match the YAML below:

server:
  port: 8082
  session:
    cookie:
      name: UISESSION
spring:
  thymeleaf:
    cache: false
  security:
    oauth2:
      client:
        registration:
          custom-client:
            client-id: R2dpxQ3vPrtfgF72
            client-secret: fDw7Mpkk5czHNuSRtmhGmAGL42CaxQB9
            client-name: Auth Server
            scope: user_info
            provider: custom-provider
            redirect-uri-template: http://localhost:8082/login/oauth2/code/
            client-authentication-method: basic
            authorization-grant-type: authorization_code
        provider:
          custom-provider:
            token-uri: http://localhost:8081/auth/oauth/token
            authorization-uri: http://localhost:8081/auth/oauth/authorize
            user-info-uri: http://localhost:8081/auth/user/me
            user-name-attribute: name

Notice that here you’re configuring the clientId and clientSecret, as well as various URIs for your authentication server. These need to match the values in the other project.

Update the SpringBootOAuthClientApplication class to match:

package com.okta.spring.SpringBootOAuthClient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringBootOAuthClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootOAuthClientApplication.class, args);
    }
}

#### Create a new Java class called WebController:

package com.okta.spring.SpringBootOAuthClient;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
public class WebController {

    @RequestMapping("/securedPage")
    public String securedPage(Model model, Principal principal) {
        return "securedPage";
    }

    @RequestMapping("/")
    public String index(Model model, Principal principal) {
        return "index";
    }
}

This is the controller that maps incoming requests to your Thymeleaf template files (which you’ll make in a sec).

#### Create another Java class named SecurityConfiguration:

package com.okta.spring.SpringBootOAuthClient;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.antMatcher("/**").authorizeRequests()
            .antMatchers("/", "/login**").permitAll()
            .anyRequest().authenticated()
            .and()
            .oauth2Login();
    }
}

This class defines the Spring Security configuration for your application: allowing all requests on the home path and requiring authentication for all other routes. it also sets up the Spring Boot OAuth login flow.

The last files you need to add are the two Thymeleaf template files. A full look at Thymeleaf templating is well beyond the scope of 
this tutorial, but you can take a look at their website for more info.

The templates go in the src/main/resources/templates directory. You’ll notice in the controller above that they’re simply returning
strings for the routes. When the Thymeleaf dependencies are included the build, Spring Boot automatically assumes you’re returning 
the name of the template file from the controllers, and so the app will look in src/main/resources/templates for a file name with the returned string plus .html.

#### Create the home template: src/main/resources/templates/index.html:

<!DOCTYPE html>  
<html lang="en">  
<head>  
    <meta charset="UTF-8">  
    <title>Home</title>  
</head>  
<body>  
    <h1>Spring Security SSO</h1>  
    <a href="securedPage">Login</a>  
</body>  
</html>

And the secured template: src/main/resources/templates/securedPage.html:

<!DOCTYPE html>  
<html xmlns:th="http://www.thymeleaf.org">  
<head>  
    <meta charset="UTF-8">  
    <title>Secured Page</title>  
</head>  
<body>  
    <h1>Secured Page</h1>  
    <span th:text="${#authentication.name}"></span>  
</body>  
</html>

I’ll just point out this one line:

<span th:text="${#authentication.name}"></span>  

This is the line that will insert the name of the authenticated user. This line is why you needed the
org.thymeleaf.extras:thymeleaf-extras-springsecurity5 dependency in the build.gradle file.

##### Start the client application:

./gradlew bootRun

Wait a moment for it to finish. The terminal should end with something like this:

...
2019-02-23 19:29:04.448  INFO 54893 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 
8082 (http) with context path ''
2019-02-23 19:29:04.453  INFO 54893 --- [           main] c.o.s.S.SpringBootOAuthClientApplication : Started SpringBootOAuthClientApplication in 3.911 seconds (JVM running for 4.403)

Test the Resource Server

Navigate in your browser of choice to your client app at http://localhost:8082/.

Click the Login link.

You’ll be directed to the login page:

Sign-In Form

Enter username Andrew and password abcd (from the application.properties file from the authentication server).

Click Sign In and you’ll be taken to the super fancy securedPage.html template that should say “Secured Page” and “Andrew”.

Great! It works. Now you’re gonna make it even simpler.

You can stop both server and client Spring Boot apps.
Create an OpenID Connect Application

Okta is a SaaS (software-as-service) authentication and authorization provider. We provide free accounts to developers 
so they can develop OIDC apps with no fuss. Head over to developer.okta.com and sign up for an account. After you’ve
verified your email, log in and perform the following steps:

    Go to Application > Add Application.
    Select application type Web and click Next.
    Give the app a name. I named mine “Spring Boot OAuth”.
    Under Login redirect URIs change the value to http://localhost:8080/login/oauth2/code/okta. T
    he rest of the default values will work.
    Click Done.

Leave the page open of take note of the Client ID and Client Secret. You’ll need them in a moment.
Create a New Spring Boot App

Back to the Spring Initializr one more time. Create a new project with the following settings:

    Change project type from Maven to Gradle.
    Change the Group to com.okta.spring.
    Change the Artifact to OktaOAuthClient.
    Add three dependencies: Web, Thymeleaf, Okta.
    Click Generate Project.

## Create Okta OAuth App

Copy the project and unpack it somewhere.

In the build.gradle file, add the following dependency:

implementation 'org.thymeleaf.extras:thymeleaf-extras-springsecurity5:3.0.4.RELEASE'  

Also while you’re there, notice the dependency com.okta.spring:okta-spring-boot-starter:1.1.0. This is the Okta Spring Boot Starter. 
It’s a handy project that makes integrating Okta with Spring Boot nice and easy. For more info, take a look at the project’s GitHub.

#### Change the src/main/resources/application.properties to application.yml and add the following:

server:
  port: 8080
okta:
  oauth2:
    issuer: https://{yourOktaDomain}/oauth2/default
    client-id: {yourClientId}
    client-secret: {yourClientSecret}
spring:
  thymeleaf:
    cache: false

Remember when I said you’ll need your ClientID and Client Secret above. Well, the time has come. You need to fill them into the file, as well as your Okta issuer URL. It’s gonna look something like this: dev-123456.okta.com. You can find it under API > Authorization Servers.

You also need two similar template files in the src/main/resources/templates directory. The index.html template file is exactly the same, and can be copied over if you like. The securedPage.html template file is slightly different because of the way the authentication information is returned from Okta as compared to the simple authentication server you built earlier.

#### Create the home template: src/main/resources/templates/index.html:

<!DOCTYPE html>  
<html lang="en">  
<head>  
    <meta charset="UTF-8">  
    <title>Home</title>  
</head>  
<body>  
    <h1>Spring Security SSO</h1>  
    <a href="securedPage">Login</a>  
</body>  
</html>

And the secured template: src/main/resources/templates/securedPage.html:

<!DOCTYPE html>  
<html xmlns:th="http://www.thymeleaf.org">  
<head>  
    <meta charset="UTF-8">  
    <title>Secured Page</title>  
</head>  
<body>  
    <h1>Secured Page</h1>  
    <span th:text="${#authentication.principal.attributes.name}">Joe Coder</span>  
</body>  
</html>

#### Create a Java class named WebController in the com.okta.spring.SpringBootOAuth package:

package com.okta.spring.OktaOAuthClient;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
public class WebController {
    
    @RequestMapping("/securedPage")
    public String securedPage(Model model, Principal principal) {
        return "securedPage";
    }
    
    @RequestMapping("/")
    public String index(Model model, Principal principal) {
        return "index";
    }
}

This class simply creates two routes, one for the home route and one for the secured route. Again, Spring Boot and Thymeleaf are auto-magicking this to the two template files in src/main/resources/templates.

#### Finally, create another Java class names SecurityConfiguration:

package com.okta.spring.OktaOAuthClient;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.antMatcher("/**").authorizeRequests()
            .antMatchers("/").permitAll()
            .anyRequest().authenticated()
            .and()
            .oauth2Login();
    }
}

That’s it! Bam!

Run the Okta-OAuth-powered client:

./gradlew bootRun

You should see a bunch of output that ends with:

...
2019-02-23 20:09:03.465  INFO 55890 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : 
Tomcat started on port(s): 8080 (http) with context path ''
2019-02-23 20:09:03.470  INFO 55890 --- [           main] c.o.s.O.OktaOAuthClientApplication       : 
Started OktaOAuthClientApplication in 3.285 seconds (JVM running for 3.744)

#### Navigate to http://localhost:8080.

#### Click the Login button.

This time you’ll be directed to the Okta login page. You may need to use an incognito browser or log out of your developer.okta.com dashboard here so that you don’t skip the login page and get directed immediately to the secured endpoint.

#### Okta Login Form

Log in, and you’ll see the secured page with your name!
