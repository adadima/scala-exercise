import org.scalatest.FunSuite
import boolexpr._

class BoolExpressionJsonSerializerTest extends FunSuite {
    // Testing strategy

    /* Partitions:
     * - case class: can be primitive True/False or Variable(empty and non-empty string) (base case)
     *               can be And, Or, Not (recursive case)
     * - valid object: no field is equal to null
     * - invalid object: case class has a null field passed to constructor
     */

     // covers the primitive case classes, Variable with empty and non-empty string
     test ("Testing serialiation of base case of BooleanExpression trait") {
        assert(BoolExpressionJsonSerializer.serialize(True) == "{\n  \"value\" : true,\n  \"type\" : \"True\"\n}")
        assert(BoolExpressionJsonSerializer.serialize(False) == "{\n  \"value\" : false,\n  \"type\" : \"False\"\n}")
        assert(BoolExpressionJsonSerializer.serialize(Variable("variableName")) == "{\n  \"name\" : \"variableName\",\n  \"type\" : \"Variable\"\n}")
        assert(BoolExpressionJsonSerializer.serialize(Variable("")) == "{\n  \"name\" : \"\",\n  \"type\" : \"Variable\"\n}")
     }

     // covers And, True and Variable with non-empty string
     test("Testing serialization of recursive case And") {
        val expected: String = "{\n" +
          "  \"leftExpression\" : " +
          "{\n" +
          "    \"value\" : true,\n" +
          "    \"type\" : \"True\"\n  },\n" +
          "  \"rightExpression\" : " +
          "{\n" +
          "    \"name\" : \"x\",\n" +
          "    \"type\" : \"Variable\"\n" +
          "  },\n" +
          "  \"type\" : \"And\"\n" +
          "}"
        val actual: String = BoolExpressionJsonSerializer.serialize(And(True, Variable("x")))
        assert(actual == expected)
     }

    // covers Or, False and Variable with empty string
    test("Testing serialization of recursive case Or") {
      val expected: String = "{\n" +
        "  \"leftExpression\" : " +
        "{\n" +
        "    \"name\" : \"\",\n" +
        "    \"type\" : \"Variable\"\n" +
        "  },\n" +
        "  \"rightExpression\" : " +
        "{\n" +
        "    \"value\" : false,\n" +
        "    \"type\" : \"False\"\n  },\n" +
        "  \"type\" : \"Or\"\n" +
        "}"
      val actual: String = BoolExpressionJsonSerializer.serialize(Or(Variable(""), False))
      assert(actual == expected)
    }

    // covers Not, True, Variable - non-empty string
    test("Testing serialization of recursive case Not") {
      val expectedNotTrue: String = "{\n" +
        "  \"expression\" : " +
        "{\n" +
        "    \"value\" : true,\n" +
        "    \"type\" : \"True\"\n  },\n" +
        "  \"type\" : \"Not\"\n" +
        "}"
      val actualNotTrue: String = BoolExpressionJsonSerializer.serialize(Not(True))
      assert(actualNotTrue == expectedNotTrue)
    }

    // covers And, Not, True, False, Or, includes nested binary boolean expressions
    test("Testing serialization of And - nested expression") {
      val expected: String = "{\n" +
        "  \"leftExpression\" : " +
        "{\n" +
        "    \"leftExpression\" : " +
        "{\n" +
        "      \"value\" : false,\n" +
        "      \"type\" : \"False\"\n" +
        "    },\n" +
        "    \"rightExpression\" : " +
        "{\n" +
        "      \"name\" : \"haha\",\n" +
        "      \"type\" : \"Variable\"\n" +
        "    },\n" +
        "    \"type\" : \"Or\"\n" +
        "  },\n"+
        "  \"rightExpression\" : " +
        "{\n    \"expression\" : {\n" +
        "      \"value\" : true,\n" +
        "      \"type\" : \"True\"\n    },\n"+
        "    \"type\" : \"Not\"\n"+
        "  },\n" +
        "  \"type\" : \"And\"\n}"
      val actual: String = BoolExpressionJsonSerializer.serialize(
                                                                  And(
                                                                      Or(False, Variable("haha")),
                                                                      Not(True)
                                                                      )
                                                                  )
      assert(actual == expected)
    }

    test("Testing serializing Not - nested expression") {
      val expected: String = "{\n" +
        "  \"expression\" : " +
        "{\n" +
        "    \"leftExpression\" : " +
        "{\n" +
        "      \"value\" : false,\n" +
        "      \"type\" : \"False\"\n" +
        "    },\n" +
        "    \"rightExpression\" : " +
        "{\n" +
        "      \"name\" : \"haha\",\n" +
        "      \"type\" : \"Variable\"\n" +
        "    },\n" +
        "    \"type\" : \"Or\"\n" +
        "  },\n"+
        "  \"type\" : \"Not\"\n" +
        "}"
      val actual: String = BoolExpressionJsonSerializer.serialize(Not(Or(False, Variable("haha"))))
      assert(actual == expected)
    }
}
