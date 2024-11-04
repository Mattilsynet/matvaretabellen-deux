#!/bin/sh

export FC_BEARER=$(gcloud secrets versions access latest --secret foodcase-bearer-token --project matvaretabellen-b327)

if [ $? -ne 0 ]; then
    gcloud auth login
    gcloud auth application-default login
    export FC_BEARER=$(gcloud secrets versions access latest --secret foodcase-bearer-token --project matvaretabellen-b327)
fi

mv data/foodcase-food-nb.json data/foodcase-food-nb.old.json
mv data/foodcase-food-en.json data/foodcase-food-en.old.json
mv data/foodcase-data-nb.json data/foodcase-data-nb.old.json
mv data/foodcase-data-en.json data/foodcase-data-en.old.json
curl https://foodcase.prod.nfsa.foodcase-services.com/FoodCASE_WebAppMattilsynet/ws/dataexport/food_norwegian -H "Authorization: Bearer $FC_BEARER" | jq '.' > data/foodcase-food-nb.json
curl https://foodcase.prod.nfsa.foodcase-services.com/FoodCASE_WebAppMattilsynet/ws/dataexport/food_english -H "Authorization: Bearer $FC_BEARER" | jq '.' > data/foodcase-food-en.json
curl https://foodcase.prod.nfsa.foodcase-services.com/FoodCASE_WebAppMattilsynet/ws/dataexport/data_norwegian -H "Authorization: Bearer $FC_BEARER" | jq '.' > data/foodcase-data-nb.json
curl https://foodcase.prod.nfsa.foodcase-services.com/FoodCASE_WebAppMattilsynet/ws/dataexport/data_english -H "Authorization: Bearer $FC_BEARER" | jq '.' > data/foodcase-data-en.json
