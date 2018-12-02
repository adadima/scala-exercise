package boolexpr

object AlgebraicTransformations {

  private[this] def simplifyAnd(expression: And): BooleanExpression = {
      expression match {
        case And(True, e) => e
        case And(e, True) => e
        case And(True, True) => True
        case And(False, e) => False
        case And(e, False) => False
        case And(e1, e2) => And(e1, e2)
      }
  }

  private[this] def simplifyOr(expression: Or): BooleanExpression = {
      expression match {
        case Or(True, e) => True
        case Or(e, True) => True
        case Or(False, e) => e
        case Or(e, False) => e
        case Or(e1, e2) => Or(e1, e2)
      }
  }

  private[this] def simplifyNot(expression: Not): BooleanExpression = {
      expression match {
        case Not(True) => False
        case Not(False) => True
        case Not(e) => Not(e)
      }
  }

  def simplify(expression: BooleanExpression, variable: Variable, value: BooleanExpression): BooleanExpression = {
    expression match {
      case True => True
      case False => False
      case Variable(name) => if (expression.equals(variable)) value else Variable(name)
      case Not(e) => simplifyNot(Not(simplify(e, variable, value)))
      case Or(e1, e2) => simplifyOr(Or(simplify(e1, variable, value), simplify(e2, variable, value)))
      case And(e1, e2) => simplifyAnd(And(simplify(e1, variable, value), simplify(e2, variable, value)))
    }
  }

}
