#!/bin/sh

set -e
set -o pipefail

fc_bearer=$(gcloud secrets versions access latest --secret foodcase-bearer-token --project matvaretabellen-b327)

if [ $? -ne 0 ]; then
    gcloud auth login
    gcloud auth application-default login
    fc_bearer=$(gcloud secrets versions access latest --secret foodcase-bearer-token --project matvaretabellen-b327)
fi

mv data/foodcase-food-nb.json data/foodcase-food-nb.old.json
mv data/foodcase-food-en.json data/foodcase-food-en.old.json
mv data/foodcase-data-nb.json data/foodcase-data-nb.old.json
mv data/foodcase-data-en.json data/foodcase-data-en.old.json
curl -f https://foodcase.prod.nfsa.foodcase-services.com/FoodCASE_WebAppMattilsynet/ws/dataexport/food_norwegian -H "Authorization: Bearer $fc_bearer" | jq '.' > data/foodcase-food-nb.json
curl -f https://foodcase.prod.nfsa.foodcase-services.com/FoodCASE_WebAppMattilsynet/ws/dataexport/food_english -H "Authorization: Bearer $fc_bearer" | jq '.' > data/foodcase-food-en.json
curl -f https://foodcase.prod.nfsa.foodcase-services.com/FoodCASE_WebAppMattilsynet/ws/dataexport/data_norwegian -H "Authorization: Bearer $fc_bearer" | jq '.' > data/foodcase-data-nb.json
curl -f https://foodcase.prod.nfsa.foodcase-services.com/FoodCASE_WebAppMattilsynet/ws/dataexport/data_english -H "Authorization: Bearer $fc_bearer" | jq '.' > data/foodcase-data-en.json

# Etter overgangen til FoodEx2 (se doc/foodex2.md), trenger vi også en
# XML-datadump for å forstå FoodEx2-koder.
mv data/MTX.ecf data/MTX.old.ecf
curl -o data/MTX.ecf https://github.com/openefsa/efsa-catalogues/raw/bbc4e2008f2541b2f3a5e975ebc37d42e71da276/ecf_catalogues/MTX.ecf
