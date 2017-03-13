package ru.maizy.ambient7.mt8057agent.tests

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2015-2017
 * See LICENSE.txt for details.
 */

import ru.maizy.ambient7.core.config.Ambient7Options
import ru.maizy.ambient7.core.config.options.InfluxDbOptions
import ru.maizy.ambient7.core.data.{ AgentTags, Co2Agent, Co2Device, Devices }
import ru.maizy.ambient7.mt8057agent._

class InfluxDbWriterSpec extends AbstractBaseSpec with WritersTestUtils {

  class InfluxDbWriterUnderTest(opts: Ambient7Options) extends InfluxDbWriter(opts) {
    def getRequestLine(event: Event): Option[String] = formatLine(event)
  }

  // TODO: check http requests

  private val opts = optsWithCustomTags("")

  private def optsWithCustomTags(rawTags: String): Ambient7Options = {
    Ambient7Options(
      influxDb = Some(InfluxDbOptions(
        baseUrl = "http://example.com/write",
        database = Some("mydb")
      )),
      selectedDeviceId = Some("test-device"),
      devices = Some(
        Devices(
          List(Co2Device(
            id = "test-device",
            Co2Agent("main", tags = AgentTags(rawTags))
          ))
        )
      )
    )
  }

  val writer = new InfluxDbWriterUnderTest(opts)

  "InfluxDbWriter" should "write co2 events" in {
    writer.getRequestLine(Co2Updated(Co2(1123), time)) shouldBe
      Some("co2,agent=main,device=mt8057 ppm=1123i,high=false 1445785424583000000")
    writer.getRequestLine(Co2Updated(Co2(3000, high = true), time)) shouldBe
      Some("co2,agent=main,device=mt8057 ppm=3000i,high=true 1445785424583000000")
  }

  it should "write temp events" in {
    writer.getRequestLine(TempUpdated(Temp(22.3), time)) shouldBe
      Some("temp,agent=main,device=mt8057 celsius=22.30 1445785424583000000")
  }

  it should "write custom tags reorder them with standart tags" in {
    val writerWithTags = new InfluxDbWriterUnderTest(
      optsWithCustomTags("position=outdoor,altitude=200")
    )
    writerWithTags.getRequestLine(TempUpdated(Temp(22.3), time)) shouldBe
      Some("temp,agent=main,altitude=200,device=mt8057,position=outdoor celsius=22.30 1445785424583000000")
  }

  it should "understand tags with escaping" in {
    val writerWithTags = new InfluxDbWriterUnderTest(
      optsWithCustomTags("position=outdoor,altitude=200,awesome=val\\,ue,bu\\,bu=123i")
    )
    writerWithTags.getRequestLine(TempUpdated(Temp(22.3), time)) shouldBe
      Some("temp,agent=main,altitude=200,awesome=val\\,ue,bu\\,bu=123i,"
        +"device=mt8057,position=outdoor celsius=22.30 1445785424583000000")

  }

}
