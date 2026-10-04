## 1. Keele ja keskkonna ülevaade

* Programmeerimiskeel: Kotlin
* Arenduskeskkond: Android Studio Quail 4
* Sihtkeskkond ja SDK: Android SDK 36, Android Runtime (ART)
* UI-raamistik: Jetpack Compose

## 2. Sarnasus teiste keeltega

* Java: Mõlemad on JVM-i koodiks kompileeritavad staatiliselt tüübitud OOP-keeled, mis kasutavad automaatset prügikoristust (GC) ja tagavad kompileerimisaegse tüübikontrolli.
* C#: Mõlemad tekitavad automaatselt konstruktorid ning atribuutide juurdepääsud (getter ja setter). Asünkroonne kood põhineb kooperatiivsel multitegumtööl (async/await ning coroutines).
* JS/TS: Vaikimisi mittetühiste tüüpidega null-ohutus toimib sarnaselt (nt `String?`). TypeScripti unioontüüpide analoogiks on Kotlinis suletud klassid (`sealed classes`) ning nutikad tüübiteisendused (`smart casts`). Kotlin/JS ja Kotlin/Wasm kaudu saab Kotlini koodi kompileerida ka veebikeskkonda.

## 3. Erinevus teiste keeltega

* Java: Kotlin on puhtama süntaksiga ja kompaktsem. Kaob vajadus korduva koodi (boilerplate) järele nagu semikoolonid, käsitsi kirjutatud getterid või setterid ja manuaalsed nullkontrollid.
* C#: Kotlin Multiplatform võimaldab koodi jagada eri platvormide vahel (Android, iOS, töölauarakendused, veeb). C# keskendub eelkõige Microsofti .NET-i ökosüsteemile.
* JS/TS: Kotlin käivitub tavaliselt JVM-is või ART-is ning tagab otsese juurdepääsu platvormi API-dele. JS ja TS käivituvad brauserimootorites või Node.js-is ning vajavad mobiilis suhtluseks natiivsildu.

## 4. Mobiiliarenduses olulised omadused

### Null-ohutus (Null safety)
Kotlini tüübisüsteem eristab tühistatavaid (`String?`) ja mittetühistatavaid (`String`) tüüpe. Kompilaator hoiab ohtlikud `NullPointerException` vead ära juba koodi kirjutamisel turvaliste operaatoritega (`?` ja `?:`). See hoiab ära ebakindlast võrgu- ja andmebaasisuhtlusest tingitud rakenduse ootamatu sulgumise.

### Suletud klassid ja liidesed (Sealed classes / interfaces)
`sealed`-märgis piirab alamklasside hulga kompileerimisaegselt teadaolevate tüüpidega. Seda kasutatakse eelkõige UI olekute (nt `Loading`, `Success`, `Error`) mudeldamiseks. Koos `when`-lausega tagab kompilaator kõigi olekute käsitlemise, mis välistab vead dünaamiliste andmevoogude juhtimisel.

## 5. Asünkroonsus ja elutsükkel

Mahukad tegevused (võrgupäringud, failide ja andmebaasi lugemine) peamise UI-lõime (Main Thread) peal seiskaksid kasutajaliidese tööd.

* Kotlin Coroutines: Võimaldab kirjutada asünkroonset koodi lineaarselt ja loetavalt ilma tagasikutseteta (callbacks).
* Elutsükliga seotud skoop (`viewModelScope`): Seob taustategevused Androidi komponendi elutsükliga. Kui kasutaja lahkub ekraanilt, katkestatakse taustaprotsessid automaatselt, mis hoiab ära mälulekked ja säästab akut.

## 6. Teegid ja raamistikud

### Jetpack Compose
Asendab XML-paigutused puhta Kotlini koodiga. Oleku muutudes joonistatakse uuesti ainult need komponendid, mida muudatus otseselt puudutab (rekompositsioon). Olekuhalduseks kasutatakse funktsioone `remember` ja `mutableStateOf`.

### Kotlin Flows / StateFlow
Võimaldab reaktiivseid andmevooge tarbida ja UI olekuteks teisendada (`collectAsState()`), tagades sujuva andmevahetuse andmekihi ja kasutajaliidese vahel.
