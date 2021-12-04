# LanternPowerMonitor
The Lantern Power Monitor is a Raspberry Pi service, Java Web Service, and Android application that allow you to monitor every electrical breaker in your house, regardless of how many panels or breakers you have.
<br><br>
The official website has a lot of technical information:
<br>
[LanternPowerMonitor.com](https://lanternpowermonitor.com)
<br><br>
Here's an imgur album showing what this is and how it works:
<br>
[Lantern Power Monitor - Imgur](https://imgur.com/gallery/SPOJYBR)
<br><br>
The android application is available here:
<br>
[Lantern Power Monitor - Google Play](https://play.google.com/store/apps/details?id=com.lanternsoftware.lantern)
<br><br>
You can use the bom in the source below to buy the parts to build a hub.

# Index
## bom
An excel file that lists every part required to construct a Lantern Power Monitor Hub and links to where each part can be purchased.<br>

## case
STL files and Blender models to print your own case for a Lantern Power Monitor Hub
## currentmonitor
Contains all source code for the raspberry pi service and the web service.  The raspberry-pi service posts data to the web service which also handles requests for data from the android app.
#### lantern-currentmonitor
This is the raspberry pi service.
#### lantern-dataaccess-currentmonitor
A mongodb implementation of the data access for the current monitor service.
#### lantern-datamodel-currentmonitor
A shared data model used by the raspberry pi service and the web service
#### lantern-service-currentmonitor
This is the web service (compiles to a war and can most easily be deployed to tomcat)
## pcb
Gerber files and the source EasyEDA file for the Lantern Power Monitor Hub pcb.
## util
Utility libraries used by the raspberry pi service and the web service.
## zwave
This is only tangentially related.  A java library for running a zwave controller and a web service that the android app can use to control zwave devices in your home.  This isn't currently part of the hub implementation, but in the future the hubs will be able to run a zwave controller.

# Ok, how do I run this thing?
The easiest way to run the software on a hub is to download a pre-built SD card image.  One can be downloaded here:<br>
[hub_1.0.5.zip](https://lanternsoftware.com:13781/hub_1.0.5.zip)<br><br>
Flash this to any micro sd card (4gig or larger) and you're good to go.  Fire up the hub and the phone app should be able to connect to it via bluetooth to finish the configuration.  The default password on this image is pi/LanternPowerMonitor<br><br>
When you add the hub to your configuration via the app, you can change where the hub posts data.  If you use lanternsoftware.com (the default host), your data will be stored there securely and won't be shared with or sold to anyone.  If you really want to run your own server, you're of course welcome to do that instead, instructions are located further down.

## Now that the service is running on the pi, how do I configure everything in the android app?
1. Create your panel in the "Configure Panels" page from the main menu. Before you have your hub connected, there will be no place to select a hub and port for each breaker. Don't worry, we'll get to that later.

1. With your hub plugged in and running for at least 30 seconds or so, go into the "Configure Hubs" page from the main menu. In here you'll see a status of "Scanning for Hubs..." (if you're on at least 1.0.7 of the app). If you're in range of your hub and its service is running, the app should find it pretty quickly (less than 15 seconds). If this is the first hub you've added, it will prompt you for your wifi credentials. After that, it will send via bluetooth the hub index (so it knows which hub it is), host (so it knows where to post data), auth code (so it knows which account it is and can post data), the encrypted wifi credentials, and finally a command to reboot.

1. After your hub reboots, it should acquire an ip from your router, start the service, and try to start posting data. The hubs will try to auto-calibrate your voltage to 120V too, but if your AC/AC transformer is not plugged in, it will notice that and not try to auto-calibrate. It will continue to try to auto-calibrate each time you restart the hub until it does so succesfully.

1. Now that your hub is in the app, you need to go back into your panel and map each space to the port that you plugged the CT into. So say you put a CT on space 4, and you plugged that CT into port 7 on your hub, in your panel config, select space 4 and at the bottom, select hub 0 and port 7.

1. After you have your ports mapped, the hub does not currently pick up that change automatically. I will make it do that eventually, but for right now, you need to restart your hub one more time. (You can do this via bluetooth, but it's probably easier to just yank the power and plug it back in)

1. After that second restart, you should start seeing data. If you're not getting data in your app, the hub probably was not able to get a wifi connection. You can pull the last 10 lines of the log file and network details from the hub via bluetooth on the hub config page to troubleshoot.

# Ok, but I don't like doing things the easy way.
First, you and I will get along just fine.  Second, do a reactor build from the root folder:
```
~/LanternPowerMonitor$ mvn clean install
```
## raspberry pi service
The compiled service will be at LanternPowerMonitor/currentmonitor/lantern-currentmonitor/target/lantern-currentmonitor.jar<br>
This is a shaded jar that contains all of the required components to function.  It must be copied to /opt/currentmonitor on the pi.<br><br>

After that, you need to install wiring-pi:
```
sudo apt-get install wiringpi
```
You also need to have java 1.8 or newer installed.

Create a configuration file at /opt/currentmonitor/config.json<br>
Use the format below to get started
```
{
	"hub": 0,
	"host": "https://lanternsoftware.com/currentmonitor",
	"auto_calibration_voltage": 120.0,
	"needs_calibration": true
}
```
To install the current monitor service, use a service file like the one below:
```
[Unit]
Description=Current Monitor
After=syslog.target network.target

[Service]
Type=simple

User=root
Group=root

WorkingDirectory=/opt/currentmonitor

ExecStart=/opt/java/jdk1.8.0_231/bin/java -cp /opt/currentmonitor/lantern-currentmonitor.jar com.lanternsoftware.currentmonitor.MonitorApp

[Install]
WantedBy=multi-user.target
```
You may need to change the path of your jdk, but lantern-currentmonitor.jar must be installed at /opt/currentmonitor<br>
Move your currentmonitor.service file to /etc/systemd/system/ and enable the service
```
sudo mv currentmonitor.service /etc/systemd/system/
sudo systemctl enable currentmonitor
```
## web service
So you don't trust me with your data, eh?  No worries, I get it.  Here's how you run your own server.<br><br>
After your reactor build, the compiled war will be at LanternPowerMonitor/currentmonitor/lantern-service-currentmonitor/target/lantern-service-currentmonitor-1.x.x.war
That can be deployed to tomcat.  The 'host' parameter in the raspberry pi config.json file needs to point to wherever you deploy the service so your hubs post the data to your server instead of the official lantern software one.<br>
I'd recommend a valid dns entry and an ssl certificate, but, it's up to you, you're already knee deep in "I'll do what I want" territory here.<br><br>
Before you deploy it, you need to generate a config file that contains the mongodb credentials.<br>
There is a file at lantern-service-currentmonitor/src/test/java/com/lanternsoftware/currentmonitor/CreateMongoConfig.java that can do this for you.<br>
Place the generated config file in /opt/tomcat (which is where I have tomcat installed).  If you want it to be read from somewhere else, you can modify the paths in LanternFiles.java<br><br>
The last thing you need is a private aes key to encrypt user auth tokens.  One of those can be generated with CreateAuthKey.java.<br>
I realize these instructions aren't complete, but if you're going down this path, I suspect you sort of already know what you're doing, so hopefully that's enough to point you in the right direction.

