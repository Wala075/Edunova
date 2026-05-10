# Configuration des Variables d'Environnement (.env)

## Description

Le fichier `.env` contient les clés sensibles et les configurations spécifiques à l'environnement. Ce fichier **ne doit pas être commité** sur le repository.

## Fichiers

- **`.env`** - Fichier de configuration local (ignoré par Git)
- **`.env.example`** - Exemple de structure (commité sur Git)

## Configuration

### 1. Créer le fichier `.env`

Copiez le fichier `.env.example` et renommez-le en `.env`:

```bash
cp .env.example .env
```

### 2. Remplir les clés

Éditez le fichier `.env` et remplacez les valeurs par vos vraies clés:

```env
# Configuration hCaptcha
HCAPTCHA_SECRET_KEY=votre_clé_secrète_hcaptcha
HCAPTCHA_SITE_KEY=votre_clé_site_hcaptcha

# Configuration Google OAuth2
GOOGLE_CLIENT_ID=votre_client_id_google
GOOGLE_CLIENT_SECRET=votre_client_secret_google
```

### 3. Charger les variables

Pour charger les variables d'environnement dans votre application Java:

```java
import io.github.cdimascio.dotenv.Dotenv;

public class Config {
    private static final Dotenv dotenv = Dotenv.load();
    
    public static String getHcaptchaSecretKey() {
        return dotenv.get("HCAPTCHA_SECRET_KEY");
    }
    
    public static String getHcaptchaSiteKey() {
        return dotenv.get("HCAPTCHA_SITE_KEY");
    }
    
    public static String getGoogleClientId() {
        return dotenv.get("GOOGLE_CLIENT_ID");
    }
    
    public static String getGoogleClientSecret() {
        return dotenv.get("GOOGLE_CLIENT_SECRET");
    }
}
```

## Dépendances Maven

Ajoutez la dépendance `dotenv-java` au `pom.xml`:

```xml
<dependency>
    <groupId>io.github.cdimascio</groupId>
    <artifactId>java-dotenv</artifactId>
    <version>0.0.1</version>
</dependency>
```

## Sécurité

⚠️ **Important**: 
- Ne commitez **jamais** le fichier `.env`
- Le fichier `.env` est ignoré par Git (voir `.gitignore`)
- Utilisez `.env.example` pour montrer la structure
- Chaque développeur doit créer son propre `.env` local

## Clés de Test

Les clés fournies dans `.env.example` sont des **clés de test** pour développement:

- **hCaptcha**: Clés de test officielles
- **Google OAuth2**: Clés de test (à remplacer en production)

## Production

En production, utilisez les vraies clés et stockez-les de manière sécurisée:

- Variables d'environnement du serveur
- Gestionnaire de secrets (AWS Secrets Manager, Azure Key Vault, etc.)
- Fichier `.env` sécurisé sur le serveur

## Troubleshooting

### Les variables ne sont pas chargées

1. Vérifiez que le fichier `.env` existe à la racine du projet
2. Vérifiez que la dépendance `dotenv-java` est installée
3. Vérifiez le format du fichier (pas d'espaces autour du `=`)

### Erreur "Cannot find .env file"

Assurez-vous que le fichier `.env` est à la racine du projet:

```
Login/
├── .env          ← Ici
├── .env.example
├── pom.xml
├── src/
└── ...
```

## Références

- [dotenv-java](https://github.com/cdimascio/java-dotenv)
- [hCaptcha Documentation](https://docs.hcaptcha.com/)
- [Google OAuth2 Documentation](https://developers.google.com/identity/protocols/oauth2)
