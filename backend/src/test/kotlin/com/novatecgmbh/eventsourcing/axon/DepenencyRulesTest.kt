package com.novatecgmbh.eventsourcing.axon

import com.novatecgmbh.eventsourcing.axon.project.participant.command.Participant
import com.novatecgmbh.eventsourcing.axon.project.project.command.Project
import com.tngtech.archunit.base.DescribedPredicate.alwaysTrue
import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.library.dependencies.SliceAssignment
import com.tngtech.archunit.library.dependencies.SliceIdentifier
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition

@AnalyzeClasses(packagesOf = [AxonApplication::class])
class DependencyRulesTest {

  @ArchTest
  val cqrs: ArchRule =
      SlicesRuleDefinition.slices()
          .assignedFrom(commandQueryWebPackages)
          .should()
          .notDependOnEachOther()

  @ArchTest
  val aggregates: ArchRule =
      SlicesRuleDefinition.slices()
          .assignedFrom(aggregatePackages)
          .should()
          .notDependOnEachOther()
          .ignoreDependency(alwaysTrue(), resideInAPackage("..api.."))
          .ignoreDependency(
              Project::class.java, Participant::class.java) // required to create first participant

  @ArchTest
  val contexts: ArchRule =
      SlicesRuleDefinition.slices()
          .assignedFrom(contextPackages)
          .should()
          .notDependOnEachOther()
          .ignoreDependency(alwaysTrue(), resideInAPackage("..api.."))
          .ignoreDependency(alwaysTrue(), resideInAPackage("..axon.common.."))
          .ignoreDependency(alwaysTrue(), resideInAPackage("..axon.application.."))
          // TODO: fix this dependency? because other contexts depend on application and therefore
          // transitively on user context
          .ignoreDependency(
              resideInAPackage("..axon.application.."), resideInAPackage("..axon.user.."))

  companion object {
    val commandQueryWebPackages =
        object : SliceAssignment {
          override fun getIdentifierOf(javaClass: JavaClass): SliceIdentifier {
            return when {
              javaClass.fullName.contains(".command.") -> SliceIdentifier.of("command")
              javaClass.fullName.contains(".query.") -> SliceIdentifier.of("query")
              javaClass.fullName.contains(".web.") -> SliceIdentifier.of("web")
              else -> SliceIdentifier.ignore()
            }
          }

          override fun getDescription(): String {
            return "command, query and web"
          }
        }

    val aggregatePackages =
        object : SliceAssignment {
          override fun getIdentifierOf(javaClass: JavaClass): SliceIdentifier {
            return when {
              javaClass.packageName.contains(".project.project") -> SliceIdentifier.of("project")
              javaClass.packageName.contains(".project.task") -> SliceIdentifier.of("task")
              javaClass.packageName.contains(".project.participant") ->
                  SliceIdentifier.of("participant")
              javaClass.packageName.contains(".company.company") -> SliceIdentifier.of("company")
              javaClass.packageName.contains(".company.employee") -> SliceIdentifier.of("employee")
              javaClass.packageName.contains(".user") -> SliceIdentifier.of("user")
              else -> SliceIdentifier.ignore()
            }
          }

          override fun getDescription(): String {
            return "aggregates"
          }
        }

    val contextPackages =
        object : SliceAssignment {
          override fun getIdentifierOf(javaClass: JavaClass): SliceIdentifier {
            return when {
              javaClass.packageName.contains(".axon.project") -> SliceIdentifier.of("project")
              javaClass.packageName.contains(".axon.company") -> SliceIdentifier.of("company")
              javaClass.packageName.contains(".axon.user") -> SliceIdentifier.of("user")
              else -> SliceIdentifier.ignore()
            }
          }

          override fun getDescription(): String {
            return "bounded contexts"
          }
        }
  }
}
