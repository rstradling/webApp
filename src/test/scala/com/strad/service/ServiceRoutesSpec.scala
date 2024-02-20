package com.strad.service

import cats.*
import cats.effect.*
import cats.implicits.*
import com.strad.ServiceRoutes
import com.strad.repositories.UserRepo
import com.strad.repositories.domain.User
import com.strad.service.ModelsJson.given
import com.strad.service.domain.UserResponse
import io.circe.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import munit.CatsEffectSuite
import org.http4s.*
import org.http4s.circe.*
import org.http4s.circe.CirceEntityDecoder.circeEntityDecoder
import org.http4s.client.Client
import org.http4s.dsl.io.*
import org.http4s.implicits.uri

class ServiceRoutesSpec extends CatsEffectSuite:
  private val success: UserRepo[IO] = new UserRepo[IO]:
    override def getUserById(id: Long): IO[Option[User]] = IO.pure(Some(User(123, None)))
    override def insertUser(user: User): IO[User] = ???
    override def deleteUserById(userId: Long): IO[Unit] = IO.pure(Some(User(123, None)))

  private val foundNone: UserRepo[IO] = new UserRepo[IO]:
    override def getUserById(id: Long): IO[Option[User]] = IO.pure(None)
    override def insertUser(user: User): IO[User] = ???
    override def deleteUserById(userId: Long): IO[Unit] = ???

  private val exception: UserRepo[IO] = new UserRepo[IO]:
    override def getUserById(id: Long): IO[Option[User]] = IO.raiseError(new RuntimeException("Should not get called!"))
    override def insertUser(user: User): IO[User] = ???
    override def deleteUserById(userId: Long): IO[Unit] = ???

  private def userServiceRoutes(repo: UserRepo[IO]) =
    ServiceRoutes.userRoutes[IO](new UserServiceImpl[IO](repo)).orNotFound

  test("GET /api/v1/users/123 should return 200 OK with the correct response"):
    val expectedResponse = UserResponse(123, None)
    val request: Request[IO] = Request(method = Method.GET, uri = uri"/api/v1/users/123")
    val client = Client.fromHttpApp(userServiceRoutes(success))
    val status = client.status(request)
    assertIO(status, Ok)
    val resp = client.expect[UserResponse](request)
    assertIO(resp, expectedResponse)

  test("GET /api/v1/users/123 should return 404 when not found"):
    val request: Request[IO] = Request(method = Method.GET, uri = uri"/api/v1/users/123")
    val client = Client.fromHttpApp(userServiceRoutes(foundNone))
    val status = client.status(request)
    assertIO(status, NotFound)

  test("GET /api/v1/users/123 should return 500 when an exception is thrown"):
    val request: Request[IO] = Request(method = Method.GET, uri = uri"/api/v1/users/123")

    val result = try {
      val client = Client.fromHttpApp(userServiceRoutes(exception))
      val status = client.status(request)
      status.unsafeRunSync()
      Left("No exception thrown")
    } catch {
      case e: RuntimeException => Right(e.getMessage)
      case _: Throwable => Left("Unexpected exception")
    }

    assert(result == Right("Should not get called!"))

  test("DELETE api/v1/users/123 should return 200 OK with correct response if user exists and was deleted"):
    val request: Request[IO] = Request(method = Method.DELETE, uri = uri"/api/v1/users/123")
    val client = Client.fromHttpApp(userServiceRoutes(success))
    val status = client.status(request)
    assertIO(status, Ok)