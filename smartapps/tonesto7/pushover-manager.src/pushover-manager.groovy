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
}

def mainPage() {
	return dynamicPage(name: "mainPage", title: "", install: true, uninstall: true) {
        def validated = (apiKey && userKey && getValidated())
        def devices = validated ? getValidated(true) : []
        section("API Authentication: (${validated ? "Good" : "Missing"})", hidden: validated, hideable: true) {
            input "apiKey", "text", title: "API Key:", description: "Pushover API Key", required: true, submitOnChange: true
            input "userKey", "text", title: "User Key:", description: "Pushover User Key", required: true, submitOnChange: true
        }
        if(validated) {
            section("Device Management:") {
                // log.debug "devices: ${getValidated("deviceList")}"
                paragraph title: "What are these?", "A device will be created for each device selected below...", state: "complete"
                input "pushDevices", "enum", title: "Select PushOver Clients", description: "", multiple: true, required: true, options: devices, submitOnChange: true
            }
        }
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	// unsubscribe()
	initialize()
}

def initialize() {
    addRemoveDevices()
    updateDevices()
}

def uninstalled() {
    log.warn "Uninstalled called... Removing all Devices..."
    addRemoveDevices(true)
}

def getValidated(devList=false){
    // if(devList) {
    //     log.debug "Generating Device List..." 
    // } else { log.debug "Validating Keys..." }
    
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
                        } else { log.error "Device List is empty" }
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
    def myOptions = []
    try {
        httpGet(uri: "https://api.pushover.net/1/sounds.json?token=${settings?.apiKey}") { resp ->
            if(resp?.status == 200) {
                log.debug "Found (${resp?.data?.sounds?.size()}) Sounds..."
                def mySounds = resp?.data?.sounds
                log.debug "mySounds: $mySounds"
                mySounds?.each { snd->
                    myOptions << ["${snd?.key}":"${snd?.value}"]
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

def addRemoveDevices(uninst=false) {
    //log.trace "addRemoveDevices($uninst)..."
    try {
        def delete = []
        if(uninst == false) {
            def devsInUse = []
            settings?.pushDevices?.each { dev ->
                def dni = "pushover_${dev}_device"

                def d = getChildDevice(dni)
                if(!d) {
                    def devSettings = [
                        // apiKey: [type:"text", value: settings?.apiKey], 
                        // userKey: [type:"text", value: settings?.userKey],
                        // deviceName: [type:"text", value: dev?.toString()]
                    ]
                    d = addChildDevice("tonesto7", "Pushover-Device", dni, null, [label: "Pushover - ${dev}", data: [apiKey: settings?.apiKey, userKey: settings?.userKey, deviceName: dev] ])
                    d.completedSetup = true
                    
                    log.info "PushOver Device Created: (${d?.displayName}) with id: [${dni}]"
                } else {
                    log.debug "found ${d?.displayName} with dni: ${dni} already exists"
                }
                devsInUse += dni
            }
            log.debug "devicesInUse: ${devsInUse}"
            // delete = app.getChildDevices(true).findAll { !(it?.deviceNetworkId in devsInUse) }
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
    getChildDevices(true)?.each { dev->
        dev?.updDataValue("apiKey", settings?.apiKey)
        dev?.updDataValue("userKey", settings?.userKey)
    }
}

