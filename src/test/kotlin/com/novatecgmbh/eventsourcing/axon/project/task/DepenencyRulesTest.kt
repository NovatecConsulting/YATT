package com.novatecgmbh.eventsourcing.axon.project.task

import com.tngtech.archunit.base.DescribedPredicate.alwaysTrue
import com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition

@AnalyzeClasses(packagesOf = [DependencyRulesTest::class])
class DependencyRulesTest {
  @ArchTest
  val cqrs: ArchRule =
      SlicesRuleDefinition.slices()
          .matching("..task.(*)..")
          .should()
          .notDependOnEachOther()
          .ignoreDependency(alwaysTrue(), resideInAPackage("..api.."))
}
