package boolexpr

import play.api.libs.json.JsResult.Exception
import play.api.libs.json._

object BoolExpressionJsonSerializer {

    def getJsonObject(expression: BooleanExpression): JsObject = {
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
          case ex: MatchError =>
           throw new IllegalArgumentException(expression + "is innvalid input. Expected type BooleanExpression.")
        }
  }

    def serialize(expression: BooleanExpression): String = {
       val jsonValue: JsObject = getJsonObject(expression)
       Json.prettyPrint(jsonValue)
    }

}
