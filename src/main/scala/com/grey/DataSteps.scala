package com.grey

import com.grey.algorithms.reference.Structuring
import com.grey.configurations.{EnvironmentAgency, EnvironmentAgencyNode}
import com.grey.source.ReferenceAsset
import org.apache.spark.sql.SparkSession

import scala.collection.parallel.CollectionConverters._
import scala.collection.parallel.immutable.ParSeq


/**
 *
 * @param spark : A instance of SparkSession
 */
class DataSteps(spark: SparkSession) {

  private val nodes: EnvironmentAgency.EnvironmentAgency = EnvironmentAgency.environmentAgency()
  private val getNode: EnvironmentAgencyNode = new EnvironmentAgencyNode(nodes = nodes)
  private val referenceInterface = new Structuring(spark = spark)

  /**
   *
   */
  def dataSteps(): Unit = {


    // Nodes
    val names: List[String] = List("determinands", "environment-agency-area", "environment-agency-subarea",
      "sampling-point", "sampling-point-types")
    val nodes: ParSeq[EnvironmentAgency.Node] = names.par.map { name =>
      getNode.environmentAgencyNode(name = name)
    }


    // Downloading the reference assets of interest
    nodes.par.foreach { node =>
      // Download the reference data asset
      new ReferenceAsset().referenceAsset(node = node)
    }


    // Structuring: The subarea & sampling point types
    val exclude = List("environment-agency-subarea", "sampling-point-types")
    val subareaFrame = referenceInterface.structuringInitial(
      node = nodes.filter(_.name == "environment-agency-subarea").head)
    val samplingPointTypesFrame = referenceInterface.structuringInitial(
      node = nodes.filter(_.name == "sampling-point-types").head)


    // Structuring: All else, i.e., filter out subarea & sampling point types
    nodes.filterNot(x => exclude.contains(x.name)).foreach { node =>
      referenceInterface.structuringMiscellaneous(
        node = node, subareaFrame = subareaFrame, samplingPointTypesFrame = samplingPointTypesFrame)
    }

  }

}




