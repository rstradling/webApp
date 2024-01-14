package com.strad.repositories

import cats.effect.*
import cats.implicits.*
import doobie.*
import doobie.implicits.*
import doobie.hikari.*

object Db:
  def mkConnection[F[_]](
                          driverName: String,
                          url: String,
                          user: String,
                          password: String
                        )(using e : Async[F]): Resource[F, HikariTransactor[F]] =
    for
      ce <- ExecutionContexts.fixedThreadPool[F](32)
      xa <- HikariTransactor.newHikariTransactor[F](driverName, url, user, password, ce)
    yield xa