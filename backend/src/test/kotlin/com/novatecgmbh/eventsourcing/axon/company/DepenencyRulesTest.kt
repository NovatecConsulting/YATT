package com.novatecgmbh.eventsourcing.axon.company

import com.novatecgmbh.eventsourcing.axon.company.company.command.Company
import com.novatecgmbh.eventsourcing.axon.company.employee.command.Employee
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
  val aggregates: ArchRule =
      SlicesRuleDefinition.slices()
          .matching("..company.(*)..")
          .`as`("aggregates")
          .should()
          .notDependOnEachOther()
          .ignoreDependency(alwaysTrue(), resideInAPackage("..api.."))
          .ignoreDependency(Employee::class.java, Company::class.java) // TODO fix dependency
          .ignoreDependency(Employee::class.java, User::class.java) // TODO fix dependency
}
