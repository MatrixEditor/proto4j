package de.proto4j.network.http.response; //@date 26.01.2022

import com.sun.net.httpserver.HttpExchange;
import de.proto4j.annotation.http.requests.ResponseType;
import de.proto4j.network.http.ResponseEntity;

import java.io.IOException;
import java.io.OutputStream;

public class EntityResponseHandler implements ResponseInvocationHandler<ResponseEntity<?>> {

    //JsonHandler
    //XmlHandler

    @Override
    public void handle(ResponseEntity<?> responseEntity, HttpExchange exchange, ResponseType responseType) {
        if (responseEntity != null && exchange != null && responseType != null) {
            if (responseEntity.getResponse() != null) {
                switch (responseType) {
                    case PLAIN_RESPONSE:
                    case OTHER_RESPONSE:
                        responseEntity.setMapper(x -> x.toString().getBytes());
                        break;
                    //handlers below
                }

                byte[] resp = responseEntity.getBytes();
                if (resp != null && resp.length == responseEntity.getLength()) {
                    try (OutputStream os = exchange.getResponseBody()) {
                        exchange.sendResponseHeaders(responseEntity.getCode(), responseEntity.getLength());
                        os.write(resp);
                    } catch (IOException e) {
                        // log error here
                    }
                }
            }
        }
    }
}
