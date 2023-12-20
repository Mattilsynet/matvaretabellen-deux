# ADR 5: Gå bort fra en ren tabell

## Kontekst

Matvaretabellen som skal erstattes er bokstavelig talt bare én stor tabell. Den
kommer med verktøy som lar brukeren konfigurere hvilke kolonner som vises, et
søk for å filtrere rader, og viser veldig mange tall på relativt liten plass.

Det mangler data på hvem brukerne er, men teorien er at det potensielle
publikummet er stort, men at den eksisterende løsningen primært snakker til mer
avanserte brukere.

Eksisterende løsning har dårlig synlighet via eksterne søkemotorer som Google.

## Beslutning

Vi lager et klikkbart nettsted der hver enkelt matvare får sin egen side. Vi
sper på med redaksjonelt innhold om matvaregrupper og næringsstoffer for å gi
innholdet et pedagogisk tilsnitt. Vi tilpasser språk og presentasjonen slik at
innholdet blir mer tilgjengelig for folk som ikke er ernæringsfagfolk.

Vi beholder den store konfigurerbare tabellen som søkeresultatside slik at
ekspertbrukernes behov ivaretas.

## Konsekvenser

Matvaretabellen blir i større grad et nettsted man kan klikke seg rundt på enn
en ren datatabell. Innholdet blir tilgjengelig for en bredere brukergruppe.

### Fordeler

- Lettere å fordype seg i enkeltmatvarer
- Hver matvare og hvert næringsstoff får en egen URL
- Siten får nok innhold til å anses som relevante treff for Google
- Navigerbare næringsstoffer og matvaregrupper gir flere måter å fordype seg i
  alle matvarene vi har data om

### Ulemper

De største ulempene ved lansering:

- Det er ikke mulig å se mange tall om mange matvarer på én gang
- Man kan ikke laste ned Excel-ark med valgfrie kolonner og matvarer
- Man kan ikke lett sammenligne et stort antall matvarer på én gang

Vi hadde opprinnelig en teori om at dette ikke var vanlige oppgaver, men fikk
raskt tilbakemelding fra erfarne brukere om at de savnet dette. Vi gjeninnførte
da den store konfigurerbare tabellen som søkeresultatside, som dekker behovene
for "ekspertbrukerne".

Med tabellen på plass er det ikke mange ulemper igjen å snakke om, annet enn at
verktøyet som er mest interessant for de mest erfarne brukerne er noe mindre
prominent plassert i løsningen enn tidligere.

## Alternativer

Det ble vurdert som uinteressant å gjenskape en helt lik løsning som den som
eksisterte.
