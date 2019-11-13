# Overview

Demonstration of MQTT Publish / Subscribe functionalities for AWS IoT.

It contains two projects :
- com.microej.demo.aws.iot :
	The demonstration code is located here, the entry point class is `com.microej.demo.aws.iot.Main`.
	It contains the launcher for the embedded mode (currently working on Wi-Fi boards).
- com.microej.demo.aws.iot-sim :
	Mock and launcher for the demonstration to work correctly in Simulator mode.

This example has been tested on Murata 1LD eval board with a MurataType1LD 1.0.0 MicroEJ platform.
	
# Requirements

- Install the MicroEJ SDK which can be found [here](http://developer.microej.com/getting-started-sdk.html) - section 1
- Add a platform with NET-1.1, SSL-2.1 and Wi-Fi support ECOM-WIFI-2.1

# Setup

You should already have cloned this repository in `[git.repo.dir]`.

First start the MicroEJ SDK on a new workspace `[workspace.dir]`.

Importing the Git repository in a MicroEJ SDK:

 - once started, import the Eclipse projects: `File` > `Import` > in `Projects from Git`, type and select `Existing local repository` > `Next` > `Add` > `Browse`
 and select the `[git.repo.dir]`> `Finish` > select your repo `Next` > `Import existing Eclipse projects` > import all the projects.
 - enable the Ivy Resolving in workspace `Window` > `Preferences` > `Ivy` > `Classpath Container` > check `Resolve dependencies in workspace`
 - after an Ivy Resolving, may take a while, the projects should compile (no red markers on the projects)

# Launching the AWS IoT Demo

## Getting ready with AWS IoT
- create AWS account through [AWS console](https://aws.amazon.com/console/)
- go to `IoT Core`
- go to `Secure` > `Policies`
- create a policy (it describes what your device will be able to do like subscribing and publishing)
- name it and configure it like this (copy/paste is available by clicking on `Advanced mode` of the `Add statements` section) :
	```
	{
	 "Version": "2012-10-17",
     "Statement": [
       {
         "Action": [
           "iot:Publish",
           "iot:Subscribe",
           "iot:Connect",
           "iot:Receive"
         ],
         "Effect": "Allow",
         "Resource": [
           "*"
         ]
       }
     ]
   }
   ```
- click on create
- go to `Manage`
- click on `Create`
- click on `Create a single thing`
- name your thing and click on `Next`
- choose the `One-click certificate creation (recommended)` option by clicking on `Create certificate`
- on the `Certificate created` page, download every certificates and keys
- click on the `Activate` button to enable the certificate authentication of your thing
- click on `Attach a policy`
- select the previously created policy

If you have any trouble, the AWS IoT full documentation can be found [here](https://docs.aws.amazon.com/iot/latest/developerguide/iot-console-signin.html)
 
## Getting ready with the certificates
- transform the private key like this using [OpenSSL](https://www.openssl.org/source/):
	`openssl.exe pkcs8 -inform PEM -in myprivate.pem.key -topk8 -outform DER -out myprivate.der -v1 PBE-SHA1-3DES -passout pass:awsdemo`
- add your private key and certificate in the folder
`[worspace.dir]/com.microej.demo.aws.iot/src/main/resources/certificates/device`
- add the paths to your private key and certificate in `[worspace.dir]/com.microej.demo.aws.iot/src/main/resources/aws.iot.demo.resources.list`
	```
	certificates/device/myprivate.der
	certificates/device/mycertificate.pem.crt
	```
- modify the properties file that will be used to initialize the SSL context, located at `[worspace.dir]/com.microej.demo.aws.iot/src/main/resources/certificates/aws.iot.demo.device.certificates.properties`
	```
	# the path of the device certificate
	certificate.file.name=/certificates/device/mycertificate.pem.crt
	# the path of the device private key (encoded with the previous openssl command)
	private.key.file.name=/certificates/device/myprivate.der
	# the password used in the previous openssl command (the part afer pass:) -passout pass:awsdemo
	keystore.password=awsdemo
	```
	
## Getting ready with the application configuration
- in order to find your broker host, go to your AWS IoT Console, click on `Manage` > `Things` and select your Thing previously created. Then click on `Interact` and the broker host is shown under the HTTPS section and should look like this : `{myowndomainid}.amazonaws.com`
- configure your information in `[worspace.dir]/com.microej.demo.aws.iot/src/main/java/com/microej/demo/aws/iot/Config.java` :
	- your MQTT AWS broker host and port :
		```
		public static final String AWS_BROKER_HOST = "myowndomainid.amazonaws.com";
		public static final int AWS_BROKER_PORT = 8883;
		```
	- your AWS thing id :
		```
		public static final String AWS_THING_ID = "myThing";
		```
	- your Wi-Fi credentials :
		```
		public static final String SSID = "my_wifi";
		public static final String PASSWORD = "passphrase";
		```

## Launching the demo in simulator mode
- Launch the Run Configuration `AWS IoT PubSub Demo [SIM]` to run it in our simulator
- Take a look at the console to see the traces of the running application

## Launching the demo on a Wi-Fi board
- Launch the Run Configuration `AWS IoT PubSub Demo [SIM]` to generate the binary `microejapp.o`
- Use the generated binary to flash your Wi-Fi board and use a serial console of your choice to see the traces of the running application

The traces should look like this :
```
[INFO] Device connected to the broker.
[INFO] Update listener added, we're now subscribed to the topic awsiot/demo/sample
[INFO] Sample data publishing timer task initialized.
[INFO] Message received on topic awsiot/demo/sample => MicroEJ
[INFO] Message received on topic awsiot/demo/sample => is
[INFO] Message received on topic awsiot/demo/sample => a
[INFO] Message received on topic awsiot/demo/sample => unique
[INFO] Message received on topic awsiot/demo/sample => solution
[INFO] Message received on topic awsiot/demo/sample => for
[INFO] Message received on topic awsiot/demo/sample => building
[INFO] Message received on topic awsiot/demo/sample => Internet
[INFO] Message received on topic awsiot/demo/sample => of
[INFO] Message received on topic awsiot/demo/sample => Things
[INFO] Message received on topic awsiot/demo/sample => and
[INFO] Message received on topic awsiot/demo/sample => embedded
[INFO] Message received on topic awsiot/demo/sample => software
[INFO] Message received on topic awsiot/demo/sample => and
[INFO] Message received on topic awsiot/demo/sample => can
[INFO] Message received on topic awsiot/demo/sample => now
[INFO] Message received on topic awsiot/demo/sample => communicate
[INFO] Message received on topic awsiot/demo/sample => with
[INFO] Message received on topic awsiot/demo/sample => AWS IoT
```

## AWS IoT dashboard
The AWS IoT console provides some tools to monitor the activity on the broker. 
- go in the `Monitor` section of the console to see graphs of successful connections to the broker and statistics on the messaging.
You can also subscribe on a topic through the console in order to see arriving messages from your device: 
- go to `Test`
- in the `Subscription topic` section, indicate the topic to subscribe to, here `awsiot/demo/sample`
- click on `Subscribe to topic`
- when the application is running, you should see messages displayed in the AWS IoT console

# References

- [MicroEJ Developer](https://developer.microej.com)
- [Ivy](https://ant.apache.org/ivy/)
- [AWS console](https://aws.amazon.com/console/)
- [AWS IoT documentation](https://docs.aws.amazon.com/iot/latest/developerguide/iot-console-signin.html) 
- [OpenSSL](https://www.openssl.org/source/)

---  
_Markdown_   
_Copyright 2018-2019 MicroEJ Corp. All rights reserved._   
_Use of this source code is governed by a BSD-style license that can be found with this software._   