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

- Kjør opp databasen

    ```
    make start-transactor
    ```
