package com.urgencesante.emergencytaxonomy.internal.adapter.out.staticsource;

import com.urgencesante.emergencytaxonomy.internal.application.port.out.LoadEmergencyTaxonomyPort;
import com.urgencesante.emergencytaxonomy.internal.domain.model.EmergencyCategory;
import com.urgencesante.emergencytaxonomy.internal.domain.model.Symptom;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Source statique de la taxonomie, fidèle au document client « Pour les urgences
 * médicales ». Référentiel rédigé par le dev (non éditable à l'exécution) : le
 * versionner en code plutôt qu'en base évite un schéma pour des données figées,
 * et le rend intégralement testable sans base de données.
 *
 * <p>Les codes de services renvoient au catalogue du module Medical Service
 * (migration V11). L'invariant « chaque code existe au catalogue » est vérifié
 * par un test d'intégration croisé côté bootstrap.
 */
@Component
class StaticEmergencyTaxonomyAdapter implements LoadEmergencyTaxonomyPort {

    /** Message affiché pour les catégories relevant d'un appel direct des secours. */
    private static final String DIRECT_CALL_MESSAGE =
            "Appelez immédiatement les secours : SAMU 185 ou Pompiers 180, "
                    + "ou rendez-vous au centre de santé le plus proche.";

    @Override
    public List<EmergencyCategory> loadAll() {
        return List.of(
                EmergencyCategory.of("respiratoires", "Urgences respiratoires", 1, false, null,
                        List.of(
                                sym("crise-asthme", "Crise d'asthme"),
                                sym("detresse-respiratoire", "Détresse respiratoire"),
                                sym("pneumonie-severe", "Pneumonie sévère"),
                                sym("crise-bpco", "Crise de BPCO"),
                                sym("suffocation", "Suffocation / étouffement"),
                                sym("allergie-respiratoire", "Réaction allergique avec difficulté à respirer"),
                                sym("embolie-pulmonaire", "Embolie pulmonaire suspectée"),
                                sym("autres-respiratoires", "Autres urgences respiratoires")),
                        List.of("pulmonology", "intensive_care", "emergency")),

                EmergencyCategory.of("cardiaques", "Urgences cardiaques", 2, false, null,
                        List.of(
                                sym("douleur-thoracique", "Douleur thoracique"),
                                sym("infarctus", "Infarctus suspecté"),
                                sym("crise-hypertensive", "Crise hypertensive"),
                                sym("arret-cardiaque", "Arrêt cardiaque"),
                                sym("palpitations", "Palpitations sévères"),
                                sym("malaise-cardiaque", "Malaise cardiaque")),
                        List.of("cardiology", "intensive_care", "icu")),

                EmergencyCategory.of("neurologiques", "Urgences neurologiques", 3, false, null,
                        List.of(
                                sym("avc", "AVC (Accident Vasculaire Cérébral)"),
                                sym("convulsions", "Convulsions"),
                                sym("epilepsie", "Épilepsie"),
                                sym("perte-connaissance", "Perte de connaissance"),
                                sym("paralysie", "Paralysie soudaine"),
                                sym("traumatisme-cranien", "Traumatisme crânien")),
                        List.of("neurology", "neurosurgery", "intensive_care")),

                EmergencyCategory.of("accidents", "Accidents et traumatologie", 4, true, DIRECT_CALL_MESSAGE,
                        List.of(
                                sym("accident-circulation", "Accident de circulation"),
                                sym("chute-grave", "Chute grave"),
                                sym("fracture", "Fracture"),
                                sym("entorse-grave", "Entorse grave"),
                                sym("hemorragie", "Hémorragie importante"),
                                sym("polytraumatisme", "Polytraumatisme"),
                                sym("brulure", "Brûlure")),
                        List.of("ortho_trauma", "surgery", "burn_center", "emergency")),

                EmergencyCategory.of("gyneco-obstetricales", "Urgences gynécologiques et obstétricales", 5,
                        false, null,
                        List.of(
                                sym("travail", "Femme enceinte en travail"),
                                sym("accouchement-imminent", "Accouchement imminent"),
                                sym("hemorragie-grossesse", "Hémorragie de grossesse"),
                                sym("pre-eclampsie", "Pré-éclampsie"),
                                sym("fausse-couche", "Fausse couche"),
                                sym("douleurs-abdo-grossesse", "Douleurs abdominales sévères chez la femme enceinte")),
                        List.of("gynecology", "maternity", "obstetrics")),

                EmergencyCategory.of("pediatriques", "Urgences pédiatriques", 6, false, null,
                        List.of(
                                sym("fievre-nourrisson", "Forte fièvre chez nourrisson"),
                                sym("convulsions-febriles", "Convulsions fébriles"),
                                sym("difficulte-respiratoire-enfant", "Difficulté respiratoire enfant"),
                                sym("deshydratation", "Déshydratation sévère"),
                                sym("accident-domestique", "Accident domestique")),
                        List.of("pediatrics", "pediatric_icu")),

                EmergencyCategory.of("intoxications", "Intoxications", 7, true, DIRECT_CALL_MESSAGE,
                        List.of(
                                sym("empoisonnement", "Empoisonnement"),
                                sym("surdosage", "Surdosage médicamenteux"),
                                sym("intoxication-alimentaire", "Intoxication alimentaire"),
                                sym("morsure-serpent", "Morsure de serpent"),
                                sym("piqures-venimeuses", "Piqûres venimeuses")),
                        List.of("toxicology", "intensive_care", "emergency")),

                EmergencyCategory.of("chirurgicales", "Urgences chirurgicales", 8, false, null,
                        List.of(
                                sym("appendicite", "Appendicite suspectée"),
                                sym("occlusion", "Occlusion intestinale"),
                                sym("peritonite", "Péritonite"),
                                sym("hernie-etranglee", "Hernie étranglée"),
                                sym("douleurs-abdo-aigues", "Douleurs abdominales aiguës")),
                        List.of("general_surgery", "operating_room")),

                EmergencyCategory.of("infectieuses", "Urgences infectieuses", 9, false, null,
                        List.of(
                                sym("paludisme-grave", "Paludisme grave"),
                                sym("dengue-severe", "Dengue sévère"),
                                sym("meningite", "Méningite"),
                                sym("covid-severe", "Covid sévère"),
                                sym("septicemie", "Septicémie")),
                        List.of("infectiology", "intensive_care")),

                EmergencyCategory.of("ophtalmologiques", "Urgences ophtalmologiques", 10, false, null,
                        List.of(
                                sym("perte-vue", "Perte soudaine de la vue"),
                                sym("traumatisme-oculaire", "Traumatisme oculaire"),
                                sym("projection-chimique", "Projection de produit chimique"),
                                sym("douleur-oculaire", "Douleur oculaire sévère")),
                        List.of("ophthalmology")),

                EmergencyCategory.of("orl", "Urgences ORL", 11, false, null,
                        List.of(
                                sym("corps-etranger-gorge", "Corps étranger dans la gorge"),
                                sym("epistaxis", "Épistaxis importante (saignement de nez)"),
                                sym("difficulte-avaler", "Difficulté à avaler"),
                                sym("obstruction-respiratoire", "Obstruction respiratoire")),
                        List.of("ent", "emergency")),

                EmergencyCategory.of("psychiatriques", "Urgences psychiatriques", 12, false, null,
                        List.of(
                                sym("tentative-suicide", "Tentative de suicide"),
                                sym("crise-psychotique", "Crise psychotique"),
                                sym("depression-severe", "Dépression sévère"),
                                sym("agitation", "Agitation extrême"),
                                sym("automutilation", "Risque d'automutilation")),
                        List.of("psychiatry", "psychiatric_emergency")));
    }

    private static Symptom sym(String id, String label) {
        return Symptom.of(id, label);
    }
}
