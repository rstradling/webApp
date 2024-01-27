package com.strad.service

trait HealthService[F[_]]:
  def isReady: F[Boolean]
  
