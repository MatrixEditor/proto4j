# Message-Guide

![module-spec](https://img.shields.io:/static/v1?label=Module&message=proto4j-annotation&color=informational)

In this guide all information about creating messages and its required annotations are delivered. The `Message` is a java-annotation which indicates that the annotated class should be a new message type. 
```java
@Message
@NoArgsConstructor
public class EchoMessage {}
```
The `@NoArgsConstructor` shows the serializer that a new object can be created without giving any arguments to the constructor. All fields are set automatically after the new instance is created.
```java
// EchoMessage class definition
@Component(ord = 1)
public int aNumber;
//... 
```
To specify a new message-field the `Component`-annotation is required. The ordinal number is used if `AllArgsConstructor` is specified. If so, the constructor-parameter at the position of this number has to be linked with this field.

**Important:** If all message-types are declared in a different package than the client or server there has to be a `@MessageRoot("a.b.c")` specification containing the package-path.

### Annotations


|     Annotation     | Required | Target | Description                                                                                                               |
|:------------------:|:--------:|--------|:--------------------------------------------------------------------------------------------------------------------------|
|      Message       |  `true`  | Class  | indicates that this class will be a new message specification                                                             |
| AllArgsConstructor | `false`  | Class  | if this option is set, all specified field are sorted by their ordinal number and given as an argument to the constructor |
| NoArgsConstructor  | `false`  | Class  | this option is set by default and indicates that no arguments are needed to create a new instance of this message type    |
|     Component      |  `true`  | Field  | adds the field to the message type - if this option is not set or `@Deperecated`is set the field will be ignored          | 
|   OptionalField    | `false`  | Field  | shows that this field is optional - NULL-value is received if this field hasn't been initialized                          |
|   RepeatedField    |  `true`  | Field  | only necessary if the field-type is of array or list                                                                      |
|       OneOf        | `false`  | Field  | this option is set by default to indicate this object is not repeated                                                     |
|      TypeSpec      | `false`  | Field  | if an object should be sent that has no standard for serialization, this annotation with a Serializer.class is given      |
|      AnyType       | `false`  | Field  | **not implemented yet**                                                                                                   |