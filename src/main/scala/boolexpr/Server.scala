package boolexpr

import java.io.{BufferedReader, InputStreamReader, OutputStreamWriter, PrintWriter}
import java.net.{ServerSocket, Socket}
import java.nio.charset.StandardCharsets
import scala.util.control.Breaks.break
import boolexpr.BoolExpressionJsonSerializer.serialize
import boolexpr.BoolExpressionJsonDeserializer.deserialize
import boolexpr.AlgebraicTransformations.convertToDNF

/**
  * Server class which offers the conversion of a
  * BooleanExpression to its full disjunctive normal
  * form as a service. Receives one-line jsons repr.
  * BooleanExpression objects from the client and
  * responds back with a one-line json for the full
  * DNF of the input.
  *
  * Supports communication with multiple clients at the
  * same time.
  *
  * @param port the port this server is
  *             listening to.
  */
class Server(port: Integer) {
  val serverSocket = new ServerSocket(port)

  /**
    * Main function that starts the server.
    * Will not stop waiting for
    * connections unless process is terminated
    * intentionally.
    */
  def serve():Unit = {

      while (true) {
        val client = serverSocket.accept()
        println("accepted connection")
        new Thread(() =>
          try {
            handleClient(client)
          } catch{
            case e: Exception => e.getMessage + "Continuing to serve..."
          }).start()
      }

    // handles one connection
    def handleClient(client: Socket):Unit= {
      val writer: PrintWriter = new PrintWriter(new OutputStreamWriter(client.getOutputStream, StandardCharsets.UTF_8))
      val reader: BufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream, StandardCharsets.UTF_8))

      while (true) {

        val jsonExpression: String = reader.readLine()
        if (jsonExpression.equals(null)) break

        try {
          val expression: BooleanExpression = deserialize(jsonExpression)
          writer.println(serialize(convertToDNF(expression)).toCharArray.foldLeft("")((x: String, y: Char) =>
              if (! y.equals('\n')) x + y else x))
        } catch {
          case e: IllegalArgumentException => writer.println("The request was badly formatted, see the following error: " +
            e.getMessage)
        }

        writer.flush()
      }

      writer.close()
      reader.close()
    }
}

}
