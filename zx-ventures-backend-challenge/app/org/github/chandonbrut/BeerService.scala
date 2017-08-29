package org.github.chandonbrut

import play.api.mvc.{Action, InjectedController}
import javax.inject.Inject

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.stream.Materializer
import akka.util.Timeout

import scala.concurrent.duration._
import play.api.libs.json.{JsError, Json}
import akka.pattern.ask

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class Bulk(pdvs:Array[PDV])
case class QueryAll()
case class QueryById(id:String)
case class QueryNearest(coordinates: Coordinates)

case class Coordinates(lng:Double,lat:Double)
case class GeoJSONMultipolygon(`type`:String,coordinates:Seq[Seq[Seq[Seq[Double]]]])
case class GeoJSONPoint(`type`:String,coordinates:Seq[Double])
case class PDV(
    id:String,
    tradingName:String,
    ownerName:String,
    document: String, 
    coverageArea:GeoJSONMultipolygon, 
    address:GeoJSONPoint
)

 class BeerService @Inject() (implicit system:ActorSystem, materializer: Materializer) extends InjectedController {

   implicit val timeout = Timeout(5 seconds)


   implicit val multipolygonReader = Json.reads[GeoJSONMultipolygon]
   implicit val multipolygonWriter = Json.writes[GeoJSONMultipolygon]
   implicit val pointReader = Json.reads[GeoJSONPoint]
   implicit val pointWriter = Json.writes[GeoJSONPoint]
   implicit val pdvReader = Json.reads[PDV]
   implicit val pdvWriter = Json.writes[PDV]
   implicit val bulkReader = Json.reads[Bulk]
   implicit val bulkWriter = Json.writes[Bulk]
   implicit val coordinatesReader = Json.reads[Coordinates]
   implicit val coordinatesWriter = Json.writes[Coordinates]


   def getActor():ActorRef = {
//     system.actorOf(Props[MongoPDVActor])
      system.actorOf(Props[InMemoryPDVActor])
   }


  def getPDV(pdvId:Int) = Action.async(parse.json) {
    request => {
      val actorReply = getActor() ? QueryById(pdvId.toString)
      actorReply.map(_ match {
        case reply: PDV => Ok(Json.toJson(reply))
        case _ => BadRequest(Json.obj("error" -> "No PDV with"))
      })
    }
  }

   def getAllPDVs() = Action.async {
     request => {
       val actorReply = getActor() ? QueryAll()
       actorReply.map(_ match {
         case reply:List[PDV] => Ok(Json.toJson(reply))
         case _ => BadRequest(Json.obj("error" -> "No PDV with"))
       })
     }
   }

  def insertPDV = Action(parse.json) {
    request => {
      val pdvRequest = request.body.validate[PDV]
      pdvRequest.fold(
        errors => BadRequest(Json.obj("error" -> JsError.toJson(errors))),
        pdv => {
            getActor() ! pdv
            Ok(Json.obj("status" -> ("ok")))
        }
      )
    }
  }

    def bulkInsertPDV = Action(parse.json) {
    request => {
      val pdvRequest = request.body.validate[Bulk]

      pdvRequest.fold(
        errors => BadRequest(Json.obj("error" -> JsError.toJson(errors))),
        pdv => {
          getActor() ! pdv.pdvs
          Ok(Json.obj("status" -> ("ok")))
        }
      )
    }
  }

    def show = Action {
        Ok(views.html.show())
    }

    def findPDVs = Action.async(parse.json) {
    request => {
      val coordinatesRequest = request.body.validate[Coordinates]
      coordinatesRequest.fold(
        errors => Future { BadRequest(Json.obj("error" -> JsError.toJson(errors))) },
        coordinates => {
          val actorReply = getActor() ? QueryNearest(coordinates)
          actorReply.map(_ match {
            case reply:PDV => Ok(Json.toJson(reply))
            case _ => BadRequest(Json.obj("error" -> "No PDV there"))
          })
        }
      )
      }
    }

}