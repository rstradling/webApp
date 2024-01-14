package com.strad.service

import cats.effect.Concurrent
import cats.implicits._
import io.circe.{Encoder, Decoder}
import com.strad.service.domain.*
import org.http4s._
import org.http4s.implicits._
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.circe._
import org.http4s.Method._
object ModelsJson:
  given Decoder[UserRequest] = Decoder.derived[UserRequest]
  given [F[_] : Concurrent]: EntityDecoder[F, UserRequest] = jsonOf
  given Encoder[UserResponse] = Encoder.AsObject.derived[UserResponse]
  given [F[_]]: EntityEncoder[F, UserResponse] = jsonEncoderOf
