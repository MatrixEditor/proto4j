# proto4j

![GitHub repo size](https://img.shields.io/github/repo-size/matrixeditor/proto4j)
![GitHub contributors](https://img.shields.io/github/contributors/matrixeditor/proto4j)
![GitHub stars](https://img.shields.io/github/stars/matrixeditor/proto4j?style=flat)
![GitHub forks](https://img.shields.io/github/forks/matrixeditor/proto4j?style=flat)

This project contains a fully implemented client-server infrastructure controlled by Java-Annotations. Serialization is done with AES-Encryption and a http-server based on SUN-Webserver is also delivered.

**IMPORTANT:** 
    
    Currently the encryption is disabled due to errors that would be
    thrown if the payload is bigger than 256 bytes or multiple packets are
    being send at the same time. In future releases this issue will be
    resolved. Background of this issue is the following situation:

    If a client or server is sending multiple packets (asynchronously)
    scheduled to a receiver, sometimes the received messages are cut off
    in the middle of the content. The issue is solved temporarily with a
    `Queue` of objects that have been received additionally to the original 
    packet. 

## Prerequisites

Before you begin, ensure you have met the following requirements:

* You have installed the latest version of `JDK 13.0.2`
* You have read the [guide](https://github.com/MatrixEditor/proto4j/blob/main/guide.md).

Take a look at the **dev-001** branch and see the examples of http-server and a self-coded server-client infrastructure in the dev-module.

## Guides

Below a list of individual guides to specific topics of this project is given:
* [general-guide](https://github.com/MatrixEditor/proto4j/blob/main/guide.md)
* [message-guide](https://github.com/MatrixEditor/proto4j/blob/main/message-guide.md)
* [annotation-guide](https://github.com/MatrixEditor/proto4j/blob/main/annotation-guide.md)
* [serialization-format](https://github.com/MatrixEditor/proto4j/blob/main/serialization-format.md)

Updates are collected in [this](https://github.com/MatrixEditor/proto4j/blob/main/updates.md) small sheet.

## Contributing

Feel free to contribute to this project by creating issues or pull requests. The code is free to use in terms of the java-license.

