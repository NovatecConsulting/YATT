package com.novatecgmbh.eventsourcing.axon.db

import java.io.FileWriter
import java.io.IOException
import java.io.PrintWriter
import java.lang.reflect.InvocationTargetException
import java.sql.Driver
import java.util.*
import javax.persistence.spi.PersistenceUnitInfo
import org.apache.commons.lang3.text.WordUtils.capitalize
import org.assertj.core.api.Assertions
import org.hibernate.boot.Metadata
import org.hibernate.boot.model.relational.QualifiedNameImpl
import org.hibernate.boot.spi.MetadataImplementor
import org.hibernate.dialect.Dialect
import org.hibernate.jpa.HibernatePersistenceProvider
import org.hibernate.jpa.boot.internal.EntityManagerFactoryBuilderImpl
import org.hibernate.mapping.Column
import org.hibernate.mapping.ForeignKey
import org.hibernate.mapping.Table
import org.hibernate.tool.hbm2ddl.SchemaExport
import org.hibernate.tool.schema.TargetType
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean

class SchemaExporter {

  /**
   * Exports database schema to file for given dialect and driver.
   *
   * @param bean [LocalContainerEntityManagerFactoryBean] instance where to take hibernate
   * information / object references from.
   * @param dialect to use
   * @param driver to use
   * @param outputPath where to write relative to project root directory
   */
  fun exportSchema(
      bean: LocalContainerEntityManagerFactoryBean,
      dialect: Class<out Dialect?>,
      driver: Class<out Driver?>,
      outputPath: String
  ) {

    try {

      // Specify properties how to export
      val jpaPropertyMap = bean.jpaPropertyMap
      jpaPropertyMap["hibernate.hbm2ddl.auto"] = "create"
      jpaPropertyMap["hibernate.show_sql"] = "false"
      jpaPropertyMap["hibernate.format_sql"] = "true"
      jpaPropertyMap["hibernate.dialect"] = dialect.name
      jpaPropertyMap["datasource.driverClassName"] = driver.name

      // Get persistence provider
      val persistenceProvider = bean.persistenceProvider as HibernatePersistenceProvider

      // Get entity manager factory builder
      val getEntityManagerFactoryBuilder =
          HibernatePersistenceProvider::class.java.getDeclaredMethod(
              "getEntityManagerFactoryBuilder",
              PersistenceUnitInfo::class.java,
              MutableMap::class.java)

      getEntityManagerFactoryBuilder.isAccessible = true
      val persistenceUnitInfo = bean.persistenceUnitInfo
      val entityManagerFactoryBuilder =
          getEntityManagerFactoryBuilder.invoke(
              persistenceProvider, persistenceUnitInfo, jpaPropertyMap) as
              EntityManagerFactoryBuilderImpl

      // Delete output file content because hibernate 5.2.0 appends to it
      PrintWriter(outputPath).close()

      // Get metadata
      val methodMetadata = EntityManagerFactoryBuilderImpl::class.java.getDeclaredMethod("metadata")
      methodMetadata.isAccessible = true
      val metadata = methodMetadata.invoke(entityManagerFactoryBuilder) as MetadataImplementor

      // Export schema
      val schemaExport = SchemaExport()
      schemaExport.setFormat(true)
      schemaExport.setDelimiter(";")
      schemaExport.setOutputFile(outputPath)
      schemaExport.execute(EnumSet.of(TargetType.SCRIPT), SchemaExport.Action.CREATE, metadata)

      // Add indices for foreign keys
      generateIndicesForForeignKeys(metadata, outputPath)
    } catch (e: IOException) {
      Assertions.fail<Any>("Schema export failed", e)
    } catch (e: NoSuchMethodException) {
      Assertions.fail<Any>("Schema export failed", e)
    } catch (e: IllegalAccessException) {
      Assertions.fail<Any>("Schema export failed", e)
    } catch (e: InvocationTargetException) {
      Assertions.fail<Any>("Schema export failed", e)
    }
  }

  /**
   * Generate indices for all foreign keys defined in the schema. Appends data to exported file.
   *
   * @param metadata metadata where to take information from what to export
   * @param outputPath the export file where to export
   * @throws IOException if file to append cannot be found / opened
   */
  @Throws(IOException::class)
  private fun generateIndicesForForeignKeys(metadata: Metadata, outputPath: String) {
    val statements: MutableList<String> = ArrayList()

    metadata.database.namespaces.forEach { namespace ->
      namespace.tables.forEach { table: Table ->
        table.foreignKeys.values.forEach foreignKeysExitLabel@{ foreignKey: ForeignKey ->
          val iterable = Iterable { table.uniqueKeyIterator }
          val uniqueKeyColumns = iterable.map { it.columns }.filter { it.size == 1 }.flatten()

          val columns = foreignKey.columns
          columns.removeAll(uniqueKeyColumns)
          if (columns.isEmpty()) {
            return@foreignKeysExitLabel
          }

          // Create index name
          val shortColumnName =
              columns.joinToString(separator = "") { column -> getShortenedName(column.name) }

          val indexName = ("IX_" + getShortenedName(table.name) + "_" + shortColumnName)

          // Create index statement
          statements.add(getSqlCreateString(table, columns, indexName, metadata))
          statements.add("\n\n")
        }
      }
    }

    if (statements.isNotEmpty()) {
      // If statements are not empty then add a new line to separate index statements from previous
      // statements
      statements.add(0, "\n")
      FileWriter(outputPath, true).use { it.write(statements.joinToString("")) }
    }
  }

  /**
   * Returns a shortened name of the given name.
   *
   * @param name the name to shorten
   * @return the shortened name
   */
  private fun getShortenedName(name: String): String {
    var inputString = name

    val withId = inputString.endsWith("_id")
    if (withId) {
      inputString = inputString.substring(0, inputString.length - 3)
    }

    if (inputString.contains("_")) {
      val parts = inputString.split("_".toRegex()).toTypedArray()

      inputString =
          parts.joinToString(separator = "") {
            capitalize(it.toLowerCase(Locale.US).substring(0, Math.min(4, it.length)))
          }
    } else {
      val rawString = inputString

      inputString = inputString.substring(0, Math.min(4, inputString.length))
      if (rawString.length > inputString.length + 4) {
        inputString += rawString.substring(rawString.length - 4)
      } else if (rawString.length > inputString.length + 3) {
        inputString += rawString.substring(rawString.length - 3)
      } else if (rawString.length > inputString.length + 2) {
        inputString += rawString.substring(rawString.length - 2)
      } else if (rawString.length > inputString.length + 1) {
        inputString += rawString.substring(rawString.length - 1)
      }
    }

    return capitalize(inputString)
  }

  /**
   * Code is a mix of [org.hibernate.tool.schema.internal.StandardIndexExporter] and adjustments to
   * make it work without rewriting original hibernate schema exporter.
   *
   * @param table the table to create index for
   * @param indexColumns the index columns to create index for
   * @param indexName the index name
   * @param metadata metadata to verify and qualify object names
   * @return the index statement
   */
  private fun getSqlCreateString(
      table: Table,
      indexColumns: List<Column>,
      indexName: String,
      metadata: Metadata
  ): String {
    val jdbcEnvironment = metadata.database.jdbcEnvironment
    val dialect = metadata.database.dialect

    val tableName =
        jdbcEnvironment.qualifiedObjectNameFormatter.format(table.qualifiedTableName, dialect)

    val indexNameForCreation =
        if (dialect.qualifyIndexName()) {
          jdbcEnvironment.qualifiedObjectNameFormatter.format(
              QualifiedNameImpl(
                  table.qualifiedTableName.catalogName,
                  table.qualifiedTableName.schemaName,
                  jdbcEnvironment.identifierHelper.toIdentifier(indexName)),
              jdbcEnvironment.dialect)
        } else {
          indexName
        }

    val statement = indexColumns.joinToString(", ") { it.getQuotedName(dialect) }

    return "create index $indexNameForCreation on $tableName ($statement);"
  }
}
