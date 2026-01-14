locals {
  service_name = "matvaretabellen-next"
  fqdn = "2026.matvaretabellen.mattilsynet.io"
}

module "matvaretabellen-ui" {
  source = "git@github.com:Mattilsynet/map-tf-cloudrun?ref=v0.15.2"

  create_cloudrun_service_account_only = false
  service_name = local.service_name
  service_location = var.region
  service_project_id = var.project_id
  service_image = "gcr.io/cloudrun/hello"
  ignore_image = true

  replicas = {
    minScale = 1
    maxScale = 1
  }

  container_limits = {
    "cpu": "256m",
    "memory": "256Mi"
  }

  run_under_shared_lb = false
  allow_unauthenticated = true
  ingress = "internal-and-cloud-load-balancing"

  dedicated_lb = {
    managed_zone_name = "matvaretabellen-dns-managed-zone"
    fqdn = local.fqdn
  }
}

output "service" {
  value = local.service_name
}
