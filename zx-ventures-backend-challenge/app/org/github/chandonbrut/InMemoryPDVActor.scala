package org.github.chandonbrut

import akka.actor.Actor
import com.vividsolutions.jts.geom.{Coordinate, GeometryFactory}
import com.vividsolutions.jts.io.geojson.GeoJsonReader
import play.api.libs.json.{Json}

import scala.concurrent.ExecutionContext.Implicits.global

class InMemoryPDVActor extends Actor {

  implicit val multipolygonReader = Json.reads[GeoJSONMultipolygon]
  implicit val multipolygonWriter = Json.writes[GeoJSONMultipolygon]
  implicit val pointReader = Json.reads[GeoJSONPoint]
  implicit val pointWriter = Json.writes[GeoJSONPoint]
  implicit val pdvReader = Json.reads[PDV]
  implicit val pdvWriter = Json.writes[PDV]
  implicit val coordinatesReader = Json.reads[Coordinates]
  implicit val coordinatesWriter = Json.writes[Coordinates]


  override def receive = {

    //Adding
    case pdv:PDV => InMemoryPDVRepository.pdvs.add(pdv)
    case pdvs:Array[PDV] => pdvs.map(p => InMemoryPDVRepository.pdvs.add(p))

    //Querying
    case query:QueryById => {
      sender ! InMemoryPDVRepository.pdvs.filter(p => p.id == query.id).head
    }
    case query:QueryAll => {
      sender ! InMemoryPDVRepository.pdvs.toList
    }
    case qq:QueryNearest => {

      val geometryFactory = new GeometryFactory()
      val queryPoint = geometryFactory.createPoint(new Coordinate(qq.coordinates.lng,qq.coordinates.lat))
      val reader = new GeoJsonReader()

      val pdvsContainingPt = InMemoryPDVRepository.pdvs.filter(pdv => {
        val geometry = reader.read(
          Json.writes[GeoJSONMultipolygon].writes(pdv.coverageArea).toString
        )
        geometry.contains(queryPoint)
      }).toList

      val sorted = pdvsContainingPt.sortBy(
        pdv => {
          val geometry = reader.read(Json.writes[GeoJSONPoint].writes(pdv.address).toString)
          geometry.distance(queryPoint)
        }
      )

      if (sorted.isEmpty) sender ! None
      else sender ! sorted.head

    }

    //Default
    case _ => {}

  }
}
