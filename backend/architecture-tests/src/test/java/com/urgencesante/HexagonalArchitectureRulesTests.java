package com.urgencesante;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Règles hexagonales complémentaires à Spring Modulith, appliquées à l'intérieur
 * de chaque module.
 *
 * <p>Le sens de dépendance imposé est {@code adapter -> application -> domain} :
 * le domaine et l'application restent en Java pur, les adaptateurs ne se
 * contournent pas.
 *
 * <p>{@code allowEmptyShould(true)} : tant qu'un module n'a pas encore de code
 * dans la couche visée, la règle ne correspond à aucune classe. Elle reste
 * néanmoins présente et s'applique dès l'ajout du premier type concerné
 * (implémentation des modules à partir de l'issue #9).
 */
@AnalyzeClasses(
        packages = "com.urgencesante",
        importOptions = ImportOption.DoNotIncludeTests.class)
class HexagonalArchitectureRulesTests {

    @ArchTest
    static final ArchRule le_domaine_ne_depend_d_aucun_framework =
            noClasses()
                    .that().resideInAPackage("..internal.domain..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "org.springframework..",
                            "jakarta.persistence..",
                            "com.fasterxml.jackson..",
                            "org.mapstruct..")
                    .because("le domaine reste en Java pur (ADR-002)")
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule le_domaine_ne_depend_ni_de_l_application_ni_des_adaptateurs =
            noClasses()
                    .that().resideInAPackage("..internal.domain..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "..internal.application..",
                            "..internal.adapter..")
                    .because("le domaine est la couche la plus interne")
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule l_application_ne_depend_pas_des_adaptateurs =
            noClasses()
                    .that().resideInAPackage("..internal.application..")
                    .should().dependOnClassesThat().resideInAPackage("..internal.adapter..")
                    .because("l'application ne connaît ni le web ni la persistance")
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule l_application_ignore_le_web_et_jpa =
            noClasses()
                    .that().resideInAPackage("..internal.application..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "org.springframework.web..",
                            "jakarta.persistence..")
                    .because("les annotations HTTP et JPA restent dans les adaptateurs")
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule le_controleur_web_ne_touche_pas_la_persistance =
            noClasses()
                    .that().resideInAPackage("..adapter.in.web..")
                    .should().dependOnClassesThat().resideInAPackage("..adapter.out.persistence..")
                    .because("un contrôleur passe par un port entrant, jamais par un repository")
                    .allowEmptyShould(true);
}
