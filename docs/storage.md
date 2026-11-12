# Andmete salvestamine

Rakendus salvestab andmeid kolmel viisil. Kõik andmed säilivad ka pärast rakenduse sulgemist ja taaskäivitamist. Kogu salvestamise kood on ühes failis: Storage.kt.

---

## 1. Seaded – SharedPreferences

Lihtne võti-väärtus salvestus väikestele seadetele.

```kotlin
private fun prefs(context: Context) = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

fun loadDarkTheme(context: Context): Boolean = prefs(context).getBoolean("dark_theme", false)

fun saveDarkTheme(context: Context, dark: Boolean) {
    prefs(context).edit().putBoolean("dark_theme", dark).apply()
}
```

- MainActivity loeb seade käivitamisel ja annab selle teemale: MobiilrTheme(darkTheme = uiState.darkTheme).
- Settings lehe lüliti salvestab uue väärtuse kohe.
- apply() kirjutab taustal, nii et ekraan ei jää ootama.

## 2. Tekstifail – filesDir

Rakenduse privaatne kaust. Teised rakendused seda ei näe ja erinevalt cache'ist ei kustuta Android seda ise.

```kotlin
fun noteFile(context: Context) = File(context.filesDir, "note.txt")

fun loadNote(context: Context): String = noteFile(context).let { if (it.exists()) it.readText() else "" }

fun saveNote(context: Context, text: String) {
    noteFile(context).writeText(text)
}
```

Settings lehel kirjutad märke kasti „Note" ja vajutad „Save note". All on näha „Saved note: …", mis on loetud failist.

## 3. Pildid – MediaStore (galerii)

1. „Take photo" kontrollib, kas kaamera luba on antud. Kui pole, küsib seda.
2. Avaneb telefoni kaamera. Kaamera kirjutab pildi ajutiselt cache'i (photo.jpg).
3. savePhoto() kopeerib pildi telefoni galeriisse kausta Pictures/Mobiilr. Iga pilt saab uue nime (IMG_<millisekundid>.jpg), nii et midagi üle ei kirjutata.
4. Avaneb galerii leht.

```kotlin
fun savePhoto(context: Context, photo: File): Uri? {
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}.jpg")
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Mobiilr")
    }
    val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return null
    context.contentResolver.openOutputStream(uri)?.use { out -> photo.inputStream().use { it.copyTo(out) } }
    return uri
}
```
**Galerii leht** küsib loadPhotos() abil galeriist kõik meie pildid (uusimad eespool) ja näitab neid. Pildid loetakse iga kord failisüsteemist, nii et need on olemas ka pärast taaskäivitust.

## 4. Probleemid ja lahendused

### 1. Pilt jäi ainult cache'i

**Probleem:** algne lahendus salvestas pildi Cache-i. Iga uus pilt kirjutas eelmise üle ja Android võis cache'i ruumipuuduse korral ise tühjendada.

**Lahendus:** kaamera kirjutab ikka cache'i, sest FileProvider annab kaamerarakendusele õiguse kirjutada just sinna. Sealt kopeerime pildi MediaStore kaudu galeriisse.

### 2. Pildid ei säilinud rakenduses pärast taaskäivitust

**Probleem:** esimeses versioonis oli pilt pärast taaskäivitust galeriis alles, aga rakendus hoidis seda ainult mälus (Bitmap ViewModelis). Taaskäivitusel see kadus.

**Lahendus:** galerii leht küsib pildid iga kord MediaStore'ist, seega tulevad need otse failisüsteemist.

### 3. "Take photo" jooksis pärast AR-i liitmist kokku

**Probleem:** pärast main haru (AR) liitmist jooksis rakendus „Take photo" vajutamisel kokku. Enne liitmist töötas kõik.

**Põhjus:** AR-teek SceneView lisab oma manifesti rea
```xml
<uses-permission android:name="android.permission.CAMERA" />
```
Gradle liidab ehitamisel kõigi teekide manifestid üheks (*manifest merging*).

Kui kasutaja avas enne AR-i ja andis seal loa, töötas ka pildistamine.


```kotlin
val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
    if (granted) takePhoto.launch(photoUri)
}

onTakePhoto = {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
        takePhoto.launch(photoUri)
    } else {
        cameraPermission.launch(Manifest.permission.CAMERA)
    }
}
```

Esimesel vajutusel küsib telefon luba. Kui kasutaja annab loa, avaneb kaamera kohe. Kui keeldub, ei juhtu midagi ja rakendus kokku ei jookse.

Õppetund: uue teegi lisamine võib muuta rakenduse käitumist ka sealt, kus koodi ei puudutatud. Pärast teegi lisamist tasub vaadata Merged Manifesti.


### 4. Main haru liitmine gallery harusse

Storage kodutöö haru oli tehtud enne AR-i maini pushimist. main-i liitmine läks konfliktideta, aga liitmise järel ilmnes probleem 6.

## 5. Mis oli lihtne ja mis keeruline

**Lihtne:**
- SharedPreferences ja tekstifail on mõlemad paar rida koodi.
- Android 10+ peal ei pea MediaStore'iga lube küsima.
- Galerii eelvaated teeb Android ise (loadThumbnail), lisateeki pole vaja.

**Keeruline:**
- **AR-teek lõhkus pildistamise** Viga oli raske leida, sest ma ei olnud kaamera koodi muutnud ja see ilmnes ainult siis, kui AR-is polnud enne luba antud.
- **Kaamera ei saa otse galeriisse kirjutada nii lihtsalt kui cache'i**, seega tegin cache → galerii kopeerimise.

