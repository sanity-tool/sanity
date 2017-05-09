package ru.urururu.sanity

import org.springframework.stereotype.Component
import ru.urururu.sanity.api.Cfg
import ru.urururu.sanity.api.cfg._

/**
  * @author Dmitry Matveev
  */
@Component
class FlowAnalyzer {

  def evalAssign(assignment: Assignment, state: MultiState): MultiState = state

  def evalCall(call: Call, state: MultiState): MultiState = state

  def evalDefaultState(cfe: Cfe, state: MultiState): MultiState = {
    cfe match {
      case assignment: Assignment => evalAssign(assignment, state)
      case call:Call => evalCall(call, state)
    }
  }

  def evalDefault(cfe: Cfe, state: MultiState): Map[Cfe, MultiState] = Map[Cfe, MultiState](cfe.getNext -> evalDefaultState(cfe, state))

  def evalIfCondition(ifCondition: IfCondition, state: MultiState): Map[Cfe, MultiState] = ???

  def evalSwitch(switch: Switch, state: MultiState): Map[Cfe, MultiState] = ???

  def eval(cfe: Cfe, state: MultiState): Map[Cfe, MultiState] = {
    cfe match {
      case ifCondition: IfCondition => evalIfCondition(ifCondition, state)
      case switch: Switch => evalSwitch(switch, state)
      case default => evalDefault(default, state)
    }
  }

  def analyze(cfg: Cfg): Map[Cfe, MultiState] = {
    var result = Map.empty[Cfe, MultiState]
    var toUpdate = Set(cfg.getEntry)

    while (toUpdate.nonEmpty) {
      println(toUpdate)
      
      var cfe = toUpdate.head
      toUpdate = toUpdate - cfe

      var newStates = eval(cfe, result.getOrElse(cfe, new MultiState(Set.empty)))
      newStates.foreach(entry => {
        var oldState = result.getOrElse(entry._1, new MultiState(Set.empty))
        if (oldState in entry._2) {
          toUpdate += entry._1
          result += (entry._1 -> entry._2)
        }
      })
    }

    result
  }

}

class PersistentState {

}

class MultiState(val states: Set[PersistentState]) {
  def in(that: MultiState): Boolean = states.subsetOf(that.states) && states.size < that.states.size
}