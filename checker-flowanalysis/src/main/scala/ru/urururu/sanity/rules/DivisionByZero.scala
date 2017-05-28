package ru.urururu.sanity.rules

import java.util.function.Consumer

import ru.urururu.sanity.FlowAnalyzer
import ru.urururu.sanity.api.cfg.BinaryExpression.Operator
import ru.urururu.sanity.api.cfg.{BinaryExpression, _}
import ru.urururu.sanity.api.{Cfg, Violation}

/**
  * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
  */
class DivisionByZero {
  def findAll(cfg: Cfg, consumer: Consumer[Violation]): Unit = {
    val fa = new FlowAnalyzer
    val states = fa.analyze(cfg)

    states.foreach {
      case (Assign(_, Binary(_, divisor, Operator.Div | Operator.Rem), cfe), state) => state.getPossibleValues(divisor).foreach {
        case const: Const => if (const.getValue == 0) consumer.accept(new Violation {
          override def getPoint: Cfe = cfe

          override def getValue: RValue = divisor
        })
        case _ => // ignore other values
      }
      case _ =>
    }
  }

  object Assign {
    def unapply(cfe: Cfe): Option[(LValue, RValue, Cfe)] = cfe match {
      case assignment: Assignment =>
        Some(assignment.getLeft, assignment.getRight, assignment)
      case _ => None
    }
  }

  object Binary {
    def unapply(value: RValue): Option[(RValue, RValue, Operator)] = {
      value match {
        case binaryExpression: BinaryExpression =>
          Some(binaryExpression.getLeft, binaryExpression.getRight, binaryExpression.getOperator)
        case _ => None
      }
    }
  }
}
