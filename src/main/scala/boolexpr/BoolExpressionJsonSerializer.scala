package boolexpr

import play.api.libs.json._

/**
  * Json Serializer for boolexpr.BooleanExpression
  * import: boolexpr.BoolExpressionJsonSerialier._
  */
object BoolExpressionJsonSerializer {

    /* private helper function that defines how to encode case class fields into
       JsValue objects. This mapping acts as a writer similar to what toJson() method
       of the play scala framework uses.
    */
    private[this] def getJsonObject(expression: BooleanExpression): JsObject = {
        try {
        expression match {
          case True => Json.obj("value" -> JsTrue,
            "type" -> JsString("True")
          )
          case False => Json.obj("value" -> JsFalse,
            "type" -> JsString("False")
          )
          case Variable(symbol: String) => Json.obj(
            "name" -> symbol,
            "type" -> JsString("Variable")
          )
          case And(e1: BooleanExpression, e2: BooleanExpression) => Json.obj(
            "leftExpression" -> getJsonObject(e1),
            "rightExpression" -> getJsonObject(e2),
            "type" -> JsString("And")
          )

          case Or(e1: BooleanExpression, e2: BooleanExpression) => Json.obj(
            "leftExpression" -> getJsonObject(e1),
            "rightExpression" -> getJsonObject(e2),
            "type" -> JsString("Or")
          )

          case Not(e: BooleanExpression) => Json.obj(
            "expression" -> getJsonObject(e),
            "type" -> JsString("Not")
          )
        }} catch{
          case _: MatchError =>
           throw new IllegalArgumentException(expression + "is innvalid input. Expected type BooleanExpression.")
        }
  }

  /**
    * Serializes the given boolean expression to json,
    * specifying for each case class/object the value for
    * each field taken by constructor as well as the type/case class.
    *
    * Example:
    *
    *  - True => {\n  "value" : true,\n  "type" : "True"\n}
    *  - False => {\n  "value" : false,\n  "type" : "False"\n}
    *  - Variable(string) => {\n  "name" : string,\n  "type" : "Variable"\n}
    *  - And and Or have similar structure since they are both binary ops =>
    *  {\n  "leftExpression" : ...,\n  "rightExpression" : ...,\n  "type" : "And"/"Or\n}
    *  - Not(e) => {\n  "expression" : ...,\n  "type" : "Not"\n}
    *
    * @param expression BooleanExpression to serialize
    * @return a string JSON representation of expression
    */
    def serialize(expression: BooleanExpression): String = {
       val jsonValue: JsObject = getJsonObject(expression)
       Json.prettyPrint(jsonValue)
    }

}
