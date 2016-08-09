package ru.maizy.ambient7.webapp

class Ambient7WebAppServlet extends Ambient7WebAppStack {

  get("/") {
    <html>
      <body>
        <h1>Hello, world!</h1>
      </body>
    </html>
  }

}
