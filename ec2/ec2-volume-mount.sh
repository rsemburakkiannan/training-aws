#List blocks
lsblk  

#If it has file system
sudo file -s /dev/xvdb

#To create the file system: xfs is format type, other formats are ext4, ext3
sudo mkfs -t xfs /dev/xvdb

#If you get an error that mkfs.xfs is not found
sudo yum install xfsprogs

#Create mount point directory for volumne
sudo mkdir /var/raghu

#Mount the volume to directory
sudo mount /dev/xvdb /var/raghu

# Add entry in /etc/fstab : After reboot the mount to be available
/dev/xvdb /data  xfs defaults,nofail 0 2

# Unmount the mounts
sudo umount /var/raghu

#Validate if fstab works?
sudo mount -a