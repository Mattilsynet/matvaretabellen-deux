DATOMIC_VERSION=1.0.6735

datomic-transactor:
	curl https://datomic-pro-downloads.s3.amazonaws.com/$(DATOMIC_VERSION)/datomic-pro-$(DATOMIC_VERSION).zip
	unzip datomic-pro-$(DATOMIC_VERSION).zip -d datomic-transactor
	cp datomic-transactor/datomic-pro-$(DATOMIC_VERSION)/config/samples/dev-transactor-template.properties datomic-transactor/datomic-pro-$(DATOMIC_VERSION)/config/transactor.properties
	sed -i '' 's|# data-dir=data|data-dir=../../datomic-data|' datomic-transactor/datomic-pro-$(DATOMIC_VERSION)/config/transactor.properties

start-transactor: datomic-transactor
	cd datomic-transactor/datomic-pro-$(DATOMIC_VERSION) && ./bin/transactor config/transactor.properties

test:
	clojure -M:dev -m kaocha.runner

clean:
	rm datomic-pro-$(DATOMIC_VERSION).zip

.PHONY: test start-transactor
