# matvaretabellen.no

2023-utgaven av Matvaretabellen, en løsning som [så dagens
lys](https://www.idunn.no/doi/full/10.18261/ntfe.12.1.4) i form av publikasjonen
"Norske Næringsmidler" allerede i 1945.

## Statisk site

Datasettet i Matvaretabellen samles inn fra mange kilder og bearbeides internt i
Mattilsynet, og publiseres én gang i året. Det er med andre ord ingen
kontinuerlig flyt av data ut. Med blant annet dette bakteppet har vi bygget
løsningen på idéen om at vi ikke trenger en server kjørende, men heller trykker
statiske sider med HTML i et byggesteg. Da får vi:

- lynraske sider
- færre kjørende prosesser å overvåke og betale for
- lavere karbonfotavtrykk

Når vi i tillegg bygger dette opp som krysslenka innholdssider på egen URL, får
vi også:

- være treff på søkemotorer
- lettere å lenke til
- lettere å bokmerke

Alt i alt et par Kinderegg* av godsaker der, altså.

<small>* forøvrig en merkevare som ikke er å finne i matvaretabellen i
dag</small>

## Hva med søket?

Serveren [bygger en indeks](./src/matvaretabellen/search_index.clj) som serveres
som en [(statisk) JSON
payload](https://www.matvaretabellen.no/search/index/nb.json) (~200kB gzippet),
og så har vi implementert [en liten
søkemotor](./src/matvaretabellen/search_index.clj) som kjører i nettleseren.

Du kan lese litt om strategien i disse to blogginnleggene:

- [Hvordan søkemotoren er bygget](https://parenteser.mattilsynet.io/fulltekstsok/)
- [Hvordan vi har løst vekting](https://parenteser.mattilsynet.io/sok-vekting/)

Disse legger opp til en JavaScript-løsning. Vi valgte den bort til fordel for en
ClojureScript-løsning for at klienten kunne dele tokenizing-koden med backenden
(som er skrevet i Clojure) - et kritisk punkt for at søket skal fungere.

## Hvordan kjører jeg dette lokalt?

Dette oppsettet antar for øyeblikket at du sitter på en Mac. Du kan lese mer om
[hvordan dette er skrudd sammen](#arkitektur) lenger ned.

- Skaff Clojure

    ```
    brew install clojure
    ```

- Sørg for at du har [FontAwesome](https://fontawesome.com)-ikonene:

    ```
    make prepare-dev
    ```

- Start ClojureScript-bygget (Emacs-brukere kan se nedenfor)

    ```
    clj -M:dev -m figwheel.main -b dev -r
    ```

- [Se på UI-komponentene med Portfolio](http://localhost:5054/)

- Kopier eksempelkonfigurasjonen:

    ```
    cp config/local-config.sample.edn config/local-config.edn
    ```

- Kjør opp databasen

    ```
    make start-transactor
    ```

- Last inn FoodCASE-data i databasen (trenger kun å gjøres første gang):

    ```
    clj -M:dev
    (require '[matvaretabellen.foodcase-import :as foodcase-import])
    (foodcase-import/create-database-from-scratch (:foods/datomic-uri config))
    ```

- Start backenden:

    ```
    clj -M:dev
    (require 'matvaretabellen.dev)
    (matvaretabellen.dev/start)
    ```

### Emacs ❤️

Dersom du bruker Emacs - noe vi anbefaler på det aller varmeste - er det
`cider-jack-in` og deretter `cider-connect-sibling-cljs` som gjelder for å få
opp både backenden og frontenden.

<a id="import"></a>
## Nytt år, ny import av FoodCASE

- Få tak i bearer token fra FoodCASE. Snakk med en av utviklerne eller Jorån.

- Dra ned dataene fra Sveits:

    ```
    export FC_BEARER=<bearer>

    curl https://foodcase.prod.nfsa.foodcase-services.com/FoodCASE_WebAppMattilsynet/ws/dataexport/food_norwegian -H "Authorization: Bearer $FC_BEARER" > foodcase-food-nb.json
    curl https://foodcase.prod.nfsa.foodcase-services.com/FoodCASE_WebAppMattilsynet/ws/dataexport/food_english -H "Authorization: Bearer $FC_BEARER" > foodcase-food-en.json
    curl https://foodcase.prod.nfsa.foodcase-services.com/FoodCASE_WebAppMattilsynet/ws/dataexport/data_norwegian -H "Authorization: Bearer $FC_BEARER" > foodcase-data-nb.json
    curl https://foodcase.prod.nfsa.foodcase-services.com/FoodCASE_WebAppMattilsynet/ws/dataexport/data_english -H "Authorization: Bearer $FC_BEARER" > foodcase-data-en.json
    ```

- Skaff deg `jq` hvis det mangler:

    ```
    brew install jq
    ```

- Fløtt dem over til vår datakatalog ferdig formatert:

    ```
    jq '.' foodcase-food-nb.json > data/foodcase-food-nb.json
    jq '.' foodcase-food-en.json > data/foodcase-food-en.json
    jq '.' foodcase-data-nb.json > data/foodcase-data-nb.json
    jq '.' foodcase-data-en.json > data/foodcase-data-en.json
    ```

- Prøv å kjøre en import. Det gjør du fra `dev/matvaretabellen/dev.clj` ved å
  kjøre koden som ligger der, som ser omtrent sånn ut:

  ```
  (def config (load-local-config))

  (foodcase-import/create-database-from-scratch (:foods/datomic-uri config))
  ```

  Kryss fingre for at ting er i orden. Vurder å skrive litt valideringskode.

- Oppdater "Nytt i Matvaretabellen" i fila `data/new-food-ids.edn`

    Denne vil gjerne ha en liste med nye matvare-ID'er. Oppdater samtidig årstallet.

- Vurder om du skal fjerne `outdated-food-group-ids` i foodcase-import

    Her er det gamle ID'er som forhåpentligvis har blitt ryddet bort i FoodCASE.

Etter alt dette skal det bare være å dytte en commit til origin/main, og du kan
gå og spise kake sammen med klagesaksavdelingen.

## Oppsett av produksjonsmiljøet

Du må ha noen verktøy:

```sh
brew install terraform gh
```

For å sette opp miljøet må du ha en GCP-konto og tilgang til relevante
prosjekter.

Du må være autentisert mot GCP. Deretter setter du parenteser-prosjektet som
default og autentiserer maskinen din mot dette prosjektet:

```sh
gcloud auth login
gcloud config set project matvaretabellen-b327
gcloud auth application-default login
```

Terraform henter noen moduler over https-git som ikke er åpne. For at det skal
funke kan du bruke github sin CLI for å autentisere deg for https:

```sh
gh auth login
```

Velg HTTPS og fullfører flyten som følger.

NÅ! Nå, er du klar for å kjøre opp ting:

```sh
cd tf/app
terraform init
terraform plan
terraform apply
```

Dette vil sette opp nødvendig infrastruktur. Merk at [terraform-oppsettet
vårt](./tf/app/main.tf) har et "hello world" image. Dette imaget brukes kun ved
første gangs oppsett. [Github
Actions-arbeidsflyten](.github/workflows/build.yml) ber CloudRun om å kjøre nye
images ved push.

### DNS-oppsett

DNS-oppsettet lever i en egen terraform-modul:

```sh
cd tf/dns
terraform init
terraform plan
terraform apply
```

### Github Actions

Verdt å merke seg: prosjekt-id-en som brukes med `workload_identity_provider`
når vi autentiserer oss mot GCP for å oppdatere Cloud Run-konfigurasjonen vår
kan finnes på følgende vis:

```sh
gcloud projects list \
  --filter="$(gcloud config get-value project)" \
  --format="value(PROJECT_NUMBER)"
```

### Bygge og publisere lokalt

Det skal normalt ikke være nødvendig å hverken bygge eller publisere Docker
images fra lokal maskin. Allikevel ønsker man av og til å gjøre nettopp det -
kanskje for å sjekke akkurat hvilke ting som ikke fungerer eller lignende.

Bygging er rett frem:

```sh
make docker
```

For å publisere må du først logge deg selv inn i GCP, og deretter sørge for at
Docker-prosessen også får være med på moroa:

```sh
gcloud auth login
gcloud auth configure-docker europe-north1-docker.pkg.dev
```

## Arkitektur

Noen viktige beslutninger er dokumentert som [ADR-er](/adr/).

Som nevnt innledningsvis er Matvaretabellen en statisk site. Det betyr at vi i
byggesteget bygger alle HTML-sidene som utgjør løsningen til disk, og så
serveres disse bare av nginx i produksjon. Produksjonsmiljøet er dermed svært
enkelt, og kan ta unna store mengder trafikk uten videre innblanding fra vår
side.

Løsningen er bygget på [Powerpack](https://github.com/cjohansen/powerpack), som
delvis er bygget opp i parallel med Matvaretabellen. Powerpack sørger for at
assets serveres med URL-er som kan caches lenge i produksjon, gir oss et veldig
responsivt og levende utviklingsmiljø, og legger til rette for en god dataflyt.

### Datakilder

Matvaretabellen har flere datakilder. Dataene som utgjør den store verdien -
næringsinformasjonen - kommer fra systemet FoodCASE. I tillegg har vi data om
dagsinntak, samt redaksjonelt tilleggsinnhold.

#### FoodCASE

Fagseksjonen jobber med dataene i FoodCASE kontinuerlig, men publiserer årlige
versjoner av datasettet. Det er dermed ingen behov for å strømme disse dataene
kontinuerlig fra kilden.

Siden dataene endrer seg så sjelden har vi valgt å sjekke dem inn i dette
repoet. Det er rene JSON-filer som kun er formattert med `jq`. For å bygge appen
blir disse dataene massert inn i [vår datamodell](./resources/foods-schema.edn)
og lest inn i Datomic.

For produksjon leses data fra JSON i repoet til en in-memory Datomic-database
før bygget starter. Under utvikling anbefales det å kjøre en Datomic-instans med
persistering. Da trenger du kun å kjøre importen en gang (og eventuelt når
datasettet endrer seg).

Nye data importeres og sjekkes inn etter [oppskriften over](#import).

#### Berikende data

I tillegg til datamatrialet fra FoodCASE har vi noe redaksjonelt innhold, samt
data om hvilke sider som er tilgjengelig osv. Redaksjonelt innhold ligger på
disk under [resources/content](./resources/content). Innholdet i disse leses inn
i en separat in-memory Datomic-database under oppstart. Under utvikling vil
innholdet i disse filene også automatisk leses inn når filene endres.

#### Anbefalt Dagsinntak

Anbefalt Dagsinntak finnes som en [CSV-fil](./resources/adi.csv) som er
eksportert fra et Excel-ark. Disse dataene leses inn i in-memory
Datomic-databasen under oppstart. Det er ingen live reload for akkurat disse
dataene under utvikling (uten at det egentlig er noen god grunn til det - annet
enn at de sjeldent endrer seg), så for å speile endringer her må appen restartes
(evaluer `(restart)` i [dev-navnerommet](./dev/matvaretabellen/dev.clj)).

### Dataflyt og bygging av sider

Når appen kjører under utvikling -- eller man kjører eksport, som i
byggeprosessen -- er flyten som følger:

- Powerpack leter i Datomic etter en side med URL-en som ble etterspurt
- Powerpack kaller på vår kode (`render-page`, se nedenfor) for å rendre siden.
  "Siden" i dette tilfellet er et map med `:page/uri`, `:page/kind` og
  `:page/locale`.
- De forskjellige side-typene er implementert med hver sin funksjon, som plukker
  frem data fra foods-databasen og bygger HTML for siden.
- Powerpack beriker responsen vår

Powerpack-applikasjonen er konfigurert i
[`matvaretabellen.core`](./src/matvaretabellen/core.clj). Du kan følge all flyt
herfra.

Navnerommet [`matvaretabellen.ingest`](./src/matvaretabellen/ingest.clj) leser
inn sidedefinisjonene som utgjør mulige URL-er på siten.

Navnerommet [`matvaretabellen.pages`](./src/matvaretabellen/pages.clj) har
funksjonen `render-page` som ser på hvilken sidetype som er etterspurt og
sparker ballen videre til riktig funksjon for å rendre siden.

### Klienten

Matvaretabellen har noe interaktiv funksjonalitet: søk, filtreringer, sidebar på
mobil osv. Denne funksjonaliteten er implementert i ClojureScript, og følger
prinsippene i "progressive enhancement". Det vil si at siten i all hovedsak
fungerer uten dette scriptet, men når det kjører så gir det enkelte elementer
mer funksjonalitet. Søket er et hederlig unntak fra dette, ettersom det ikke
fungerer i det hele tatt uten scriptet.

Vi valgte ClojureScript til frontenden primært for at søket skulle kunne dele
implementasjon av tokenization. Avvik i denne ville ført til elendig opplevelse
med søket.

Frontend-koden sparkes igang fra
[`matvaretabellen.ui.main`](./src/matvaretabellen/ui/main.cljs).

## Kontakt

Dersom du har spørsmål, tilbakemeldinger, eller har lyst til å få tak i oss av
en annen grunn kan du enten bruke issues/pull requests her på Github, eller
sende oss en epost på [team.mat@mattilsynet.no](mailto:team.mat@mattilsynet.no).

## Bidrag

Har du funnet en feil, eller ønsker du deg en eller annen liten feature? Ja, så
kom med det. Vi er åpne for både issues og pull requests. Hvis du har lyst til å
legge til kode så vil vi anmode om å starte med å ta en diskusjon om ønsket
endring så vi er enige om at hva enn du har lyst til å gjøre passer inn før du
legger for mye jobb i saken.
