package boolexpr

import scala.collection.mutable

object AlgebraicTransformations {

  private[this] def simplifyAnd(expression: And): BooleanExpression = {
      expression match {
        case And(True, e) => e
        case And(e, True) => e
        case And(False, _) => False
        case And(_, False) => False
        case And(e1, e2) => And(e1, e2)
      }
  }

  private[this] def simplifyOr(expression: Or): BooleanExpression = {
      expression match {
        case Or(True, _) => True
        case Or(_, True) => True
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

  def simplifyWithAssignment(expression: BooleanExpression, variable: Variable, value: BooleanExpression): BooleanExpression = {
    expression match {
      case True => True
      case False => False
      case Variable(name) => if (expression.equals(variable)) value else Variable(name)
      case Not(e) => simplifyNot(Not(simplifyWithAssignment(e, variable, value)))
      case Or(e1, e2) => simplifyOr(Or(
                                      simplifyWithAssignment(e1, variable, value),
                                      simplifyWithAssignment(e2, variable, value))
                                    )
      case And(e1, e2) => simplifyAnd(And(
                                          simplifyWithAssignment(e1, variable, value),
                                          simplifyWithAssignment(e2, variable, value))
                                      )
    }
  }

  def simplify(expression: BooleanExpression): BooleanExpression = {
    expression match {
      case True => True
      case False => False
      case Variable(name) => Variable(name)
      case Not(e) => simplifyNot(Not(simplify(e)))
      case Or(e1, e2) => simplifyOr(Or(simplify(e1), simplify(e2)))
      case And(e1, e2) => simplifyAnd(And(simplify(e1), simplify(e2)))
    }
  }

  private[this] def getAllVariables(expression: BooleanExpression): Set[Variable] = {
      val vars = Set[Variable] ()

      expression match {
        case True => vars
        case False => vars
        case Variable(name) => vars + Variable(name)
        case And(e1, e2) => vars ++ getAllVariables(e1) ++ getAllVariables(e2)
        case Or(e1, e2) => vars ++ getAllVariables(e1) ++ getAllVariables(e2)
        case Not(e) => vars ++ getAllVariables(e)
      }
  }

  private[this] def findAllSolutions(expression: BooleanExpression,
                                     varsToAssign: Set[Variable],
                                     assignments: Map[Variable, BooleanExpression],
                                     solns: mutable.Set[Map[Variable, BooleanExpression]]):Unit = {
    
    if (varsToAssign.isEmpty) {
      solns.add(assignments)
    }

    if (varsToAssign.isEmpty) {
      return
    }

    val variable = varsToAssign.head

    // try reducing with variable: True
    val simpleExprTrue = simplifyWithAssignment(expression, variable, True)
    if (!simpleExprTrue.equals(False)) {
      findAllSolutions(simpleExprTrue, varsToAssign - variable, assignments + (variable -> True), solns)
    }

    //try reducing with variable: False
    val simpleExprFalse = simplifyWithAssignment(expression, variable, False)
    if (!simpleExprFalse.equals(False)) {
      findAllSolutions(simpleExprFalse, varsToAssign - variable, assignments + (variable -> False), solns)
    }

  }

  def makeAndExpression(vars: Map[Variable, BooleanExpression]): BooleanExpression = {
    var and: BooleanExpression = True

    if (vars.size == 1) {
      return vars(vars.keys.iterator.next())
    }

    for(variable <- vars.keySet) {
      if (and == True)
        and = if (vars(variable).equals(True)) variable else Not(variable)
      else
        and = if (vars(variable).equals(True)) And(and, variable) else And(and, Not(variable))

    }
    and
  }

  def putOrTogether(expression: BooleanExpression): BooleanExpression = {
    val vars = getAllVariables(expression)
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

  def convertToDNF(expression: BooleanExpression): BooleanExpression = {
      expression match {
        case True => True
        case False => False
        case e: Variable => e
        case e: BooleanExpression => putOrTogether(e)
      }

  }

}
