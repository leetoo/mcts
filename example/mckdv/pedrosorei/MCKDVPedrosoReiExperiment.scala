package cse.fitzgero.mcts.example.mckdv.pedrosorei

import cse.fitzgero.mcts.algorithm.samplingpolicy.banditfunction.UCT_PedrosoRei.{Maximize, Objective}
import cse.fitzgero.mcts.example.mckdv.implementation.{MCKDV, MCKDVGenerator}

class MCKDVPedrosoReiExperiment(val random: scala.util.Random, val costBound: Int, objective: Objective) extends MCKDVGenerator {
  case class MCKDVExperimentResult(sumOptimalCost: BigDecimal, sumSearchCost: BigDecimal, avgOptimalCost: BigDecimal, avgSearchCost: BigDecimal)
  def run(n: Int, k: Int, trials: Int, timeBudget: Long): MCKDVExperimentResult = {
    val (optCost, searchCost) = (1 to trials).map {
      n => {
        val (problem, optimal) = genProblem(n,k,objective)
        val optCost = MCKDV.costOfSelection(optimal, problem.dependencies)
        val solver = MCKDVPedrosoReiSolver(problem, 0, timeBudget, objective)
        val tree = solver.run()
//        println(s"PedrosoRei Tree search complete with ${tree.visits} iterations")
        val searchCost = MCKDV.costOfSelection(solver.bestGame(tree).toSet, problem.dependencies)
        (optCost,searchCost)
      }
    }.reduce {
      (a,b) => {
        (a._1+b._1,a._2+b._2)
      }
    }
    MCKDVExperimentResult(optCost, searchCost, optCost/trials, searchCost/trials)
  }
}

object MCKDVPedrosoReiExperiment {
  def apply(costBound: Int): MCKDVPedrosoReiExperiment = new MCKDVPedrosoReiExperiment(scala.util.Random, costBound, Maximize)
  def apply(random: scala.util.Random, costBound: Int): MCKDVPedrosoReiExperiment = new MCKDVPedrosoReiExperiment(random, costBound, Maximize)
  def apply(random: scala.util.Random, costBound: Int, objective: Objective): MCKDVPedrosoReiExperiment = new MCKDVPedrosoReiExperiment(random, costBound, objective)
}