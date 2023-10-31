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

target/public/js/compiled/app.js:
	clojure -M:build -m figwheel.main -bo prod

docker/build: target/public/js/compiled/app.js
	clojure -X:build

docker: docker/build
	cd docker && docker build -t $(IMAGE) .

publish:
	docker push $(IMAGE)

test:
	clojure -M:dev -m kaocha.runner

clean:
	rm -f datomic-pro-$(DATOMIC_VERSION).zip
	rm -fr target docker/build dev-resources/public/js/compiled

.PHONY: start-transactor docker publish test clean
