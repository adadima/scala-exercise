import boolexpr._
import boolexpr.BoolExpressionJsonSerializer

object Main extends App {
  val jstring: String = boolexpr.BoolExpressionJsonSerializer.serialize(
                      And(Or(Variable("x"), True), And(Or(False, False), Variable("xyz")))
  )
  //println(jstring)
  val invalid: String = "{ \"value\": true, \"type\" : \"True\"}"
  println(boolexpr.BoolExpressionJsonDeserializer.deserialize(invalid))

}
