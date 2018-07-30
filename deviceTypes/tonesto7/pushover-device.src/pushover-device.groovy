/**
*   
*	File: Pushover_Driver.groovy
*	Platform: Hubitat
*   Modification History:
*       Date       Who            	What
*		2018-03-11 Dan Ogorchock  	Modified/Simplified for Hubitat
*		2018-03-23 Stephan Hackett	Added new preferences/features
*
*   Inspired by original work for SmartThings by: Zachary Priddy, https://zpriddy.com, me@zpriddy.com
*
*  Copyright 2018 Dan Ogorchock
*
*  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License. You may obtain a copy of the License at:
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
*  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
*  for the specific language governing permissions and limitations under the License.
*
*
*/
def version() {"v1.0.20180730"}

metadata {
    definition (name: "Pushover-Device", namespace: "tonesto7", author: "Dan Ogorchock") {
        capability "Notification"
        capability "Switch"
        capability "Actuator"
        capability "Speech Synthesis"

        attribute "deviceName", "string"
        attribute "lastMessage", "string"
        attribute "lastMessageDT", "string"
    }
    tiles(scale: 2) {
        multiAttributeTile(name: "mainTile", type: "generic", width: 6, height: 4) {
            tileAttribute("device.switch", key: "PRIMARY_CONTROL" ) {
                attributeState "on", label: 'Available', action: "switch.off", icon: "st.Office.office8", backgroundColor: "#00A7E1", nextState: "updating"
                attributeState "off", label: 'Unavailable', action: "switch.on", icon: "st.Office.office8", backgroundColor: "#7e7d7d", nextState:"updating"
                attributeState "updating", label:"Working"
            }
        }
        valueTile("lastMessage", "device.lastMessage", inactiveLabel: true, width: 6, height: 2, decoration: "flat", wordWrap: true) {
            state("default", label: 'Last Message:\n${currentValue}')
        }
        valueTile("lastMessageDT", "device.lastMessageDT", inactiveLabel: true, width: 3, height: 1, decoration: "flat", wordWrap: true) {
            state("default", label: 'Last Message Sent:\n${currentValue}')
        }
        valueTile("deviceName", "device.deviceName", inactiveLabel: true, width: 3, height: 1, decoration: "flat", wordWrap: true) {
            state("default", label: 'Device Name:\n${currentValue}')
        }
	}
    main "mainTile"
    details(["mainTile", "deviceName", "lastMessageDT", "lastMessage"])
}

preferences {
    // input("apiKey", "text", title: "API Key:", description: "Pushover API Key")
    // input("userKey", "text", title: "User Key:", description: "Pushover User Key")
    input("priority", "enum", title: "Default Message Priority (Blank = NORMAL):", description: "", defaultValue: "0", options:[["-1":"LOW"], ["0":"NORMAL"], ["1":"HIGH"]], submitOnChange: true)
    input("sound", "enum", title: "Notification Sound (Blank = App Default):", description: "", submitOnChange: true,
            options: [
                pushover: "Pushover (default)",
                bike: "Bike",
                bugle: "Bugle",
                cashregister: "Cash Register",
                classical: "Classical",
                cosmic: "Cosmic",
                falling: "Falling",
                gamelan: "Gamelan",
                incoming: "Incoming",
                intermission: "Intermission",
                magic: "Magic",
                mechanical: "Mechanical",
                pianobar: "Piano Bar",
                siren: "Siren",
                spacealarm: "Space Alarm",
                tugboat: "Tug Boat",
                alien: "Alien Alarm (long)",
                climb: "Climb (long)",
                persistent: "Persistent (long)",
                echo: "Pushover Echo (long)",
                updown: "Up Down (long)",
                none: "None (silent)"
            ])
    input("url", "text", title: "Supplementary URL:", description: "", submitOnChange: true)
    input("urlTitle", "text", title: "URL Title:", description: "", submitOnChange: true)
    input("retry", "number", title: "Retry Interval in seconds:(30 minimum)", description: "Applies to Emergency Requests Only", submitOnChange: true)
    input("expire", "number", title: "Auto Expire After in seconds:(10800 max)", description: "Applies to Emergency Requests Only", submitOnChange: true)
}

def installed() {
    initialize()
    sendEvent(name: 'switch', value: 'on', displayed: true, isStateChange: true)
}

def updated() {
    initialize()
}

def initialize() {
    def deviceName = getDeviceName()
    if(deviceName && isStateChange(device, "deviceName", deviceName.toString())) {
        sendEvent(name: 'deviceName', value: deviceName, displayed: true, isStateChange: true)
    }
    if(!device?.currentValue("switch")) { sendEvent(name: 'switch', value: "on", displayed: true, isStateChange: true) }
}

void updDataValue(key, val) {
    if(key && val) { 
        updateDataValue("$key", "$val")
        state?."$key" = val
    }
}

String getApiKey() { return getDataValue("apiKey") ?: null }
String getUserKey() { return getDataValue("userKey") ?: null }
String getDeviceName() { return getDataValue("deviceName") ?: null }

def on() {
	sendEvent(name: 'switch', value: 'on', displayed: true, isStateChange: true)
}

def off() {
	sendEvent(name: 'switch', value: 'off', displayed: true, isStateChange: true)
}

def refresh() {
	log.trace "refresh command received..."
}

def speak(String message) {
    deviceNotification(message)
}

def customNotification() {

}

def deviceNotification(String message, addTs=false) {
    if(device?.currentState("switch")?.value == "off") { 
        log.warn "Unable to Send Notification | Device is Current Off... Ignoring!!!"
        return
    }
    String apiKey = getApiKey()
    String userKey = getUserKey()
    String deviceName = getDeviceName()
    if(message != null && message?.length() > 0 && deviceName && apiKey && userKey) {
        String msgPriority = settings?.priority ?: "0"
        if(message?.startsWith("[L]")) { 
            msgPriority = "-1"
            message = message?.minus("[L]")
        } else if(message?.startsWith("[N]")) {
            msgPriority = "0"
            message = message?.minus("[N]")
        } else if(message?.startsWith("[H]")) {
            msgPriority = "1"
            message = message?.minus("[H]")
        } else if(message?.startsWith("[E]")) {
            msgPriority = "2"
            message = message?.minus("[E]")
        }
        log.debug "Sending Message: ${message} | Priority: (${msgPriority}) | Device: (${deviceName})"

        // Define the initial postBody keys and values for all messages
        Map params = [
            uri: "https://api.pushover.net/1/messages.json",
            contentType: "application/json",
            body: [
                token: apiKey?.trim(),
                user: userKey?.trim(),
                message: message,
                priority: msgPriority,
                device: deviceName,
                retry: settings?.retry ?: 30,
                expire: settings?.expire ?: 10800
            ]
        ]
        if(settings?.sound) {
            params?.body?.sound = settings?.sound
        }
        if(settings?.url) {
            params?.body?.url = settings?.url
            params?.body?.url_title = settings?.urlTitle
        }
        if(addTs) {
            params?.body?.timestamp = new Date().getTime()
        }
        if ((apiKey =~ /[A-Za-z0-9]{30}/) && (userKey =~ /[A-Za-z0-9]{30}/)) {
            try {
                httpPostJson(params) { resp ->
                    if(resp?.status == 200) {
                        log.debug "Message Received by Pushover Server"
                        sendEvent(name: 'lastMessage', value: message, displayed: true, isStateChange: true)
                        sendEvent(name: 'lastMessageDT', value: formatDt(new Date()), displayed: true, isStateChange: true)
                    } else {
                        sendPush("ERROR: 'Pushover Me When' received HTTP error ${resp?.status}. Check your keys!")
                        log.error "Received HTTP error ${resp?.status}. Check your keys!"
                    }
                }
            } catch (ex) {
                if(ex instanceof groovyx.net.http.HttpResponseException && ex?.response) {
                    log.error "deviceNotification() HttpResponseException | Status: (${ex?.response?.status}) | Data: ${ex?.response?.data}"
                } else {
                    log.error "deviceNotification Exception: ${ex?.message}" 
                }
            } 
        } else {
            log.error "API key '${apiKey}' or User key '${userKey}' is not properly formatted!"
        }
    } else {
        if(!apiKey) {
            log.warn "Unable to Send Notification | ApiKey Missing... Skipping!!!"
        } 
        if(!userKey) {
            log.warn "Unable to Send Notification | UserKey Missing... Skipping!!!"
        }
        if(message == null) {
            log.warn "Unable to Send Notification | Message Content Empty... Skipping!!!"
        }
        if(!message?.length() > 0) {
            log.warn "Unable to Send Notification | Message Length is too Short... Skipping!!!"
        }
        if(!getDeviceName()) {
            log.warn "Unable to Send Notification | DeviceName is Missing... Skipping!!!"
        }
    }
}

def getDtNow() {
	def now = new Date()
	return formatDt(now, false)
}

def formatDt(dt, mdy = true) {
	def formatVal = mdy ? "MMM d, yyyy - h:mm:ss a" : "E MMM dd HH:mm:ss z yyyy"
	def tf = new java.text.SimpleDateFormat(formatVal)
	if(location?.timeZone) { tf.setTimeZone(location?.timeZone) }
	return tf.format(dt)
}