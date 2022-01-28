package de.yz.gen; //@date 27.01.2022

import de.proto4j.annotation.selection.Selector;
import de.proto4j.annotation.server.Context;
import de.proto4j.annotation.server.requests.Controller;
import de.proto4j.annotation.server.requests.ResponseBody;
import de.proto4j.annotation.server.threding.Parallel;
import de.proto4j.annotation.server.threding.SupplyParallel;

@Controller
public class Backend {

    @Context("HelloWorldPacket")
    @ResponseBody
    @Parallel
    public void executeSomeTask(Object helloWorldPacket) {


    }

    @SupplyParallel
    public Object executeWithReturn() {
        return null;
    }

    private static class MySelector implements Selector {
        public boolean select(Object input) {
            return true;
        }
    }
}
