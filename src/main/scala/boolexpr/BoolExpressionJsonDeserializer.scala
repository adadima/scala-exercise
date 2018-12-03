package boolexpr

import java.util.NoSuchElementException

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.io.JsonEOFException
import play.api.libs.json._

/**
  * Json Deserializer from JSON to boolexpr.BooleanExpression
  * import: boolexpr.BoolExpressionDeserializer.deserialize
  */
object BoolExpressionJsonDeserializer {

  /**
    * Deserializes a JSON given as String to its
    * corresponding BooleanExpression value.
    *
    * @param jsonString - a JSON string representing a BooleanExpression.
    *                   - whitespaces and newlines are ignored, so the
    *                   following two jsons translate to the same value, for
    *                   example: "{\n  "value" : true,\n  "type" : "True"\n}"
    *                   "{"value" : true,"type" : "True"}"
    *                   - requires that the jsonString contains the standard fields
    *                   described in BoolExpressionSerializer specification. That is:
    *
    *                   True and False have a "value" and a "type" field
    *                   Variable has a "name" and a "type" field
    *                   And/Or has a "leftExpression", a "rightExpression" and a "type" field
    *                   Not has an "expression" and a "type" field
    *
    *                   - each BooleanExpression shoud have its field : value mappings separated
    *                   by a "," and encolsed in accolades "{ .. "}"
    * @return the BooleanExpression value as read from jsonString
    * @throws IllegalArgumentException if the given string is badly formatted:
    *                                  - missing/mismatched accolades
    *                                  - missing/wrong json strin fields
    *                                  - inconsistent type with fields values, ex:
    *                                  "{"value" : true, "type" : "False"}"
    */
  def deserialize(jsonString: String): BooleanExpression = {
    try {

      val jsonValue: JsValue = Json.parse(jsonString)
      getBoolExpression(jsonValue)
    } catch {

      case _: JsonEOFException =>
        throw new IllegalArgumentException("The input json string is not formatted properly. " +
          "Did you close all accolades?")

      case _: JsonParseException =>
        throw new IllegalArgumentException("The input json string is not formatted properly. " +
          "Did you close all accolades?")

      case e: Exception =>
        throw e
    }

  }

  // Gets the BooleanExpression from a given JsValue using the validate method,
  // which uses the above defined reader
  def getBoolExpression(jsonValue: JsValue): BooleanExpression = {

    val result: JsResult[BooleanExpression] = jsonValue.validate[BooleanExpression]
    result match {
      case r: JsSuccess[BooleanExpression] => r.get
      case e: JsError => throw new IllegalArgumentException("Invalid input, here are the errors: " +
        JsError.toJson(e).toString());
    }
  }

    /*
    Gets a JsResult, which is JsSucces(BooleanExpression) if the given left and right
    BooleanExpressions could be assemmbled into a Binary Op like And/Or. JsError if
    an error occured while doing this.
   */
    def getErrorOrResultBinary(
                                resultLeft: JsResult[BooleanExpression],
                                resutRight: JsResult[BooleanExpression],
                                consFunc: (BooleanExpression, BooleanExpression) => BooleanExpression
                              ): JsResult[BooleanExpression] = {

      (resultLeft, resutRight) match {
        case (error: JsError, _: JsSuccess[BooleanExpression]) => error
        case (_: JsSuccess[BooleanExpression], error: JsError) => error
        case (error1: JsError, error2: JsError) => error1 ++ error2
        case (result1: JsSuccess[BooleanExpression], result2: JsSuccess[BooleanExpression]) =>
          JsSuccess(consFunc(result1.get, result2.get))
      }

    }

    /*
   Gets a JsResult, which is JsSucces(BooleanExpression) if the given expression
   BooleanExpressions could be assembled into a Not expression. JsError if
   an error occured while doing this.
  */
    def getErrorOrResultNot(resultLeft: JsResult[BooleanExpression]): JsResult[BooleanExpression] = {

      resultLeft match {
        case error: JsError => error
        case result: JsSuccess[BooleanExpression] => JsSuccess(Not(result.get))
      }

    }

    implicit val booleanExpressionReads: Reads[BooleanExpression] = new Reads[BooleanExpression] {

      // Handles converting a JsValue to an And boolean expression, resturns JsResult[BooleanExpression]
      def handleAnd(js: JsValue): JsResult[BooleanExpression]= {
        getErrorOrResultBinary(
          reads((js \ "leftExpression").get),
          reads((js \ "rightExpression").get),
          (e1, e2) => And(e1, e2)
        )
      }

      // Handles converting a JsValue to an Or boolean expression, resturns JsResult[BooleanExpression]
      def handleOr(js: JsValue): JsResult[BooleanExpression]= {
        getErrorOrResultBinary(
          reads((js \ "leftExpression").get),
          reads((js \ "rightExpression").get),
          (e1, e2) => Or(e1, e2)
        )
      }

      // Handles converting a JsValue to a Not boolean expression, resturns JsResult[BooleanExpression]
      def handleNot(js: JsValue): JsResult[BooleanExpression]= {
        getErrorOrResultNot(reads((js \ "expression").get))
      }

      //  Defines a reader that would translate from a JsValue to a BooleanExpression
      // Returns JsResult.
      def reads(js: JsValue): JsResult[BooleanExpression] = {
        (js \ "type").validate[String].getOrElse("wrongInput") match {

          case "Or" =>
            assertBinaryFields(js) match {
              case error: JsError => error
              case _: JsSuccess[String] =>  handleOr(js)

            }

          case "And" =>
            assertBinaryFields(js) match {
              case error: JsError => error
              case _: JsSuccess[String] => handleAnd(js)
            }

          case "Not" =>
            assertNotFields(js) match {
              case error: JsError => error
              case _: JsSuccess[String] => handleNot(js)
            }

          case "Variable" =>
            assertVariableFields(js) match {
              case error: JsError => error
              case _: JsSuccess[String] => JsSuccess(Variable((js \ "name").validate[String].get))
            }

          case "True" =>
            assertTrueFalseFields(js) match {
              case error: JsError => error
              case _: JsSuccess[String] => JsSuccess(True)
            }

          case "False" =>
            assertTrueFalseFields(js) match {
              case error: JsError => error
              case _: JsSuccess[String] => JsSuccess(False)
            }

          case _: String => JsError("Json is invalid and can not be deserialized, a \"type\" field needs" +
            "to be specified for each expression in the json and it only can be: " +
            "And, Or, Not, Variable, True or False.")
        }
      }
    }

    // chekcs that the path to a binary boolean expression (And/Or) contains the right string fields
    def assertBinaryFields(jsonValue: JsValue): JsResult[String] = {
      try {

        (jsonValue \ "type").validate[String].get
        (jsonValue \ "leftExpression").get
        (jsonValue \ "rightExpression").get
        JsSuccess("Correct And/Or fields")
      } catch {

        case _: NoSuchElementException => JsError("And/Or expressions need to have the following json fields:" +
          "leftExpression, rightExpression, type")
      }
    }

    // chekcs that the path to a Not boolean expression contains the right fields
    def assertNotFields(jsonValue: JsValue): JsResult[String] = {
      try {

        (jsonValue \ "type").validate[String].get
        (jsonValue \ "expression").get
        JsSuccess("Correct Not fields")
      } catch {

        case _: NoSuchElementException => JsError("Not expressions need to have the following json fields:" +
          "expression, type")
      }
    }

    // checks that the path to a Variable boolean expressin json has the right string fields
    def assertVariableFields(jsonValue: JsValue): JsResult[String] = {
      try {

        (jsonValue \ "type").validate[String].get
        (jsonValue \ "name").get
        JsSuccess("Correct Variable fields")
      } catch {

        case _: NoSuchElementException => JsError("Variable expressions need to have the following json fields:" +
          "name, type")
      }
    }

    //  checks that value and type fields of a True/False expression json are consistent
    def assertValueTypeConsistent(jsonValue: JsValue, eType: String): JsResult[String] = {

      if ((jsonValue == JsTrue && eType == "True") || (jsonValue == JsFalse && eType == "False"))
        JsSuccess("Correct true field")
      else
        JsError("True object must have value: true and False objects must have " +
          "value: false in the json string. Your value is: " + jsonValue + "but type is: " +
          eType)

    }

    // checks that the path to a true/False boolean expression json has the right string fields
    def assertTrueFalseFields(jsonValue: JsValue): JsResult[String] = {
      try {

        assertValueTypeConsistent((jsonValue \ "value").get, (jsonValue \ "type").validate[String].get)
      } catch {

        case _: NoSuchElementException => JsError("True/False expressions need to have the following json fields:" +
          "value, type")
      }
    }
}

