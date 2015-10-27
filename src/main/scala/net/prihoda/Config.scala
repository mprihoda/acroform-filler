package net.prihoda

import com.typesafe.config.ConfigFactory

trait Config {
  protected val config = ConfigFactory.load()
  private val httpConfig = config.getConfig("http")

  val httpInterface = httpConfig.getString("interface")
  val httpPort = httpConfig.getInt("port")
}
