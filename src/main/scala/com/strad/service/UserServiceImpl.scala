package com.strad.service

import cats.*
import cats.implicits.*
import com.strad.DomainConverter
import com.strad.repositories.UserRepo
import com.strad.service.domain.{UserRequest, UserResponse}

class UserServiceImpl[F[_]](repo: UserRepo[F])(using e: Functor[F]) extends UserService[F]:
  def getUser(userId: Long): F[Option[UserResponse]] =
    repo.getUserById(userId).map(u => u.map(DomainConverter.toUserResponse))

  def addUser(user: UserRequest): F[UserResponse] =
    repo.insertUser(DomainConverter.toUser(user)).map(DomainConverter.toUserResponse)

  def deleteUser(userId: Long): F[Unit] =
    repo.deleteUserById(userId)
