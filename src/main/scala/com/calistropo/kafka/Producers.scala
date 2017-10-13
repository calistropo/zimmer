package com.calistropo.kafka

import akka.NotUsed
import akka.actor.ActorSystem
import akka.kafka.ProducerMessage.Message
import akka.kafka.{ProducerMessage, ProducerSettings}
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.Flow
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}
import play.api.libs.json.{Json, Reads, Writes}


class Producers(system: ActorSystem) {

  lazy val settings: ProducerSettings[Array[Byte], String] = ProducerSettings.apply(system = system,
    keySerializer = new ByteArraySerializer, valueSerializer = new StringSerializer)
    .withBootstrapServers("localhost:9092")

  def create[T: Writes](topic: String): Flow[T, ProducerMessage.Result[Array[Byte], String, NotUsed.type], NotUsed] = {
    Flow[T].map {
      t =>
        Message(new ProducerRecord[Array[Byte], String](topic, Json.toJson(t).toString), NotUsed)
    }
      .via(Producer.flow(settings))

  }

}
