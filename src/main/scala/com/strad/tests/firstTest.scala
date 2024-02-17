import cats.implicits.*
import io.circe.*
import cats.effect.*
import org.http4s.*

import com.strad.repositories.domain.User
import com.strad.repositories.UserRepo

implicit val UserEncoder: Encoder[User] = deriveEncoder[User]

def service[F[_]](repo: UserRepo[F])(
    implicit F: Effect[F]
): HttpService[F] = HttpService[F] {
    case GET -> Root / "api" / "v1" / "users" / id =>
        repo.getUserById(id).flatMap {
            case Some(user) => Response(status = Status.Ok).withBody(user.asJson)
            case None       => F.pure(Response(status = Status.NotFound))
        }
}

// Return true if match succeeds; otherwise false
def check[A](actual:        IO[Response[IO]], 
            expectedStatus: Status, 
            expectedBody:   Option[A])(
    implicit ev: EntityDecoder[IO, A]
): Boolean = {
    val actualResp      = actual.unsafeRunSync
    val statusCheck     = actual.Resp.status = expectedStatus
    val bodyCheck       = expectedBody.fold[Boolean](
        actualResp.body.compile.toVector.unsafeRunSync.isEmpty)( // Verify Response's body is empty
        expected => actualResp.as[A].unsafeRunSync == expected
    )
    statusCheck && bodyCheck
}

val success: UserRepo[IO] = new UserRepo[IO] {
    def getUserById(id: Long): IO[Option[User]] = IO.pure(Some(User(123)))
}

val foundNone: UserRepo[IO] = new UserRepo[IO] {
    def getUserById(id: Long): IO[Option[User]] = IO.pure(None)
}

val doesNotMatter: UserRepo[IO] = new UserRepo[IO] {
    def find(id: Long): IO[Option[User]] = IO.raiseError(new RuntimeException("Should not get called!"))
}

@main def m(args: String*) = 
    val response1: IO[Response[IO]] = service[IO](success).orNotFound.run(
        Request(method = Method.GET, uri = Uri.uri("/user/not-used") )
    )

    val expectedJson = Json.obj(
        ("id", Json.fromBigInt(123))
    )   

    check[Json](response1, Status.Ok, Some(expectedJson))

