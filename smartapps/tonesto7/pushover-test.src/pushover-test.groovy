/**
 *  Pushover-Test
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
def appVer() {"v1.0.20180806"}

definition(
    name: "Pushover-Test",
    namespace: "tonesto7",
    author: "Anthony Santilli",
    description: "Test Pushover Location Integration",
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
        if(state?.isInstalled == true) {
            section("Test Notifications:", hideable: true, hidden: false) {
                def englishOptions = ["One", "Two", "Three"]
                def spanishOptions = ["Uno", "Dos", "Tres"]
                def groupedOptions = []
                addGroup(groupedOptions, "English", englishOptions)
                addGroup(groupedOptions, "Spanish", spanishOptions)
                groupedOptions?.each {
                    log.debug "${it}"
                }
                input "selectionGrouped", "enum", title: "Grouped options", description: "separate groups of options with headers", groupedOptions: groupedOptions, submitOnChange: true
                input "testDevices", "enum", title: "Select Devices", description: "Select Devices to Send Test Notification Too...", multiple: true, required: false, options: state?.pushoverManagerData?.devices, submitOnChange: true
                if(settings?.testDevices) {
                    input "testSound", "enum", title: "Notification Sound:", description: "Select a sound...", defaultValue: "pushover", required: false, multiple: false, submitOnChange: true, options: state?.pushoverManagerData?.sounds
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

def optionsGroup(List groups, String title) {
    def group = [values:[], order: groups.size()]
    group.title = title ?: ""
    groups << group
    return groups
}
def addValues(List groups, String key, String value) {
    def lastGroup = groups[-1]
    lastGroup["values"] << [
            key: key,
            value: value,
            order: lastGroup["values"].size()
    ]
    return groups
}
def listToMap(List original) {
    original.inject([:]) { result, v ->
        result[v] = v
        return result
    }
}
def addGroup(List groups, String title, values) {
    if (values instanceof List) {
        values = listToMap(values)
    }

    values.inject(optionsGroup(groups, title)) { result, k, v ->
        return addValues(result, k, v)
    }
    return groups
}
def addGroup(values) {
    addGroup([], null, values)
}

def messageTest() {
    return dynamicPage(name: "messageTest", title: "Notification Test", install: false, uninstall: false) {
        section() {
            if(state?.testMessageSent == true) {
                paragraph title: "Oops", "Message Already Sent...\nGo Back to MainPage to Send again..."
            } else {
                paragraph title: "Sending Message: ", "${settings?.testMessage}", state: "complete"
                paragraph "Device(s): ${settings?.testDevices}" 
                sendTestMessage()
            }
            state?.testMessageSent = true
        }
    }
}

def sendTestMessage() {
    Map data = [:]
    data?.appId = app?.getId()
    data.devices = settings?.testDevices
    data?.msgData = [
        title: "${app?.name}",
        html: false,
        message: settings?.testMessage,
        priority: 0,
        retry: 30,
        expire: 10800,
        sound: settings?.testSound,
        url: "https://www.foreverbride.com/files/6414/7527/3346/test.png",
        url_title: "Test Image",
        timestamp: new Date().getTime(),
    ]
    pushover_msg(settings?.testDevices, data)
}  

def installed() {
    log.debug "Installed with settings: ${settings}"
    state?.isInstalled = true
    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"
    unsubscribe()
    initialize()
}

def initialize() {
    pushover_init()
}

def uninstalled() {
    log.warn "Uninstalled called..."    
}

Map getPushoverSounds() { return state?.pushoverManagerData?.sounds ?: [] }
public pushover_init() { subscribe(location, "pushoverManager", pushover_handler); pushover_poll(); }
public pushover_poll() { sendLocationEvent(name: "pushoverManagerPoll", value: "poll", data: [empty: true], isStateChange: true, descriptionText: "Sending Device Poll to Pushover Manager") }
public pushover_msg(msgDevices, data) { sendLocationEvent(name: "pushoverManagerMsg", value: "sendMsg", data: data, isStateChange: true, descriptionText: "Sending Message to ${msgDevices}") }
public pushover_handler(evt) {
    switch (evt?.value) {
        case "refresh":
            Map pomData = state?.pushoverManagerData ?: [:]
            pomData?.devices["${evt?.jsonData?.id}"] = [:]
            pomData?.devices["${evt?.jsonData?.id}"] = evt?.jsonData?.devices ?: []
            pomData?.devices["${evt?.jsonData?.id}"]?.appName = evt?.jsonData?.appName
            pomData?.sounds = evt?.jsonData?.sounds ?: []
            state?.pushoverManagerData = pomData
            break
        case "reset": 
            state?.pushoverManagerData = [:]

    } 
}