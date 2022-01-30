package de.yz.dev; //@date 23.01.2022

import de.proto4j.internal.AllowAutoConfiguration;
import de.proto4j.annotation.http.Http;
import de.proto4j.annotation.http.WebServer;
import de.proto4j.internal.RootPackage;
import de.proto4j.network.http.HttpServerContext;
import de.proto4j.network.http.WebServlet;

import java.io.IOException;

@Http
@WebServer(port = 8080)
@RootPackage(value = "de.yz.dev")
@AllowAutoConfiguration
public class SimpleJavaWebServer {

    public static void main(String[] args) throws IOException {
        HttpServerContext ctx = WebServlet.runHttpServer(SimpleJavaWebServer.class);

        for (String path : ctx.getWebRoutes()) {
            System.out.printf("path : %s\n", path);
        }
    }
}
