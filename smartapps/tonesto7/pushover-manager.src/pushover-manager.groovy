/**
 *  Pushover-Manager
 *
 *  Copyright 2018 Anthony Santilli
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
 */

def appVer() {"v1.0.20180730"}

definition(
    name: "Pushover-Manager",
    namespace: "tonesto7",
    author: "Anthony Santilli",
    description: "Creates and Manages Pushover devices",
    category: "My Apps",
    iconUrl: "https://pushover.net/images/icon-72.png",
    iconX2Url: "https://pushover.net/images/icon-256.png",
    iconX3Url: "https://pushover.net/images/icon-512.png")


preferences {
    page(name: "mainPage")
    page(name: "messageTest")
}

def appInfoSect()	{
    section() {
        def str = ""
        str += "${app?.name}"
        str += "\nVersion: ${appVer()}"
        paragraph str, image: "https://pushover.net/images/icon-512.png"
    }
}

def mainPage() {
	return dynamicPage(name: "mainPage", title: "", install: true, uninstall: true) {
        appInfoSect()
        def validated = (apiKey && userKey && getValidated())
        def devices = validated ? getValidated(true) : []
        section("API Authentication: (${validated ? "Good" : "Missing"})", hidden: validated, hideable: true) {
            input "apiKey", "text", title: "API Key:", description: "Pushover API Key", required: true, submitOnChange: true
            input "userKey", "text", title: "User Key:", description: "Pushover User Key", required: true, submitOnChange: true
        }
        if(validated) {
            section("Device Management:") {
                // log.debug "devices: ${getValidated("deviceList")}"
                paragraph title: "What are these?", "A device will be created for each device selected below..."
                input "pushDevices", "enum", title: "Select PushOver Clients", description: "", multiple: true, required: true, options: devices, submitOnChange: true
            }
            section("Test Notifications:", hideable: true, hidden: true) {
                input "testDevices", "enum", title: "Select Devices", description: "Select Devices to Send Test Notification Too...", multiple: true, required: false, options: settings?.pushDevices, submitOnChange: true
                if(settings?.testDevices) {
                    input "testMessage", "text", title: "Test Message to Send:", description: "Enter message to send...", required: false, submitOnChange: true
                    if(settings?.testMessage && settings?.testMessage?.length() > 0) {
                        href "messageTest", title: "Send Message", description: ""
                    }
                }
            }
        }
        state?.testMessageSent = false
    }
}

def messageTest() {
    return dynamicPage(name: "messageTest", title: "Notification Test", install: false, uninstall: false) {
        section() {
            if(state?.testMessageSent == true) {
                paragraph "Message Already Sent... Go Back to send again..."
            } else {
                paragraph "Sending ${settings?.testMessage} to ${settings?.testDevices}" 
                sendTestMessage()
            }
            state?.testMessageSent = true
        }
    }
}

def sendTestMessage() {
    app?.getChildDevices(true)?.each { dev->
        if(dev?.getDeviceName()?.toString() in settings?.testDevices) {
            log.debug "sending test message to ${dev?.displayName}"
            dev?.deviceNotification(settings?.testMessage as String)
        }
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"  
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	initialize()
}

def initialize() {
    unsubscribe()
    addRemoveDevices()
    updateDevices()
    subscribe(location, "pushoverManagerMsg", locMessageHandler)
    sendLocationEvent(name: "pushoverManager", value: "refresh", data: [devices: state?.pushoverDevices ?: [], sounds: getSoundOptions()] , isStateChange: true, descriptionText: "Pushover Manager Device List Refresh")
}

def uninstalled() {
    log.warn "Uninstalled called... Removing all Devices..."
    addRemoveDevices(true)
}

// def getDeviceMap() {
//     Map items = [:]
//     state?.pushoverDevices?.each {
//         items["$"]
//     }
// }

def getValidated(devList=false){
    def validated = false
    def params = [
        uri: "https://api.pushover.net/1/users/validate.json",
        contentType: "application/json",
        body: [
            token: "$apiKey",
            user: "$userKey",
            device: ""
        ]
    ]
    def deviceOptions
    if ((apiKey =~ /[A-Za-z0-9]{30}/) && (userKey =~ /[A-Za-z0-9]{30}/)) {
        try {
            httpPost(params) { resp ->
                // log.debug "response: ${resp.status}"
                if(resp?.status != 200) {
                    // sendPush("ERROR: 'Pushover Me When' received HTTP error ${resp?.status}. Check your keys!")
                    log.error "Received HTTP error ${resp.status}. Check your keys!"
                } else {
                    if(devList) {
                        if(resp?.data && resp?.data?.devices) {
                            log.debug "Found (${resp?.data?.devices?.size()}) Pushover Devices..."
                            deviceOptions = resp?.data?.devices
                            state?.pushoverDevices = resp?.data?.devices
                        } else { 
                            log.error "Device List is empty"
                            state?.pushoverDevices = []
                        }
                    } else {
                        // log.debug "Keys Validated..."
                        validated = true
                    }
                }
            }
        } catch (Exception ex) {
            if(ex instanceof groovyx.net.http.HttpResponseException && ex?.response) {
                log.error "getValidated() HttpResponseException | Status: (${ex?.response?.status}) | Data: ${ex?.response?.data}"
            } else {
                log.error "An invalid key was probably entered. PushOver Server Returned: ${ex}"
            }
        } 
    } else {
        log.error "API key '${apiKey}' or User key '${userKey}' is not properly formatted!"
    }
    return devList ? deviceOptions : validated
}

def getSoundOptions() {
    log.debug "Generating Sound Notification List..."
    def myOptions = [:]
    try {
        httpGet(uri: "https://api.pushover.net/1/sounds.json?token=${settings?.apiKey}") { resp ->
            if(resp?.status == 200) {
                log.debug "Found (${resp?.data?.sounds?.size()}) Sounds..."
                def mySounds = resp?.data?.sounds
                // log.debug "mySounds: $mySounds"
                mySounds?.each { snd->
                    myOptions["${snd?.key}"] = snd?.value
                }
            } else {
                sendPush("ERROR: 'Pushover Me When' received HTTP error ${resp?.status}. Check your keys!")
                log.error "Received HTTP error ${resp?.status}. Check your keys!"
            }
        }
    } catch (Exception ex) {
        if(ex instanceof groovyx.net.http.HttpResponseException && ex?.response) {
            log.error "getSoundOptions() HttpResponseException | Status: (${ex?.response?.status}) | Data: ${ex?.response?.data}"
        }
    }
    state?.soundOptions = myOptions
    return myOptions
}

def getDeviceDni(devName) {
    return "pushover_device_${devName}"
}

def getDeviceLabel(devName) {
    return "Pushover - ${devName}"
}

def addRemoveDevices(uninst=false) {
    //log.trace "addRemoveDevices($uninst)..."
    try {
        def delete = []
        if(uninst == false) {
            List devsInUse = []
            List selectedDevices = settings?.pushDevices
            selectedDevices?.each { dev ->
                String dni = getDeviceDni(dev)
                def d = getChildDevice(dni)
                if(!d) {
                    d = addChildDevice("tonesto7", "Pushover-Device", dni, null, [label: getDeviceLabel(dev), data: [apiKey: settings?.apiKey, userKey: settings?.userKey, deviceName: dev] ])
                    d.completedSetup = true
                    log.info "PushOver Device Created: (${d?.displayName}) with id: [${dni}]"
                } else {
                    // log.debug "found ${d?.displayName} with dni: ${dni} already exists"
                }
                devsInUse?.push(dni as String)
            }
            delete = app.getChildDevices(true)?.findAll { !(it?.deviceNetworkId?.toString() in devsInUse) }
        } else {
            delete = app.getChildDevices(true)
        }
        if(delete?.size() > 0) {
            log.warn "Device Delete: ${delete} | Removing (${delete?.size()}) Devices..."
            delete?.each {
                deleteChildDevice(it?.deviceNetworkId, true)
                log.warn "Deleted the Device: [${it?.displayName}]"
            }
        }
        return true
    } catch (ex) {
        if(ex instanceof physicalgraph.exception.ConflictException) {
            def msg = "Error: Can't Delete App because Devices are still in use in other Apps, Routines, or Rules.  Please double check before trying again."
            log.warn "addRemoveDevices Exception | $msg"
        } else if(ex instanceof physicalgraph.app.exception.UnknownDeviceTypeException) {
            def msg = "Error: Device Handlers are likely Missing or Not Published.  Please verify all device handlers are present before continuing."
            log.warn "addRemoveDevices Exception | $msg"
        } else { log.error "addRemoveDevices Exception: ${ex}" }
        return false
    }
}

def updateDevices() {
    app?.getChildDevices(true)?.each { dev->
        dev?.updDataValue("apiKey", settings?.apiKey)
        dev?.updDataValue("userKey", settings?.userKey)
    }
}

def locMessageHandler(evt) {
    log.debug "locMessageHandler: ${evt?.jsonData}"
    if (!evt) return
    switch (evt?.value) {
        case "send":
            List pushDevices = []
            if (evt?.jsonData && evt?.jsonData?.devices && evt?.jsonData?.msgData?.size()) {
                evt?.jsonData?.devices?.each { nd->
                    pushoverNotification(nd as String, evt?.jsonData?.msgData, evt?.jsonData?.image)
                }
            }
            break
    }
}

def pushoverNotification(deviceName, msgData, imageData) {
    log.debug "pushoverNotification($deviceName, $msgData, $imageData)"
    if(deviceName && msgData) {
        if(msgData?.message != null && msgData?.message?.length() > 0 && deviceName && settings?.apiKey && settings?.userKey) {
            String msgPriority = msgData?.priority ?: "0"
            if(msgData?.message?.startsWith("[L]")) { 
                msgPriority = "-1"
                msgData?.message = msgData?.message?.minus("[L]")
            } else if(msgData?.message?.startsWith("[N]")) {
                msgPriority = "0"
                msgData?.message = msgData?.message?.minus("[N]")
            } else if(msgData?.message?.startsWith("[H]")) {
                msgPriority = "1"
                msgData?.message = msgData?.message?.minus("[H]")
            } else if(msgData?.message?.startsWith("[E]")) {
                msgPriority = "2"
                msgData?.message = msgData?.message?.minus("[E]")
            }
            // log.debug "Sending Message: ${msgData?.message} | Priority: (${msgPriority}) | Device: (${deviceName})"

            // Define the initial postBody keys and values for all messages
            Map params = [
                uri: "https://api.pushover.net/1/messages.json",
                contentType: "application/json",
                body: [
                    token: settings?.apiKey?.trim(),
                    user: settings?.userKey?.trim(),
                    message: msgData?.message,
                    priority: msgPriority,
                    device: deviceName,
                    retry: msgData?.retry ?: 30,
                    expire: msgData?.expire ?: 10800
                ]
            ]
            if(msgData?.sound) {
                params?.body?.sound = msgData?.sound
            }
            if(msgData?.url) {
                params?.body?.url = msgData?.url
                params?.body?.url_title = msgData?.urlTitle
            }
            if(msgData?.timestamp) {
                params?.body?.timestamp = msgData?.timestamp
            }
            if(msgData?.image) {
                params?.body?.attachment = msgData?.image
            }
            if(msgData?.title) {
                params?.body?.title = msgData?.title
            }
            if ((settings?.apiKey =~ /[A-Za-z0-9]{30}/) && (settings?.userKey =~ /[A-Za-z0-9]{30}/)) {
                try {
                    httpPostJson(params) { resp ->
                        def limit = resp?.getHeaders("X-Limit-App-Limit")
                        def remain = resp?.getHeaders("X-Limit-App-Remaining")
                        def resetDt = resp?.getHeaders("X-Limit-App-Reset")
                        if(resp?.status == 200) {
                            log.debug "Message Received by Pushover Server | Monthly Messages Remaining (${remain?.value[0]} of ${limit?.value[0]})"
                            state?.lastMessage = msgData?.message
                            state?.lastMessageDt = formatDt(new Date())
                        } else if (resp?.status == 429) { 
                            log.warn "Can't Send Notification... You have reached your (${limit?.value[0]}) notification limit for the month"
                        } else {
                            sendPush("pushoverNotification ERROR: 'Pushover' received HTTP error ${resp?.status}. Check your keys!")
                            log.error "Received HTTP error ${resp?.status}. Check your keys!"
                        }
                    }
                } catch (ex) {
                    if(ex instanceof groovyx.net.http.HttpResponseException && ex?.response) {
                        log.error "pushoverNotification() HttpResponseException | Status: (${ex?.response?.status}) | Data: ${ex?.response?.data}"
                    } else {
                        log.error "pushoverNotification Exception: ${ex?.message}" 
                    }
                } 
            } else {
                log.error "API key '${apiKey}' or User key '${userKey}' is not properly formatted!"
            }
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