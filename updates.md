# Updates

This file contains recent changes and updates to this library, starting with the newest.

---

#### 04/02/2022: Updated the Proto4jReader

![module-spec](https://img.shields.io:/static/v1?label=Module&message=proto4j-network&color=informational)

The main issue can be found in the README file. Additionally, removed the `ConnectionHandler` annotation due to invalid processing.

#### 02/08/2022: Added new Module: proto4j-security

![module-spec](https://img.shields.io:/static/v1?label=Module&message=proto4j-security&color=informational)

All related information on this module can be found in the general-guide. Additionally, the communication between client and server has been changed. Before any packets are sent a `CertificateExchange` is done to exchange the `Publickey`. 

* `ObjectConnection`: `Input`- and `OutputStream` changed to `Proto4jReader`and -`Writer`