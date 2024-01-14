package com.strad

import cats.effect.Concurrent
import com.strad.service.domain.*
import com.strad.service.ModelsJson.given
import cats.implicits.*
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.circe.*
import com.strad.repositories.UserRepo
import com.strad.service.UserService
object ServiceRoutes:
  def userRoutes[F[_]: Concurrent](U: UserService[F]): HttpRoutes[F] =
    val dsl = new Http4sDsl[F]{}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "users" / LongVar(id) =>
        for {
          user <- U.getUser(id)
          resp <- user.fold(NotFound("not found"))(x => Ok(x))
        } yield resp
      case DELETE -> Root / "users" / LongVar(id) =>
        for {
          _ <- U.deleteUser(id)
          resp <- Ok()
        } yield resp
      case request @ POST -> Root / "users" =>
        for {
          user <- request.as[UserRequest]
          userResp <- U.addUser(user)
          resp <- Ok(userResp)
        } yield resp
    }