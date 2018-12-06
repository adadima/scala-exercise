import boolexpr._


import ExpressionParser._


object Main extends App {

  for (tok <- tokenize("And(True, And(False, Not(Variable(\"x\"))))").toList) {
    println(tok)
    println(tok.length)
  }
  println(parse("And(True, And(False, Not(Variable(\"x\"))))"))
}
