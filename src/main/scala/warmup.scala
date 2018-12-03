
/**
  * This is the warm up problem: Write a recursive function
  * that given x, computes 2**x. In other words: f(x) = f(x-1) * 2
  * if x > 0. f(0) = 1
  */
object warmup extends App {

  def exp(x: Integer):Integer = if (x == 0) 1 else 2 * exp(x - 1)

  println(exp(0))
  println(exp(1))
  println(exp(2))
  println(exp(6))
  println(exp(8))

  /* The time complexity of this algorithm is exponential O(2^x) since we
     do not memoize the answer of smaller sub-problems. Therefore, when we need
     to compute f(x), we recompute f(x-1) everytime instead of storing its previously-
     computed value. This can also be seen from the run time recurrence: T(N) = 2 * T(N-1)
     which indicates that the size of the subproblems decreases by 1 at each level of the recurssion
     tree, but the number of subproblems doubles.

     Can we do better? Yes!

     A better solution is dynamic programming/memoization. Store previosuly computed solutions in a Map
     which maps each i from
     0 to x - 1 to the value f(i). This way, we can access f(x - 1) in constant time when we want to
     compute f(x). Therefore, the runtime significantly improves to be O(x) (O(1) / sub-problems and there
     are x subproblems).

   */
}
