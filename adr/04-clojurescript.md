# ADR 4: ClojureScript for dynamisk frontend

## Kontekst

Matvaretabellen har "ingen backend", så all form for dynamisk funksjonalitet må
implementeres på klienten. Søk var det opprinnelige caset, men i løpet av
utviklingen har det også kommet til en god del interaktiv funksjonalitet for
sortering, filtrering og navigering i tabeller, som uansett må løses på
klienten.

## Beslutning

Vi bruker ClojureScript for klientside funksjonalitet.

## Konsekvenser

Vi kan dele kode på tvers av backend og frontend.
Vi får et byggesteg for frontendkoden.

### Fordeler

- Gjenbruk av kode mellom server og klient
- Felles språk for hele kodebasen (mindre kognitiv last og kontekstsvitsjing)
- Immutable data
- Utviklingsmiljø med live reload

Tungen på vektskålen i denne avgjørelsen ble søket. Søkeindeksen bygges på
serveren. For at søket skal fungere godt er det helt avgjørende at tokenizeren
fungerer likt ved indeksering og søk. Ved å bruke ClojureScript på klienten kan
vi gjenbruke den samme koden for å tokenize søkestrenger og innhold i indeksen
og garantere et godt søk.

### Ulemper

- Bundelen som går til klienten blir større
- Frontendkoden må gjennom et byggesteg
- Mer kode medfører flere muligheter for feil "in the wild"

Den største ulempen er den første: mengden kode som sendes til klienten.
Heldigvis blir koden aggressivt komprimert av Closure compileren, og med gzip er
det ikke den største nedlastingen. Ettersom vi ellers har gått for små og raske
løsninger anser vi dette som en levelig kostnad.

## Alternativer

Den opprinnelige planen var å kun ha bittelitt JavaScript i søkemotoren.
Modellen ble skissert i noen
[bloggposter](https://parenteser.mattilsynet.io/fulltekstsok/). Med denne
modellen måtte vi ha reimplementert tokenization for klienten, med risiko for at
søket fungerte mindre godt.
