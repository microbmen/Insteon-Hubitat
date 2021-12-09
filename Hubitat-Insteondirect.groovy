/**
 *  Insteon Direct Dimmer/Switch
 *  Original Author     : ethomasii@gmail.com
 *  Creation Date       : 2013-12-08
 *
 *  Rewritten by        : idealerror
 *  Last Modified Date  : 2016-12-13 
 *
 *  Rewritten by        : kuestess
 *  Last Modified Date  : 2017-09-30
 * 
 *  Hubitat port by    @cwwilson08
 *  Last Modified Date  : 2018-09-09
 *
 *  Added Fast ON/OFF & Refresh setting in driver by     @cwwilson08   
 *  Last Modified Date  : 2018-09-14
 * 
 *  Added support for Express server for checking status @microbmen
 *
 *  Changelog:
 *  2021-12-09: adjusted for status to be recieved from express server :3000
 *  2018-10-06: Removed auto refresh in driver - Too many issues
 *  2018-10-01: Added ability to disable auto refresh in driver
 *  2018-09-14: Added Fast ON/OFF & Refresh setting in driver
 *  2018-09-09: Initial release for Hubitat Elevation Hub
 *  2016-12-13: Added polling for Hub2
 *  2016-12-13: Added background refreshing every 3 minutes
 *  2016-11-21: Added refresh/polling functionality
 *  2016-10-15: Added full dimming functions
 *  2016-10-01: Redesigned interface tiles
 */

import groovy.json.JsonSlurper

metadata {
    definition (name: "Insteon direct dimmer/switch (express status)", namespace: "MM", author: "microbmen") {
        capability "Switch Level"
        capability "Switch"
        capability "Refresh"
    }
}

def fon = [:]
    fon << ["true" : "True"]
    fon << ["false" : "False"]

preferences {
    input("deviceid", "text", title: "Device ID", description: "Your Insteon device.  Do not include periods example: FF1122.")
    input("host", "text", title: "URL", description: "The URL of your Hub (without http:// example: my.hub.com ")
    input("port", "text", title: "Port", description: "The hub port.")
    input("username", "text", title: "Username", description: "The hub username (found in app)")
    input("password", "text", title: "Password", description: "The hub password (found in app)")
    input(name: "fastOn", type: "enum", title: "FastOn", options: fon, description: "Use FastOn/Off?", required: true)
    input("expressserver", "text", title: "Express Server", description: "The IP of you Express Server")
    input("expressport", "text", title: "Express Port", description: "The port of your Express Server (ex. 3000)")
} 
 
// Not in use
def parse(String description) {
}

def on() {
    log.debug "Turning device ON"
    sendEvent(name: "switch", value: "on");
    sendEvent(name: "level", value: 100, unit: "%")
    if (fastOn == "false") {
    	sendCmd("11", "FF")
    } else {
        sendCmd("12", "FF")
    }
}

def off() {
    log.debug "Turning device OFF"
    sendEvent(name: "switch", value: "off");
    sendEvent(name: "level", value: 0, unit: "%")
    if (fastOn == "false") {
    	sendCmd("13", "00")
    } else {
        sendCmd("14", "00")
    }
}                    
                  
def setLevel(value) {

    // log.debug "setLevel >> value: $value"
    
    // Max is 255
    def percent = value / 100
    def realval = percent * 255
    def valueaux = realval as Integer
    def level = Math.max(Math.min(valueaux, 255), 0)
    if (level > 0) {
        sendEvent(name: "switch", value: "on")
    } else {
        sendEvent(name: "switch", value: "off")
    }
    // log.debug "dimming value is $valueaux"
    log.debug "setLevet(x) - dimming to ${level}. with value: ${value}"
    dim(level,value)
}

def setLevel(value,rate) {

    // log.debug "setLevel >> value: $value"
    
    // Max is 255
    def percent = value / 100
    def realval = percent * 255
    def valueaux = realval as Integer
    def level = Math.max(Math.min(valueaux, 255), 0)
    if (level > 0) {
        sendEvent(name: "switch", value: "on")
    } else {
        sendEvent(name: "switch", value: "off")
    }
    // log.debug "dimming value is $valueaux"
    log.debug "setLevel(x,y) - dimming to ${level}. with value: ${value} and rate ${rate} (rate not supported)"
    dim(level,value)
}

def dim(level, real) {
    String hexlevel = level.toString().format( '%02x', level.toInteger() )
    // log.debug "Dimming to hex $hexlevel"
    sendCmd("11",hexlevel)
    sendEvent(name: "level", value: real, unit: "%")
}

def sendCmd(num, level)
{
    log.debug "Sending Command"

    // Will re-test this later
    // sendHubCommand(new physicalgraph.device.HubAction("""GET /3?0262${settings.deviceid}0F${num}${level}=I=3 HTTP/1.1\r\nHOST: IP:PORT\r\nAuthorization: Basic B64STRING\r\n\r\n""", physicalgraph.device.Protocol.LAN, "${deviceNetworkId}"))
    httpGet("http://${settings.username}:${settings.password}@${settings.host}:${settings.port}//3?0262${settings.deviceid}0F${num}${level}=I=3") {response -> 
        def content = response.data
        
        // log.debug content
    }
    
    log.debug "Command Completed"
}

def refresh()
{
    log.debug "Refreshing.."
    getStatus()
}


def installed() {
	updated()
}

def updated() {}
       

def getStatus() {
    def buffer_status = runIn(2, getBufferStatus)
}

def getBufferStatus() {
    def buffer = ""
	def params = [
         uri: "http://${settings.expressserver}:${settings.expressport}/light/${settings.deviceid}/status"    
    ]
   
    log.debug "getBufferStatus URI: ${params}"
    
    try {
        httpGet(params) {resp ->
            buffer = "${resp.data}"
            log.debug "Buffer: ${buffer}"
       }
    } catch (e) {
        log.error "something went wrong when getting the response: $e"
        return 1
    }

     def status = buffer.substring(7,buffer.length()-1)
     log.debug "Status: ${status}"
 		
     //def level = Math.round(Integer.parseInt(status, 16)*(100/255))
     def level = Integer.parseInt(status, 10)
     log.debug "Level: ${level}"
          
     if (level == 0) {
     log.debug "Device is off..."
     sendEvent(name: "switch", value: "off")
     sendEvent(name: "level", value: level, unit: "%")
     }
     else if (level > 0) {
     log.debug "Device is on..."
     sendEvent(name: "switch", value: "on")
     sendEvent(name: "level", value: level, unit: "%")
     }
}
