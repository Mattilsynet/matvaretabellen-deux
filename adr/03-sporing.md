# ADR 3: Lettvekts trafikksporing med Matomo

## Kontekst

For å lage den beste mulige Matvaretabellen trenger vi innsikt i hvordan
publikum bruker tjenesten.

Matvaretabellen har "ingen backend", slik at det å lage et "first party" mottak
ikke er mulig.

Mattilsynet har flere mulige verktøy i bruk på forskjellige flater, inkludert
Matomo og Google Analytics.

## Beslutning

Vi bruker Matomo for å spore grunnleggende adferd på nettstedet. Google
Analytics er dokumentert problematisk med tanke på GDPR, og vil kreve en
cookie-popup.

Vi bruker Matomos [forenklede
img-løsning](https://matomo.org/faq/how-to/faq_176/). Matomos sporingsscript er
200kB med "bredspekter" JavaScript for sporing av besøkende. Bilde-løsningen
gjør kun enkel sporing av sidevisninger og søk.

Siden backenden er statisk løser vi sporingen på klienten for å fange opp query
parametere og userAgent/nettleserinformasjon.

## Konsekvenser

Vi får tall på sidevisninger og søk i Matvaretabellen. Vi får ikke demografi,
(gode tall på) unike brukere, og andre mer personaliserte tall.

Vi trenger ikke en cookie consent popup.

### Fordeler

- Brukernes personvern ivaretas
- Ingen tredjeparts cookies og tilhørende consent popup
- Ingen store tredjeparts JavaScript å laste ned
- Full kontroll på hva slags informasjon vi samler inn
- Oversikt over sidevisninger
- Oversikt over søk
- Får sporet referrer, som forteller oss om brukerne kom fra søkemotorer osv

### Ulemper

- Får ikke brukt all funksjonaliteten som er tilgjengelig i Matomo

## Alternativer

Ingen alternativer ble grundig vurdert. Vi har nok tidligere erfaring med Google
Analytics til å vite at vi ikke ønsket det.
