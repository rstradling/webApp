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
import org.http4s.circe.CirceEntityEncoder.circeEntityEncoder
import org.http4s.client.Client
import org.http4s.dsl.io.*
import org.http4s.implicits.uri
import com.strad.service.domain.{UserRequest, UserResponse}

class ServiceRoutesSpec extends CatsEffectSuite:
  private val success: UserRepo[IO] = new UserRepo[IO]:
    override def getUserById(id: Long): IO[Option[User]] = IO.pure(Some(User(123, None)))
    override def insertUser(user: User): IO[User] = IO.pure(User(123, Some("new-email@email.com")))
    override def deleteUserById(userId: Long): IO[Unit] = IO { println(s"Deleting user: $userId") }

  private val foundNone: UserRepo[IO] = new UserRepo[IO]:
    override def getUserById(id: Long): IO[Option[User]] = IO.pure(None)
    // I don't think it makes sense to have a test case for 'foundNone' when inserting a user
    override def insertUser(user: User): IO[User] = ???
    override def deleteUserById(userId: Long): IO[Unit] = IO { println(s"User $userId not found") }

  private val exception: UserRepo[IO] = new UserRepo[IO]:
    override def getUserById(id: Long): IO[Option[User]] = IO.raiseError(new RuntimeException("Exception thrown!"))
    override def insertUser(user: User): IO[User] = IO.raiseError(new RuntimeException("Exception thrown!"))
    override def deleteUserById(userId: Long): IO[Unit] = IO.raiseError(new RuntimeException("Exception thrown!"))

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

    assert(result == Right("Exception thrown!"))

  test("DELETE api/v1/users/123 should return 200 OK if user is found and deleted. Logs that user was deleted"):
    val request: Request[IO] = Request(method = Method.DELETE, uri = uri"/api/v1/users/123")
    val client = Client.fromHttpApp(userServiceRoutes(success))
    val status = client.status(request)
    assertIO(status, Ok)

  // Is this right? Or should it be 404? Is there a way to get 404 error when return type is IO[Unit] or is this not possible?
  test("DELETE api/v1/users/123 should return 200 if user is not found and nothing deleted. Logs that nothing was deleted"):
    val request: Request[IO] = Request(method = Method.DELETE, uri = uri"/api/v1/users/123")
    val client = Client.fromHttpApp(userServiceRoutes(foundNone))
    val status = client.status(request)
    assertIO(status, Ok)

  test("DELETE api/v1/users/123 should return 500 when an exception is thrown"):
    val request: Request[IO] = Request(method = Method.DELETE, uri = uri"/api/v1/users/123")

    val result = try {
      val client = Client.fromHttpApp(userServiceRoutes(exception))
      val status = client.status(request)
      status.unsafeRunSync()
      Left("No exception thrown")
    } catch {
      case e: RuntimeException => Right(e.getMessage)
      case _: Throwable => Left("Unexpected exception")
    }

    assert(result == Right("Exception thrown!"))

  test("POST api/v1/users/ should return 200 OK if user is inserted properly"):
    val request: Request[IO] = Request(method = Method.POST, uri = uri"/api/v1/users").withEntity(UserRequest(Some("new-email@email.com")))
    val client = Client.fromHttpApp(userServiceRoutes(success))
    val status = client.status(request)
    assertIO(status, Ok)

    // Is this sufficient or should we check that the right user is returned too?

  test("POST api/v1/users/ should return 500 when an exception is thrown"):
    val request: Request[IO] = Request(method = Method.POST, uri = uri"/api/v1/users").withEntity(UserRequest(Some("new-email@email.com")))
    val result = try {
      val client = Client.fromHttpApp(userServiceRoutes(exception))
      val status = client.status(request)
      // still don't know if using unsafeRunSync inside a test would be the best approach or not...
      status.unsafeRunSync()
      Left("No exception thrown")
    } catch {
      case e: RuntimeException => Right(e.getMessage)
      case _: Throwable => Left("Unexpected exception")
    }

    assert(result == Right("Exception thrown!"))
    