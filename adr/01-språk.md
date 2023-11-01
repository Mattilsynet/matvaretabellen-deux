# ADR 1: Norsk som arbeidsspråk, engelsk som kodespråk

## Kontekst

Vi som jobber i Mattilsynet er norske, Mattilsynet henvender seg til Norge, men
de fleste verktøyene vi bruker for å lage programvare er engelskspråklige. Noen
kjøreregler kan hjelpe oss å være konsekvente i språkbruken slik at vi ikke får
en innfallsbasert fordeling av norsk og engelsk.

## Beslutning

Vi bruker norsk som arbeidsspråk og engelsk som kodespråk. "Kodespråk" er i
denne sammenheng all kode, datamodell, nøkler i data som bor i edn-filer osv,
men ikke nødvendigvis tekst om kode (dokumentasjon, commits, osv).

"Arbeidsspråk" dekker resterende arenaer der språkbruk er relevant:

- Skriftlig og muntlig kommunikasjon (Slack, epost, møter, osv)
- Dokumentasjon (README, ADR, Confluence, møtereferater, osv)
- Commit-meldinger

## Konsekvenser

Det er til enhver tid åpenbart hvilket språk man skal bruke når det skal skrives
tekst.

Kode og data operer kun med ett språk. Ettersom programmeringsspråket er på
engelsk vil ikke det vært mulig med norsk som kodespråk.

Uformell og lett tone på dokumentasjon og tekst om koden. Norsk er morsmålet
vårt. Det er lettere å være litt uformell og likefrem i tekstlig omgang med sitt
eget morsmål.

### Fordeler

- Lettere å skrive god dokumentasjon på eget morsmål
- Konsekvent språkbruk i kode
- Trivelige commit-meldinger

### Ulemper

- Stenger døren for team-medlemmer som ikke behersker norsk
- Domenemodellen må uttrykkes på engelsk, selvom fagpersonell snakker norsk

## Alternativer

Det ble diskutert at man kunne kodet på engelsk, men holdt domenemodellen på
norsk. Ettersom det innenfor domenet næringsstoffer er lett tilgjengelig engelsk
språk vurderte vi det som trygt å holde all kode på engelsk.
