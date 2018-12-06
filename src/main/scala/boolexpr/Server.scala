package boolexpr

import java.io.{BufferedReader, InputStreamReader, OutputStreamWriter, PrintWriter}
import java.net.{ServerSocket, Socket}
import java.nio.charset.StandardCharsets

import scala.util.control.Breaks.break
import boolexpr.BoolExpressionJsonSerializer.serialize
import boolexpr.BoolExpressionJsonDeserializer.deserialize
import boolexpr.AlgebraicTransformations.convertToDNF

class Server(port: Integer) {
  val serverSocket = new ServerSocket(port)

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
