resource "google_dns_managed_zone" "matvaretabellen_dns_zone" {
  name = "matvaretabellen-dns-managed-zone"
  dns_name = "matvaretabellen.no"
  project = var.project_id
  description = "Matvaretabellen"
  dnssec_config {
    state = "on"
  }
}

resource "google_dns_record_set" "matvaretabellen" {
  name = data.google_dns_managed_zone.matvaretabellen_dns_zone.dns_name
  managed_zone = data.google_dns_managed_zone.matvaretabellen_dns_zone.name
  type = "A"
  ttl = 300
  rrdatas = ["194.19.30.143"]
}

resource "google_dns_record_set" "www-matvaretabellen" {
  name = "www.${data.google_dns_managed_zone.matvaretabellen_dns_zone.dns_name}"
  managed_zone = data.google_dns_managed_zone.matvaretabellen_dns_zone.name
  type = "A"
  ttl = 300
  rrdatas = ["194.19.30.143"]
}
