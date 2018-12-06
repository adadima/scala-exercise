package boolexpr

/**
  * Parser that converts properly formatted strings
  * to BooleanExpression, to import:
  * boolexpr.ExpressionParser.parse
  */
object ExpressionParser {

    /*
     * Splits a given string into separate tokens relevant for
     * parsing a BooleanExpression obejct. For example the following string:
     * "And(Variable("x"), True) has the following array of tokens:
     * ["And", "(", "Variable", "(", "x", ")", ",", "True", ")"]
     */
    def tokenize(expression: String): Array[String] = {

      val newString: String = expression.toCharArray.foldLeft("")( (x: String, y: Char) =>
        if (y.equals('(') || y.equals(')') || y.equals(',')) x + " " + y + " " else x + y)

      newString.split("\\s+")
    }

    /*
     * Handles parsing a binary boolean expression (And/Or)
     */
    def buildBinaryExpression(
                         tokens: Array[String],
                         nextIndex: Int,
                         function2: (BooleanExpression, BooleanExpression) => BooleanExpression
                       ): (BooleanExpression, Int) = {

      if (!tokens(nextIndex + 1).equals("(")) {
        throw new IllegalArgumentException("Syntax Error")
      }

      val left = parseWithIndex(tokens, nextIndex + 2)
      val right = parseWithIndex(tokens, left._2 + 1)

      Tuple2(function2(left._1, right._1), right._2 + 1)
    }

  /*
   * Handles parsing a Not expression
   */
    def buildNotExpression(
                               tokens: Array[String],
                               nextIndex: Int
                          ): (BooleanExpression, Int) = {

      if (!tokens(nextIndex + 1).equals("(")) {
        throw new IllegalArgumentException("Syntax Error")
      }

      val expr = parseWithIndex(tokens, nextIndex + 2)
      Tuple2(Not(expr._1), expr._2 + 1)
    }

  /*
   * Handles parsing a Variable expression
   */
    def buildVariableExpression(
                                tokens: Array[String],
                                nextIndex: Int
                               ): (BooleanExpression, Int) = {

      if (!tokens(nextIndex + 1).equals("(")) {
        throw new IllegalArgumentException("Syntax Error")
      }

      Tuple2(Variable(tokens(nextIndex + 2)), nextIndex + 3)
    }

    /*
     * Given a list of tokens and a nextIndex integer, returns a tuple in which
     * the first element is the BooleanExpression expression that starts
     * at the token with index nextIndex and the second elements is the index
     * in the tokens array immediately after the index where the parsed expression ends.
     */
    def parseWithIndex(tokens: Array[String], nextIndex: Int): (BooleanExpression, Int) = {
      if (tokens(nextIndex).equals("And"))
        buildBinaryExpression(tokens, nextIndex, (e1, e2) => And(e1, e2))
      else if (tokens(nextIndex).equals("Or"))
        buildBinaryExpression(tokens, nextIndex, (e1, e2) => Or(e1, e2))
      else if (tokens(nextIndex).equals("Not"))
        buildNotExpression(tokens, nextIndex)
      else if (tokens(nextIndex).equals("Variable"))
        buildVariableExpression(tokens, nextIndex)
      else if (tokens(nextIndex).equals("True"))
        (True, nextIndex + 1)
      else if (tokens(nextIndex).equals("False"))
        (False, nextIndex + 1)
      else {
        throw new IllegalArgumentException("The expression you entered can not be parsed. Check for spelling errors")
      }
    }

  /**
    * Parses the given string into a BooleanExpression
    * object.
    * @param expression string to parse. Needs to follow the
    *                   formatting rules one would use when
    *                   actually instantiating a BooleanExpression
    *                   object in scala. Additional spaces do not
    *                   influence the value returned by this method,
    *                   but new lines should not be present in this string.
    *
    *                   Ex: the strings below evaluate to the same BoolenExpression
    *                   "And( Not(  True), Variable(  "x"  ))" and
    *                   "And(Not(True), Variable("x"))"
    *
    * @return a BooleanExpression object represented by the input string.
    * @throws IllegalArgumentException if the given string could not be parsed
    *                                  due to synthax errors
    */
    def parse(expression: String):BooleanExpression = {
      parseWithIndex(tokenize(expression), 0)._1
    }


}
