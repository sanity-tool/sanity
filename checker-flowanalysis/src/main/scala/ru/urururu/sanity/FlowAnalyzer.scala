package ru.urururu.sanity

import org.springframework.stereotype.Component
import ru.urururu.sanity.api.Cfg
import ru.urururu.sanity.api.cfg.{Assignment, Cfe}

/**
  * @author Dmitry Matveev
  */
@Component
class FlowAnalyzer {

  def evalAssign(assignment: Assignment, state: MultiState): Map[Cfe, MultiState] = {
    Map[Cfe, MultiState](assignment.getNext -> state)
  }

  def eval(cfe: Cfe, state: MultiState): Map[Cfe, MultiState] = {
    cfe match {
      case assignment: Assignment => evalAssign(assignment, state)
    }
  }

  def analyze(cfg: Cfg): Map[Cfe, MultiState] = {
    var result = Map.empty[Cfe, MultiState]
    var toUpdate = Set(cfg.getEntry)

    while (toUpdate.nonEmpty) {
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
  def in(that:MultiState):Boolean = states.subsetOf(that.states) && states.size < that.states.size
}