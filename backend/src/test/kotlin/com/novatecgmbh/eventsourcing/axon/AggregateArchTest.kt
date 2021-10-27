package com.novatecgmbh.eventsourcing.axon

import com.novatecgmbh.eventsourcing.axon.common.command.BaseAggregate
import com.tngtech.archunit.base.DescribedPredicate
import com.tngtech.archunit.base.DescribedPredicate.not
import com.tngtech.archunit.core.domain.JavaMember
import com.tngtech.archunit.core.domain.properties.HasName
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.spring.stereotype.Aggregate

@AnalyzeClasses(packagesOf = [AxonApplication::class])
class AggregateArchTest {
  @ArchTest
  val extendBaseAggregateRule: ArchRule =
      classes()
          .that()
          .areAnnotatedWith(Aggregate::class.java)
          .should()
          .beAssignableTo(BaseAggregate::class.java)
          .`as`("Aggregates should extend ${BaseAggregate::class.java.simpleName}")

  @ArchTest
  val dontCallAggregateLifecycleApplyRule: ArchRule =
      classes()
          .that()
          .areAnnotatedWith(Aggregate::class.java)
          .should()
          .onlyCallMethodsThat(not(AggregateLifecycle_applyPredicate))
          .`as`("Aggregates should use apply of base class instead of AggregateLifecycle.apply")

  companion object {
    val AggregateLifecycle_applyPredicate: DescribedPredicate<JavaMember> =
        JavaMember.Predicates.declaredIn(AggregateLifecycle::class.java)
            .and(HasName.Predicates.name("apply"))
  }
}
