package com.novatecgmbh.eventsourcing.axon

import com.tngtech.archunit.base.DescribedPredicate.alwaysTrue
import com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition

@AnalyzeClasses(packagesOf = [DependencyRulesTest::class])
class DependencyRulesTest {
  @ArchTest
  val contexts: ArchRule =
      SlicesRuleDefinition.slices()
          .matching("..axon.(*)..")
          .`as`("bounded contexts")
          .should()
          .notDependOnEachOther()
          .ignoreDependency(alwaysTrue(), resideInAPackage("..api.."))
          .ignoreDependency(alwaysTrue(), resideInAPackage("..axon.common.."))
          .ignoreDependency(alwaysTrue(), resideInAPackage("..axon.application.."))
          // TODO: fix this dependency? because other contexts depend on application and therefore transitively on user context
          .ignoreDependency(resideInAPackage("..axon.application.."), resideInAPackage("..axon.user.."))
}
