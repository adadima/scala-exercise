package boolexpr

sealed trait BooleanExpression
case object True extends BooleanExpression
case object False extends BooleanExpression
case class Variable(symbol: String) extends BooleanExpression
case class Not(e: BooleanExpression) extends BooleanExpression
case class And(e1: BooleanExpression, e2: BooleanExpression) extends BooleanExpression
case class Or(e1: BooleanExpression, e2: BooleanExpression) extends BooleanExpression
