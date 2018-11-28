package boolexpr

import java.util.NoSuchElementException

import play.api.libs.json._

object BoolExpressionJsonDeserializer {

  def getErrorOrResultBinary(resultLeft: JsResult[BooleanExpression],
                       resutRight: JsResult[BooleanExpression],
                       consFunc: (BooleanExpression, BooleanExpression) => BooleanExpression): JsResult[BooleanExpression] = {
    val leftAndRight = (resultLeft, resutRight): (JsResult[BooleanExpression], JsResult[BooleanExpression])

    leftAndRight match {
      case (error: JsError, result: JsSuccess[BooleanExpression]) => error
      case (result: JsSuccess[BooleanExpression], error:JsError) => error
      case (error1: JsError, error2: JsError) => error1 ++ error2
      case (result1: JsSuccess[BooleanExpression], result2: JsSuccess[BooleanExpression]) =>
        JsSuccess(consFunc(result1.get, result2.get))
    }

  }

  def getErrorOrResultNot(resultLeft: JsResult[BooleanExpression]): JsResult[BooleanExpression] = {

    resultLeft match {
      case error: JsError => error
      case result: JsSuccess[BooleanExpression] => JsSuccess(Not(result.get))
    }

  }

  implicit val orReads: Reads[BooleanExpression] = new Reads[BooleanExpression] {

    def handleOr(js: JsValue): JsResult[BooleanExpression]= {
      val left: JsResult[BooleanExpression] = reads((js \ "leftExpression").get)
      val right: JsResult[BooleanExpression] = reads((js \ "rightExpression").get)

      getErrorOrResultBinary(left, right, (e1: BooleanExpression, e2: BooleanExpression) => Or(e1, e2))
    }

    def reads(js: JsValue) : JsResult[BooleanExpression] =  {
      (js \ "type").validate[String].getOrElse("wrongInput") match {

        case "Or" =>
          assertBinaryFields(js) match {
            case error: JsError => error
            case result: JsSuccess[String] =>
                handleOr(js)
          }
        case "And" =>
          val left: JsResult[BooleanExpression] =  reads((js \ "leftExpression").get)
          val right: JsResult[BooleanExpression] = reads((js \ "rightExpression").get)

          getErrorOrResultBinary(left, right, (e1: BooleanExpression, e2: BooleanExpression) => And(e1, e2))

        case "Not" =>
          val expression: JsResult[BooleanExpression] = reads((js \ "expression").get)
          getErrorOrResultNot(expression)

        case "Variable" => JsSuccess(Variable((js \ "name").validate[String].get))

        case "True" => JsSuccess(True)

        case "False" => JsSuccess(False)

        case "wrongInput" => JsError("Json is invalid and can not be deserialized, a \"type\" field needs" +
          "to be specified for each expression in the json")
      }
    }
  }

  def assertBinaryFields(jsonValue: JsValue): JsResult[String] = {
      try {
        val exprType: String = (jsonValue \ "type").validate[String].get
        val leftField: JsValue = (jsonValue \ "leftExpression").get
        val rightField: JsValue = (jsonValue \ "rightExpression").get
        JsSuccess("Correct And/Or fields")
      } catch {
          case ex: NoSuchElementException => JsError("And expressions need to have the following json fields:" +
            "leftExpression, rightExpression, type")
      }
  }

  def assertNotFields(jsonValue: JsValue): JsResult[String] = {
    try {
      val exprType: String = (jsonValue \ "type").validate[String].get
      val leftField: JsValue = (jsonValue \ "expression").get
      JsSuccess("Correct Not fields")
    } catch {
      case ex: NoSuchElementException => JsError("Not expressions need to have the following json fields:" +
        "expression, type")
    }
  }

  def assertVariableFields(jsonValue: JsValue): JsResult[String] = {
    try {
      val exprType: String = (jsonValue \ "type").validate[String].get
      val leftField: JsValue = (jsonValue \ "name").get
      JsSuccess("Correct Variable fields")
    } catch {
      case ex: NoSuchElementException => JsError("Variable expressions need to have the following json fields:" +
        "name, type")
    }
  }

  def assertTrueFalseFields(jsonValue: JsValue): JsResult[String] = {
    try {
      val exprType: String = (jsonValue \ "type").validate[String].get
      val leftField: JsValue = (jsonValue \ "value").get
      JsSuccess("Correct True/False fields")
    } catch {
      case ex: NoSuchElementException => JsError("True/False expressions need to have the following json fields:" +
        "value, type")
    }
  }

  def getBoolExpression(jsonValue: JsValue): BooleanExpression = {

      val result: JsResult[BooleanExpression] = jsonValue.validate[BooleanExpression]
      result match {
        case r: JsSuccess[BooleanExpression] => r.get
        case e: JsError => throw new IllegalArgumentException("Invalid input, here are the errors: " +
          JsError.toJson(e).toString());
      }
  }

  def deserialize(jsonString: String): BooleanExpression = {
      val jsonValue: JsValue = Json.parse(jsonString)
      getBoolExpression(jsonValue)
  }
}
