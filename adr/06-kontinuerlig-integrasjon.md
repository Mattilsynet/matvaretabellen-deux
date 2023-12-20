# ADR 6: Kontinuerlig integrasjon med "trunk based development"

## Kontekst

Teamet består av to utviklere som skal jobbe sammen om å utvikle den nye
Matvaretabellen. Vi ønsker kort vei til produksjon med løpende oppdateringer
underveis i arbeidet.

## Beslutning

Vårt styrende prinsipp er kontinuerlig integrasjon, og konkret jobber vi med
"trunk based development", altså pushes kode løpende til main-branchen. Vi
bruker ikke feature brancher, vi bruker ikke pull requests, og vi har ikke flere
miljøer enn produksjonsmiljøet vårt. Vi har automatiserte tester og automatisert
bygg og deployment til produksjon.

Vi jobber sammen om viktige beslutninger, grunnleggende arkitektur og
ikke-trivielle veivalg.

## Konsekvenser

Kode må skrives på en sånn måte at den til enhver tid kan gå i produksjon. Når
vi bygger nye features må de ikke "kobles på" før de er i en eller annen
brukandes form (dette kan være en slags MVP, eller at committen som kobler den
på kommer helt til slutt).

Testene må til enhver tid være grønne, slik at ikke andres arbeid blokkeres fra
å gå i produksjon. Tilsvarende må bygget kjøre, og den eksporterte siten må
passere alle sine sjekker.

Vi er mennesker, og feil skjer. Når bygget brekker må den ansvarlige prioritere
å få rydda opp, så ikke andre på teamet blir blokkert.

Produksjonsmiljøet vil få løpende oppdateringer med små og store features daglig
mens løsningen er under utvikling.

Diskusjoner tas i større grad muntlig, eventuelt med en skriftlig oppsummering
når de er interessante nok.

### Fordeler

- Bedre flyt i arbeidet
- Færre merge-konflikter
- Hyppigere oppdatering av produksjonsmiljøet slik at resten av teamet, og
  publikum, får tilgang til nye ting
- Hyppigere utveksling av kode mellom utviklerne på teamet
- Raskere å fikse feil i produksjon
- Mindre tid brukt på bike-shedding i pull requests
- Mer effektive diskusjoner muntlig enn skriftlig i en pull request

### Ulemper

- Noe mindre sporbarhet i diskusjoner - DERSOM vi ikke dokumenterer valgene som
  tas
- Raskt og enkelt å snike inn småfeil i prod (veies opp av at det er like raskt
  og enkelt å fikse disse)

## Alternativer

Ingen alternativer ble vurdert. All forskning tilsier at kontinuerlig
integrasjon er veien å gå for vellykkede software-prosjekter.
