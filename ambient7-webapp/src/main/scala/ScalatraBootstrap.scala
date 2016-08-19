import org.scalatra._
import javax.servlet.ServletContext
import ru.maizy.ambient7.webapp.bootstrap.{ AppConfigInit, ScalikeJdbcInit }
import ru.maizy.ambient7.webapp.servlet.{ Co2HourlyReportServlet, DevicesServlet, RootServlet }

class ScalatraBootstrap
  extends LifeCycle
  with ScalikeJdbcInit
  with AppConfigInit
{

  override def init(context: ServletContext): Unit = {
    val appConfig = loadAppConfig()
    setupDbConnectionPool(appConfig)
    context.mount(new Co2HourlyReportServlet(appConfig), "/co2_hourly_report")
    context.mount(new DevicesServlet(appConfig), "/devices")
    context.mount(new RootServlet, "/")
  }

  override def destroy(context: ServletContext): Unit = {
    closeDbConnectionPool()
  }
}
