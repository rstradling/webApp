package com.strad.repositories

import cats.effect.*
import cats.implicits.*
import doobie.*
import doobie.implicits.*
import doobie.hikari.*

object Db:
  case class DbConfig(driverName: String, db: String, user: String, password: String)
  def mkConnection[F[_]](
                        dbConfig: DbConfig
                        )(using e : Async[F]): Resource[F, Transactor[F]] =
    for
      ce <- ExecutionContexts.fixedThreadPool[F](32)
      url = s"jdbc:postgresql://postgres:5432/${dbConfig.db}"
      xa <- HikariTransactor.newHikariTransactor[F](dbConfig.driverName, url, dbConfig.user, dbConfig.password, ce)
    yield xa
  def mkDbConfigFromEnv(driverName: String): DbConfig =
    val db = sys.env.getOrElse("POSTGRES_DB", throw new RuntimeException("Must specify POSTGRES_DB env var"))
    val pass = sys.env.getOrElse("POSTGRES_PASSWORD", throw new RuntimeException("Must specify POSTGRES_PASSWORD env var"))
    val user = sys.env.getOrElse("POSTGRES_USER", throw new RuntimeException("Must specify POSTGRES_USER env var"))
    DbConfig(driverName, db, user, pass)


