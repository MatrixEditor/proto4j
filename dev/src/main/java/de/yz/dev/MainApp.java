package de.yz.dev; //@date 23.01.2022

import de.proto4j.annotation.http.Http;
import de.proto4j.annotation.http.WebServer;
import de.proto4j.internal.RootPackage;
import de.proto4j.network.http.WebServlet;

import java.io.IOException;


@Http
@WebServer(port = 8080)
@RootPackage(path = "de.yz.dev")
public class MainApp {

    public static void main(String[] args) throws IOException {
        //http://localhost:8080/hui/hello
        WebServlet.runHttpServer(MainApp.class);
    }
}
