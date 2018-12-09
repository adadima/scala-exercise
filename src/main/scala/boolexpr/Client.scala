package boolexpr
import java.io.{BufferedReader, InputStreamReader, OutputStreamWriter, PrintWriter}
import java.net.Socket

import scala.io.StdIn.readLine
import scala.util.control.Breaks.break
import java.nio.charset.StandardCharsets

/**
  * Client for boolexpr.Server
  *
  * Accepts input from the console that
  * should represent a BooleanExpression object.
  * Communicates with Server in order to compute
  * the full disjunctive normal form of the
  * input formula and prints it to the console.
  */
object Client extends App {

  val socket = new Socket("localhost", 4444)

  val writer: PrintWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream, StandardCharsets.UTF_8))
  val reader: BufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream, StandardCharsets.UTF_8))

  while(true) {

    val input = readLine()

    if(input.equals("QUIT")) {
      println("Closing connection to server...")
      break
    }

    try {
      // parse the input to a BooleanExpression, serialize it to JSON, write it to server
      writer.println(
        BoolExpressionJsonSerializer.serialize(ExpressionParser.parse(input.substring(0, input.length))).
          toCharArray.
          foldLeft("")((x: String, y: Char) =>
            if (!y.equals('\n')) x + y else x)
      )
    } catch {

      case e: Exception =>  println("Your input could not be parsed, see the errors: " + e.getMessage)
    }

    writer.flush()
    // deserialize the response from server and print it
    print(BoolExpressionJsonDeserializer.deserialize(reader.readLine()) + "\n")

  }

  writer.close()
  reader.close()
  socket.close()
}
