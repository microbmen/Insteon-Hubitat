# Insteon-Hubitat

Hubitat driver for direct control of insteon devices

Updated the code to use Express Server for Insteon status.  Works great if you are using Homebridge and the Insteon Local plugin which has the express server running. Just point to your Homebridge server on port 3000 and status will work.

microbmen @ gmail.com

--------------------------------------------

Info on Express Server from - https://github.com/kuestess/homebridge-platform-insteonlocal#readme

Express Server
This plugin will set up a local Express server at the port specified in your config.json (see below) that can also be accessed via a browser to get or manipulate Insteon device status and view hub or device information. Endpoints for the Express sever include:

/light/[id]/on: turn on the light with Insteon [id]
/light/[id]/off: turn off the light with Insteon [id]
/light/[id]/status: get status of the light with Insteon [id]
/light/[id]/level/[targetlevel]: set brightness of the light with Insteon [id] to [targetlevel]
/scene/[group]/on: turn on the scene with Insteon [group] number
/scene/[group]/off: turn off the scene with Insteon [group] number
/iolinc/[id]/relay_on: turn on the relay for iolinc with Insteon [id]
/iolinc/[id]/relay_off: turn off the relay for iolinc with Insteon [id]
/iolinc/[id]/relay_status: get status of the relay for iolinc with Insteon [id]
/iolinc/[id]/sensor_status: get status of the sensor for iolinc with Insteon [id]
/links: get all links from your Insteon Hub
/links/[id]: get links for device with Insteon [id]
/info/[id]: get info for device with Insteon [id]



