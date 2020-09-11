#!/bin/bash

yum update -y
yum install -y httpd
systemctl start httpd.service
systemctl enable httpd.service
EC2_AVAIL_ZONE=$(curl -s http://169.254.169.254/latest/meta-data/placement/availability-zone)
EC2_IP_ADDR=$(curl http://checkip.amazonaws.com)
echo "<h1>Hello World from Raghu - Host Public IP: $EC2_IP_ADDR in AZ $EC2_AVAIL_ZONE </h1>" > /var/www/html/index.html
