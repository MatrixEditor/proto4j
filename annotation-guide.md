# Annotation-Guide

![module-spec](https://img.shields.io:/static/v1?label=Module&message=proto4j-annotation&color=informational)
![module-spec](https://img.shields.io:/static/v1?label=Package&message=de.proto4j.annotation.http&color=9cf)
![module-spec](https://img.shields.io:/static/v1?label=Package&message=de.proto4j.annotation.server&color=9cf)
![module-spec](https://img.shields.io:/static/v1?label=Package&message=de.proto4j.annotation.threading&color=9cf)

Below all declared annotations with their descriptions are listed. Message related annotations can be
found [here](https://github.com/MatrixEditor/proto4j/blob/main/message-guide.md).

### HTTP-Annotations

--- 


Annotations listed below are used to configure a simple HTTP-server. Do not use them anywhere else.

|      Annotation       | Required | Target    | Description                                                                                                                                             |
|:---------------------:|:--------:|:----------|:--------------------------------------------------------------------------------------------------------------------------------------------------------|
|       Webserver       |  `true`  | Class     | specifies the target server-class and the server port                                                                                                   |
|         Http          | `false`  | Class     | indicates that this server is running http - in the future there could be also Https                                                                    |
| HttpRequestController |  `true`  | Class     | specifies a request-handler bean - the mapping is optional ("/" is used if nothing is specified)                                                        |
|  HttpRequestListener  |  `true`  | Method    | creates a new context at the specified path which will be added to the `HttpRequestController`-value                                                    |
|   HttpResponseBody    | `false`  | Method    | indicates that this handler will produce a result - default type is `PLAIN_RESPONSE`                                                                    |
|   HttpResponseCode    | `false`  | Method    | shows the user what codes can be produced - no usage at all                                                                                             |
|     RequestParam      | `false`  | Parameter | if information should be provided directly, the parameter has to be annotated with this annotation and the name of the property that should be provided |

### Client-Server-Annotations

---


Most of the listed annotations are similar to the given ones above.

|    Annotation     | Required | Target       | Description                                                                                                                              |
|:-----------------:|:--------:|:-------------|:-----------------------------------------------------------------------------------------------------------------------------------------|
|    TypeServer     |  `true`  | Server-Class | specifies the target server-class and the server port                                                                                    |
|    TypeClient     |  `true`  | Client-Class | specifies the target client-class                                                                                                        |
|   Configuration   | `false`  | Client-Class | additional options can be added here - information about this annotation is provided below this table                                    |
|    Controller     |  `true`  | Class        | used by server and client to create a new controller which contains at least one context (`ConnectionHandler` or `RequestHandler`)       |
| ConnectionHandler |  `true`  | Method       | `client`: if Configuration.BY_CONNECTION is set, the method with this annotation should loop and handle input                            |
|  RequestHandler   |  `true`  | Method       | used by server and client to specify a new context                                                                                       |
|   ResponseBody    | `false`  | Method       | if a specified `RequestHandler` should send a response this option should be set                                                         |
|       Param       | `false`  | Parameter    | if additional parameters except from `ObjectExchange` or `MessageType` should be used - the name specifies a field in the message object |

`Configuration` is used by TypeClients and currently contains the following features:

* **BY_VALUE:** every request handler is executed separately
* **BY_CONNECTION:** here the `Controller`-annotation must contain the remote address of the server-backend; every
  context is executed if a new connection is established
* **IGNORE_VALUES:** the `BeanManager` removes all irrelevant classes before creating an instance

### Threading-Annotations

---

|   Annotation   | Required | Target | Description                                                                               |
|:--------------:|:--------:|:-------|:------------------------------------------------------------------------------------------|
|    Parallel    | `false`  | Method | indicates that the annotated method is executed in parallel                               |    
| SupplyParallel | `false`  | Method | indicates that the annotated method is executed parallel and the return-value is provided |
| ThreadPooling  | `false`  | Class  | indicates that the specified server or client will be executed in a threadpool            |