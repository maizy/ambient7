import ru.maizy.ambient7.webapp._
import org.scalatra._
import javax.servlet.ServletContext

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext): Unit = {
    context.mount(new Ambient7WebAppServlet, "/*")
  }
}
