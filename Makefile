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
	clojure -X:build

tracer/target: tracer/src/matvaretabellen/*.clj
	cd tracer && clj -T:build uber

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

.PHONY: start-transactor docker publish test clean prepare-dev
