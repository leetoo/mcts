package cse.bdlab.fitzgero.mcts.algorithm.defaultpolicy

import scala.annotation.tailrec

import cse.bdlab.fitzgero.mcts.MonteCarloTreeSearch

trait StandardDefaultPolicy[S,A] extends MonteCarloTreeSearch[S,A] {

  override protected final def defaultPolicy(monteCarloTree: Tree): (Update, S)= {
    if (stateIsNonTerminal(monteCarloTree.state)) {

      // simulate moves until a terminal game state is found, then evaluate
      @tailrec
      def _defaultPolicy(state: S): (Update, S) = {
        if (stateIsNonTerminal(state)) {
          selectAction(generatePossibleActions(state)) map { applyAction(state,_) } match {
            case None =>
              // should never reach this line if State and Actions are well defined
              throw new IllegalStateException(s"Applying action to state $state but it produced an empty state. your applyAction and generatePossibleActions are not well-defined on all inputs.")
            case Some(nextState) =>
              _defaultPolicy(nextState)
          }
        } else {
          (evaluateTerminal(state), state)
        }
      }

      _defaultPolicy(monteCarloTree.state)
    } else {
      (evaluateTerminal(monteCarloTree.state), monteCarloTree.state)
    }
  }
}
