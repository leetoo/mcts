package cse.bdlab.fitzgero.mcts.example.mckdv.pedrosorei

import cse.bdlab.fitzgero.mcts.algorithm.samplingpolicy.scalar.UCTScalarPedrosoReiReward.{Coefficients, ExplorationCoefficient}
import cse.bdlab.fitzgero.mcts.core.terminationcriterion.{TerminationCriterion, TimeTermination}
import cse.bdlab.fitzgero.mcts.core.{BuiltInRandomGenerator, RandomGenerator, RandomSelection}
import cse.bdlab.fitzgero.mcts.example.mckdv.implementation.MCKDV._
import cse.bdlab.fitzgero.mcts.variant.PedrosoReiMCTS

trait MCKDVPedrosoReiMCTS extends PedrosoReiMCTS[Selection, Choice] {
  def problem: Problem

  def seed: Long

  def timeBudget: Long

  // user must implement evaluateTerminal, getSearchCoefficients, and getDecisionCoefficients

  final override def applyAction(state: Selection, action: Choice): Selection = state + action

  final override def generatePossibleActions(state: Selection): Seq[Choice] = {
    val remainingSubsets = for {
      subset <- problem.multiset
      if subset.intersect(state).isEmpty
    } yield subset

    remainingSubsets.flatten.toSeq
  }

  final override def selectAction(actions: Seq[Choice]): Option[Choice] = actionSelection.selectAction(actions)

  final override def stateIsNonTerminal(state: Selection): Boolean = state.size != problem.multiset.size

  final override def startState: Selection = Set()

  final override def random: RandomGenerator = new BuiltInRandomGenerator(Some(seed))

  final override val terminationCriterion: TerminationCriterion[Selection, Choice, Tree] = TimeTermination[Selection,Choice,Tree](timeBudget)
  final override val actionSelection = RandomSelection(random, generatePossibleActions)
}
