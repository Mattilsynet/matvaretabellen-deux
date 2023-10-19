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

- Skaff Clojure

    ```
    brew install clojure
    ```

- Start Portfolio

    ```
    clj -M:ui -m figwheel.main -b ui
    ```

- [Se på UI-komponentene](http://localhost:5050/)

- Kopier eksempelkonfigurasjonen:

    ```
    cp config/local-config.sample.edn config/local-config.edn
    ```

- Kjør opp databasen

    ```
    make start-transactor
    ```

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
cd tf
terraform init
terraform plan
terraform apply
```

Dette vil sette opp nødvendig infrastruktur. Merk at [terraform-oppsettet
vårt](./tf/main.tf) har et "hello world" image. Dette imaget brukes kun ved
første gangs oppsett. [Github
Actions-arbeidsflyten](.github/workflows/build.yml) ber CloudRun om å kjøre nye
images ved push.

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
