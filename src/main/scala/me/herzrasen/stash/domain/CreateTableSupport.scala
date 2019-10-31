package me.herzrasen.stash.domain

trait CreateTableSupport {
  def createTableStatement: String
}
