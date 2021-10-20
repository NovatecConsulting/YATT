package com.novatecgmbh.eventsourcing.axon.project.participant

import com.novatecgmbh.eventsourcing.axon.company.company.command.Company
import com.novatecgmbh.eventsourcing.axon.project.participant.command.Participant
import com.novatecgmbh.eventsourcing.axon.user.command.User
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
          .matching("..participant.(*)..")
          .should()
          .notDependOnEachOther()
          .ignoreDependency(alwaysTrue(), resideInAPackage("..api.."))
}
