import org.scalatest.FunSuite
import boolexpr.ExpressionParser.parse
import boolexpr._

class ExpressionParserTest extends FunSuite{
    // Testing strategy

    /*
     * Partitions: - contains primitives: True, False, Variable or recursive cases: Not, And, Or
     *             - contains nested expressions
     *             - contains/does not contain whitespace
     *             - valid input
     *             - invalid input - syntax errors
     */

     // covers primitives, valid input
     test("Parsing to True, False and Variable") {
       assert(parse("True") == True)
       assert(parse("False") == False)
       assert(parse("Variable(\"x\")") == Variable("x"))
     }

     // covers binary op And, valid input and contains whitespace
     test("Parsing to And expression") {
       assert(parse("And(  Variable(\"x\"), False  )") == And(Variable("x"), False))
     }

     // covers binary op Or, valid input and does not contain whitespace
     test("Parsing to Or expression") {
       assert(parse("Or(Variable(\"withQuotes\"),True)") == Or(Variable("withQuotes"), True))
     }

     // covers Not, valid input and contains whitespace
     test("Parsing to Not expression") {
       assert(
              parse("Not(And(Not(Variable(  \"x\"  )),    False) )") ==
                Not(And(Not(Variable("x")), False))
             )
     }

     // covers nested expressions, Or, And, Not, Variable, True, False, contains whitespace, valid input
     test("Parsing nested expression") {
       assert(
              parse("Or(And(Not(False) , Variable(\"yz\")),  Or(True, False ) )") ==
              Or(And(Not(False), Variable("yz")), Or(True, False))
             )
     }

     // covers invalid input - too many left parenthesis/too many right parenthesis
     // variable name not put in quotes, wrong class name
     test("Trying to parse invalid input - syntax error") {
       assertThrows[IllegalArgumentException] {
         parse("Or(And(True, False), False ")
       }

       assertThrows[IllegalArgumentException] {
         parse("Or((And(True, False), False)")
       }

       assertThrows[IllegalArgumentException] {
         parse("Or(And(True, False), False)))")
       }

       assertThrows[IllegalArgumentException] {
         parse("Variable(x)")
       }

       assertThrows[IllegalArgumentException] {
         parse("true")
       }
     }
}
