package com.strad.service

import cats.effect.Concurrent
import cats.implicits.*
import io.circe.{Decoder, Encoder}
import com.strad.service.domain.*
import org.http4s.*
import org.http4s.implicits.*
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.circe.*
import org.http4s.Method.*

object ModelsJson:
  given Decoder[UserRequest] = Decoder.derived[UserRequest]

  given [F[_]: Concurrent]: EntityDecoder[F, UserRequest] = jsonOf

  given Encoder[UserResponse] = Encoder.AsObject.derived[UserResponse]
  given Decoder[UserResponse] = Decoder.derived[UserResponse]

  given [F[_]]: EntityEncoder[F, UserResponse] = jsonEncoderOf
