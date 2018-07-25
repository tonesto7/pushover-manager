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
def version() {"v1.0.20180725"}

metadata {
    definition (name: "Pushover-Device", namespace: "tonesto7", author: "Dan Ogorchock") {
        capability "Notification"
        capability "Switch"
        capability "Actuator"
        capability "Speech Synthesis"

        attribute "deviceName", "string"
    }
    tiles(scale: 2) {
        multiAttributeTile(name: "mainTile", type: "generic", width: 6, height: 4) {
            tileAttribute("device.switch", key: "PRIMARY_CONTROL" ) {
                attributeState "on", label: 'Available', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00A7E1", nextState: "updating"
                attributeState "off", label: 'Unavailable', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#7e7d7d", nextState:"updating"
                attributeState "updating", label:"Working"
            }
            tileAttribute("device.deviceName", key: "SECONDARY_CONTROL") {
                attributeState("deviceName", label:'${currentValue}', defaultState: true)
            }
        }
	}
    main "mainTile"
    details(["mainTile"])
}

preferences {
    // input("apiKey", "text", title: "API Key:", description: "Pushover API Key")
    // input("userKey", "text", title: "User Key:", description: "Pushover User Key")
    input("priority", "enum", title: "Default Message Priority (Blank = NORMAL):", description: "", defaultValue: "0", options:[["-1":"LOW"], ["0":"NORMAL"], ["1":"HIGH"]], submitOnChange: true)
    input("sound", "enum", title: "Notification Sound (Blank = App Default):", description: "", options: parent?.getSoundOptions(), submitOnChange: true)
    input("url", "text", title: "Supplementary URL:", description: "", submitOnChange: true)
    input("urlTitle", "text", title: "URL Title:", description: "", submitOnChange: true)
    input("retry", "number", title: "Retry Interval in seconds:(30 minimum)", description: "Applies to Emergency Requests Only", submitOnChange: true)
    input("expire", "number", title: "Auto Expire After in seconds:(10800 max)", description: "Applies to Emergency Requests Only", submitOnChange: true)
}

def installed() {
    initialize()
}

def updated() {
    initialize()   
}

def initialize() {
    state.version = version()
    if(isStateChange(device, "deviceName", getDeviceName().toString())) {
        sendEvent(name: 'deviceName', value: getDeviceName(), displayed: true, isStateChange: true)
    }
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
	state?.deviceDisabled = false
}

def off() {
	state?.deviceDisabled = true
}

def refresh() {
	
}

def speak(String message) {
    deviceNotification(message)
}

def deviceNotification(String message) {
    if(state?.deviceDisabled == true) { 
        log.debug "Message is ignored while device is Disabled..."
        return
    }
    def apiKey = getApiKey()
    def userKey = getUserKey()
    if(message != null && message?.size() > 0 && apiKey && userKey) {
        def msgPriority = settings?.priority ?: "0"
        def deviceName = getDeviceName()
        if(message.startsWith("[L]")) { 
            msgPriority = "-1"
            message = message.minus("[L]")
        } else if(message.startsWith("[N]")) {
            msgPriority = "0"
            message = message.minus("[N]")
        } else if(message.startsWith("[H]")) {
            msgPriority = "1"
            message = message.minus("[H]")
        } else if(message.startsWith("[E]")) {
            msgPriority = "2"
            message = message.minus("[E]")
        }
        // Define the initial postBody keys and values for all messages
        def postBody = [
            token: "${apiKey}",
            user: "${userKey}",
            message: "${message}",
            priority: msgPriority,
            sound: settings?.sound,
            url: settings?.url,
            device: "${deviceName}",
            url_title: settings?.urlTitle,
            retry: settings?.retry,
            expire: settings?.expire,
            //timestamp: new Date().getTime()
        ]

        if (deviceName) { 
            log.debug "Sending Message: ${message} Priority: (${settings?.priority}) to Device: (${deviceName})"
        } else { log.debug "Sending Message: [${message}] Priority: [${settings?.priority}] to [All Devices]" }

        // Prepare the package to be sent
        def params = [
            uri: "https://api.pushover.net/1/messages.json",
            contentType: "application/json",
            body: postBody
        ]

        if ((apiKey =~ /[A-Za-z0-9]{30}/) && (userKey =~ /[A-Za-z0-9]{30}/)) {
            try {
                httpPostJson(params) { response ->
                    if(resp?.status == 200) {
                        log.debug "Message Received by Pushover Server"
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
    } else { log.warn "Invalid/Missing Message Content Received... Skipping!!!"}
}
