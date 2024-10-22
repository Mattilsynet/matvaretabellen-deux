DATOMIC_VERSION = 1.0.6735
VERSION = $$(git rev-parse --short=10 HEAD)
IMAGE = europe-north1-docker.pkg.dev/artifacts-352708/mat/matvaretabellen:$(VERSION)

datomic-transactor:
	curl https://datomic-pro-downloads.s3.amazonaws.com/$(DATOMIC_VERSION)/datomic-pro-$(DATOMIC_VERSION).zip
	unzip datomic-pro-$(DATOMIC_VERSION).zip -d datomic-transactor
	cp datomic-transactor/datomic-pro-$(DATOMIC_VERSION)/config/samples/dev-transactor-template.properties datomic-transactor/datomic-pro-$(DATOMIC_VERSION)/config/transactor.properties
	sed -i '' 's|# data-dir=data|data-dir=../../datomic-data|' datomic-transactor/datomic-pro-$(DATOMIC_VERSION)/config/transactor.properties

start-transactor: datomic-transactor
	cd datomic-transactor/datomic-pro-$(DATOMIC_VERSION) && ./bin/transactor config/transactor.properties

ui/resources/fontawesome-icons:
	clojure -Sdeps "{:deps {no.cjohansen/fontawesome-clj {:mvn/version \"2023.10.26\"} \
	clj-http/clj-http {:mvn/version \"3.12.3\"} \
	hickory/hickory {:mvn/version \"0.7.1\"}}}" \
	-M -m fontawesome.import :download ui/resources 6.4.2

target/public/js/compiled/app.js: ui/resources/fontawesome-icons
	clojure -M:build -m figwheel.main -bo prod

docker/build: target/public/js/compiled/app.js ui/resources/fontawesome-icons
	env GIT_SHA=$(VERSION) clojure -X:build

tracer/target: tracer/src/matvaretabellen/*.clj
	cd tracer && clojure -T:build uber

docker/tracer.jar: tracer/target
	cp tracer/target/tracer.jar docker

docker: docker/build docker/tracer.jar
	cd docker && docker build -t $(IMAGE) .

publish:
	docker push $(IMAGE)

test: ui/resources/fontawesome-icons
	clojure -M:dev -m kaocha.runner

prepare-dev: ui/resources/fontawesome-icons

clean:
	rm -f datomic-pro-$(DATOMIC_VERSION).zip
	rm -fr target docker/build dev-resources/public/js/compiled tracer/target docker/tracer.jar

import-foodcase:
	export FC_BEARER=$(gcloud secrets versions access latest --secret foodcase-bearer-token --project matvaretabellen-b327)
	curl https://foodcase.prod.nfsa.foodcase-services.com/FoodCASE_WebAppMattilsynet/ws/dataexport/food_norwegian -H "Authorization: Bearer $$FC_BEARER" | jq '.' > data/foodcase-food-nb.json
	curl https://foodcase.prod.nfsa.foodcase-services.com/FoodCASE_WebAppMattilsynet/ws/dataexport/food_english -H "Authorization: Bearer $$FC_BEARER" | jq '.' > data/foodcase-food-en.json
	curl https://foodcase.prod.nfsa.foodcase-services.com/FoodCASE_WebAppMattilsynet/ws/dataexport/data_norwegian -H "Authorization: Bearer $$FC_BEARER" | jq '.' > data/foodcase-data-nb.json
	curl https://foodcase.prod.nfsa.foodcase-services.com/FoodCASE_WebAppMattilsynet/ws/dataexport/data_english -H "Authorization: Bearer $$FC_BEARER" | jq '.' > data/foodcase-data-en.json

.PHONY: start-transactor docker publish test clean prepare-dev import-foodcase
