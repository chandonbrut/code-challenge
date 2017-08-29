package org.github.chandonbrut

import scala.collection.mutable

object InMemoryPDVRepository {

  val pdvs:scala.collection.mutable.Set[PDV] = new mutable.HashSet[PDV]()

}
