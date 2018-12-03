# scala-exercise

This repository contains the boolexpr package which offers support for JSON 
serialization and deserialization of the objects and case classes pertaining 
to the `BooleanExpression` trait (defined in `boolexpr.BooleanExpression`).

### Using the serializer

```scala
import boolexpr.BoolExpressionSerializer.serialize
```

The `serialize(booleanExpression)` method takes an object/case class from the 
BooleanExpression trait and returns its JSON serialization. All non-null input 
that passes scala static-checking should be considered valid input for this 
method.

The output is of the form:
```
- True
{
    "value" : true,
    "type" : "True"
}
- False
{
    "value" : false,
    "type" : "False"
}
- Variable(string)
{
    "name" : string,
    "type" : "Variable"
}
- And and Or have similar structure since they are both binary operations
{
    "leftExpression" : ...,
    "rightExpression" : ...,
    "type" : "And"/"Or"
}
- Not(e)
{
    "expression" : ...,
    "type" : "Not"
}
```

The method will throw an `IllegalArgumentException` in the case that any 
expression or field passed to a BooleanExpression is null.

### Using the deserializer

 `import boolexpr.BoolExpressionDeserializer.deserialize`
 
 The `deserialize(jsonString)` method takes a JSON as a string and produces the 
 `BooleanExpression` represented by the input. If the input is not properly 
 formatted, the method will throw an `IllegalArgumentException` with a message 
 describing what the problem might be. Whitespace and newlines outside of the 
 field names and field values will be ignored. Therefore, the following two jsons
  are deserialized to the same `BooleanExpression` value:

 ```
 "{"value" : true,\n  "type" : "True"\n}"
 "{"value" : true,"type" : "True"}"
 ```

Examples of valid input:
 ```
 - "{
      "value" : true,
      "type" : "True"
    }"
 - "{"name":"x", "type":"Variable"}"
 - "{
    "leftExpression": ...,
    "rightExpression": ...,
    "type":"And
    }"
 ```
 Examples of invalid input (method will throw IllegalArgumentException):
 ```
 - "{
      "value" : true,
      "type" : "True" " (mismatched parenthesis)
 - {"var" : "x", "class" : "Variable"} (not following the standard for field names)
 - {"value": true, "type":False} (inconsistent field values)
 - {"value":true} (missing field "type")
 ```
 

Moreover, this package also allows algebraic transformations 
(through boolexpr.AlgebraicTransformations, which is still under
improvement) like:

- Simplifying a BooleanExpression given an assignment from a Variable(x) 
to True/False
   ```
       import boolexpr.AlgebraicTransformations.simplify
       simplify(expression, variable, value)
   ```
- Convert a BooleanExpression to its Disjunctive Normal Form.
    ```
        import boolexpr.AlgebraicTransformations.convertToDNF
        convertToDNF(expression)
    ```
Currently in work:

- Server that receives expressions (as JSON strings) from a client, convert them to
DNF and sends back the response.
- Client that takes input from a console user, parser input to BooleanExpression, 
and sends request (for DNF conversion) to the server, serializing the input first.
- ExpressionParser which parses a string and returns a BooleanExpression.
