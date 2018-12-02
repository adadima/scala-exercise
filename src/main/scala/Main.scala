import boolexpr._
import boolexpr.BoolExpressionJsonSerializer
import boolexpr.AlgebraicTransformations.simplify

object Main extends App {
  val exp: BooleanExpression = And(Variable("x"), And(Not(Variable("z")), Or(Variable("y"), Not(Variable("x")))))
  println(simplify(simplify(exp, Variable("z"), False), Variable("x"), True))

}
