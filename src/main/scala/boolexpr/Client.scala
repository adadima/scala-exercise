package boolexpr
import java.io.{BufferedReader, InputStreamReader, OutputStreamWriter, PrintWriter}
import java.net.Socket
import scala.io.StdIn.readLine
import java.nio.charset.StandardCharsets


object Client {
  val socket = new Socket("localhost", 4444)

  val writer: PrintWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream, StandardCharsets.UTF_8))
  val reader: BufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream, StandardCharsets.UTF_8))

  while(true) {

    val input = readLine()
    val expression: BooleanExpression = ExpressionParser.parse(input)

    writer.println(BoolExpressionJsonSerializer.serialize(expression))

    //TODO: the json is on multiple lines, so figure out a protocol and how to read it
    print(BoolExpressionJsonDeserializer.deserialize(reader.readLine()))

  }

}
