package boolexpr
import java.io.{BufferedReader, InputStreamReader, OutputStreamWriter, PrintWriter}
import java.net.Socket

import scala.io.StdIn.readLine
import scala.util.control.Breaks.break
import java.nio.charset.StandardCharsets


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
    print(BoolExpressionJsonDeserializer.deserialize(reader.readLine()) + "\n")

  }

  writer.close()
  reader.close()
  socket.close()
}
