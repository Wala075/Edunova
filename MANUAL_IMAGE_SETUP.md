# 📝 Manual Image Setup - Child with Books & Globe

## Problem
L'image téléchargée automatiquement n'est pas celle que vous voulez. Vous voulez l'image avec l'enfant sur les livres et le globe.

## Solution

### Option 1: Fournir l'Image Directement (Recommandé)

1. **Sauvegardez l'image** que vous avez fournie:
   - Nommez-la: `temp_image.jpg`
   - Placez-la dans: `c:\Users\PC\IdeaProjects\Login\`

2. **Redimensionnez-la** à 450x660 pixels:
   - Utilisez un outil en ligne: https://www.iloveimg.com/resize-image
   - Ou utilisez un éditeur d'images local

3. **Remplacez le fichier**:
   ```
   src/main/resources/images/login_sidebar_bg.jpg
   ```

4. **Compilez**:
   ```bash
   mvn clean compile
   ```

### Option 2: Utiliser Python avec Pillow

1. **Installez Pillow**:
   ```bash
   pip install Pillow
   ```

2. **Créez un script** `resize_my_image.py`:
   ```python
   from PIL import Image
   
   # Ouvrir l'image
   img = Image.open("temp_image.jpg")
   
   # Redimensionner à 450x660
   img_resized = img.resize((450, 660), Image.Resampling.LANCZOS)
   
   # Sauvegarder
   img_resized.save("src/main/resources/images/login_sidebar_bg.jpg", 
                    "JPEG", quality=95)
   
   print("✅ Image resized and saved!")
   ```

3. **Exécutez le script**:
   ```bash
   python resize_my_image.py
   ```

### Option 3: Utiliser un Outil en Ligne

1. Allez sur: https://www.iloveimg.com/resize-image
2. Téléchargez votre image (enfant avec livres et globe)
3. Redimensionnez à 450x660 pixels
4. Téléchargez en JPG
5. Remplacez: `src/main/resources/images/login_sidebar_bg.jpg`

## Current Status

- **Image actuelle**: Café (pas la bonne)
- **Image voulue**: Enfant sur livres avec globe
- **Dimensions requises**: 450x660 pixels
- **Format**: JPEG

## Next Steps

1. Préparez l'image (450x660 JPG)
2. Remplacez le fichier
3. Compilez: `mvn clean compile`
4. Testez: `mvn javafx:run`

## File Location

```
src/main/resources/images/login_sidebar_bg.jpg
```

---

**Besoin d'aide?** Fournissez l'image et je vais la redimensionner pour vous.
