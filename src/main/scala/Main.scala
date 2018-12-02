import boolexpr._
import boolexpr.BoolExpressionJsonSerializer
import boolexpr.AlgebraicTransformations.{simplify, convertToDNF}

object Main extends App {
  val exp: BooleanExpression = And(Or(Variable("x"), Variable("z")), And(Or(Not(Variable("y")), Variable("p")), Variable("q")))
  println(simplify(simplify(exp, Variable("z"), False), Variable("x"), True))
  println(convertToDNF(exp))
}
