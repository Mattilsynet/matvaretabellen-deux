locals {
  service_name = "matvaretabellen"
  fqdn = "${var.dns_zone_name}.mattilsynet.io"
}

resource "google_dns_managed_zone" "matvaretabellen_dns_zone" {
  name = "matvaretabellen-dns-managed-zone"
  dns_name = "matvaretabellen.no"
  project = var.project_id
  description = "Matvaretabellen"
  dnssec_config {
    state = "on"
  }
}

resource "google_dns_record_set" "dns" {
  name = data.google_dns_managed_zone.matvaretabellen_dns_zone.dns_name
  managed_zone = data.google_dns_managed_zone.matvaretabellen_dns_zone.name
  type = "A"
  ttl = 300
}

resource "google_dns_record_set" "dns" {
  name = "www.${data.google_dns_managed_zone.matvaretabellen_dns_zone.dns_name}"
  managed_zone = data.google_dns_managed_zone.matvaretabellen_dns_zone.name
  type = "A"
  ttl = 300
}

module "matvaretabellen-ui" {
  source = "git@github.com:Mattilsynet/map-tf-cloudrun?ref=v0.7.3"

  create_cloudrun_service_account_only = false
  service_name = local.service_name
  service_location = var.region
  service_project_id = var.project_id
  service_image = "gcr.io/cloudrun/hello"
  ignore_image = true

  replicas = {
    minScale = 1
    maxScale = 2
  }

  container_limits = {
    "cpu": "256m",
    "memory": "256Mi"
  }

  run_under_shared_lb = false
  allow_unauthenticated = true
  ingress = "internal-and-cloud-load-balancing"

  dedicated_lb = {
    managed_zone_name = "${var.dns_zone_name}-dns-managed-zone"
    fqdn = local.fqdn
  }
}

output "fqdn" {
  value = local.fqdn
}

output "service" {
  value = local.service_name
}
