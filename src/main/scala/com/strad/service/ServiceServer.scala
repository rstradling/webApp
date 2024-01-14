package com.strad.service

import cats.effect.{Async, Resource}
import cats.*
import cats.data.*
import cats.syntax.all.*
import com.comcast.ip4s.*
import com.strad.ServiceRoutes
import com.strad.repositories.{Db, UserRepoPostgres}
import fs2.Stream
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits.*
import org.http4s.server.middleware.Logger
import org.http4s.{Request, Response}

object ServiceServer:
  def stream[F[_]: Async]: Stream[F, Nothing] =
    for
      client <- Stream.resource(EmberClientBuilder.default[F].build)
      db <- Stream.resource(Db.mkConnection("org.postgresql.Driver", "jdbc:postgresql://postgres:5432/web-app", "admin", "test123"))
      userService = new UserServiceImpl(new UserRepoPostgres(db))
      httpApp: Kleisli[F, Request[F], Response[F]] = (ServiceRoutes.userRoutes[F](userService)).orNotFound
      finalHttpApp = Logger.httpApp(true, true)(httpApp)
      exitCode <- Stream.resource(
        EmberServerBuilder.default[F]
          .withHost(ipv4"0.0.0.0")
          .withPort(port"8080")
          .withHttpApp(finalHttpApp)
          .build >>
          Resource.eval(Async[F].never)
      )
    yield exitCode

