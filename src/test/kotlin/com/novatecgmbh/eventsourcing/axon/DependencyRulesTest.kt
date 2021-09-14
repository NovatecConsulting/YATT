package com.novatecgmbh.eventsourcing.axon

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
  private val commandQueryWeb =
      object : SliceAssignment {
        override fun getIdentifierOf(javaClass: JavaClass) =
            when {
              javaClass.packageName.startsWith(AxonApplication::class.java.packageName) ->
                  when {
                    javaClass.packageName.contains("command") ->
                        SliceIdentifier.of(javaClass.packageName.replaceAfter("command", ""))
                    javaClass.packageName.contains("query") ->
                        SliceIdentifier.of(javaClass.packageName.replaceAfter("query", ""))
                    javaClass.packageName.contains("web") ->
                        SliceIdentifier.of(javaClass.packageName.replaceAfter("web", ""))
                    else -> SliceIdentifier.ignore()
                  }
              else -> SliceIdentifier.ignore()
            }

        override fun getDescription() = "packages command, query and web"
      }

  @ArchTest
  val cqrs: ArchRule =
      SlicesRuleDefinition.slices()
          .assignedFrom(commandQueryWeb)
          .should()
          .notDependOnEachOther()
          .ignoreDependency(alwaysTrue(), resideInAPackage("..common.."))
          .because("of CQRS")
}
