import boolexpr._
import boolexpr.BoolExpressionJsonSerializer

object Main extends App {
  val jstring: String = boolexpr.BoolExpressionJsonSerializer.serialize(
                      And(Or(Variable("x"), True), And(Or(False, False), Variable("xyz")))
  )
  //println(jstring)
  //println(boolexpr.BoolExpressionJsonDeserializer.deserialize(jstring))
  println(BoolExpressionJsonSerializer.serialize(Variable("x")))
}
