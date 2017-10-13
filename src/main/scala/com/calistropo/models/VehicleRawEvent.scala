package com.calistropo.models

import play.api.libs.json.{Json, Reads}



case class VehicleRawEvent(
                    id: String,
                    run_id: Option[String],
                    longitude: Double,
                    latitude: Double,
                    heading: Double,
                    seconds_since_report: Int,
                    predictable: Boolean,
                    route_id: String
                  )





object VehicleRawEvent {
  implicit def reads = Json.reads[VehicleRawEvent]
  implicit def writes = Json.writes[VehicleRawEvent]
}

case class ItemsHolder[T](
                           items: List[T]
                         )

object ItemsHolder {
  implicit def reads[T: Reads] = Json.reads[ItemsHolder[T]]
}
