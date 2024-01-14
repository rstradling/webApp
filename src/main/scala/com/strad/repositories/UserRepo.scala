package com.strad.repositories

import com.strad.repositories.domain.User

trait UserRepo[F[_]]:
  def insertUser(user: User): F[User]
  def deleteUserById(userId: Long): F[Unit]
  def getUserById(userId: Long): F[Option[User]]
