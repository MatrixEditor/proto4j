package de.yz.gen.service; //@date 29.01.2022

import de.proto4j.annotation.server.requests.Controller;
import de.proto4j.annotation.server.requests.RequestHandler;
import de.proto4j.annotation.server.requests.ResponseBody;
import de.proto4j.annotation.threding.SupplyParallel;
import de.yz.gen.HelloWorldMessage;

@Controller
public class HelloWorldReactor {

    @RequestHandler
    @ResponseBody
    @SupplyParallel
    public HelloWorldMessage helloWorldReceived(HelloWorldMessage message){
        System.out.println(message.toString());

        HelloWorldMessage back = new HelloWorldMessage();
        back.setMessage("Hello back!");
        return back;
    }
}
