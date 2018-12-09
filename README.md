# scala-exercise

This repository contains the boolexpr package which offers support for JSON 
serialization and deserialization of the objects and case classes pertaining 
to the `BooleanExpression` trait (defined in `boolexpr.BooleanExpression`). 

It also allows doing certain alegbraic transformations on BooleanExpressions 
(like simplifcation and convertion to full disjunctive normal form) as well as parsing
string expressions to BooleanExpression objects. A client process is also presented.
It accepts boolean expressions input from the user, parses it and communicates with the server,
which applies an algebraic transformation on the expression. Ultimately, the client prints to
the user the result of the transformation. 

All these features are detailed below.

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
 
### Algebraic Transformation 

Moreover, this package also allows algebraic transformations 
(through boolexpr.AlgebraicTransformations, which is still under
improvement) like:

- Simplifying a BooleanExpression given an assignment from a Variable(x) 
to True/False
   ```
       import boolexpr.AlgebraicTransformations.simplify
       simplifyWithAssignment(expression, variable, value)
   ```
   Examples: 
     ```
       simplifyWithAssignment(Or(Variable("x"), Variable("y")), Variable("y"), False) = Variable("x")
       simplifyWithAssignment(And(Variable("x"), Variable("y")), Variable("x"), False) = False
     ```
- Convert a BooleanExpression to its Full Disjunctive Normal Form (and clauses connected by or operators). 
  This means that the each inner and clause contains all variables in the initial boolean expression, either
  negated or not.
    ```
        import boolexpr.AlgebraicTransformations.convertToDNF
        convertToDNF(expression)
    ```
    
    Example:
     ```
        convertToDNF(And(Or(True, Variable("x")), Not(Variable("y")))) = Or(
                                                                            And(Not(Variable("x")), Not(Variable("y"))),  
                                                                            And(Variable("x"), Not(Variable("y"))
                                                                            )
    ```
    Note that the order of the inner "And" clauses inside the Or expressions as well as the order of the variables inside the "And" 
    expressions is arbitrary.
        
### Parsing Expressions 
The package also offers support for parsing strings as BooleanExpressions. To use it, one needs to import the
parse method from boolexpr.ExpressionParser. The input string should look exactly as the code one would use to instantiate a BooleanExpression object (see example below). If the string input to the parse() method is not properly formatted,
the method would raise an IllegalArgumentException.

 ```
        parse("True") = True
        parse("False") = False
        parse("And(Variable(\"x\"), Variable(\"y\"))") = And(Variable("x"), Variable("y"))
        etc.
 ```

### The server and the client
Finally, there is also a Server class (boolexpr.Server) which represents a server receiving BooleanExpression expressions serialized
into JSON from a client, converts them to the Full DNF and then sends the response as a JSON back to the client.

The server can be started by instantiating the Server class with a given port number and calling it's method serve(). This is exemplified in boolexpr.ServerMain, which can be ran to start the server. The server continues to listen for new connections unless
the process is intentionally terminated.

The client process is located in boolexpr.Client. This client accepts boolean expression strings from the console and prints back their
full disjunctive normal forms( communicates with the server to do this, so the server needs to be started beforehand ). 

Again, similar to the requirements for the ExpressionParser, the console input should look exactly as the code one would use to instantiate a BooleanExpression object.

