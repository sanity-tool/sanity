package ru.urururu.sanity

import org.springframework.stereotype.Component
import ru.urururu.sanity.api.Cfg
import ru.urururu.sanity.api.cfg.Cfe

/**
 * @author Dmitry Matveev
 */
@Component
class FlowAnalyzer {

  def analyze(cfg:Cfg):Map[Cfe,Vector[State]] = Map.empty

}

class State {

}
