# Format-Specification

![module-spec](https://img.shields.io:/static/v1?label=Module&message=proto4j-serialization&color=informational)

Below a small description of the serialization format of messages is provided. This method of encoding is used in the communication between the client and server. 

### Header

---
The `Header` of the serialized message contains information about the message class and its constructor modifiers. The structure is as follows: 
> package-name.ClassName::ClassName::Modifiers\n

* `package-name`: the list of root-packages this class is declared in, for instance `a.b.c.[ClassName]`
* `ClassName`: the `Class::getSimpleName()` result
* `Modifiers`: all modifiers added to this class, currently only if there is a `@AllArgsConstructor` annotation or not

### Body

---
The `Body` consists of all fields from the message-class that are annotated with `@Component`. There are different options that can be enabled for every field. These options are explained in the [message-guide](https://github.com/MatrixEditor/proto4j/blob/main/message-guide.md).

Overall, the structure can be summarized to the following:
> ord-[modifiers]-type-length-value-\r

Through the different field-types the `length`, `type`and `value` serialization is specified by the field. There are three common field-types implemented in this module:
* `PrimitiveFieldDesc`: this type of field contains all java-related primitive types such as `int`, `double`, ...
* `RepeatedFieldDesc`: this field type contains a standard implementation for serializing `Collections` and `Arrays` 
* `TypeSpecField`: fields annotated with `@TypeSpec` have an own serializer specified which this field type handles