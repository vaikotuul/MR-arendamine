# Võrgupäringud (REST API)

Rakendus suhtleb välisserveriga: toob sealt kasutajate nimekirja koos profiilipiltidega ja saadab serverile sisselogimisandmed. Avaekraanil vajuta nuppu **„Network"**.

## 1. Mida rakendus teeb

Network leht:

1. **Too serverist andmed (GET).** Lehe avamisel küsitakse reqres.in-ist kasutajate nimekiri. Iga kasutaja juures on nimi, e-post ja profiilipilt. Pilt laaditakse eraldi GET päringuga. Nupp **„Reload"** teeb päringu uuesti.
2. **Saada serverile andmed (POST).** Väljadel on reqres.in testkasutaja (eve.holt@reqres.in / cityslicka). **„Login"** saadab need serverile ja ekraanil on näha vastuseks saadud token.
3. **Vead.** Kui internetti pole või server vastab veaga, näeb kasutaja punast veateadet. Näiteks parooli kustutamisel annab server vastuse: Server error 400: Missing password.

## 2. Päringud

| Meetod | URL | Saadetakse | Vastus |
| --- | --- | --- | --- |
| `GET` | `https://reqres.in/api/users?page=2` | `Accept: application/json`, sisselogimise järel ka `Authorization: Bearer <token>` | `{"page":2, "data":[{"id":7,"email":"…","first_name":"…","last_name":"…","avatar":"https://…"}, …]}` |
| `GET` | kasutaja `avatar` URL | – | JPEG pilt |
| `POST` | `https://reqres.in/api/login` | `Content-Type: application/json`, keha `{"email":"eve.holt@reqres.in","password":"cityslicka"}` | `{"token":"QpwL5tke4Pnpja7X4"}` või vea korral `400 {"error":"Missing password"}` |

reqres.in näidispäringud ei vaja API-võtit, aga neil on päevane päringute limiit. POST päringud midagi tegelikult ei salvesta: server ainult vastab, nagu oleks salvestanud.



## 3. Kuidas see töötab

```
NetworkScreen ──"Login"/"Reload"──► NetworkViewModel ──► Network.kt ──HTTPS──► reqres.in
      ▲                                   │                   │
      └──────── NetworkUiState ◄──────────┘◄── List<User> / token / viga
```

**Üks HTTP päring** (Network.kt, request()):

```kotlin
val connection = URL(url).openConnection() as HttpURLConnection
connection.requestMethod = method                       // "GET" või "POST"
connection.connectTimeout = 10_000                      // 10 s
connection.setRequestProperty("Accept", "application/json")
if (token != null) connection.setRequestProperty("Authorization", "Bearer $token")
if (body != null) {                                     // POST keha
    connection.doOutput = true
    connection.setRequestProperty("Content-Type", "application/json")
    connection.outputStream.use { it.write(body.toByteArray()) }
}
val code = connection.responseCode
if (code !in 200..299) throw HttpException(code, parseError(errorBody))
return connection.inputStream.bufferedReader().use { it.readText() }
```

**Taustalõim.** Android keelab võrgupäringud UI-lõimes (NetworkOnMainThreadException). Seepärast on iga päring suspend funktsioon ja käib withContext(Dispatchers.IO) sees:

## 4. Autentimine (Bearer token)

1. „Login" saadab e-posti ja parooli POST päringuga.
2. Server tagastab tokeni, mis kuvatakse ekraanil (`NetworkUiState.token`).

Päris API-des saadetakse token järgmistes päringutes päises `Authorization: Bearer <token>`. Proovisime seda, aga **reqres.in näidispäringud seda ei toeta**: 
Authorization päise korral arvab server, et see on API-võtme katse, ja vastab `401 missing_api_key`. Seepärast me tokenit edasi ei saada. Päris rakenduses tuleks token hoida turvaliselt (`EncryptedSharedPreferences`).

## 5. Veahaldus ja võrguolek

**Enne päringut** kontrollitakse, kas internet on olemas (isOnline() → ConnectivityManager). Kui ei ole, päringut ei tehta ja ekraanil on „No internet connection".

**Päringu ajal** püütakse vead try/catch-iga ja muudetakse arusaadavaks tekstiks (errorMessage()):

| Viga | Millal | Kasutaja näeb |
| --- | --- | --- |
| `HttpException` | server vastas koodiga, mis pole 2xx | `Server error 400: Missing password` |
| `UnknownHostException` | serverit ei leitud (pole internetti, vale aadress) | `Server not found – check internet connection` |
| `SocketTimeoutException` | server ei vastanud 10 s jooksul | `Server did not respond in time` |
| muu `IOException` | ühendus katkes | `Network error: …` |

Vigase vastuse sisu on errorStream-is, mitte inputStream-is. Sealt loetakse serveri veateade ({"error":"Missing password"}).

Laadimise ajal näeb kasutaja ringi (CircularProgressIndicator). Kui pilti ei õnnestu laadida, näidatakse kasutajat ilma pildita, ilma veateateta.

## 6. Load

AndroidManifest.xml:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```
Mõlemad on "normaalsed" load: Android annab need paigaldamisel ise ja kasutajalt midagi ei küsita. Kasutame ainult HTTPS-i, sest Android 9+ keelab vaikimisi krüpteerimata HTTP päringud.


## Mis oli lihtne ja mis keeruline

**Lihtne:**
- HttpURLConnection ja org.json on Androidis sisse ehitatud, lisateeke pole vaja.
- Kotlini suspend + Dispatchers.IO teevad taustalõime lihtsaks.
- reqres.in ei vaja registreerimist ja vastused on alati samad, mis teeb testimise lihtsaks.

**Keeruline:**
- **Vigase vastuse sisu on teises kohas.** inputStream viskab 400 korral erindi. Serveri veateade (Missing password) tuleb lugeda `errorStream`-ist.
- **Pildid on eraldi päringud.** Iga kasutaja pilt on oma GET päring, mis laaditakse alles siis, kui rida ekraanile tuleb (`produceState`).
- **Interneti puudumise testimine emulaatoris** vajab Extended Controls seadistust.
- **„Reload" andis pärast sisselogimist `401 missing_api_key`.** Saatsime tokeni `Authorization: Bearer` päises nagu päris API-des, aga reqres.in tõlgendab `Authorization` päist API-võtmena ja keeldub. Esimene laadimine töötas, sest siis tokenit veel polnud. Leidsime vea ekraanil oleva serveri veateate abil (see näitas, et veahaldus töötab). Lahendus: kasutajate pärimisel tokenit ei saadeta.