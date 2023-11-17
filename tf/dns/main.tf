resource "google_dns_managed_zone" "mvt_dns_zone" {
  name = "matvaretabellen-dns-zone"
  dns_name = "matvaretabellen.no."
  project = var.project_id
  description = "Matvaretabellen"
  dnssec_config {
    state = "on"
  }
}

resource "google_dns_record_set" "matvaretabellen" {
  name = "${resource.google_dns_managed_zone.mvt_dns_zone.dns_name}"
  managed_zone = resource.google_dns_managed_zone.mvt_dns_zone.name
  type = "A"
  ttl = 300
  project = var.project_id
  rrdatas = ["194.19.30.143"]
}

resource "google_dns_record_set" "www-matvaretabellen" {
  name = "www.${resource.google_dns_managed_zone.mvt_dns_zone.dns_name}"
  managed_zone = resource.google_dns_managed_zone.mvt_dns_zone.name
  type = "A"
  ttl = 300
  project = var.project_id
  rrdatas = ["194.19.30.143"]
}

resource "google_dns_record_set" "empty_spf1_txt_record" {
  name = "${resource.google_dns_managed_zone.mvt_dns_zone.dns_name}"
  type = "TXT"
  ttl = 300
  managed_zone = resource.google_dns_managed_zone.mvt_dns_zone.name
  rrdatas = ["v=spf1 -all"]
}
