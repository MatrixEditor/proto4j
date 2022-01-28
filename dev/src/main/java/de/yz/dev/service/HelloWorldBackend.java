package de.yz.dev.service; //@date 23.01.2022

import com.sun.net.httpserver.HttpExchange;
import de.proto4j.annotation.http.requests.RequestController;
import de.proto4j.annotation.http.requests.RequestListener;
import de.proto4j.annotation.server.requests.RequestParam;
import de.proto4j.annotation.server.requests.ResponseBody;

import java.io.IOException;
import java.util.List;

import static de.proto4j.network.http.invocation.HttpExchangeProcessor.HEADER_USER_AGENT;

@RequestController(mapping = "/hui")
public class HelloWorldBackend {

    @RequestListener(path = "hello") // -> /hui/hello
    @ResponseBody
    private String firstPacketReceived(HttpExchange packet) throws IllegalAccessException, IOException {
        return "Hello world1";
    }

    @RequestListener(path = "world")
    @ResponseBody
    private String secondPacketReceived(@RequestParam(name = HEADER_USER_AGENT) List<String> userAgent) {
        return userAgent.get(0);
    }
}
