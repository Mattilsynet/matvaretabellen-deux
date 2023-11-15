terraform {
  required_version = ">= 1.1.7"
  backend "gcs" {
    bucket = "tf-state-matvaretabellen-b327"
    prefix = "tf"
  }
}

provider "google" {
  region = "europe-north1"
  impersonate_service_account = "tf-admin-sa@matvaretabellen-b327.iam.gserviceaccount.com"
}

provider "google-beta" {
  region = "europe-north1"
  impersonate_service_account = "tf-admin-sa@matvaretabellen-b327.iam.gserviceaccount.com"
}
