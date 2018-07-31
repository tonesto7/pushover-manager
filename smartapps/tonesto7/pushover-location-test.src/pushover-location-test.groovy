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
    name: "Pushover-Location-Test",
    namespace: "tonesto7",
    author: "Anthony Santilli",
    description: "Test Pushover Location Integration",
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
        if(state?.isInstalled == true) {
            section("Test Notifications:", hideable: true, hidden: false) {
                input "testDevices", "enum", title: "Select Devices", description: "Select Devices to Send Test Notification Too...", multiple: true, required: false, options: state?.pushDevices, submitOnChange: true
                if(settings?.testDevices) {
                    input "testSound", "enum", title: "Notification Sound:", description: "Select a sound...", defaultValue: "pushover", required: false, multiple: false, submitOnChange: true, options: state?.pushSounds
                    input "testMessage", "text", title: "Test Message to Send:", description: "Enter message to send...", required: false, submitOnChange: true
                    if(settings?.testMessage && settings?.testMessage?.length() > 0) {
                        href "messageTest", title: "Send Message", description: ""
                    }
                }
            }
        } else {
            section() {
                paragraph "This is a new install... Press done to install and then come back into the app to configure"
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
    Map msgData = [
        title: "${app?.name}",
        html: 0,
        message: settings?.testMessage,
        priority: msgPriority,
        retry: 30,
        expire: 10800,
        sound: settings?.testSound,
        url: "",
        url_title: "",
        timestamp: new Date().getTime(),
        image: "https://community.hubitat.com/uploads/default/original/1X/f994d8c0dd92a7e88d22c5f84a633925f02d66e5.png"
    ]
    sendLocationEvent(name: "pushoverManagerMsg", value: "send", data: [devices: settings?.testDevices, msgData: msgData], isStateChange: true, descriptionText: "Sending Message to ${settings?.testDevices}")
}

def installed() {
    log.debug "Installed with settings: ${settings}"
    state?.isInstalled = true
    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"
    // unsubscribe()
    initialize()
}

def initialize() {
    subscribe(location, "pushoverManager", pushoverHandler)
}

def uninstalled() {
    log.warn "Uninstalled called..."
    
}

def pushoverHandler(evt) {
    if (!evt) return
    log.debug "pushoverHandler: ${evt?.jsonData}"
    switch (evt?.value) {
        case "refresh":
            state?.pushSounds = evt?.jsonData?.sounds
            state?.pushDevices = evt?.jsonData?.devices ?: []
            break
    }
}