# Guide to proto4j

This file contains the basics of this library, including `create client-server infrastructures`, `create a new message fromat`, `running a web-server` and `configure a client`. 

## Basics

#### Create and run a simple Webserver

To run and create an HTTP-Webserver the following structure is necessary:
```java
@Http
@WebServer(port = 8080)
public class MyWebServer {
    public static void main(String[] args) {
        WebServlet.runHttpServer(MyWebServer.class);
    }
}
```
The annotations needed to start a webserver for now are: `WebServer`. There will be a must of `Http`or `Https` in the future if https is implemented. The server given above just starts and never displays content to a user visiting this server except from `'404 page not found'`.

Therefore, creating controllers that handle incoming requests are required and implemented like the following:

```java
import com.sun.net.httpserver.HttpExchange;

@HttpRequestController(mapping = "/start")
public class MyController {

 @RequestListener(path = "hello")
 @ResponseBody(HttpResponseType.PLAIN_RESPONSE)
 public String handleHelloRequest(HttpExchange exchange) {
     return "Hello there!";
 }
}
```

The code above indicates that a new context will be created at `"http://address/start/hello"` and a plain result is given as a response. Filtering by different HTTP-Methods is not implemented yet. Please refer to the [annotation-guide](https://github.com/MatrixEditor/proto4j/blob/main/annotation-guide.md) to understand which annotation can be used in which context.

#### Create a new message-format

Defining new types of messages is important for the own client-server infrastructure. See the [message-guide](https://github.com/MatrixEditor/proto4j/blob/main/message-guide.md) to get more information.

#### Create a client-server infrastructure

Three components are required to build a new infrastructure:
* `client`: The client-side with a class annotated as a `TypeClient`
* `server`: The server-backend with a class annotated as a `TypeServer`
* `message`: At least one message has to be defined. Otherwise, no communication between client and endpoint is possible.

See the [message-guide](https://github.com/MatrixEditor/proto4j/blob/main/message-guide.md) to get more information about defining messages. The server-side is similar to the Webserver definition:

```java
@TypeServer(port = 9000)
public class MyServer { 
    public static void main(String[] args) {
        ServerProvider.runServer(MyServer.class);
    }
}

@Controller
public class ServerHandler {
    @RequestHandler
    @ResponseBody
    public EchoMessage handleEcho(EchoMessage em) {
        return em;
    }
}
```

The method `runServer(Class<?> c)` returns an instance of `TypeContext` which contains all environment information related to this server. Additional options with annotation are described [here](https://github.com/MatrixEditor/proto4j/blob/main/annotation-guide.md).

The client-side has a similar structure. There is a difference in configuration, because a client could connect to a number of server in parallel. For Annotation-related information refer to the [annotation-guide](https://github.com/MatrixEditor/proto4j/blob/main/annotation-guide.md).
```java
@TypeClient
public class MyClient { 
    public static void main(String[] args) {
        ClientContext ctx = ClientProvider.createClient(MyClient.class);
    }
}

@Controller
public class MyController {
    @RequestHandler
    @Parallel
    public void handleEcho(EchoMessage em) {
        //do something with this message
    }
}
```


## Modules

Yet, the library consists of five modules:

### proto4j-security

---

In order to prevent everyone can read the traffic sent through the network the RSA encryption method is used. If a client connects to the specified server a `CerticicateExchange` is done by both server and client. The backend only sends his public-key and continues. The client waits until it receives the generated public key. 

To ensure keys are not known before the client or server have been started, they are generated at runtime with calling the following method in `Proto4jAsymKeyProvider`: 

```java
public static synchronized KeyPair newProto4jKeyPair();
```

The default `key.length` is defined with 1024.


### proto4j-serialization 

---

This module is used to serialize Message-objects by converting them into simple Strings that will be sent through the Proto4jWriter. It is explicitly recommended to **not** serialize messages with own implementations of a Proto4jWriter. The following methods are used for serialization and de-serialization in the DescProviderFactory-class:
```
public static StringBuffer allocate(Object message); // serialization

public static Object convert(byte[] data, Set<Class<?>> messageTypes); //de-serialization
```
Converting serialized data back into Objects is linked with security issues. Untrusted data should not be de-serialized. Therefore, a `java.util.Set` with all system-loaded message-types is provided. **Important:** This method should only be called by the Proto4jReader to prevent security issues and I/O-Errors.

To understand which format is used in serialization, follow this [description](https://github.com/MatrixEditor/proto4j/blob/main/serialization-format.md).

### proto4j-annotation

---

This module contains almost every important annotation cor creating the client-server infrastructure. Only Annotations of package `de.proto4j.annotation.documentation` are source-related, so they can't be viewed at runtime. 

Below the packages and their usage are listed:
* `http`: All Annotations declared in this package are used by an HTTP-Server. The basic processing of these annotations are explained in an extra [annotation-guide](https://github.com/MatrixEditor/proto4j/blob/main/annotation-guide.md)
* `message`: In order to declare new message formats annotations of this package are used. View this [message-guide](https://github.com/MatrixEditor/proto4j/blob/main/message-guide.md) to get more information.
* `server`: This package contains all server context-related annotations like the http-package. See [annotation-guide](https://github.com/MatrixEditor/proto4j/blob/main/annotation-guide.md) for more Information.
* `threading`: Annotations that show a parallelism of a method as well as an implementation for a `ThreadPool`, that executes all incoming commands in a new Thread. 

### proto4j-internal
 
---

The `internal`-module contains the implementations for `InputStream` and `OutputStream` named `Proto4jReader`and -`Writer`, a small Logger-Factory and utilities for scanning java-packages and bean-creation. Although, this module contains useful classes, these shouldn't be used outside this library.

In terms of serialization the AES is used by reader and writer to ensure nobody can read network traffic without having the key. In the future there will be something like a certificate which can be used to encrypt and decrypt data. For now a hardcoded key is used.

### proto4j-network:

---

The webserver and message-server implementations as well as a client are provided by this module. 


### proto4j-json:

---

A small module containing a fully qualified Json-Parser with parsing, serializing and dumping Objects. Parsing is done in under 150 lines of code. The implementation is using The main methods that can be used are the following: 
```
public JsonObject parse(String content); // string-parsing

public <T> T load(String content, Class<T> c); // string-parsing and object-creation

public void dump(JsonObject o, File file); // save a JsonObject

public void dump(Object o, File file); // saving in generic form
```
Objects are stored as a JsonProperty in JsonObjects and JsonArrays. In order to get the specific type
the following steps have to be done:

    JsonObject jo = parser.parse(someJsonContent);
    JsonArray  ja = jo.get("thePropertyTag").asArray();

These steps can raise a NullPointerException if the `propertyTag` isn't found. If the object behind `propertyTag` could be of another type, use `isArray()`, `isObject()`, `isSimpleProperty()` to check
 if the right type is stored.