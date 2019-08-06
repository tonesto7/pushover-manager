/**
 *  Pushover Manager
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
def appVer() { return "v1.0.1" }
def appDate() { return "7-08-2019" }

definition(
    name: "Pushover Manager",
    namespace: "tonesto7",
    author: "Anthony Santilli",
    description: "Location Event based Pushover Message Manager",
    category: "My Apps",
    iconUrl: "https://raw.githubusercontent.com/tonesto7/pushover-manager/master/images/icon-72.png",
    iconX2Url: "https://raw.githubusercontent.com/tonesto7/pushover-manager/master/images/icon-256.png",
    iconX3Url: "https://raw.githubusercontent.com/tonesto7/pushover-manager/master/images/icon-512.png")


preferences {
    page(name: "mainPage")
    page(name: "authKeysPage")
    page(name: "infoPage")
    page(name: "getAppsPage")
    page(name: "testMessagePage")
    page(name: "messageTest")
}

def appInfoSect()	{
    section() {
        def str = ""
        str += "${app?.name}"
        str += "\nVersion: ${appVer()}"
        str += "\nModified: ${appDate()}"
        paragraph str, image: "https://raw.githubusercontent.com/tonesto7/pushover-manager/master/images/icon-512.png"
    }
}

String androidAppUrl() { return "https://pushover.net/clients/android" }
String iosAppUrl() { return "https://pushover.net/clients/ios" }
String desktopAppUrl() { return "https://pushover.net/clients/desktop" }
String appViewUrl() { return "https://pushover.net/login?back_to=/apps" }
String appRegisterUrl() { return "https://pushover.net/login?back_to=/apps/build" }
String userLoginUrl() { return "https://pushover.net/login" }
String costFaqUrl() { return "https://pushover.net/faq#overview-fees" }
String faqUrl() { return "https://pushover.net/faq" }
String apiDocUrl() { return "https://pushover.net/api" }

def mainPage() {
    return dynamicPage(name: "mainPage", title: "", install: true, uninstall: true) {
        Boolean isInstalled = (state?.isInstalled == true)
        appInfoSect()
        if(isInstalled) {
            def validated = (settings?.apiKey && settings?.userKey && getValidated())
            def devices = validated ? getValidated(true) : []
            section("Pushover API Authentication: (${validated ? "Good" : "Missing"})", hideable: true, hidden: validated) {
                href "authKeysPage", title: "Authentication Keys", description: "Configure your Pushover Keys", state: (validated ? "complete" : null), required: true
            }
            if(validated) {
                if(state?.messageData?.size()) {
                    section() {
                        def msgData = state?.messageData
                        def str = ""
                        def limit = msgData?.limit
                        def remain = msgData?.remain
                        def reset = msgData?.resetDt
                        str += limit ? "${str=="" ? "" : "\n"} • Limit: (${limit})" : ""
                        str += remain ? "${str=="" ? "" : "\n"} • Remaining: (${remain})" : ""
                        str += (remain?.isNumber() && limit?.isNumber()) ? "\n • Used: (${(limit?.toLong() - remain?.toLong())})" : ""
                        paragraph title: "Pushover App Statistics (Month):", (str != "" ? str : "No data available"), state: (str != "" ? "complete" : null)
                    }
                }
                section() {
                    def str = ""
                    devices?.each { cl-> str += "${str=="" ? "" : "\n"} • ${cl}" }
                    paragraph title: "Discovered Clients:", (str != "" ? str : "No Clients Found..."), state: "complete"
                    href "testMessagePage", title: "Send a Test Message", description: "", required: false
                }
            }
            section("Pushover Clients:") {
                href "getAppsPage", title: "Pushover Client Apps", description: "", required: false
            }
            section("More Info:") {
                href "infoPage", title: "More Information", description: "", required: false
            }
            section("Name this App:") {
                paragraph "This name is used to help identify this install in 3rd Party apps, and is especially important if you install this app multiple times for different App keys"
                label title: "App Name", defaultValue: "${app?.name}", required: false
            }
        } else {
            section() { paragraph title: "New install detected...", "1. Press Done to install the app\n2. Return to main screen\n3. Tap on Automations\n4. Tap on SmartApps tab\n5. Scroll and Tap on Pushover-Manager\n6. Complete App configuration process\n7. Press Done to Complete platform integration", state: "complete"}
        }
    }
}

def authKeysPage() {
    return dynamicPage(name: "authKeysPage", title: "Pushover Authentication", install: false, uninstall: false) {
        section() {
            href url: userLoginUrl(), style: "embedded", title: "Create Pushover Account/Get User Key", description: "Tap here to Create a New Pushover Account and/or get your User Key.  Then copy/paste the User key into the input below.", state: "complete"
            href url: appViewUrl(), style: "embedded", title: "Get Existing App Key", description: "Tap here to get your App Key.  Then copy/paste the App Key into the input below.", state: "complete"
            href url: appRegisterUrl(), style: "embedded", title: "Create Pushover App", description: "Tap here to Create a New Pushover App and get your new App Key.  Then copy/paste the App Key into the input below.", state: "complete"
        }
        section() {
            input "userKey", "text", title: "User/Group Key:", description: "Pushover User/Group Key", required: true, submitOnChange: true
            input "apiKey", "text", title: "App Key:", description: "Pushover App Key", required: true, submitOnChange: true
        }
    }
}

def infoPage() {
    return dynamicPage(name: "infoPage", title: "Information", install: false, uninstall: false) {
        section() {
            href url: costFaqUrl(), style: "embedded", title: "How much does this cost?", description: "Tap to open", state: "complete"
            href url: faqUrl(), style: "embedded", title: "Frequently Asked Question?", description: "Tap to open", state: "complete"
            href url: apiDocUrl(), style: "embedded", title: "Pushover API Documentation", description: "Tap to open", state: "complete"
        }
    }
}

def getAppsPage() {
    return dynamicPage(name: "getAppsPage", title: "Get the Apps", install: false, uninstall: false) {
        section() {
            href url: androidAppUrl(), style: "external", title: "Android App", description: "Tap to open", state: "complete"
            href url: iosAppUrl(), style: "external", title: "iPhone/iPad App", description: "Tap to open", state: "complete"
            href url: desktopAppUrl(), style: "external", title: "Desktop (Browser) App", description: "Tap to open", state: "complete"
        }
    }
}

def testMessagePage() {
    return dynamicPage(name: "testMessagePage", title: "Message Test Page", install: false, uninstall: false) {
        section() {
            input "testDevices", "enum", title: "Select Devices", description: "Select Devices to Send Message Too...", multiple: true, required: false, options: getDeviceList(), submitOnChange: true
            if(settings?.testDevices) {
                input "testSound", "enum", title: "Notification Sound:", description: "Select the Notification Sound", defaultValue: "pushover", required: false, multiple: false, submitOnChange: true, options: getSoundOptions()
                input "testMessage", "text", title: "Test Message to Send:", description: "Enter message to send...", required: false, submitOnChange: true
                if(settings?.testMessage && settings?.testMessage?.length() > 0) {
                    href "messageTest", title: "Send Message", description: ""
                }
            }
        }
        state?.testMessageSent = false
    }
}

def getDeviceList() { return (settings?.apiKey && settings?.userKey && getValidated()) ? getValidated(true) : [] }

def messageTest() {
    return dynamicPage(name: "messageTest", title: "Notification Test", install: false, uninstall: false) {
        section() {
            if(state?.testMessageSent == true) {
                paragraph title: "Oops", "Message Already Sent...\nGo Back to MainPage to Send again..."
            } else {
                paragraph title: "Sending Message: ", "${settings?.testMessage}", state: "complete"
                paragraph "Device(s): ${settings?.testDevices}"
                buildPushMessage(settings?.testDevices, app?.getLabel() + " Test", settings?.testMessage)
            }
            state?.testMessageSent = true
        }
    }
}

private buildPushMessage(List devices, String title, String message) {
    Map data = [:]
    data?.appId = app?.getId()
    data.devices = devices
    data?.msgData = [title: title, message: message, priority: 0, sound: settings?.testSound]
    sendTestMessage(devices, data)
}

def sendTestMessage(devices, data) {
    log.debug "Sending Test Message: (${data?.msgData?.message}) to Devices: ${devices}"
    devices?.each { nd-> sendPushoverMessage(nd as String, data?.msgData)}
}

def installed() {
    state?.isInstalled = true
    log.debug "Installed with settings: ${settings}"
    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"
    unsubscribe()
    initialize()
}

def initialize() {
    subscribe(location, "pushoverManagerMsg", locMessageHandler)
    subscribe(location, "pushoverManagerCmd", locCommandHandler)
    sendDeviceRefreshEvt()
}

def uninstalled() {
    log.warn "Uninstalled called..."
    sendDeviceResetEvt()
}

private sendDeviceRefreshEvt() {
    log.info "Sending Pushover Device Refresh Event..."
    sendLocationEvent(name: "pushoverManager", value: "refresh", data: [appId: app?.getId(), devices: getDeviceList(), sounds: getSoundOptions(), appName: app?.getLabel()], isStateChange: true, descriptionText: "Pushover-Manager Device List Refresh")
}

private sendDeviceResetEvt() {
    log.warn "Sending Pushover Device Reset Event..."
    sendLocationEvent(name: "pushoverManager", value: "reset", data: [], isStateChange: true, descriptionText: "Pushover-Manager Device List Reset")
}

Boolean userKeyOk() {
    if(!(settings?.userKey?.trim() =~ /[A-Za-z0-9]{30}/)) {
        log.error "User key '${settings?.userKey}' is missing or not properly formatted!"
        return false
    }
    return true
}

Boolean apiKeyOk() {
    if(!(settings?.apiKey?.trim() =~ /[A-Za-z0-9]{30}/)) {
        log.error "API Key '${settings?.apiKey}' is missing or not properly formatted!"
        return false
    }
    return true
}

def locMessageHandler(evt) {
    log.trace "locMessageHandler(${evt?.value})"
    // log.trace "locMessageHandler(${evt?.value}): ${evt?.jsonData}"
    if (!evt) return
    if (!apiKeyOk() || !userKeyOk()) { return }
    switch (evt?.value) {
        case "sendMsg":
            List pushDevices = []
            if (evt?.jsonData && evt?.jsonData?.devices && evt?.jsonData?.msgData?.size()) {
                evt?.jsonData?.devices?.each { nd->
                    if(nd?.toString()?.contains(app?.getId() as String)) { sendPushoverMessage(nd as String, evt?.jsonData?.msgData) }
                }
            }
            break
    }
}

def locCommandHandler(evt) {
    log.trace "locCommandHandler(${evt?.value})"
    // log.trace "locCommandHandler(${evt?.value}): ${evt?.jsonData}"
    if (!evt) return
    if (!apiKeyOk() || !userKeyOk()) { return }
    switch (evt?.value) {
        case "poll":
            sendDeviceRefreshEvt()
            break
    }
}

def getValidated(devList=false){
    Boolean validated = false
    Map params = [
        uri: "https://api.pushover.net",
        path: "/1/users/validate.json",
        contentType: "application/json",
        requestContentType: "application/json",
        body: [token: settings?.apiKey?.trim() as String, user: settings?.userKey?.trim() as String] as Map
    ]
    List deviceOptions = []
    if (!apiKeyOk() || !userKeyOk()) { return }
    try {
        httpPostJson(params) { resp ->
            if(resp?.status != 200) {
                log.error "Received HTTP error ${resp.status}. Check your keys!"
            } else {
                if(resp?.data) {
                    if(resp?.data?.status && resp?.data?.status == 1) {
                        validated = true
                    }
                    if(devList) {
                        if(resp?.data?.devices) {
                            // log.debug "Found (${resp?.data?.devices?.size()}) Pushover Devices..."
                            deviceOptions = resp?.data?.devices
                            state?.pushoverDevices = resp?.data?.devices
                        } else {
                            log.error "Device List is empty"
                            state?.pushoverDevices = []
                        }
                    }
                } else { validated = false }
            }
            log.debug "getValidated | Validated: ${validated} | Resp | status: ${resp?.status} | data: ${resp?.data}"
        }
    } catch (Exception ex) {
        if(ex instanceof groovyx.net.http.HttpResponseException && ex?.response) {
            log.error "getValidated() HttpResponseException | Status: (${ex?.response?.status}) | Data: ${ex?.response?.data}"
        } else {
            log.error "An invalid key was probably entered. PushOver Server Returned: ${ex}"
        }
    }
    return devList ? deviceOptions : validated
}

def getSoundOptions() {
    // log.debug "Generating Sound Notification List..."
    def myOptions = [:]
    if(!apiKeyOk()) { return myOptions }
    try {
        httpGet(uri: "https://api.pushover.net/1/sounds.json?token=${settings?.apiKey}") { resp ->
            if(resp?.status == 200) {
                resp?.data?.sounds?.each { snd-> myOptions["${snd?.key}"] = snd?.value }
            } else { log.error "Received HTTP error ${resp?.status}. Check your keys!" }
        }
    } catch (Exception ex) {
        if(ex instanceof groovyx.net.http.HttpResponseException && ex?.response) {
            log.error "getSoundOptions() HttpResponseException | Status: (${ex?.response?.status}) | Data: ${ex?.response?.data}"
        }
    }
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



void sendPushoverMessage(deviceName, msgData) {
    // log.debug "sendPushoverMessage($deviceName, $msgData)"
    if(deviceName && msgData) {
        if(msgData?.message != null && msgData?.message?.length() > 0 && deviceName && apiKeyOk() && userKeyOk()) {
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

            Map params = [uri: "https://api.pushover.net/1/messages.json"]
            // String imgStr = hasImage ? getImageData(msgData?.image?.url, msgData?.image?.type) as String : null
            String imgStr = null
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
            if(getPlatform() != "SmartThings") {
                asynchttpPost(pushoverResponse, params, [hasImage: (hasImage && imgStr)])
            } else {
                include 'asynchttp_v1'
                asynchttp_v1.post(pushoverResponse, params, [hasImage: (hasImage && imgStr)])
            }
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

// def getImageData(url, fileType) {
//     try {
//         def params = [uri: url, contentType: "text/plain; charset=UTF-8"]
//         httpGet(params) { resp ->
//             if(resp?.status == 200) {
//                 if(resp?.data) {
//                     String rawText = ""
//                     def sizeHeader = resp?.getHeaders("Content-Length")
//                     def size = (sizeHeader?.value && sizeHeader?.value[0] && sizeHeader?.value[0]?.isNumber()) ? sizeHeader.value[0] : null
//                     if(size) {
//                         if(size?.toLong() > 2621440) {
//                             log.debug("FileSize: (${getFileSize(size)})")
//                             log.warn "unable to encode file because it is larger than the 2.5MB size limit"
//                             return null
//                         } else {
//                             StringReader sr = resp?.getData()
//                             for (int i = 0; i < size?.toInteger(); i++) { char c = (char) sr?.read(); rawText += c; }
//                             sr?.close()
//                             return rawText as String
//                         }
//                     } else { return null }
//                 }
//             } else {
//                 log.error("getImageData() Resp: ${resp?.status} ${url}")
//                 return null
//             }
//         }
//     } catch (ex) {
//         log.error "getImageData() Exception:", ex
//         return null
//     }
// }

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

private getPlatform() {
    def p = "SmartThings"
    if(state?.hubPlatform == null) {
        try { [dummy: "dummyVal"]?.encodeAsJson(); } catch (e) { p = "Hubitat" }
        // p = (location?.hubs[0]?.id?.toString()?.length() > 5) ? "SmartThings" : "Hubitat"
        state?.hubPlatform = p
        log.debug "hubPlatform: (${state?.hubPlatform})"
    }
    return state?.hubPlatform
}
