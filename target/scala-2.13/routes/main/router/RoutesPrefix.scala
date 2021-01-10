// @GENERATOR:play-routes-compiler
// @SOURCE:C:/Users/John/Points/conf/routes
// @DATE:Fri Jan 08 21:14:19 CST 2021


package router {
  object RoutesPrefix {
    private var _prefix: String = "/"
    def setPrefix(p: String): Unit = {
      _prefix = p
    }
    def prefix: String = _prefix
    val byNamePrefix: Function0[String] = { () => prefix }
  }
}
