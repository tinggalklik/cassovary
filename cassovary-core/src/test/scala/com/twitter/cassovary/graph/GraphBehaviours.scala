/*
 * Copyright (c) 2014. Twitter, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.twitter.cassovary.graph

import org.scalatest.WordSpec

trait GraphBehaviours {
  this: WordSpec =>

  def correctNumberOfNodesAndEdges(graph: DirectedGraph, numNodes: Int) {
    "have correct number of nodes" in {
      assert(graph.nodeCount === numNodes)
    }
    "have number of edges less than in full graph" in {
      assert(graph.edgeCount <= (numNodes * (numNodes - 1).toLong))
    }

  }

  def completeGraph(graph: => DirectedGraph, numNodes: Int) {
    "have correct number of nodes" in {
      assert(graph.nodeCount === numNodes, "wrong number of nodes")
    }
    "have all possible edges" in {
      assert(graph.edgeCount === (numNodes * (numNodes - 1).toLong))
      graph foreach { node =>
        val neighbors = (0 until numNodes) filter {
          _ != node.id
        }
        assert(node.inboundNodes().sorted === neighbors)
        assert(node.outboundNodes().sorted === neighbors)
      }
    }
  }

  def correctDirectedGraph(graph: => DirectedGraph, numNodes: Int) {
    correctNumberOfNodesAndEdges(graph, numNodes)

    "be consistent among all nodes" in {
      graph foreach { node =>
        (0 to 1) foreach { dir =>
          val neighbors = if (dir == 0) node.outboundNodes() else node.inboundNodes()
          assert(neighbors.size === neighbors.toSet.size, "duplicates in neighbors array detected")
          neighbors foreach { nbr =>
            assert(nbr >= 0, "nbr.id < 0 (" + nbr + ")")
            assert(nbr < numNodes, "nbr.id >= numNodes (" + nbr + ")")
            assert(nbr != node.id, "self-loop in graph")
            val neighborNode = graph.getNodeById(nbr).get
            val neighborNodeOtherDirEdges =
              if (dir == 0) neighborNode.inboundNodes()
              else neighborNode.outboundNodes()
            assert(neighborNodeOtherDirEdges.contains(node.id), "edge existence not consistent in nodes")
          }
        }
      }
    }
  }

  def correctUndirectedGraph(graph: => DirectedGraph, numNodes: Int) {
    correctNumberOfNodesAndEdges(graph, numNodes)

    "be consistent among all nodes" in {
      graph foreach { node =>
        assert(node.inboundNodes().sorted === node.outboundNodes().sorted, "inbound nodes not equal to outbound nodes" +
          "in undirected graph")
        assert(node.inboundNodes().size === node.inboundNodes().toSet.size)
        node.inboundNodes() foreach { nbr =>
          assert(nbr >= 0, "nbr.id < 0 (" + nbr + ")")
          assert(nbr < numNodes, "nbr.id >= numNodes (" + nbr + ")")
          assert(nbr != node.id, "node " + node.id + " contains self-loop")
          assert(graph.getNodeById(nbr).get.outboundNodes().contains(node.id), "edge existence not consistent in nodes")
        }
      }
    }
  }
}