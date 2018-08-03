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

import groovy.json.*
def appVer() {"v1.0.20180801"}

definition(
    name: "Pushover-Manager",
    namespace: "tonesto7",
    author: "Anthony Santilli",
    description: "Creates and Manages Pushover devices",
    category: "My Apps",
    iconUrl: "https://raw.githubusercontent.com/tonesto7/pushover-manager/master/images/icon-72.png",
    iconX2Url: "https://raw.githubusercontent.com/tonesto7/pushover-manager/master/images/icon-256.png",
    iconX3Url: "https://raw.githubusercontent.com/tonesto7/pushover-manager/master/images/icon-512.png")


preferences {
    page(name: "mainPage")
    page(name: "messageTest")
}

def appInfoSect()	{
    section() {
        def str = ""
        str += "${app?.name}"
        str += "\nVersion: ${appVer()}"
        paragraph str, image: "https://raw.githubusercontent.com/tonesto7/pushover-manager/master/images/icon-512.png"
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
            section("Statistics:") {
                def msgData = state?.messageData
                def str = ""
                def limit = msgData?.limit
                def remain = msgData?.remain
                def reset = msgData?.resetDt
                str += remain || limit ? "Message Details (Month):" : ""
                str += limit ? "\n • Limit: (${limit})" : ""
                str += remain ? "\n • Remaining: (${remain})" : ""
                str += (remain?.isNumber() && limit?.isNumber()) ? "\n • Used: (${(limit?.toLong() - remain?.toLong())})" : ""
                paragraph str
            }
            section("Clients:") {
                def str = ""
                devices?.each { cl-> str += "\n • ${cl}" }
                paragraph title: "Pushover Clients:", (str != "" ? str : "No Clients Found..."), state: "complete"
            }
            section("Test Notifications:", hideable: true, hidden: true) {
                input "testDevices", "enum", title: "Select Devices", description: "Select Devices to Send Test Notification Too...", multiple: true, required: false, options: devices, submitOnChange: true
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

def isValidated() { }

def getDeviceList() {
    return (settings?.apiKey && settings?.userKey && getValidated()) ? getValidated(true) : []
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
    subscribe(location, "pushoverManagerMsg", locMessageHandler)
    subscribe(location, "pushoverManagerPoll", locMessageHandler)
    sendDeviceListEvent()
}

def sendDeviceListEvent() {
    log.trace "sendDeviceListEvent..."
    sendLocationEvent(name: "pushoverManager", value: "refresh", data: [devices: getDeviceList(), sounds: getSoundOptions()], isStateChange: true, descriptionText: "Pushover Manager Device List Refresh")
}

def uninstalled() {
    log.warn "Uninstalled called... Removing all Devices..."
    addRemoveDevices(true)
}

def locMessageHandler(evt) {
    log.debug "locMessageHandler: ${evt?.jsonData}"
    if (!evt) return
    if (!(settings?.apiKey =~ /[A-Za-z0-9]{30}/) && (settings?.userKey =~ /[A-Za-z0-9]{30}/)) {
        log.error "API key '${apiKey}' or User key '${userKey}' is not properly formatted!"
        return 
    }
    switch (evt?.value) {
        case "sendMsg":
            List pushDevices = []
            if (evt?.jsonData && evt?.jsonData?.devices && evt?.jsonData?.msgData?.size()) {
                log.trace "locMessageHandler(sendMsg)"
                evt?.jsonData?.devices?.each { nd->
                    pushoverNotification(nd as String, evt?.jsonData?.msgData)
                }
            }
            break
        case "poll":
            log.debug "locMessageHandler: poll()"
            sendDeviceListEvent()
            break
    }
}

def getValidated(devList=false){
    def validated = false
    def params = [
        uri: "https://api.pushover.net/1/users/validate.json",
        contentType: "application/json",
        body: [
            token: settings?.apiKey?.trim(),
            user: settings?.userKey?.trim(),
            device: ""
        ]
    ]
    def deviceOptions
    if ((settings?.apiKey?.trim() =~ /[A-Za-z0-9]{30}/) && (settings?.userKey?.trim() =~ /[A-Za-z0-9]{30}/)) {
        try {
            httpPost(params) { resp ->
                // log.debug "response: ${resp.status}"
                if(resp?.status != 200) {
                    // sendPush("ERROR: 'Pushover Me When' received HTTP error ${resp?.status}. Check your keys!")
                    log.error "Received HTTP error ${resp.status}. Check your keys!"
                } else {
                    if(devList) {
                        if(resp?.data && resp?.data?.devices) {
                            // log.debug "Found (${resp?.data?.devices?.size()}) Pushover Devices..."
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
    // log.debug "Generating Sound Notification List..."
    def myOptions = [:]
    try {
        httpGet(uri: "https://api.pushover.net/1/sounds.json?token=${settings?.apiKey}") { resp ->
            if(resp?.status == 200) {
                // log.debug "Found (${resp?.data?.sounds?.size()}) Sounds..."
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

def filterPriorityMsg(msg, msgPr) {
    if(msg?.startsWith("[L]")) { 
        msgPr = "-1"
        msg = msg?.minus("[L]")
    } else if(msg?.startsWith("[N]")) {
        msgPr = "0"
        msg = msg?.minus("[N]")
    } else if(msg?.startsWith("[H]")) {
        msgPr = "1"
        msg = msg?.minus("[H]")
    } else if(msg?.startsWith("[E]")) {
        msgPr = "2"
        msg = msg?.minus("[E]")
    }
    return [msg: msg, msgPr: msgPr]
}

void pushoverNotification(deviceName, msgData) {
    // log.debug "pushoverNotification($deviceName, $msgData)"
    if(deviceName && msgData) {
        if(msgData?.message != null && msgData?.message?.length() > 0 && deviceName && settings?.apiKey && settings?.userKey) {
            def hasImage = (msgData?.image && msgData?.image?.url && msgData?.image?.type)
            def filtr = filterPriorityMsg(msgData?.message, msgData?.priority)
            String message = filtr?.msg
            String priority = filtr?.msgPr ?: "0"
            Map bodyItems = [
                token: settings?.apiKey?.trim(),
                user: settings?.userKey?.trim(),
                title: msgData?.title,
                message: message,
                priority: priority,
                device: deviceName,
                retry: msgData?.retry ?: 30,
                expire: msgData?.expire ?: 10800
            ]
            if(msgData?.sound) { bodyItems?.sound = msgData?.sound }
            if(msgData?.url) { bodyItems?.url = msgData?.url }
            if(msgData?.url_title) { bodyItems?.url_title = msgData?.urlTitle }
            if(msgData?.timestamp) { bodyItems?.timestamp = msgData?.timestamp }
            if(msgData?.html == true) { bodyItems?.html = 1 }
            Map params = [uri: "https://api.pushover.net/1/messages.json"]
            if(hasImage) {
                String boundary = "----${(new Date())?.getTime()}"
                List bodyStr = []
                bodyItems?.each { k, v ->
                    bodyStr?.push("--${boundary}")
                    bodyStr?.push("Content-Disposition: form-data; name=\"${k}\"")
                    bodyStr?.push("")
                    bodyStr?.push("${v}")
                }
                if(msgData?.image && msgData?.image?.url && msgData?.image?.type) {
                    bodyStr?.push("--${boundary}")
                    bodyStr?.push("Content-Disposition: form-data; name=\"attachment\"; filename=\"${msgData?.image?.name}\"")
                    bodyStr?.push("Content-Type: ${msgData?.image?.type}")
                    bodyStr?.push("Content-Transfer-Encoding: base64")
                    bodyStr?.push("")
                    // def img = "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"
                    // bodyStr?.push("${img}")
                    bodyStr?.push("${getImageData(msgData?.image?.url, msgData?.image?.type)?.toString()}")
                }
                bodyStr.push("--${boundary}--")
                params?.contentType = "multipart/form-data; boundary=${boundary}"
                params?.contentLength = bodyStr.join('\r\n')?.length()
                params?.body = bodyStr.join('\r\n')
                // log.debug "${params?.body}"
            } else {
                params?.contentType = "application/json"
                params?.body = new JsonOutput().toJson(bodyItems)
            }
            
            // log.debug "$params"
            try {
                httpPost(params) { resp ->
                    def limit = resp?.getHeaders("X-Limit-App-Limit")
                    def remain = resp?.getHeaders("X-Limit-App-Remaining")
                    def resetDt = resp?.getHeaders("X-Limit-App-Reset")
                    if(resp?.status == 200) {
                        log.debug "Message Received by Pushover Server | Monthly Messages Remaining (${remain?.value[0]} of ${limit?.value[0]})"
                        state?.messageData = [lastMessage: msgData?.message, lastMessageDt: formatDt(new Date()), remain: remain?.value[0], limit: limit?.value[0], resetDt: resetDt?.value[0]]
                    } else if (resp?.status == 429) { 
                        log.warn "Can't Send Notification... You have reached your (${limit?.value[0]}) notification limit for the month"
                    } else {
                        sendPush("pushoverNotification() ERROR: 'Pushover' received HTTP error ${resp?.status}. Check your keys!")
                        log.error "Received HTTP error ${resp?.status}. Check your keys!"
                    }
                }
            } catch (ex) {
                if(ex instanceof groovyx.net.http.HttpResponseException && ex?.response) {
                    log.error "pushoverNotification() HttpResponseException | Status: (${ex?.response?.status})${ex?.response?.data ? " | Data: ${ex?.response?.data}" : "" }"
                } else {
                    log.error "pushoverNotification Exception:", ex 
                }
            }
        }
    }
}

def getImageData(url, fileType) {
    try {
        def params = [uri: url, requestContentType: "${fileType}"]
        httpGet(params) { resp ->
            if(resp?.status == 200) {
                if(resp?.data) {
                    Byte[] bytes = resp?.data?.getBytes()
                    def size = resp?.getHeaders("Content-Length")
                    if(size?.value && size?.value[0] && size?.value[0]?.isNumber()) {
                        if(size?.value[0]?.toLong() > 2621440) {
                            log.debug("FileSize: (${getFileSize(size?.value[0])})")
                            log.warn "unable to encode file because it is larger than the 2.5MB size limit"
                            return null
                        }
                    }
                    // ByteArrayOutputStream buffer = new ByteArrayOutputStream()
                    // InputStream is = new ByteArrayInputStream(bytes)
                    // byte[] temp = new byte[1024];
                    // int read;
                    // while((read = is.read(temp)) >= 0){
                    //     buffer.write(temp, 0, read)
                    // }
                    // byte[] data = buffer.toByteArray()
                    InputStream input = new ByteArrayInputStream(bytes);
                    int data = input.read();
                    while(data != -1) {
                        //do something with data
                        data = input.read();
                    }
                    input.close();
                    log.debug "data: $data"
                    String enc = bytes?.encodeBase64() as String 
                    return enc ? "data:${fileType};base64,${enc?.toString()}" : null 
                }
            } else {
                log.error("getImageData Resp: ${resp?.status} ${url}")
                return null
            }
        }
    } catch (ex) {
        log.error "getImageData Exception:", ex
        return null
    }
}

def getFileSize(sizeVal) {
    String outSize = null;
    double fileSize = 0.0;
    fileSize = sizeVal?.toDouble()
    if (fileSize < 1024) {
        outSize = String.valueOf(fileSize).concat("B")
    } else if (fileSize > 1024 && fileSize < (1024 * 1024)) {
        outSize = String.valueOf(Math.round((fileSize / 1024 * 100.0)) / 100.0).concat("KB")
    } else {
        outSize = String.valueOf(Math.round((fileSize / (1024 * 1204) * 100.0)) / 100.0).concat("MB")
    }
    return outSize
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