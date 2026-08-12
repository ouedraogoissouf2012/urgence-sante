# Validation interne (CI locale)

Tant que la CI cloud n'est pas financée (dépôt privé sans minutes GitHub
Actions ni protection de branche), **la source de vérité est la validation
interne** :

```bash
bash scripts/verify-all.sh
```

Le script exécute **tous** les contrôles :

1. limite de 300 lignes par fichier manuel ;
2. validation du contrat OpenAPI (Redocly) ;
3. **base PostGIS éphémère vierge** (Docker) + `mvn verify` complet avec les
   **tests d'intégration réellement exécutés** (`IT_DB_URL`), migrations
   V1..V5 appliquées par Flyway, et contrôle **anti-skip** (les 3 classes
   d'intégration doivent afficher `Skipped: 0`) ;
4. analyse et tests Flutter de tous les membres du workspace ;
5. APK Android debug (si le SDK est présent, sinon N/A tracé).

Chaque exécution écrit un **rapport daté** (`<horodatage>.md`) contenant le
commit vérifié, l'environnement et le détail des contrôles.

## Règle de fusion

> Une branche ne se fusionne vers `main` qu'avec un rapport **✅ VALIDE**
> couvrant son commit de tête, committé dans la PR.

Le hook `pre-push` exige lui aussi Docker et un anti-skip PostGIS réel à
chaque push (mêmes garanties que la CI, voir `scripts/git-hooks/pre-push`) ;
`verify-all.sh` reste la garde complète de fusion (OpenAPI, Flutter, APK,
rapport daté). Les workflows GitHub Actions restent en place et reprendront
ce rôle le jour où le compte est débloqué.
