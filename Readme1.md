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

