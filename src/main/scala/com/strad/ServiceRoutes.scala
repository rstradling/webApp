package com.strad

import cats.effect.Concurrent
import com.strad.service.domain.*
import com.strad.service.ModelsJson.given
import cats.implicits.*
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.circe.*
import com.strad.repositories.UserRepo
import com.strad.service.{HealthService, UserService}

object ServiceRoutes:
  def userRoutes[F[_] : Concurrent](u: UserService[F]): HttpRoutes[F] =
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "api" / "v1" / "users" / LongVar(id) =>
        for {
          user <- u.getUser(id)
          resp <- user.fold(NotFound("not found"))(x => Ok(x))
        } yield resp
      case DELETE -> Root / "api" / "v1" / "users" / LongVar(id) =>
        for {
          _ <- u.deleteUser(id)
          resp <- Ok()
        } yield resp
      case request@POST -> Root / "api" / "v1" / "users" =>
        for {
          user <- request.as[UserRequest]
          userResp <- u.addUser(user)
          resp <- Ok(userResp)
        } yield resp
    }

  def healthRoutes[F[_] : Concurrent](h: HealthService[F]): HttpRoutes[F] =
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "api" / "v1" / "ready" =>
        h.isReady.flatMap(x => if (x) NoContent() else InternalServerError("Unable to pass readiness check"))
      case GET -> Root / "api" / "v1" / "health" =>
        NoContent()
    }

