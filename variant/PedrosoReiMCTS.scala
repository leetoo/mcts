package cse.fitzgero.mcts.variant

import cse.fitzgero.mcts.MonteCarloTreeSearch
import cse.fitzgero.mcts.algorithm.backup.Backup
import cse.fitzgero.mcts.algorithm.bestchild.StandardBestChild
import cse.fitzgero.mcts.algorithm.defaultpolicy.DefaultPolicy
import cse.fitzgero.mcts.algorithm.expand.StandardExpand
import cse.fitzgero.mcts.algorithm.samplingpolicy.banditfunction.UCT_PedrosoRei.Objective
import cse.fitzgero.mcts.algorithm.samplingpolicy.scalar.UCTScalarPedrosoReiReward
import cse.fitzgero.mcts.algorithm.treepolicy.StandardTreePolicy
import cse.fitzgero.mcts.tree._

trait PedrosoReiMCTS[S,A] extends MonteCarloTreeSearch[S,A]
                        with StandardBestChild[S,A]
                        with StandardTreePolicy[S,A]
                        with DefaultPolicy[S,A]
                        with Backup[S,A]
                        with StandardExpand[S,A] {

  def objective: Objective

  final override type Reward = Double
  final override type Update = BigDecimal
  final override type Coefficients = UCTScalarPedrosoReiReward.Coefficients

  // these are updated by updateSearchCoefficients in the implementing class
  // best and worst depend on maximizing vs. minimizing objective
  final var globalBestSimulation: Update = BigDecimal.decimal(0)
  final var globalWorstSimulation: Update = BigDecimal.decimal(0)

  final override def rewardOrdering: Ordering[Reward] = scala.math.Ordering.Double

  final override type Tree = MCTreePedrosoReiReward[S,A]

  final override def startNode(s: S): MCTreePedrosoReiReward[S, A] = MCTreePedrosoReiReward(s, None, objective = objective)

  final override def createNewNode(state: S, action: Option[A]): MCTreePedrosoReiReward[S, A] =
    MCTreePedrosoReiReward(state, action, objective)
}

