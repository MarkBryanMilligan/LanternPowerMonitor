# LanternPowerMonitor
The Lantern Power Monitor is a Raspberry Pi service, Java Web Service, and Android application that allow you to monitor every electrical breaker in your house, regardless of how many panels or breakers you have.
<br><br>
Here's an imgur album showing what this is and how it works:
<br>
[Lantern Power Monitor - Imgur](https://imgur.com/gallery/SPOJYBR)
<br><br>
The android application is available here:
<br>
[Lantern Power Monitor - Google Play](https://play.google.com/store/apps/details?id=com.lanternsoftware.lantern)
<br><br>
You can buy the parts to build a hub or a pre-built hub here:<br>
[Lantern Power Monitor - Etsy](https://www.etsy.com/shop/LanternPowerMonitor)<br>
If you don't want to buy one of these from me, you can use the bom in the source below to buy the parts yourself (highly recommended)

# Index
## bom
An excel file that lists every part required to construct a Lantern Power Monitor Hub and links to where each part can be purchased.<br>
I'd much prefer everyone use this bom to get their own parts.  Building kits is tedious.
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
[hub_1.0.0.zip](https://lanternsoftware.com:13781/hub_1.0.0.zip)<br>
Flash this to any micro sd card (4gig or larger) and you're good to go.  Fire up the hub and the phone app should be able to connect to it via bluetooth to finish the configuration.<br>
This image will post the data to lanternsoftware.com.  It's stored there securely and only you will have access to it; the data won't be shared or sold to anyone.  If you really want to run your own server, you're of course welcome to do that instead, instructions are located further down.

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
After you do all of this and have the service running, the app still won't be able to connect to the service via bluetooth due to a bug in bluez in the raspbian OS.<br>
To fix this, you must manually recompile bluez after changing some of the source. (Ready to just download that SD image yet?)<br>
In the file gatt-helpers.c, in the method bt_gatt_exchange_mtu, change the line<br>
```
id = bt_att_send(att, BT_ATT_OP_MTU_REQ, pdu, sizeof(pdu), mtu_cb, op, destroy_mtu_op);<br>
```
to<br>
```
id = bt_att_send(att, BT_ATT_OP_MTU_RSP, pdu, sizeof(pdu), mtu_cb, op, destroy_mtu_op);<br>
```
I need to submit this fix to the bluez project, but I haven't taken the time yet.

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

