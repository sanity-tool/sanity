package ru.urururu.sanity

import ru.urururu.sanity.api.cfg.{NullPtr, RValue, TemporaryVar, Value}

/**
  * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
  */
object StatePrinter {
  def toString(state: MultiState): String = {
    val sb: StringBuilder = new StringBuilder

    state.states.zipWithIndex.foreach(p => {
      sb.append("State ").append(p._2).append('\n')
      appendState(sb, 1, p._1)
    })

    sb.toString()
  }

  private def appendState(sb: StringBuilder, indentSize: Int, state: PersistentState) = {

    var tmps = scala.collection.mutable.Map[TemporaryVar, Int]()

    val printRValue: RValue => String = {
      case tmp: TemporaryVar =>
        if (!tmps.contains(tmp))
          tmps += (tmp -> tmps.size)
        "tmp" + tmps(tmp)
      case any => any.toString
    }

    var unknowns = scala.collection.mutable.Map[UnknownValue, Int]()

    val printValue: Value => String = {
      case uv: UnknownValue =>
        if (!unknowns.contains(uv))
          unknowns += (uv -> unknowns.size)
        "U" + unknowns(uv)
      case _: NullPtr => "null"
      case any => any.toString
    }

    val printFormula: Formula => String = {
      case binary: BinaryFormula => printValue(binary.left) + ' ' + binary.operator + ' ' + printValue(binary.right)
    }

    indent(sb, indentSize)
    sb.append("Symbols:").append('\n')
    appendMap[RValue, Value](sb, indentSize + 1, state.symbols, printRValue, printValue)

    indent(sb, indentSize)
    sb.append("Memory:").append('\n')
    appendMap[Value, Value](sb, indentSize + 1, state.memory, printValue, printValue)

    indent(sb, indentSize)
    sb.append("Expressions:").append('\n')
    appendMap[Formula, Value](sb, indentSize + 1, state.expressions, printFormula, printValue)
  }

  private def appendMap[A, B](sb: StringBuilder, indentSize: Int, symbols: Map[A, B], keyPrinter: A => String, valuePrinter: B => String) = {
    symbols.foreach(e => {
      indent(sb, indentSize)
      sb.append(keyPrinter(e._1)).append(" -> ").append(valuePrinter(e._2)).append('\n')
    })
  }

  private def indent(sb: StringBuilder, size: Int) = sb.append(" " * 2 * size)
}
