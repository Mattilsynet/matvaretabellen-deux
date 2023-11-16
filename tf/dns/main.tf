resource "google_dns_managed_zone" "mvt_dns_zone" {
  name = "matvaretabellen-dns-zone"
  dns_name = "matvaretabellen.no."
  project = var.project_id
  description = "Matvaretabellen"
  dnssec_config {
    state = "on"
  }
}
