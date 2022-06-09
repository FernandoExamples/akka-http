package part2_lowlevelserver.guitarsrest

import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait GuitarFormatters extends DefaultJsonProtocol {
  implicit val guitarFormat: RootJsonFormat[Guitar] = jsonFormat3(Guitar)
}