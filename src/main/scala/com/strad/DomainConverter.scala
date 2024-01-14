package com.strad

import com.strad.repositories.domain.User
import com.strad.service.domain.{UserRequest, UserResponse}

object DomainConverter:
  def toUserResponse(user: User): UserResponse =
    UserResponse(userId = user.userId, email = user.email)

  def toUser(userReq: UserRequest) =
    // Purposely setting to -1 because this is going to be overwritten by the db
    User(userId = -1L, email = userReq.email)

