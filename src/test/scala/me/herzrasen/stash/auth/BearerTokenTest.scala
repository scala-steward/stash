package me.herzrasen.stash.auth

import org.scalatest.{FlatSpec, Matchers}

class BearerTokenTest extends FlatSpec with Matchers {

  "A BearerToken" should "be created" in {
    val bearerStr =
      "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoidXNlciIsImlzcyI6InN0YXNoIiwiZXhwIjoxNTcyMTkxMTA3LCJ1c2VyIjoic29tZSB1c2VyIn0.Awqc04Y0a8EEOgurtePs90U1-lWhZhWETj3tKuQAxeg"

    val bearerToken = BearerToken(bearerStr)
    bearerToken.token shouldBe defined
    bearerToken.token.get shouldEqual "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoidXNlciIsImlzcyI6InN0YXNoIiwiZXhwIjoxNTcyMTkxMTA3LCJ1c2VyIjoic29tZSB1c2VyIn0.Awqc04Y0a8EEOgurtePs90U1-lWhZhWETj3tKuQAxeg"
  }

  "No BearerToken" should "be created" in {
    BearerToken("invalid").token shouldBe None
  }

  it should "not be created when passing an empty string" in {
    BearerToken("").token shouldBe None
  }
}
