# ADR 2: Implementere Matvaretabellen som en statisk site

## Kontekst

Det skal utvikles en ny versjon av Matvaretabellen. Løsningen skal eies av team
Mat, som hovedsaklig skal fokusere på smilefjesordningen.

Dataene i Matvaretabellen oppdateres løpende i underliggende systemer, men
ønskes kun publisert på nett årlig av fagseksjonen som forvalter dem.

## Beslutning

Vi implementerer den nye Matvaretabellen som en statisk site. Det vil si at vi
bygger en kodebase for å generere sider, og når løsningen skal i produksjon
eksporterer vi alt sammen til statiske HTML-filer og pakker det inn i et Docker
image med nginx.

For å ha noe roterende innhold (sesonginnhold, noe variasjon på forsiden osv)
blir siten bygget på nytt minst én gang daglig.

## Konsekvenser

Løsningen får i praksis "ingen backend". Det vil si, den vil jo ha en kjørende
Docker container, men denne vil kun inneholde nginx som serverer statiske filer.

Vi har ikke muligheten til å gjøre dynamiske forespørsler til backenden, så alt
må enten forhåndsgenereres eller løses på klienten.

Løsningen bruker mer maskinkraft på Github Actions, ettersom det er der alle
sidene bygges. Dette er også tilfelle på lang sikt, ettersom siten bygges en
gang daglig.

### Fordeler

- Løsningen blir rask, stabil og krever få ressurser
- Løsningen har lavt behov for drift og vedlikehold
- Løsningen krever lite monitorering og overvåkning
- Løsningen kan verifiseres grundig før deploy (lenkesjekk på hele nettstedet,
  sjekke at alle bilder og assets er gyldige, etc)
- Billig drift

### Ulemper

- Kan ikke ha dynamisk server-generert innhold, feks brukerdefinerte Excel-filer
  (disse lages heller som CSV på klienten)
- Bruker relativt mange byggeminutter på Github actions
- Treigt bygg (10-20 minutter forsinkelse til deploy ved push)

## Alternativer

Vi hadde opprinnelig sett for oss å ha en helt serveless backend, og laste opp
genererte filer i GCP Object store med en lastdeler foran. Dette ble valgt bort
til fordel for Cloud run, for å kunne lene oss på ferdig arbeid fra
plattform-gjengen.
