package com.lightbend.ad.modelserver.actors

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, Props}
import com.lightbend.ad.model.{Model, ModelToServeStats, ModelWithDescriptor, ServingResult}
import com.lightbend.ad.modelserver.actors.persistence.FilePersistence
import com.lightbend.model.cpudata.CPUData

// Workhorse - doing model serving for a given data type

class ModelServingActor(dataType : String) extends Actor {

  println(s"Creating model serving actor $dataType")
  private var currentModel: Option[Model] = None
  private var newModel: Option[Model] = None
  var currentState: Option[ModelToServeStats] = None
  private var newState: Option[ModelToServeStats] = None

  override def preStart : Unit = {
    val state = FilePersistence.restoreState(dataType)
    newState = state._2
    newModel = state._1
  }

  override def receive = {
    case model : ModelWithDescriptor =>
      // Update model
      println(s"Updated model: $model")
      newState = Some(ModelToServeStats(model.descriptor))
      newModel = Some(model.model)
      FilePersistence.saveState(dataType, newModel.get, newState.get)
      sender() ! "Done"

    case record : CPUData =>
      // Process data
      newModel.foreach { model =>
        // Update model
        // close current model first
        currentModel.foreach(_.cleanup())
        // Update model
        currentModel = newModel
        currentState = newState
        newModel = None
      }

      currentModel match {
        case Some(model) =>
          val start = System.nanoTime()
          val result = model.score(record)
          val duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start)
          currentState = currentState.map(_.incrementUsage(duration))
          sender() ! ServingResult(currentState.get.description, record._class, result, duration)

        case None =>
          sender() ! ServingResult.noModel
      }

    case request : GetState => {
      // State query
      sender() ! currentState.getOrElse(ModelToServeStats.empty)
    }
  }
}

object ModelServingActor{
  def props(dataType : String) : Props = Props(new ModelServingActor(dataType))
}

case class GetState(dataType : String)
