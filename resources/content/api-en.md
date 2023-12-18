:page/uri /en/api/
:page/i18n-uris {:nb "/api/" :en "/en/api/"}
:page/kind :page.kind/article
:page/locale :en
:page/title Matvaretabellen API
:page/body

# Matvaretabellen API

The food composition table is available in its entirety through five separate
endpoints: [foods](#foods), [food groups](#food-groups),
[nutrients](#nutrients), [LanguaL codes](#langual), and [sources](#sources).
Food items contain ID references that can be looked up in the other APIs.

Our APIs are not versioned. We strive for full backwards compatibility at all
times. Any additions to the endpoints will be documented on this page. You can
also check [the Food Table source
code](https://github.com/mattilsynet/matvaretabellen-deux) on GitHub for more
details.

The Food Table has annual updates in the fall. There are few or no changes to
the content for the rest of the year, so you can safely cache the data locally.
We request that the Food Table be cited as the source when using data from us.

<a id="foods"></a>
## Foods

You can get all foods from a single endpoint.

### JSON

- Norwegian: [/api/nb/foods.json](/api/nb/foods.json)
- English: [/api/en/foods.json](/api/en/foods.json)

### EDN

- Norwegian: [/api/nb/foods.edn](/api/nb/foods.edn)
- English: [/api/en/foods.edn](/api/en/foods.edn)

<a id="food-groups"></a>
## Food Groups

Food groups are organized hierarchically, and each food item has only one place
in the hierarchy.

### JSON

- Norwegian: [/api/nb/food-groups.json](/api/nb/food-groups.json)
- English: [/api/en/food-groups.json](/api/en/food-groups.json)

### EDN

- Norwegian: [/api/nb/food-groups.edn](/api/nb/food-groups.edn)
- English: [/api/en/food-groups.edn](/api/en/food-groups.edn)

<a id="nutrients"></a>
## Nutrients

The definition of nutrients includes [EuroFIR](https://www.eurofir.org/)
classification, appropriate decimal places, and more.

### JSON

- Norwegian: [/api/nb/nutrients.json](/api/nb/nutrients.json)
- English: [/api/en/nutrients.json](/api/en/nutrients.json)

### EDN

- Norwegian: [/api/nb/nutrients.edn](/api/nb/nutrients.edn)
- English: [/api/en/nutrients.edn](/api/en/nutrients.edn)

<a id="langual"></a>
## LanguaL Codes

[LanguaL](https://www.langual.org/default.asp) (*Langua aL*imentaria or
"language of food") is a classification system for food items. This endpoint
contains textual explanations of all LanguaL codes used by food items.

Unfortunately, LanguaL codes are not translated and are therefore not available
in different languages.

### JSON

- Endpoint: [/api/langual.json](/api/langual.json)

### EDN

- Endpoint: [/api/langual.edn](/api/langual.edn)

<a id="sources"></a>
## Sources

Sources indicate where the various numbers in the Food Table come from,
or how they are calculated, etc.

### JSON

- Norwegian: [/api/nb/sources.json](/api/nb/sources.json)
- English: [/api/en/sources.json](/api/en/sources.json)

### EDN

- Norwegian: [/api/nb/sources.edn](/api/nb/sources.edn)
- English: [/api/en/sources.edn](/api/en/sources.edn)
