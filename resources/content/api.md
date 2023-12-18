:page/uri /api/
:page/i18n-uris {:nb "/api/" :en "/en/api/"}
:page/kind :page.kind/article
:page/locale :nb
:page/title Matvaretabellens API
:page/body

# Matvaretabellens API

Matvaretabellen er i sin helhet tilgjengelig i fem separate endepunkter:
[matvarer](#matvarer), [matvaregrupper](#matvaregrupper),
[næringsstoffer](#næringsstoffer), [LanguaL-koder](#langual) og
[kilder](#kilder). Matvarene inneholder id-referanser som kan slås opp i de
andre API-ene.

Våre API-er er ikke versjonerte. Vi etterstreber full bakoverkompatibilitet til
enhver tid. Eventuelle tillegg i endepunktene vil bli dokumentert på denne
siden. Du kan også se [Matvaretabellens
kildekode](https://github.com/mattilsynet/matvaretabellen-deux) på Github for
flere detaljer.

Matvaretabellen har årlige oppdateringer på høsten. Det skjer få eller ingen
endringer i innholdet resten av året, så du kan trygt mellomlagre data hos deg
selv. Vi ber om at Matvaretabellen oppgis som kilde når du bruker data fra oss.

<a id="matvarer"></a>
## Matvarer

Du kan hente alle matvarer i en smekk fra foods-endepunktet.

### JSON

- Norsk: [/api/nb/foods.json](/api/nb/foods.json)
- Engelsk: [/api/en/foods.json](/api/en/foods.json)

### EDN

- Norsk: [/api/nb/foods.edn](/api/nb/foods.edn)
- Engelsk: [/api/en/foods.edn](/api/en/foods.edn)

<a id="matvaregrupper"></a>
## Matvaregrupper

Matvaregruppene er organisert som et hierarki, og hver matvare har kun én plass
i hierarkiet.

### JSON

- Norsk: [/api/nb/food-groups.json](/api/nb/food-groups.json)
- Engelsk: [/api/en/food-groups.json](/api/en/food-groups.json)

### EDN

- Norsk: [/api/nb/food-groups.edn](/api/nb/food-groups.edn)
- Engelsk: [/api/en/food-groups.edn](/api/en/food-groups.edn)

<a id="næringsstoffer"></a>
## Næringsstoffer

Definisjonen av næringsstoffene inkluderer
[EuroFIR](https://www.eurofir.org/)-klassifisering, passende antall desimaler,
med mer.

### JSON

- Norsk: [/api/nb/nutrients.json](/api/nb/nutrients.json)
- Engelsk: [/api/en/nutrients.json](/api/en/nutrients.json)

### EDN

- Norsk: [/api/nb/nutrients.edn](/api/nb/nutrients.edn)
- Engelsk: [/api/en/nutrients.edn](/api/en/nutrients.edn)

<a id="langual"></a>
## LanguaL-koder

[LanguaL](https://www.langual.org/default.asp) (*Langua aL*imentaria eller
"language of food") er et klassifiseringssystem for matvarer. Dette endepunktet
inneholder tekstlig forklaring av alle LanguaL-koder som brukes av matvarene.

LanguaL-kodene er dessverre ikke oversatt, og er derfor ikke tilgjengelig under
forskjellige språk.

### JSON

- Endepunkt: [/api/langual.json](/api/langual.json)

### EDN

- Endepunkt: [/api/langual.edn](/api/langual.edn)

<a id="kilder"></a>
## Kilder

Kildene angir hvor de forskjellige tallene i Matvaretabellen kommer fra,
eventuelt hvordan de er beregnet, osv.

### JSON

- Norsk: [/api/nb/sources.json](/api/nb/sources.json)
- Engelsk: [/api/en/sources.json](/api/en/sources.json)

### EDN

- Norsk: [/api/nb/sources.edn](/api/nb/sources.edn)
- Engelsk: [/api/en/sources.edn](/api/en/sources.edn)
