package ru.urururu.sanity

import org.springframework.stereotype.Component
import ru.urururu.sanity.api.Cfg
import ru.urururu.sanity.api.cfg._

/**
  * @author Dmitry Matveev
  */
@Component
class FlowAnalyzer {

  def evalAssign(assignment: Assignment, state: MultiState): MultiState = {
    // todo eval actual
    state.evalAssign(assignment.getLeft, assignment.getRight)
  }

  def evalCall(call: Call, state: MultiState): MultiState = state

  def evalDefaultState(cfe: Cfe, state: MultiState): MultiState = {
    cfe match {
      case noop: NoOp => state
      case assignment: Assignment => evalAssign(assignment, state)
      case call: Call => evalCall(call, state)
    }
  }

  def evalDefault(cfe: Cfe, state: MultiState): Map[Cfe, MultiState] = {
    cfe.getNext match {
      case null => Map.empty // todo store exit states somewhere
      case some => Map[Cfe, MultiState](some -> evalDefaultState(cfe, state))
    }
  }

  def evalIfCondition(ifCondition: IfCondition, state: MultiState): Map[Cfe, MultiState] = {
    // todo go to single branch if value is known
    Map[Cfe, MultiState](ifCondition.getElseElement -> state, ifCondition.getThenElement -> state)
  }

  def evalSwitch(switch: Switch, state: MultiState): Map[Cfe, MultiState] = ???

  def eval(cfe: Cfe, state: MultiState): Map[Cfe, MultiState] = {
    cfe match {
      case ifCondition: IfCondition => evalIfCondition(ifCondition, state)
      case switch: Switch => evalSwitch(switch, state)
      case default => evalDefault(default, state)
    }
  }

  def analyze(cfg: Cfg): Map[Cfe, MultiState] = {
    val initial = new MultiState(Set(new PersistentState()))
    var result = Map(cfg.getEntry -> initial)
    var toUpdate = Set(cfg.getEntry)

    while (toUpdate.nonEmpty) {
      var cfe = toUpdate.head
      toUpdate = toUpdate - cfe

      var newStates = eval(cfe, result.getOrElse(cfe, new MultiState(Set.empty)))
      newStates.foreach(entry => {
        var oldState = result.get(entry._1)
        if (oldState.isEmpty || (oldState.get in entry._2)) {
          toUpdate += entry._1
          result += (entry._1 -> entry._2)
        }
      })
    }

    result
  }

}

trait State[S] {
  def evalAssign(lValue: LValue, rValue: RValue): S
}

class PersistentState(symbols: Map[Value, Value], memory: Map[Value, Value]) extends State[PersistentState] {
  def this() = this(Map.empty, Map.empty)

  def getValue(rValue: RValue): Value = {
    rValue match {
      case value: Value => value
    }
  }

  private def initializeReference(pointer: Value) = {
    symbols.get(pointer) match {
      case some: Some[Value] => (this, some)
      case None =>
        val reference = new Reference(symbols.size)
        (new PersistentState(symbols + (pointer -> reference), memory), reference)
    }
  }

  private def putReferenceTarget(reference: Value, value: Value) = new PersistentState(symbols, memory + (reference -> value))

  def putIntoIndirection(indirection: Indirection, value: Value): PersistentState = {
    val (newState: PersistentState, reference: Value) = initializeReference(getValue(indirection.getPointer))
    newState.putReferenceTarget(reference, value)
  }

  def putValue(lValue: LValue, value: Value): PersistentState = {
    lValue match {
      case indirection: Indirection => putIntoIndirection(indirection, value)
    }
  }

  override def evalAssign(lValue: LValue, rValue: RValue): PersistentState = putValue(lValue, getValue(rValue))
}

class MultiState(val states: Set[PersistentState]) extends State[MultiState] {
  def in(that: MultiState): Boolean = states.subsetOf(that.states) && states.size < that.states.size

  override def toString: String = states.toString

  override def evalAssign(lValue: LValue, rValue: RValue): MultiState = new MultiState(states.map(p => p.evalAssign(lValue, rValue)))
}

class Reference(id: Int) extends Value {

}