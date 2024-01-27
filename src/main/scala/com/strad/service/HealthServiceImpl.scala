package com.strad.service

import cats.*
import cats.implicits.*
import cats.effect.Async
import doobie.*
import doobie.implicits.*
class HealthServiceImpl[F[_]](db: Transactor[F])(using e: Async[F]) extends HealthService[F]:
  def isReady: F[Boolean] =
    sql"""SELECT 1 as one"""
      .query[Long]
      .option
      .transact(db)
      .map(_.isDefined)
