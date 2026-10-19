# Kaamera

Avaekraanil on nupp „Take photo". See avab telefoni kaamerarakenduse ja kui pilt on tehtud, avaneb ekraan `photo`, mis näitab pilti. Nupp „Back" viib tagasi avaekraanile.

## Kuidas see töötab

Rakendus ei kasuta kaamerat ise, vaid palub seda teha telefoni kaamerarakendusel (`ActivityResultContracts.TakePicture()`). Kaamerarakendus salvestab täismõõdus pildi faili, mille aadressi rakendus talle annab.

1. `AppNav.kt` – nupule vajutades käivitatakse kaamera ja antakse kaasa faili `photo.jpg` URI rakenduse cache'i kaustas.
2. Kaamerarakendus kirjutab pildi sellesse faili ja tagastab `true`.
3. `loadPhoto()` (`PhotoScreen.kt`) loeb faili, vähendab pilti ja pöörab selle õiget pidi.
4. `AppViewModel.onPhotoTaken()` paneb pildi olekusse ja rakendus navigeerib ekraanile `photo`.
5. `PhotoScreen` näitab pilti olekust.

Kui kasutaja kaamera tühistab, jääb rakendus avaekraanile.

## Failid

```
app/src/main/
├── AndroidManifest.xml          ← FileProvider
├── res/xml/file_paths.xml       ← millist kausta FileProvider jagab (cache)
└── java/com/example/mobiilr/
    ├── AppNav.kt                ← kaamera käivitamine ja marsruut "photo"
    ├── AppViewModel.kt          ← olekus väli photo ja onPhotoTaken()
    ├── HomeScreen.kt            ← nupp "Take photo"
    └── PhotoScreen.kt           ← pildi ekraan, loadPhoto() ja sampleSize()
```

## Kas rakendus peab kaamera luba küsima?

Ei. Kaamerat kasutab telefoni kaamerarakendus, millel on luba juba olemas. Meie rakendus ainult palub tal pildi teha.

Tähtis: kui `AndroidManifest.xml`-i lisada `<uses-permission android:name="android.permission.CAMERA" />`, peab rakendus seda luba ka kasutajalt küsima. Muidu kaamera ei avane ja rakendus saab `SecurityException`'i. Luba on vaja alles siis, kui kaamera pilti näidatakse rakenduse enda ekraanil (nt CameraX-iga).

## Pildi hoidmine

- Pilt salvestatakse rakenduse cache'i kausta (`context.cacheDir`) faili `photo.jpg`.
- Fail on alati üks: iga uus pilt kirjutab eelmise üle.
- Android võib cache'i ise tühjendada, kui telefonis on ruumi vähe. Kasutaja saab seda teha ka käsitsi: Settings → Apps → rakendus → Storage → Clear cache.
- Teised rakendused faili ei näe. `FileProvider` annab kaamerarakendusele ajutise õiguse kirjutada ainult sellesse faili.

## Pildi vähendamine ja pööramine

Täismõõdus pilt on liiga suur, et seda otse ekraanil näidata. Xiaomi 15T 50MP pilt võtaks mälus umbes 200 MB ja rakendus jookseks kokku. Seepärast:

- `sampleSize()` arvutab, mitu korda pilti vähendada (2, 4, 8, ...), et pikem külg oleks kuni 2048 pikslit;
- `loadPhoto()` loeb EXIF-ist, mis asendis telefon pildistamise ajal oli, ja pöörab pildi õiget pidi. Ilma selleta oleks püstine pilt paljudes telefonides külili.

## Testimine

Ühiktest `SampleSizeTest.kt` (`app/src/test/`) kontrollib `sampleSize()` funktsiooni:

| Test | Mida kontrollib |
| --- | --- |
| `vaikePilt_jaabSamaks` | 1920×1080 pilti ei vähendata |
| `suurPilt_kahandatakse` | 12MP pilti vähendatakse 2 korda, 50MP pilti 4 korda, ka püstises asendis |

Käsitsi kontroll seadmes:

| # | Samm | Oodatud tulemus | Tulemus |
| --- | --- | --- | --- |
| 1 | Vajuta „Take photo" | Avaneb kaamerarakendus | |
| 2 | Tee pilt ja kinnita | Avaneb ekraan pildiga | |
| 3 | Tee pilt telefoni püsti hoides | Pilt on õiget pidi | |
| 4 | Vajuta „Back" | Avaekraan on tagasi | |
| 5 | Vajuta „Take photo" ja tühista | Jääb avaekraanile | |

Emulaatoris näitab kaamera virtuaalset tuba või testmustrit, mitte päris pilti.

## Teadaolevad piirangud

- Kui telefonis pole kaamerarakendust, jookseb rakendus nupule vajutades kokku.
- Pilt loetakse põhilõimes, nii et pärast pildistamist võib ekraan hetkeks seisma jääda.
- Pilt on mälus ViewModel'is. Kui Android rakenduse taustal sulgeb, pilti enam ei näidata, kuigi fail võib cache'is veel olemas olla.
