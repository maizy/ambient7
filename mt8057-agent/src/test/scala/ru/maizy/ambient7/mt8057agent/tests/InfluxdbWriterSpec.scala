package ru.maizy.ambient7.mt8057agent.tests

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2015-2017
 * See LICENSE.txt for details.
 */

import ru.maizy.ambient7.mt8057agent._

class InfluxDbWriterSpec extends AbstractBaseSpec with WritersTestUtils {

  class InfluxDbWriterUnderTest(opts: AppOptions) extends InfluxDbWriter(opts) {
    def getRequestLine(event: Event): Option[String] = formatLine(event)
  }

  // TODO: check http requests

  val opts = AppOptions(
    influxDbBaseUrl = "http://example.com/write",
    influxDbDatabase = Some("mydb")
  )
  val writer = new InfluxDbWriterUnderTest(opts)

  "InfluxDbWriter" should "write co2 events" in {
    writer.getRequestLine(Co2Updated(Co2(1123), time)) shouldBe
      Some("co2,device=mt8057 ppm=1123i,high=false 1445785424583000000")
    writer.getRequestLine(Co2Updated(Co2(3000, high = true), time)) shouldBe
      Some("co2,device=mt8057 ppm=3000i,high=true 1445785424583000000")
  }

  it should "write temp events" in {
    writer.getRequestLine(TempUpdated(Temp(22.3), time)) shouldBe
      Some("temp,device=mt8057 celsius=22.30 1445785424583000000")
  }

  it should "write custom tags reorder them with standart tags" in {
    val writerWithTags = new InfluxDbWriterUnderTest(opts.copy(
      influxDbTags = "position=outdoor,altitude=200"
    ))
    writerWithTags.getRequestLine(TempUpdated(Temp(22.3), time)) shouldBe
      Some("temp,altitude=200,device=mt8057,position=outdoor celsius=22.30 1445785424583000000")
  }

  it should "understand tags with escaping" in {
    val writerWithTags = new InfluxDbWriterUnderTest(opts.copy(
      influxDbTags = "position=outdoor,altitude=200,awesome=val\\,ue,bu\\,bu=123i"
    ))
    writerWithTags.getRequestLine(TempUpdated(Temp(22.3), time)) shouldBe
      Some("temp,altitude=200,awesome=val\\,ue,bu\\,bu=123i,"
        +"device=mt8057,position=outdoor celsius=22.30 1445785424583000000")

  }

}
