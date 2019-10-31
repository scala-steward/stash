package me.herzrasen.stash.domain

trait DropTableSupport {
  def dropTableStatement: String
}
