# Plan Architecture - 4 Features EduNova

> Roadmap complète pour l'ajout des 4 features : Alertes Intelligentes, Gemini AI, Brevo Email, Twilio SMS.
> Stack actuel: JavaFX 21 + MySQL 8 + Java 17 + Maven.

---

## VUE D'ENSEMBLE

| # | Feature | Type | Difficulté | Impact Jury |
|---|---------|------|------------|-------------|
| 1 | Système d'Alerte Intelligent | Métier | 🟢 Moyen | ⭐⭐⭐ |
| 2 | Gemini - Appréciations IA | AI | 🟢 Facile | ⭐⭐⭐ |
| 3 | Brevo - Emails auto | API REST | 🟡 Moyen | ⭐⭐⭐ |
| 4 | Twilio - SMS alertes | API REST | 🟡 Moyen | ⭐⭐⭐ |

**Ordre d'implémentation recommandé** : 1 → 2 → 3 → 4
> Le système d'alertes (1) génère les events qui seront ensuite envoyés par email (3) et SMS (4).
> Gemini (2) est indépendant et rapide à implémenter.

---

## DÉPENDANCES MAVEN À AJOUTER

```xml
<!-- HTTP client pour Gemini, Brevo, Twilio -->
<dependency>
    <groupId>com.squareup.okhttp3</groupId>
    <artifactId>okhttp</artifactId>
    <version>4.12.0</version>
</dependency>

<!-- JSON parsing -->
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.10.1</version>
</dependency>

<!-- Twilio SDK officiel -->
<dependency>
    <groupId>com.twilio.sdk</groupId>
    <artifactId>twilio</artifactId>
    <version>10.4.2</version>
</dependency>

<!-- (Optionnel) Lecture .env pour cacher les API keys -->
<dependency>
    <groupId>io.github.cdimascio</groupId>
    <artifactId>dotenv-java</artifactId>
    <version>3.0.0</version>
</dependency>
```

---

## FEATURE 1 : SYSTÈME D'ALERTE INTELLIGENT ⭐⭐⭐

### Objectif
Détecter automatiquement les situations critiques pour les élèves et générer des alertes priorisées.

### Règles métier (les "intelligences")

| Type d'alerte | Condition de déclenchement | Sévérité |
|--------------|---------------------------|----------|
| **CHUTE_NIVEAU** | Moyenne trimestre N < Moyenne trimestre N-1 de plus de 2 points | 🟠 Moyenne |
| **MOYENNE_FAIBLE** | Moyenne générale < 8/20 | 🔴 Critique |
| **ECHEC_MATIERE** | Moyenne matière < 6/20 | 🔴 Critique |
| **PROGRESSION_POSITIVE** | Moyenne +2 points vs trimestre précédent | 🟢 Positive |
| **EXCELLENCE** | Moyenne ≥ 16/20 | 🟢 Positive |
| **MATIERE_CRITIQUE_CLASSE** | Moyenne classe < 10 sur une matière | 🟠 Moyenne |
| **NOTE_ANORMALE** | Note isolée < 4 sur une matière où l'élève a moyenne ≥ 12 | 🟡 Info |

### Architecture

**Nouvelle table SQL** :
```sql
CREATE TABLE alerte (
    id_a INT AUTO_INCREMENT PRIMARY KEY,
    type_alerte VARCHAR(50) NOT NULL,
    severite ENUM('INFO','MOYENNE','CRITIQUE','POSITIVE') NOT NULL,
    titre VARCHAR(255) NOT NULL,
    message TEXT,
    student_id INT,
    matiere_id INT NULL,
    trimestre INT NULL,
    annee_scolaire VARCHAR(20),
    date_creation DATETIME DEFAULT CURRENT_TIMESTAMP,
    statut ENUM('NOUVELLE','LUE','TRAITEE','IGNOREE') DEFAULT 'NOUVELLE',
    email_envoye BOOLEAN DEFAULT FALSE,
    sms_envoye BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (student_id) REFERENCES student(id_s) ON DELETE CASCADE,
    FOREIGN KEY (matiere_id) REFERENCES matiere(id_m) ON DELETE SET NULL
);
```

**Nouveaux fichiers Java** :
```
edu/edunova/entities/Alerte.java                 (entity + enum Severite)
edu/edunova/services/AlerteService.java          (CRUD)
edu/edunova/services/AlerteEngine.java           (le "cerveau" - détecte les alertes)
controllers/Alertes.java                          (UI)
src/main/resources/Alertes.fxml                   (vue liste alertes)
```

**AlerteEngine.java** - méthodes clés :
```java
public List<Alerte> analyserEleve(int studentId, String annee);
public List<Alerte> analyserClasse(int classeId, String annee);
public void scanComplet(String annee);  // scan toute l'école
public List<Alerte> detecterChuteNiveau(int studentId);
public List<Alerte> detecterEchecMatiere(int studentId);
public List<Alerte> detecterExcellence(int studentId);
```

### UI

- **Bouton "Lancer Scan Intelligent"** dans le Dashboard
- **Page Alertes** : TableView avec colonnes (Date, Type, Sévérité, Élève, Message, Actions)
- **Filtres** : par sévérité, par statut, par élève
- **Couleurs visuelles** : rouge (critique), orange (moyenne), vert (positive), bleu (info)
- **Badge compteur** dans le menu : "🔔 3" alertes non lues

### Démo jury
> "Je clique sur **Scan Intelligent** → l'algorithme analyse 50 élèves en 2 secondes → 8 alertes détectées dont 2 critiques."

---

## FEATURE 2 : GEMINI AI - APPRÉCIATIONS BULLETIN ⭐⭐⭐

### Objectif
Générer automatiquement les appréciations personnalisées sur les bulletins via Gemini (Google AI).

### Architecture

**Nouveaux fichiers** :
```
edu/edunova/ai/GeminiService.java                (appel API Gemini)
edu/edunova/ai/PromptBuilder.java                (construction du prompt)
edu/edunova/utils/ConfigLoader.java              (lecture .env / config.properties)
```

**Endpoint Gemini** :
```
POST https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=API_KEY
```

**Prompt template** (FR) :
```
Tu es un professeur expérimenté. Génère une appréciation pédagogique
en 2-3 phrases (50-80 mots) en français pour un élève avec ces données :

Nom : {nom} {prenom}
Trimestre : {trimestre}
Moyenne générale : {moyenne}/20
Détail par matière :
- Mathématiques : 14.5/20
- Français : 11/20
- ...

Sois bienveillant, précise les points forts et les axes d'amélioration.
Termine par un encouragement.
```

### Intégration UI

Dans `Bulletin.fxml` :
- Bouton **"✨ Générer appréciation IA"**
- Zone de texte (TextArea) qui se remplit avec la réponse Gemini
- Indicateur de chargement (spinner) pendant l'appel
- Bouton **"Régénérer"** pour avoir une autre version

### Code clé (GeminiService.java)
```java
public CompletableFuture<String> genererAppreciation(Bulletin b) {
    String prompt = PromptBuilder.buildAppreciation(b);
    return appelGemini(prompt);
}

private CompletableFuture<String> appelGemini(String prompt) {
    // OkHttp async + JSON body
    // Retourne le texte de candidates[0].content.parts[0].text
}
```

### Sécurité
- API key dans `.env` (jamais commit)
- Ajouter `.env` au `.gitignore`
- Fichier exemple `.env.example` à committer

### Démo jury
> "Je clique sur **Générer Appréciation IA** → Gemini analyse les notes et génère en 2 secondes une appréciation personnalisée et professionnelle."

---

## FEATURE 3 : BREVO - EMAILS AUTOMATIQUES ⭐⭐⭐

### Objectif
Envoyer automatiquement des emails aux parents (bulletins, alertes critiques, convocations).

### Cas d'usage

| Trigger | Destinataire | Template |
|---------|-------------|----------|
| Bulletin généré | Parent | Bulletin trimestriel + PDF |
| Alerte CRITIQUE détectée | Parent | Convocation + détails |
| Alerte EXCELLENCE | Parent | Félicitations |
| Note < 6 saisie | Parent | Notification |

### Schéma base à mettre à jour

```sql
ALTER TABLE student
    ADD COLUMN email_parent VARCHAR(150),
    ADD COLUMN telephone_parent VARCHAR(20);

CREATE TABLE email_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    destinataire VARCHAR(150),
    sujet VARCHAR(255),
    statut ENUM('ENVOYE','ECHEC'),
    erreur TEXT,
    date_envoi DATETIME DEFAULT CURRENT_TIMESTAMP,
    alerte_id INT NULL
);
```

### Nouveaux fichiers
```
edu/edunova/notifications/BrevoService.java         (appel API Brevo)
edu/edunova/notifications/EmailTemplate.java        (templates HTML)
edu/edunova/notifications/EmailLogService.java      (log envois)
controllers/EmailHistory.java                        (UI historique)
src/main/resources/EmailHistory.fxml
```

### Endpoint Brevo
```
POST https://api.brevo.com/v3/smtp/email
Headers: api-key: xkeysib-xxxxx
```

### Templates HTML (3 templates min.)

1. **Bulletin trimestriel** - tableau notes + appréciation
2. **Alerte critique** - mise en garde + convocation
3. **Félicitations** - excellence

### Bouton dans UI
Dans la page Bulletin : **"📧 Envoyer aux parents"** après génération.

### Démo jury
> "Le bulletin est généré → je clique sur **Envoyer aux parents** → email reçu sur Gmail dans les 5 secondes avec mise en page pro."

---

## FEATURE 4 : TWILIO - SMS ALERTES ⭐⭐⭐

### Objectif
Envoyer des SMS pour les alertes urgentes (critique uniquement, pour ne pas spammer).

### Règle métier
> Un SMS est envoyé UNIQUEMENT pour alertes de sévérité **CRITIQUE** (pas les infos/moyennes).

### Architecture

**Nouveau fichier** :
```
edu/edunova/notifications/TwilioService.java
```

### Code clé
```java
public class TwilioService {
    private static final String ACCOUNT_SID = ConfigLoader.get("TWILIO_SID");
    private static final String AUTH_TOKEN = ConfigLoader.get("TWILIO_TOKEN");
    private static final String FROM = ConfigLoader.get("TWILIO_FROM");

    static {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
    }

    public static String envoyerSMS(String to, String message) {
        Message msg = Message.creator(
            new PhoneNumber(to),
            new PhoneNumber(FROM),
            message
        ).create();
        return msg.getSid();
    }
}
```

### Templates SMS courts (160 caractères max)

```
🚨 EduNova: Alerte importante concernant {prenom}.
Moyenne {trimestre}: {moyenne}/20.
RDV demandé. Détails par email.
```

### Intégration

- Dans `AlerteEngine.scanComplet()` : si sévérité = CRITIQUE → appel TwilioService
- Coche `sms_envoye = TRUE` dans la table alerte
- Compte Twilio Trial = numéro vérifié uniquement (à montrer au jury)

### Démo jury LIVE 🎯
> "Je crée une note de 3/20 → scan automatique détecte alerte critique → mon téléphone vibre devant le jury → SMS reçu → APPLAUDISSEMENTS."

---

## STRUCTURE FINALE DU PROJET

```
src/main/java/
├── controllers/
│   ├── AjouterNote.java
│   ├── Bulletin.java                    [MODIF: + bouton Gemini, + bouton Email]
│   ├── Dashboard.java                   [MODIF: + bouton Scan Intelligent + badge alertes]
│   ├── ListeNotes.java
│   ├── ModifierNote.java
│   ├── Alertes.java                     [NEW - F1]
│   └── EmailHistory.java                [NEW - F3]
│
├── edu/edunova/
│   ├── entities/
│   │   ├── Alerte.java                  [NEW - F1]
│   │   └── ... (existants)
│   │
│   ├── services/
│   │   ├── AlerteService.java           [NEW - F1]
│   │   ├── AlerteEngine.java            [NEW - F1]
│   │   └── ... (existants)
│   │
│   ├── ai/                              [NEW - F2]
│   │   ├── GeminiService.java
│   │   └── PromptBuilder.java
│   │
│   ├── notifications/                   [NEW - F3, F4]
│   │   ├── BrevoService.java
│   │   ├── TwilioService.java
│   │   ├── EmailTemplate.java
│   │   └── EmailLogService.java
│   │
│   └── utils/
│       ├── ConfigLoader.java            [NEW]
│       └── MyConnection.java
│
└── resources/
    ├── Alertes.fxml                     [NEW - F1]
    ├── EmailHistory.fxml                [NEW - F3]
    ├── alertes.css                      [NEW]
    └── ... (existants)

.env                                     [NEW - NOT COMMITTED]
.env.example                             [NEW - committed]
```

---

## PLANNING DE DÉVELOPPEMENT (suggestion)

| Étape | Durée | Tâche |
|-------|-------|-------|
| 1 | 30 min | Setup Maven dependencies + ConfigLoader + .env |
| 2 | 2-3h | **Feature 1** : Alerte entity, service, engine, UI |
| 3 | 1-2h | **Feature 2** : Gemini integration + UI bulletin |
| 4 | 2h | **Feature 3** : Brevo emails + templates HTML |
| 5 | 1h | **Feature 4** : Twilio SMS + intégration AlerteEngine |
| 6 | 1h | Tests + script SQL + données démo |
| 7 | 1h | Préparation démo jury |

**TOTAL : ~10h** pour les 4 features.

---

## PRÉPARATION POUR LA DÉMO JURY

### Scénario "WOW" en 5 minutes

1. **Login** → Dashboard avec badge "🔔 5 alertes"
2. **Page Alertes** → liste colorée, montrer une critique
3. **Bulletin élève** → cliquer "✨ Générer IA" → Gemini répond LIVE
4. **Bulletin** → cliquer "📧 Envoyer parents" → email reçu sur écran
5. **Saisir note 2/20** → scan auto détecte → SMS reçu sur téléphone du jury 🤯

### Argument soutenance
> "Notre système ne se contente pas de stocker des notes : il **comprend** la situation de l'élève, **alerte** automatiquement, **génère** du contenu pédagogique avec l'IA, et **communique** avec les parents en temps réel."

---

## ÉTAPES SUIVANTES

Une fois ce plan validé, on commence par **Feature 1** :
1. Création de la table `alerte` en SQL
2. Entity `Alerte.java`
3. `AlerteService.java` (CRUD)
4. `AlerteEngine.java` (les 7 règles métier)
5. UI `Alertes.fxml` + controller
6. Bouton scan dans Dashboard

**Ready to code ? 🚀**
