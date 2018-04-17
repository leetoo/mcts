package cse.fitzgero.mcts.tree

import scala.collection.GenMap

/**
  * a base trait that describes a node in a monte carlo tree
  * @tparam S a state type
  * @tparam A an action type (typically an ADT)
  * @tparam R a reward type (for default MCTS, that would be a real number)
  * @tparam U an update type, which is the type of the simulation result
  * @tparam C a coefficient type, which can be used to guide updates
  * @tparam N the derived type (F-Bounded Polymorphic type)
  */
trait MonteCarloTreeArbitraryUpdate[S,A,R,U,C,N <: MonteCarloTreeArbitraryUpdate[S,A,R,U,C,N]] {

  ////// user defined values
  // these are idempotent throughout the lifetime of the tree
  def state: S
  def action: Option[A]
  // this changes through regular tree operations
//  var reward: R
  /**
    * calculates the reward value for this node
    * @param coefficients the coefficients used for this calculation
    * @return a reward value
    */
  def reward(coefficients: C): R

  ////// internal state
  // these change through regular tree operations
  var visits: Long = 0
  var children: Option[GenMap[A, () => N]] = None
  var parent: () => Option[N] = () => None
  // this can change if we combine trees
  var depth: Int = 0


  /**
    * predicate function to tell if this node is a leaf
    * @return
    */
  def isLeaf: Boolean = children.isEmpty

  /**
    * sets the parent for this node
    * @param parent some other tree node that will become this node's parent
    */
  def setParent(parent: N): Unit = {
    this.parent = () => Some(parent)
    ()
  }

  /**
    * implemented by the subclass, should call "update reward" and pass a lambda to compare and optionally pass a new reward value to set
    * @param simulationResult the data that we use to update this node
    */
  def update(simulationResult: U, coefficients: C): Unit

  /**
    * adds a tree node to the children of this node
    * @param node the child node to add
    * @return the updated (mutated) tree
    */
  def addChild(node: N): Unit = {
    node.action match {
      case None => throw new IllegalArgumentException("adding child without an action in tree")
      case Some(a) =>
        node.depth = this.depth + 1
        node.setParent(this.asInstanceOf[N])
        children match {
          case None =>
            this.children = Some(GenMap(a -> (() => node)))
            ()
          case Some(childrenToUpdate) =>
            this.children = Some(childrenToUpdate.updated(a, () => node))
            ()
        }
    }
  }

  /**
    * children nodes are wrapped in a closure. this function unpacks them for the user. created while sorting out a type error.
    * @return
    */
  def childrenNodes: GenMap[A, N] =
    children match {
      case None => Map()
      case Some(childrenClosure) =>
        for {
          tuple <- childrenClosure
        } yield (tuple._1, tuple._2())
    }


  def hasChildren: Boolean = children.nonEmpty
  def hasNoChildren: Boolean = children.isEmpty

  def treeSpecificPrintData: String

  override def toString: String = {
    val leadIn = if (depth == 0) "" else s"$depth${ " " * depth }"
    val nodeType = if (depth == 0) "root" else if (children.isEmpty) "leaf" else "brch"
    val actionUsed = action match {
      case None => ""
      case Some(a) => s"   $a"
    }
    s"$leadIn$nodeType$actionUsed   $treeSpecificPrintData\n"
  }

  def defaultTransform(nodes: List[(A, () => N)]): List[N] = nodes.map { _._2() }
  def printTree(printDepth: Int, transform: List[(A, () => N)] => List[N] = defaultTransform): String = {
    val childrenStrings: String =
      if (depth >= printDepth)
        ""
      else {
        children match {
          case None => ""
          case Some(childrenUnpacked) =>

            val childrenData = transform(childrenUnpacked.toList) map {
              child => s"${child.printTree(printDepth, transform)}"
            }
            childrenData.mkString("")
        }
      }
    toString + childrenStrings
  }
}



