package com.estapar.parking.arch

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HexagonalArchitectureTest {
    private lateinit var classes: JavaClasses

    @BeforeAll
    fun importClasses() {
        classes =
            ClassFileImporter()
                .withImportOption(ImportOption.DoNotIncludeTests())
                .importPackages("com.estapar.parking")
    }

    @Test
    fun `domain must not depend on Spring`() {
        noClasses()
            .that()
            .resideInAPackage("..domain..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                "org.springframework..",
                "jakarta.persistence..",
                "com.fasterxml.jackson..",
                "org.hibernate..",
            ).allowEmptyShould(true)
            .check(classes)
    }

    @Test
    fun `application layer must not depend on infrastructure`() {
        noClasses()
            .that()
            .resideInAPackage("..application..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("..infrastructure..")
            .allowEmptyShould(true)
            .check(classes)
    }

    @Test
    fun `application layer must not depend on Spring Web or JPA directly`() {
        noClasses()
            .that()
            .resideInAPackage("..application..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                "org.springframework.web..",
                "jakarta.persistence..",
            ).allowEmptyShould(true)
            .check(classes)
    }

    @Test
    fun `domain must not depend on JPA annotations`() {
        noClasses()
            .that()
            .resideInAPackage("..domain..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("jakarta.persistence..")
            .allowEmptyShould(true)
            .check(classes)
    }

    @Test
    fun `garage infrastructure must not import parkingsession infrastructure`() {
        noClasses()
            .that()
            .resideInAPackage("..garage.infrastructure..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("..parkingsession.infrastructure..")
            .allowEmptyShould(true)
            .check(classes)
    }

    @Test
    fun `billing infrastructure must not import parkingsession infrastructure`() {
        noClasses()
            .that()
            .resideInAPackage("..billing.infrastructure..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("..parkingsession.infrastructure..")
            .allowEmptyShould(true)
            .check(classes)
    }
}
