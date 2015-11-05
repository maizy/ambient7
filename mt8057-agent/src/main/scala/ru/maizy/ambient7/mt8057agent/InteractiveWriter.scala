package ru.maizy.ambient7.mt8057agent

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2015
 * See LICENSE.txt for details.
 */
class InteractiveWriter(opts: AppOptions) extends StatefullWriter {
  val CO2_OK = 800
  val C02_NOT_OK = 1200

  var lastLength = 0

  override protected def internalWrite(event: Event): Unit = outputState()

  override def onInit(): Unit = {
    Console.println()
    outputState()
  }

  private def outputState(): Unit = {
    val formatted = formatCurrentState()
    val cleaner = "\b" * lastLength
    lastLength = formatted.length
    Console.print(cleaner + formatted)
    Console.flush()
  }

  private def formatCurrentState(): String = {
    val red = Console.RED
    val reset = Console.RESET

    val (co2Color, co2Reset) = co2
      .map(_.ppm)
      .map {
        case v: Int if v <= CO2_OK => Console.GREEN
        case v: Int if v <= C02_NOT_OK => Console.YELLOW
        case v: Int => Console.BOLD + Console.RED
      }
      .map ((_, reset))
      .getOrElse (("", ""))

    val status = if (connected) "    " else s" ${red}ø$reset  "

    val co2Val = co2.map(_.ppm).map { v => f"$co2Color$v%4s ppm$co2Reset" }.getOrElse("----    ")
    val tempVal = temp.map(_.celsus).map { v => f"$v%2.2f °C".replace(',', '.') }.getOrElse("-----   ")
    s"${status}co2: $co2Val    temp: $tempVal    "
  }
}
