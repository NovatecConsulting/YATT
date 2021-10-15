package com.novatecgmbh.eventsourcing.axon.company.company.web

import com.novatecgmbh.eventsourcing.axon.company.company.api.AllCompaniesQuery
import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyId
import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyQuery
import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyQueryResult
import com.novatecgmbh.eventsourcing.axon.company.company.web.dto.CreateCompanyDto
import java.util.concurrent.CompletableFuture
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.extensions.kotlin.queryMany
import org.axonframework.extensions.kotlin.queryOptional
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_NDJSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux

@RequestMapping("/v2/companies")
@RestController
class CompanyController(
    private val commandGateway: CommandGateway,
    private val queryGateway: QueryGateway,
) {
  @GetMapping
  fun getAllCompanies(): CompletableFuture<List<CompanyQueryResult>> =
      queryGateway.queryMany(AllCompaniesQuery())

  @GetMapping(produces = [APPLICATION_NDJSON_VALUE])
  fun getAllCompaniesAndUpdates(): Flux<CompanyQueryResult> {
    val query =
        queryGateway.subscriptionQuery(
            AllCompaniesQuery(),
            ResponseTypes.multipleInstancesOf(CompanyQueryResult::class.java),
            ResponseTypes.instanceOf(CompanyQueryResult::class.java))

    return query
        .initialResult()
        .flatMapMany { Flux.fromIterable(it) }
        .concatWith(query.updates())
        .doFinally { query.cancel() }
  }

  @GetMapping("/{companyId}")
  fun getCompanyById(
      @PathVariable("companyId") companyId: CompanyId
  ): ResponseEntity<CompanyQueryResult> =
      queryGateway
          .queryOptional<CompanyQueryResult, CompanyQuery>(CompanyQuery(companyId))
          .join()
          .map { ResponseEntity(it, HttpStatus.OK) }
          .orElse(ResponseEntity(HttpStatus.NOT_FOUND))

  @GetMapping(path = ["/{companyId}"], produces = [APPLICATION_NDJSON_VALUE])
  fun getCompanyByIdAndUpdates(
      @PathVariable("companyId") companyId: CompanyId
  ): Flux<CompanyQueryResult> {
    val query =
        queryGateway.subscriptionQuery(
            CompanyQuery(companyId),
            ResponseTypes.instanceOf(CompanyQueryResult::class.java),
            ResponseTypes.instanceOf(CompanyQueryResult::class.java))

    return query.initialResult().concatWith(query.updates()).doFinally { query.cancel() }
  }

  @PostMapping
  fun createCompany(@RequestBody body: CreateCompanyDto): CompletableFuture<String> =
      createCompanyWithId(CompanyId(), body)

  @PostMapping("/{companyId}")
  fun createCompanyWithId(
      @PathVariable("companyId") companyId: CompanyId,
      @RequestBody body: CreateCompanyDto,
  ): CompletableFuture<String> = commandGateway.send(body.toCommand(companyId))
}
