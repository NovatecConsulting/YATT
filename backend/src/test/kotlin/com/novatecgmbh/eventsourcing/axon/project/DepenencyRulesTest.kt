package com.novatecgmbh.eventsourcing.axon.project

import com.novatecgmbh.eventsourcing.axon.company.company.command.Company
import com.novatecgmbh.eventsourcing.axon.project.participant.command.Participant
import com.novatecgmbh.eventsourcing.axon.project.project.command.Project
import com.novatecgmbh.eventsourcing.axon.project.task.command.Task
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
          .matching("..project.(*)..")
          .`as`("aggregates")
          .should()
          .notDependOnEachOther()
          .ignoreDependency(alwaysTrue(), resideInAPackage("..api.."))
          .ignoreDependency(Task::class.java, Project::class.java) // TODO fix dependency
          .ignoreDependency(Participant::class.java, Company::class.java) // TODO fix dependency
          .ignoreDependency(Participant::class.java, User::class.java) // TODO fix dependency
}
