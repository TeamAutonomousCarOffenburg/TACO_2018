#open console with crtl+alt+f1
#!/bin/bash

sudo /etc/init.d/lightdm stop
sudo bash /home/aadc/taco/server/scripts/switchGCC5.sh
sudo /root/NVIDIA-Linux-x86_64-375.20.run --uninstall -s
#!óptional restart after uninstall --> sudo init 6
sudo /etc/init.d/lightdm stop
sudo /root/NVIDIA-Linux-x86_64-375.20.run -s --dkms
sudo bash /home/aadc/taco/server/scripts/switchGCC4.8.sh
sudo init 6
