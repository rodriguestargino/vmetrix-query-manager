package com.vmetrix.querymanager.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Architecture enforcement tests for vmetrix-query-manager.
 *
 * <p>Each rule maps directly to a clause of
 * {@code vmetrix-query-manager/CLAUDE.md} — the single source of truth
 * for the hexagonal architecture, strict rules and naming conventions
 * of this project.</p>
 *
 * <p>These rules exist to PREVENT future architecture drift. If any test
 * here fails, the failure message identifies the offending class so the
 * violation can be corrected (or, if intentional, the rule revisited
 * together with CLAUDE.md).</p>
 *
 * <p>Only production code is analyzed ({@link ImportOption.DoNotIncludeTests}).</p>
 */
@AnalyzeClasses(
        packages = "com.vmetrix.querymanager",
        importOptions = { ImportOption.DoNotIncludeTests.class, ImportOption.DoNotIncludeJars.class })
class ArchitectureTest {

    // ---------------------------------------------------------------------
    // Layer Dependency Rules — CLAUDE.md §Architecture / Layer Dependency
    // ---------------------------------------------------------------------
    //
    //   api            → can import application, domain, shared
    //   application    → can import domain, shared
    //   domain         → can import shared ONLY (no Spring, no JPA)
    //   infrastructure → can import domain, shared
    //   shared         → no imports from other layers

    @ArchTest
    static final ArchRule hexagonal_layering_is_respected = layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .layer("Api").definedBy("com.vmetrix.querymanager.api..")
            .layer("Application").definedBy("com.vmetrix.querymanager.application..")
            .layer("Domain").definedBy("com.vmetrix.querymanager.domain..")
            .layer("Infrastructure").definedBy("com.vmetrix.querymanager.infrastructure..")
            .layer("Shared").definedBy("com.vmetrix.querymanager.shared..")

            // Api is the outermost adapter-in layer — nothing may depend on it.
            .whereLayer("Api").mayNotBeAccessedByAnyLayer()

            // Application orchestrates use cases — only Api may call in.
            .whereLayer("Application").mayOnlyBeAccessedByLayers("Api")

            // Domain is the core — every outer layer may depend on it.
            .whereLayer("Domain")
                .mayOnlyBeAccessedByLayers("Api", "Application", "Infrastructure")

            // Infrastructure is adapter-out — no other layer may depend on it
            // at compile time; Spring wires it at runtime.
            .whereLayer("Infrastructure").mayNotBeAccessedByAnyLayer()

            // Shared is cross-cutting — everyone may use it.
            .whereLayer("Shared")
                .mayOnlyBeAccessedByLayers("Api", "Application", "Domain", "Infrastructure")

            .as("Hexagonal layering rules from CLAUDE.md §Architecture must hold");

    // ---------------------------------------------------------------------
    // Strict Rule #4 — NO Spring annotations in domain layer
    // ---------------------------------------------------------------------

    @ArchTest
    static final ArchRule domain_must_not_depend_on_spring = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..")
            .as("CLAUDE.md Strict Rule #4 — domain must have ZERO Spring dependencies "
                    + "(no @Component, @Service, @Repository, @Autowired, etc.)")
            .allowEmptyShould(true);

    // ---------------------------------------------------------------------
    // Strict Rule #5 — NO JPA imports in domain layer
    // ---------------------------------------------------------------------

    @ArchTest
    static final ArchRule domain_must_not_depend_on_jpa = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "javax.persistence..",
                    "jakarta.persistence..")
            .as("CLAUDE.md Strict Rule #5 — domain must have ZERO JPA imports")
            .allowEmptyShould(true);

    // ---------------------------------------------------------------------
    // Strict Rule #7 — NO Spring Data interfaces leaking into domain
    // ---------------------------------------------------------------------

    @ArchTest
    static final ArchRule domain_must_not_depend_on_spring_data = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework.data..")
            .as("CLAUDE.md Strict Rule #7 — Spring Data (JpaRepository, etc.) "
                    + "must not leak into the domain layer")
            .allowEmptyShould(true);

    // ---------------------------------------------------------------------
    // Shared is a leaf — it must not import from any internal layer.
    // ---------------------------------------------------------------------

    @ArchTest
    static final ArchRule shared_must_not_depend_on_other_internal_layers = noClasses()
            .that().resideInAPackage("..shared..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "com.vmetrix.querymanager.api..",
                    "com.vmetrix.querymanager.application..",
                    "com.vmetrix.querymanager.domain..",
                    "com.vmetrix.querymanager.infrastructure..")
            .as("CLAUDE.md §Layer Dependency Rules — shared must not import "
                    + "from api, application, domain or infrastructure")
            .allowEmptyShould(true);

    // ---------------------------------------------------------------------
    // JPA entities live only in infrastructure.persistence.entity
    // (CLAUDE.md §Package Structure + §Naming Conventions)
    // ---------------------------------------------------------------------

    @ArchTest
    static final ArchRule jpa_entities_reside_in_infrastructure_entity = classes()
            .that().areAnnotatedWith("javax.persistence.Entity")
                .or().areAnnotatedWith("jakarta.persistence.Entity")
            .should().resideInAPackage("..infrastructure.persistence.entity..")
            .as("JPA entities must live in com.vmetrix.querymanager"
                    + ".infrastructure.persistence.entity")
            .allowEmptyShould(true);

    // ---------------------------------------------------------------------
    // Spring Data JpaRepository interfaces live only in
    // infrastructure.persistence.repository (Strict Rule #7, structural)
    // ---------------------------------------------------------------------

    @ArchTest
    static final ArchRule jpa_repositories_reside_in_infrastructure_repository = classes()
            .that().areAssignableTo("org.springframework.data.jpa.repository.JpaRepository")
                .or().areAssignableTo("org.springframework.data.repository.Repository")
                .or().areAssignableTo("org.springframework.data.repository.CrudRepository")
            .and().areInterfaces()
            .should().resideInAPackage("..infrastructure.persistence.repository..")
            .as("CLAUDE.md Strict Rule #7 — Spring Data repository interfaces must live "
                    + "only in com.vmetrix.querymanager.infrastructure.persistence.repository")
            .allowEmptyShould(true);

    // ---------------------------------------------------------------------
    // Controllers live only in api.controller
    // (CLAUDE.md §Naming Conventions + §Strict Rule #6)
    // ---------------------------------------------------------------------

    @ArchTest
    static final ArchRule controllers_reside_in_api_controller = classes()
            .that().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                .or().areAnnotatedWith("org.springframework.stereotype.Controller")
            .should().resideInAPackage("..api.controller..")
            .as("Spring controllers must live in com.vmetrix.querymanager.api.controller")
            .allowEmptyShould(true);

    // ---------------------------------------------------------------------
    // @Service components live only in application.service
    // (keeps Spring stereotypes out of api/, domain/, infrastructure/)
    // ---------------------------------------------------------------------

    @ArchTest
    static final ArchRule spring_services_reside_in_application_service = classes()
            .that().areAnnotatedWith("org.springframework.stereotype.Service")
            .should().resideInAPackage("..application.service..")
            .as("Spring @Service components must live in "
                    + "com.vmetrix.querymanager.application.service")
            .allowEmptyShould(true);

    // ---------------------------------------------------------------------
    // Naming Conventions — CLAUDE.md §Naming Conventions
    // ---------------------------------------------------------------------

    @ArchTest
    static final ArchRule controller_classes_follow_naming_and_placement = classes()
            .that().haveSimpleNameEndingWith("Controller")
            .should().resideInAPackage("..api.controller..")
            .as("Classes named *Controller must reside in api.controller")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule service_classes_follow_naming_and_placement = classes()
            .that().haveSimpleNameEndingWith("Service")
                .or().haveSimpleNameEndingWith("ServiceImpl")
            .should().resideInAPackage("..application.service..")
            .as("Classes named *Service / *ServiceImpl must reside in "
                    + "application.service")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule strategy_classes_live_in_domain_filter = classes()
            .that().haveSimpleNameEndingWith("Strategy")
            .should().resideInAPackage("..domain.engine.filter..")
            .as("*Strategy classes (ComparatorStrategy impls) must reside in "
                    + "domain.engine.filter")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule builder_and_assembler_classes_live_in_domain_builder = classes()
            .that().haveSimpleNameEndingWith("Builder")
                .or().haveSimpleNameEndingWith("Assembler")
            .and().resideInAPackage("com.vmetrix.querymanager..")
            // Lombok-generated *Builder inner classes are allowed anywhere
            .and().areNotNestedClasses()
            .should().resideInAPackage("..domain.engine.builder..")
            .as("*Builder / *Assembler classes must reside in "
                    + "domain.engine.builder (Lombok @Builder inner classes excluded)")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule resolver_classes_live_in_domain_join = classes()
            .that().haveSimpleNameEndingWith("Resolver")
            .should().resideInAPackage("..domain.engine.join..")
            .as("*Resolver classes must reside in domain.engine.join")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule jpa_prefixed_classes_live_in_infrastructure_persistence = classes()
            .that().haveSimpleNameStartingWith("Jpa")
            .should().resideInAPackage("..infrastructure.persistence..")
            .as("Jpa* classes (entities and adapters) must reside in "
                    + "infrastructure.persistence")
            .allowEmptyShould(true);

    // ---------------------------------------------------------------------
    // Cyclic dependency hygiene — no package cycles under the root.
    // ---------------------------------------------------------------------

    @ArchTest
    static final ArchRule no_cyclic_dependencies_between_top_level_slices = slices()
            .matching("com.vmetrix.querymanager.(*)..")
            .should().beFreeOfCycles()
            .as("There must be no circular dependencies between top-level packages "
                    + "(api, application, domain, infrastructure, shared)");
}
