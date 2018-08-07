/**
 *  Pushover-Manager
 *
 *  Inspired by original work for SmartThings by: Dan Ogorchock, Stephan Hackett, and Zachary Priddy
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
def appVer() {"v1.0.20180806"}

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
        section("Name this Instance:") {
            paragraph "This name will be used to help identify this install in 3rd Party apps."
            label title: "Name this Config", required: true, defaultValue: "${app?.name}"
        }
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
    sendLocationEvent(name: "pushoverManager", value: "refresh", data: [id: app?.getId(), devices: getDeviceList(), sounds: getSoundOptions(), appName: app?.getLabel()], isStateChange: true, descriptionText: "Pushover Manager Device List Refresh")
}

def uninstalled() {
    log.warn "Uninstalled called... Removing all Devices..."
    addRemoveDevices(true)
    sendLocationEvent(name: "pushoverManager", value: "reset", data: [], isStateChange: true, descriptionText: "Pushover Manager Device List Reset")
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
            if (evt?.jsonData && evt?.jsonData?.id == app?.getId() && evt?.jsonData?.devices && evt?.jsonData?.msgData?.size()) {
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
                // sendPush("ERROR: 'Pushover Me When' received HTTP error ${resp?.status}. Check your keys!")
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

include 'asynchttp_v1'

void pushoverNotification(deviceName, msgData) {
    // log.debug "pushoverNotification($deviceName, $msgData)"
    if(deviceName && msgData) {
        if(msgData?.message != null && msgData?.message?.length() > 0 && deviceName && settings?.apiKey && settings?.userKey) {
            def hasImage = false//(msgData?.image && msgData?.image?.url && msgData?.image?.type)
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

            def test = false
            Map params = [uri: test ? "http://requestbin.fullcontact.com/r50ennr5" : "https://api.pushover.net/1/messages.json"]
            String imgStr = hasImage ? getImageData(msgData?.image?.url, msgData?.image?.type) as String : null
            // log.debug "imgStr length: ${imgStr != null ? "${imgStr?.toString()?.length()}" : "null"}"
            if(hasImage && imgStr) {
                imgStr = new JsonOutput().toJson(imgStr) as String
                imgStr = imgStr.replaceFirst("\"","")
                imgStr = imgStr?.endsWith("\"") ? imgStr = removeLastChar(imgStr) : imgStr
                String boundary = "pushover_${(new Date())?.getTime()}"
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
                    // bodyStr?.push("Content-Transfer-Encoding: binary")
                    bodyStr?.push("")
                    bodyStr?.push("${imgStr}")
                    bodyStr?.push("--${boundary}--")
                }
                params?.requestContentType = "multipart/form-data; boundary=${boundary}"
                params?.body = bodyStr.join('\r\n')
            } else {
                params?.requestContentType = "application/json"
                params?.body = new JsonOutput().toJson(bodyItems)
            }
            // log.debug "body (${params?.body?.toString()?.length()}): ${params?.body}"
            // log.debug "$params"
            
            asynchttp_v1.post(pushoverResponse, params, [hasImage: (hasImage && imgStr)])
        }
    }
}

public String removeLastChar(String s) {
    return (s == null || s.length() == 0) ? null : (s.substring(0, s.length() - 1))
}

def pushoverResponse(resp, data) {
    try {
        Map headers = resp?.getHeaders()
        def limit = headers["X-Limit-App-Limit"]
        def remain = headers["X-Limit-App-Remaining"]
        def resetDt = headers["X-Limit-App-Reset"]
        if(resp?.status == 200) {
            log.debug "Message Received by Pushover Server${(remain && limit) ? " | Monthly Messages Remaining (${remain} of ${limit})" : ""}"
            state?.messageData = [lastMessage: msgData?.message, lastMessageDt: formatDt(new Date()), remain: remain, limit: limit, resetDt: resetDt]
        } else if (resp?.status == 429) { 
            log.warn "Couldn't Send Notification... You have reached your (${limit}) notification limit for the month"
        } else {
            if(resp?.hasError()) {
                log.error "pushoverResponse: status: ${resp.status} | errorMessage: ${resp?.getErrorMessage()}"
                // log.error "Received HTTP error ${resp?.status}. Check your keys!"
            }
        }
    } catch (ex) {
        if(ex instanceof groovyx.net.http.HttpResponseException && ex?.response) {
            def rData = (ex?.response?.data && ex?.response?.data != "") ? " | Data: ${ex?.response?.data}" : ""
            log.error "pushoverResponse() HttpResponseException | Status: (${ex?.response?.status})${rData}"
        } else { log.error "pushoverResponse() Exception:", ex }
    }
}

def getImageData(url, fileType) {
    try {
        def test = false
        def params = [uri: url, contentType: test ? "${fileType}; charset=UTF-8" : "text/plain; charset=UTF-8"]
        httpGet(params) { resp ->
            if(resp?.status == 200) {
                if(resp?.data) {
                    // Byte[] rawBytes = resp?.data?.getBytes()
                    // log.debug "encoding: ${resp?.getEntity()}"
                    String rawText = ""
                    def sizeHeader = resp?.getHeaders("Content-Length")
                    def size = (sizeHeader?.value && sizeHeader?.value[0] && sizeHeader?.value[0]?.isNumber()) ? sizeHeader.value[0] : null
                    if(size) {
                        if(size?.toLong() > 2621440) {
                            log.debug("FileSize: (${getFileSize(size)})")
                            log.warn "unable to encode file because it is larger than the 2.5MB size limit"
                            return null
                        } else {
                            StringReader sr = resp?.getData()
                            for (int i = 0; i < size?.toInteger(); i++) { char c = (char) sr?.read(); rawText += c; }
                            sr?.close()
                            return rawText as String
                            
                            // def respData = resp?.data
                            
                            // int n = respData.available();
                            // byte[] bytes = new byte[n];
                            // respData.read(bytes, 0, n);
                            // String s = new String(bytes, org.apache.commons.lang3.CharEncoding.UTF_8);
                            
                            // rawText = s.toString()
                            
                            // log.debug "respData: $respData"
                            // ByteArrayOutputStream bos = new ByteArrayOutputStream()
                            // int len
                            // int sz = 4096
                            // byte[] buf = new byte[sz]
                            // while ((len = respData.read(buf, 0, sz)) != -1)
                            //     bos.write(buf, 0, len)
                            // buf = bos.toByteArray()
                            // log.debug "buf: ${buf}"
                            // log.debug "rawText: $rawText"
                            // return rawText as String
                        }
                    } else { return null }
                }
            } else {
                log.error("getImageData() Resp: ${resp?.status} ${url}")
                return null
            }
        }
    } catch (ex) {
        log.error "getImageData() Exception:", ex
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