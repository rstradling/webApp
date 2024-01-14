package com.strad

import cats.effect.{ExitCode, IO, IOApp}
import com.strad.service.ServiceServer

object Main extends IOApp.Simple:
  def run: IO[Unit] =
    ServiceServer.stream[IO].compile.drain.as(ExitCode.Success)  
