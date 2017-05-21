package ru.urururu.sanity

import ru.urururu.sanity.api.cfg.Cfe

/**
  * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
  */
case class EvaluationException(cfe: Cfe, cause: Exception) extends RuntimeException("Can't evaluate: " + cfe, cause)