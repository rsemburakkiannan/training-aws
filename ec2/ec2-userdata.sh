#!/bin/bash

# get admin privileges
sudo su

# install httpd (Linux 2 version)
yum update -y
yum install -y httpd.x86_64
systemctl start httpd.service
systemctl enable httpd.service
echo "Hello World from Raghu - Host public IP: $(curl http://checkip.amazonaws.com)" > /var/www/html/index.html

#To get public IP
curl http://169.254.169.254/latest/meta-data/public-ipv4
curl http://checkip.amazonaws.com

