import boolexpr._
import boolexpr.AlgebraicTransformations.convertToDNF

object ServerMain extends App {

  new Server(4444).serve()
}
