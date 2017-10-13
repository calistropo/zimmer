package com.auditest


import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, Uri}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import com.auditest.kafka.Producers
import com.auditest.models.{ItemsHolder, VehicleRawEvent}
import play.api.libs.json.Json

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

object Main {


  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem()
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val executionContext = materializer.executionContext
    val producers = new Producers(system)
    //I send the same request every 5 seconds
    println("Started")
    Source.tick(initialDelay = 1.second,
      interval = 10.seconds,
      //The second value in the tuple is to match Request with Response, we dont need that
      tick = HttpRequest(uri = Uri("http://api.metro.net/agencies/lametro/vehicles/")) -> NotUsed)

      .via(Http().superPool[NotUsed]())
      .mapAsync[Try[ItemsHolder[VehicleRawEvent]]](5) {
      case (Success(response), _) =>
        Unmarshal(response)
          .to[String]
          .map {
            jsonString =>

              //Used play json because according to the benchmarks, it's the faster one.
              Try(Json.parse(jsonString)
                .as[ItemsHolder[VehicleRawEvent]])
          }
      case (Failure(e), _) =>
        Future.successful(Failure(e))
    }
      .mapConcat {
        case Success(items) =>
              println(s"message!-${items.items.size}")
         //if kafka isn't up, the stream wont fail, it just backpressures and gets stuck
          items.items
        case Failure(e) =>

          Nil

      }
      .via(producers.create("raw_vehicle_events"))
      .to(Sink.onComplete(println))

      .run()

    println("Done....!")
  }
}
