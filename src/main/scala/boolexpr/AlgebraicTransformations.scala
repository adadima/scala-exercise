package boolexpr

import scala.collection.mutable

object AlgebraicTransformations {

  /**
    * Simplifies a given BooleanExpression given an
    * assignment of a Variable to either True or False.
    * If the variable is not present in the expression,
    * then the output is identical to the input expression
    * @param expression BooleanExpression object to simplify
    * @param variable Variable object in the assignment
    * @param value True or False, suggested assignment for variable
    * @return a new BooleanExpression object that represents the simplification
    *         of expression given that  variable gets converted to value.
    *
    *        For example:
    *         simplifyWithAssignment(Or(Variable("x"), Variable("y")), Variable("y"), False) = Variable("x")
    */
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

  /**
    * Simplifies the given expression as much as
    * possible without any knowledge of the variable's
    * boolean values.
    *
    * For example, Or(True, Variable("x")) is
    * simplified to True. Similarly, Or(False, Variable("y")) is
    * simplified to Variable("y").
    *
    * @param expression expression to be simplified as a
    *                   BooleanExpression
    * @return a new simplified expression
    */
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

  /**
    * Computes the full disjunctive normal form
    * of the given expression. This means that each
    * inner "and" expression contains all variables
    * in this expression, negated or not.
    *
    * If input expression doe not have any solution,
    * the DNF will be returned as False.
    *
    * @param expression BooleanExpression expression
    *                   to convert to DNF
    * @return the full DNF of expression. Since in
    *         BooleanExpression Or is binary, multiple
    *         expressions connected by "or" operators are
    *         represented as nested Or objects, grouped from
    *         left to right, like this:
    *         (a or b or c) ---> Or(Or(a, b), c)
    *
    *         Same for the inner and expressions:
    *         (a and b and c) ---> And(And(a, b), c)
    */
  def convertToDNF(expression: BooleanExpression): BooleanExpression = {
    expression match {
      case True => True
      case False => False
      case e: Variable => e
      case e: BooleanExpression => putOrTogether(e)
    }

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

}
