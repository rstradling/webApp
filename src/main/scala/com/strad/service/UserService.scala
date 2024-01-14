package com.strad.service

import com.strad.service.domain.{UserRequest, UserResponse}

trait UserService[F[_]] {
  def getUser(userId: Long) : F[Option[UserResponse]]
  def addUser(user: UserRequest) : F[UserResponse]
  def deleteUser(userId: Long): F[Unit]
}
