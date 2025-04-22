data "aws_vpc" "default" {
  default = true
}

data "aws_subnet" "elk_subnet" {
  id = var.elk_subnet_id
}

resource "aws_network_interface" "elk_eni" {
  subnet_id       = data.aws_subnet.elk_subnet.id
  security_groups = [aws_security_group.elk_sg.id]
  private_ips     = [var.elk_private_ip]

  tags = {
    Name = "${var.prefix}-elk-eni"
  }
}