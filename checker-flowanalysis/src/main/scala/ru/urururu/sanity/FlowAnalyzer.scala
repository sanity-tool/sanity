package ru.urururu.sanity

import org.springframework.stereotype.Component
import ru.urururu.sanity.api.Cfg
import ru.urururu.sanity.api.cfg._
import ru.urururu.util.Coverage

/**
  * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
  */
@Component
class FlowAnalyzer {

  def onError(cfe: Cfe, e: Throwable): Unit = {}

  def evalAssign(assignment: Assignment, state: MultiState): MultiState = {
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
    // todo go to single branch if value is known, update state when some direction is taken
    Map[Cfe, MultiState](ifCondition.getElseElement -> state, ifCondition.getThenElement -> state)
  }

  def evalSwitch(switch: Switch, state: MultiState): Map[Cfe, MultiState] = ???

  def eval(cfe: Cfe, state: MultiState): Map[Cfe, MultiState] = {
    try {
      if (cfe.getSourceRange != null) Coverage.hit(cfe.getSourceRange)

      cfe match {
        case ifCondition: IfCondition => evalIfCondition(ifCondition, state)
        case switch: Switch => evalSwitch(switch, state)
        case default => evalDefault(default, state)
      }
    } catch {
      case e: Exception => throw EvaluationException(cfe, e);
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

  def getPossibleValues(rValue: RValue): Set[Value]
}

class PersistentState(val symbols: Map[RValue, Value], val memory: Map[Value, Value], val expressions: Map[Formula, Value], modCount: Int) extends State[PersistentState] {
  def this() = this(Map.empty, Map.empty, Map.empty, 0)

  private def withSymbols(newSymbols: Map[RValue, Value]) = new PersistentState(newSymbols, memory, expressions, modCount + 1)

  private def withMemory(newMemory: Map[Value, Value]) = new PersistentState(symbols, newMemory, expressions, modCount + 1)

  private def withExpressions(newExpressions: Map[Formula, Value]) = new PersistentState(symbols, memory, newExpressions, modCount + 1)

  def tryGetValue(rValue: RValue): Option[Value] = {
    rValue match {
      case value: Value => Some(value)

      case parameter: Parameter => symbols.get(parameter)
      case global: GlobalVar => symbols.get(global)
      case temporary: TemporaryVar => symbols.get(temporary)

      case indirection: Indirection =>
        val pointer = tryGetValue(indirection.getPointer)
        if (pointer.isEmpty) None else memory.get(pointer.get)

      case binaryExpression: BinaryExpression =>
        val leftVal = tryGetValue(binaryExpression.getLeft)
        val rightVal = tryGetValue(binaryExpression.getRight)
        if (leftVal.isEmpty || rightVal.isEmpty) None else expressions.get(BinaryFormula(leftVal.get, binaryExpression.getOperator, rightVal.get))
    }
  }

  def getValue(rValue: RValue): Value = {
    tryGetValue(rValue).get
  }

  private def initializeReference(pointer: Value) = {
    memory.get(pointer) match {
      case some: Some[Value] => (this, some)
      case None =>
        val reference = new Reference("U_" + symbols.size)
        (withMemory(memory + (pointer -> reference)), reference)
    }
  }

  private def createUnknownValue(rValue: RValue): (PersistentState, Value) = {
    val value = new UnknownValue("U_" + modCount)

    rValue match {
      case parameter: Parameter => (withSymbols(symbols + (parameter -> value)), value)
      case global: GlobalVar => (withSymbols(symbols + (global -> value)), value)
      case temporary: TemporaryVar => (withSymbols(symbols + (temporary -> value)), value)

      case indirection: Indirection =>
        var (newState: PersistentState, pointer: Value) = getOrCreateValue(indirection.getPointer)
        (newState.withMemory(memory + (pointer -> value)), value)
      case binaryExpression: BinaryExpression =>
        val (newState1: PersistentState, left: Value) = getOrCreateValue(binaryExpression.getLeft)
        val (newState2: PersistentState, right: Value) = newState1.getOrCreateValue(binaryExpression.getRight)
        val expression = BinaryFormula(left, binaryExpression.getOperator, right)
        (newState2.withExpressions(expressions + (expression -> value)), value)
    }
  }

  private def getOrCreateValue(rValue: RValue): (PersistentState, Value) = {
    tryGetValue(rValue) match {
      case some: Some[Value] => (this, some.get)
      case None => createUnknownValue(rValue)
    }
  }

  private def putReferenceTarget(reference: Value, value: Value) = withMemory(memory + (reference -> value))

  def putIntoIndirection(indirection: Indirection, value: Value): PersistentState = {
    var (newState: PersistentState, pointer: Value) = getOrCreateValue(indirection.getPointer)
    //val (newState2: PersistentState, reference: Value) = newState.initializeReference(pointer)
    //newState2.putReferenceTarget(reference, value)
    newState.putReferenceTarget(pointer, value)
  }

  def putValue(lValue: LValue, value: Value): PersistentState = {
    lValue match {
      case indirection: Indirection => putIntoIndirection(indirection, value)
      case temporary: TemporaryVar => withSymbols(symbols + (temporary -> value))
    }
  }

  override def evalAssign(lValue: LValue, rValue: RValue): PersistentState = {
    var (newState: PersistentState, value: Value) = getOrCreateValue(rValue)
    newState.putValue(lValue, value)
  }

  override def getPossibleValues(rValue: RValue): Set[Value] = Set(tryGetValue(rValue).getOrElse(new UnknownValue("unknown")))

  override def toString: String = "symbols:" + symbols + ", memory:" + memory + ", expressions:" + expressions
}

class MultiState(val states: Set[PersistentState]) extends State[MultiState] {
  def in(that: MultiState): Boolean = states.subsetOf(that.states) && states.size < that.states.size

  override def toString: String = StatePrinter.toString(this)

  override def evalAssign(lValue: LValue, rValue: RValue): MultiState = new MultiState(states.map(p => p.evalAssign(lValue, rValue)))

  override def getPossibleValues(rValue: RValue): Set[Value] = states.flatMap(p => p.getPossibleValues(rValue))
}

class Reference(id: String) extends UnknownValue(id) {
}

class UnknownValue(id: String) extends Value {
  override def toString: String = id
}

class Formula {}

case class BinaryFormula(left: Value, operator: BinaryExpression.Operator, right: Value) extends Formula {}