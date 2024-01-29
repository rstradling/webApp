package com.strad.service.domain

case class UserRequest(email: Option[String])

case class UserResponse(userId: Long, email: Option[String])
