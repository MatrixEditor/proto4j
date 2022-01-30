package de.webtest.server; //@date 30.01.2022

import de.proto4j.annotation.http.Http;
import de.proto4j.annotation.http.WebServer;
import de.proto4j.internal.AllowAutoConfiguration;
import de.proto4j.internal.RootPackage;
import de.proto4j.network.http.HttpServerContext;
import de.proto4j.network.http.WebServlet;

import java.io.IOException;

@Http
@WebServer(port = 8080)
@AllowAutoConfiguration
@RootPackage
public class JavaWebServer {

    public static void main(String[] args) throws IOException {
        // This method creates and starts the SUN-Webserver implementation
        // on the port provided by the @WebServer-Annotation. With the
        // SimpleBackendHandler.java you can go to the following path
        // and should see "Hello World" as a result.
        // -> link: http://localhost:8080/java/hello
        HttpServerContext ctx = WebServlet.runHttpServer(JavaWebServer.class);
    }
}
