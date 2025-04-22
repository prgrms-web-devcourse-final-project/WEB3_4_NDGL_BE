variable "prefix" {
  description = "Prefix for all resources"
  default     = "devcos4-team01"
}

variable "region" {
  description = "region"
  default     = "ap-northeast-2"
}

variable "password" {
  description = "container's passwd"
  default     = "team01!admin"
}

variable "nickname" {
  description = "nickname"
  default     = "NRKim"
}

variable "my_ip" {
  type        = string
  description = "Your IP in CIDR format (e.g. 203.0.113.1/32)"
}

variable "elk_subnet_id" {
  type        = string
  description = "Subnet ID for ELK ENI"
}

variable "elk_private_ip" {
  type        = string
  description = "Fixed private IP for ELK ENI"
}