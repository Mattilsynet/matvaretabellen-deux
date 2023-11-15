# matvaretabellen.no

Vi begynner så smått å lage den nye matvaretabellen.no i forbindelse med at den
gamle må kobles av on-prem innen årets utgang. Håper da å få tatt noen grep for
å gjøre den mer generelt brukanes.

## Statisk site

Vår gjeldende teori er at vi ikke trenger en server kjørende, men heller trykker
ut sider med HTML i et byggesteg. Da får vi:

- lynraske sider
- færre kjørende prosesser å overvåke og betale for
- lavere karbonfotavtrykk

Når vi også bygger dette opp som krysslenka innholdssider på egen URL, så får vi
også:

- være treff på søkemotorer
- lettere å lenke til
- lettere å bokmerke

Alt i alt et par Kinderegg* av godsaker der, altså.

<small>* forøvrig en merkevare som ikke er å finne i matvaretabellen i dag</small>

## Hva med søket?

Her kommer det bloggpost så snart vi har smekket opp mat-teamets blogg, men kort fortalt:

- vi bygger en bitteliten søkemotor i JavaScript
- leverer siden med ferdig tygget ordbok/indeks for søket

Våre beregninger så langt tilsier at en slik søkeindeks vil ende opp på færre
kilobyte enn ett enkelt bilde på VGs forside. Utrolig hvor mye tekst du kan få
inn sammenlignet med bilde og video. Ytelsen på søket går fort og fint.

## Hvordan kjører jeg dette lokalt?

Dette oppsettet antar for øyeblikket at du sitter på en Mac.

I skrivende stund har vi bare fyrt opp en Portfolio-site hvor vi kan snekre
sammen komponenter og UI-elementer. Denne kan du kjøre ved å gjøre noe slikt:

- Sørg for at du har [FontAwesome](https://fontawesome.com)-ikonene:

    ```
    make prepare-dev
    ```

- Skaff Clojure

    ```
    brew install clojure
    ```

- Start ClojureScript-bygget

    ```
    clj -M:dev -m figwheel.main -b dev -r
    ```

- [Se på UI-komponentene](http://localhost:5054/)

- Kopier eksempelkonfigurasjonen:

    ```
    cp config/local-config.sample.edn config/local-config.edn
    ```

- Kjør opp databasen

    ```
    make start-transactor
    ```

### Nytt år, ny import av FoodCASE

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
    jq '.' foodcase-food-nb.json > work/matvaretabellen/data/foodcase-food-nb.json
    jq '.' foodcase-food-en.json > work/matvaretabellen/data/foodcase-food-en.json
    jq '.' foodcase-data-nb.json > work/matvaretabellen/data/foodcase-data-nb.json
    jq '.' foodcase-data-en.json > work/matvaretabellen/data/foodcase-data-en.json
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

### Oppsett av produksjonsmiljøet

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
