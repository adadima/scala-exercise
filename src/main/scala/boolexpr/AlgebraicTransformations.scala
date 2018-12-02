package boolexpr

import scala.collection.mutable

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

  private[this] def getAllVariables(expression: BooleanExpression): mutable.Set[Variable] = {
      val vars: mutable.Set[Variable] = mutable.Set()
      expression match {
        case True => vars
        case False => vars
        case Variable(name) => vars += Variable(name)
        case And(e1, e2) => vars ++= getAllVariables(e1) ++ getAllVariables(e2)
        case Or(e1, e2) => vars ++= getAllVariables(e1) ++ getAllVariables(e2)
        case Not(e) => vars ++= getAllVariables(e)
      }
  }

  private[this] def findAllSolutions(expression: BooleanExpression,
                                     varsToAssign: mutable.Set[Variable],
                                     assignments: Map[Variable, BooleanExpression],
                                     solns: mutable.Set[Map[Variable, BooleanExpression]]):Unit = {
    if (expression.equals(True)) {
      solns.add(assignments)
    }

    if (varsToAssign.isEmpty) {
      return
    }

    val variable = varsToAssign.head
    varsToAssign.remove(variable)

    // try reducing with variable: True
    val simpleExprTrue = simplify(expression, variable, True)
    if (!simpleExprTrue.equals(False)) {
      findAllSolutions(simpleExprTrue, varsToAssign, assignments + (variable -> True), solns)
    }

    //try reducing with variable: False
    val simpleExprFalse = simplify(expression, variable, False)
    if (!simpleExprFalse.equals(False)) {
      findAllSolutions(simpleExprFalse, varsToAssign, assignments + (variable -> False), solns)
    }

  }

  private[this] def makeAndExpression(vars: Map[Variable, BooleanExpression]): BooleanExpression = {
    var and: BooleanExpression = True

    for(variable <- vars.keySet) {
//      if (and.equals(True)){
//        and = vars.getOrElse(variable, True)
//      } else {
        and = if (vars.getOrElse(variable, True).equals(True)) And(and, variable) else And(and, Not(variable))
//      }
    }
    and
  }

  def convertToDNF(expression: BooleanExpression): BooleanExpression = {
      val vars: mutable.Set[Variable] = getAllVariables(expression)
      val allSolutions = mutable.Set[Map[Variable, BooleanExpression]]()

      findAllSolutions(expression, vars, Map(), allSolutions)
      var dnf: BooleanExpression = False

      for (sol <- allSolutions) {
        if (dnf.equals(False)) {
          dnf = makeAndExpression(sol)
        } else {
          dnf = Or(dnf, makeAndExpression(sol))
        }
      }

      dnf
  }

}
