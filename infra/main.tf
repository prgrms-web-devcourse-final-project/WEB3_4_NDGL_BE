terraform {
  required_providers {
    aws = {
      source = "hashicorp/aws"
    }
  }
}

provider "aws" {
  region = var.region
}

# 기본 VPC 및 서브넷
data "aws_vpc" "default" {
  default = true
}

data "aws_subnet" "selected" {
  id = "subnet-0d818841c8ccb16a7"
}

# 최신 Amazon Linux 2023 AMI
data "aws_ami" "latest_amazon_linux" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-2023.*-x86_64"]
  }

  filter {
    name   = "architecture"
    values = ["x86_64"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }

  filter {
    name   = "root-device-type"
    values = ["ebs"]
  }
}

# 일반 EC2 보안 그룹
resource "aws_security_group" "sg_1" {
  name        = "${var.prefix}-Security-Group"
  description = "Allow all traffic"
  vpc_id      = data.aws_vpc.default.id

  ingress {
    description = "HTTP"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "HTTP"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "SSH"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["172.30.1.28/32"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.prefix}-sg-1"
    Team = "devcos4-team01"
  }
}

# ELK 전용 보안 그룹
resource "aws_security_group" "elk_sg" {
  name        = "${var.prefix}-elk-sg"
  description = "Allow ELK stack access"
  vpc_id      = data.aws_vpc.default.id

  ingress {
    description = "Elasticsearch"
    from_port   = 9200
    to_port     = 9200
    protocol    = "tcp"
    cidr_blocks = [var.my_ip]
  }

  ingress {
    description = "Kibana"
    from_port   = 5601
    to_port     = 5601
    protocol    = "tcp"
    cidr_blocks = [var.my_ip]
  }

  ingress {
    description = "Logstash"
    from_port   = 5044
    to_port     = 5044
    protocol    = "tcp"
    cidr_blocks = [var.my_ip]
  }

  ingress {
    description = "SSH"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = [var.my_ip]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.prefix}-elk-sg"
  }
}

# EC2 IAM Role 및 인스턴스 프로파일
resource "aws_iam_role" "ec2_role_1" {
  name = "${var.prefix}-ec2-role-1"

  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Service": "ec2.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
EOF
}

resource "aws_iam_role_policy_attachment" "s3_full_access" {
  role       = aws_iam_role.ec2_role_1.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonS3FullAccess"
}

resource "aws_iam_role_policy_attachment" "ec2_ssm" {
  role       = aws_iam_role.ec2_role_1.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonEC2RoleforSSM"
}

resource "aws_iam_instance_profile" "instance_profile_1" {
  name = "${var.prefix}-instance-profile-1"
  role = aws_iam_role.ec2_role_1.name
}

# EC2에서 실행할 초기 스크립트
locals {
  ec2_user_data_base = <<-EOT
#!/bin/bash
# 스왑 설정
sudo dd if=/dev/zero of=/swapfile bs=128M count=32
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
sudo sh -c 'echo "/swapfile swap swap defaults 0 0" >> /etc/fstab'

# 도커 설치 및 실행
yum install docker -y
systemctl enable docker
systemctl start docker

docker network create common
EOT
}

# ELK용 ENI
resource "aws_network_interface" "elk_eni" {
  subnet_id       = data.aws_subnet.selected.id
  security_groups = [aws_security_group.elk_sg.id]
  private_ips     = [var.elk_private_ip]

  tags = {
    Name = "${var.prefix}-elk-eni"
  }
}

# ELK EC2 인스턴스
resource "aws_instance" "elk_server" {
  ami           = data.aws_ami.latest_amazon_linux.id
  instance_type = "t3.small"
  key_name      = "team01-api-server-key"

  network_interface {
    network_interface_id = aws_network_interface.elk_eni.id
    device_index         = 0
  }

  iam_instance_profile = aws_iam_instance_profile.instance_profile_1.name

  user_data = local.ec2_user_data_base

  root_block_device {
    volume_type = "gp3"
    volume_size = 30
  }

  tags = {
    Name = "${var.prefix}-elk-server"
  }
}

# 출력
output "elk_private_ip" {
  value = aws_network_interface.elk_eni.private_ip
}
