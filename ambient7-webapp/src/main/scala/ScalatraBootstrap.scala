import ru.maizy.ambient7.webapp._
import org.scalatra._
import javax.servlet.ServletContext
import ru.maizy.ambient7.webapp.bootstrap.{ AppConfigInit, ScalikeJdbcInit }

class ScalatraBootstrap
  extends LifeCycle
  with ScalikeJdbcInit
  with AppConfigInit
{

  override def init(context: ServletContext): Unit = {
    val appConfig = loadAppConfig()
    setupDbConnectionPool(appConfig)
    context.mount(new Co2HourlyReportServletServlet(appConfig), "/*")
  }

  override def destroy(context: ServletContext): Unit = {
    closeDbConnectionPool()
  }
}
