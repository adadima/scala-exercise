import org.scalatest.FunSuite
import boolexpr._
import boolexpr.BoolExpressionJsonDeserializer.deserialize


class BoolExpressionJsonDeserializerTest extends FunSuite {
  // Testign strategy

  /* Partitions:
   *    valid jsons that represent: - binary recursive cases: And, Or (nested or not)
   *                                - unary recursive case: Not (nested or not)
   *                                - primitives/base cases: True/False/Variable
   *    syntax: includes/doe snot include newlines and whitespaces
   *    invalid jsons: - wrong/misspelled/missing json fields
   *                   - syntax errors: missing parenthesis
   *                   - wrong values for json fields, ex: {value: true, type: False}
   */

  // Covers base cases: True, False, Variable, includes/does not include \n and " "
  test("Deserializing to primitive/base case - True/False/Variable") {
      val inputTrue: String = "{\"value\":true,\"type\":\"True\"}"
      assert(deserialize(inputTrue) == True)

      val inputFalse: String = "{\"value\": false,\"type\": \"False\"}"
      assert(deserialize(inputFalse) == False)

      val inputVar: String = "{\n  \"name\" : \"variableName\",\n  \"type\" : \"Variable\"\n}"
      assert(deserialize(inputVar) == Variable("variableName"))
  }

  // Covers False/Variable and recursive case And, no spaces or newlines
  test("Deserializing to And case class") {
      val inputAnd: String = "{\"leftExpression\":{\"name\":\"x\",\"type\":\"Variable\"},"+
                     "\"rightExpression\":{\"value\":false,\"type\":\"False\"},"+
                     "\"type\":\"And\"}"
      assert(deserialize(inputAnd) == And(Variable("x"), False))
  }

  // Covers Variable and recursive case Or, no spaces or newlines
  test("Deserializing to Or case class") {
    val inputOr: String = "{\"leftExpression\":{\"name\":\"first\",\"type\":\"Variable\"},"+
      "\"rightExpression\":{\"name\":\"second\",\"type\":\"Variable\"},"+
      "\"type\":\"Or\"}"
    assert(deserialize(inputOr) == Or(Variable("first"), Variable("second")))
  }

  // Covers True and recursive case Not, no spaces or newlines
  test("Deserializing to Not case class") {
    val inputNot: String = "{\"expression\":{\"value\":true,\"type\":\"True\"},"+
      "\"type\":\"Not\"}"
    assert(deserialize(inputNot) == Not(True))
  }

  // Covers nested binary expressions, includes whitespaces, but not new lines
  test("Deserializing to nested binary expression - And(Not(..), Or(.., ..))") {
    val inputAnd: String = "{ \"leftExpression\" : { \"expression\" : {\"value\" : true, " +
                           "\"type\" : \"True\"}, \"type\" : \"Not\" },"+
                           "    \"rightExpression\" : { \"leftExpression\" : { \"name\" : \"x\", "+
                           "\"type\" : \"Variable\" }, \"rightExpression\" : { \"name\" : \"y\", " +
                           "\"type\" : \"Variable\" }, \"type\" : \"Or\" }, "+
                           "\"type\" : \"And\" }"
    assert(deserialize(inputAnd) == And(Not(True), Or(Variable("x"), Variable("y"))))
  }

  // covers invalid input: unmatched accolades in json
  test("Deserializing with invalid input - unmatched accolades") {
    val invalid: String = "{ \"e1\" : {\"value\" : true, \"type\": \"True\" }, "+
                          "  \"e2\" : {\"name\" : \"var\", \"type\" : \"Variable\"," +
                          " \"type\" : \"And\" }"
    val ex = intercept[IllegalArgumentException] {
      deserialize(invalid)
    }
    assert(ex.getMessage == "The input json string is not formatted properly. " +
      "Did you close all accolades?")

    val invalidTrue: String = "{ \"val\": true, \"type\" : \"True\"}}}"
    val ex2 = intercept[IllegalArgumentException] {
      deserialize(invalidTrue)
    }
    assert(ex2.getMessage == "The input json string is not formatted properly. " +
      "Did you close all accolades?")
  }

  // covers invalid input: wrong json attributes/fields
  test("Deserializing with invalid input - wrong json fields") {
    val invalidOr: String = "{ \"e1\" : {\"value\" : true, \"type\": \"True\" }, "+
      "  \"e2\" : {\"name\" : \"var\", \"type\" : \"Variable\" }," +
      " \"type\" : \"Or\" }"

    assertThrows[IllegalArgumentException] {
      deserialize(invalidOr)
    }

    val invalidAnd: String = "{ \"leftExpression\" : {\"value\" : true, \"type\": \"True\" }, "+
      "  \"rightExpression\" : {\"name\" : \"var\", \"type\" : \"Variable\" }," +
      " \"class\" : \"And\" }"

    assertThrows[IllegalArgumentException] {
      deserialize(invalidAnd)
    }

    val invalidNot: String = "{ \"Expression\" : {\"value\" : true, \"type\" : \"True\" }, \"type\" :" +
                              "\"Not\" }"
    assertThrows[IllegalArgumentException] {
      deserialize(invalidNot)
    }

    val invalidTrue: String = "{ \"val\": true, \"type\" : \"True\"}"
    assertThrows[IllegalArgumentException] {
      deserialize(invalidTrue)
    }

    val invalidFalse: String = "{ \"value\": true, \"case\" : \"False\"}"
    assertThrows[IllegalArgumentException] {
      deserialize(invalidFalse)
    }

    val invalidVariable: String = "{ \"var\" : \"x\", \"type\": \"Variable\"}"
    assertThrows[IllegalArgumentException] {
      deserialize(invalidVariable)
    }
  }

  // covers invalid input - missing json fields
  test("Deserializing with invalid input - missing fields") {
    val invalidOr: String = "{ \"e1\" : {\"value\" : true, \"type\": \"True\" }, "+
      " \"type\" : \"Or\" }"

    assertThrows[IllegalArgumentException] {
      deserialize(invalidOr)
    }

    val invalidNot: String = "{\"expression\" : {\"value\" : true, \"type\": \"True\" }}"

    assertThrows[IllegalArgumentException] {
      deserialize(invalidNot)
    }

    val invalidFalse: String = "{\"type\": \"False\"}"

    assertThrows[IllegalArgumentException] {
      deserialize(invalidFalse)
    }
  }

  // covers invalid input - wrong values associated with json fields
  test("Deserializing with invalid input - wrong strings for field values") {
    val invalidOr: String = "{ \"leftExpression\" : {\"value\" : true, \"type\": \"True\" }, "+
      "  \"rightExpression\" : {\"name\" : \"var\", \"type\" : \"Variable\" }," +
      " \"type\" : \"OR\" }"

    assertThrows[IllegalArgumentException] {
      deserialize(invalidOr)
    }

    val invalidNot: String = "{ \"expression\" : {\"value\" : false, \"type\" : \"True\" }, \"type\" :" +
      "\"Not\" }"
    assertThrows[IllegalArgumentException] {
      deserialize(invalidNot)
    }

    val invalidTrue: String = "{ \"val\": true, \"type\" : \"true_booool\"}"
    assertThrows[IllegalArgumentException] {
      deserialize(invalidTrue)
    }
  }


}
