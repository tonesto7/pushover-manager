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
def appVer() { return "v1.0.0" }
def appDate() { return "8-09-2018" }

definition(
    name: "Pushover-Integration-Demo",
    namespace: "tonesto7",
    author: "Anthony Santilli",
    description: "Demo App to demonstrate using the Pushover Location Integration",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    page(name: "mainPage")
    page(name: "sendMessagePage")
}

def appInfoSect()	{
    section() {
        def str = ""
        str += "${app?.name}"
        str += "\nVersion: ${appVer()}"
        str += "\nModified: ${appDate()}"
        paragraph str, image: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
    }
}

def mainPage() {
    return dynamicPage(name: "mainPage", title: "", install: true, uninstall: true) {
        appInfoSect()
        
        if(state?.isInstalled) {
            if(!atomicState?.pushoverManager) {
                section() {
                    paragraph "If this is your first time enabling Pushover leave this page and come back so the pushover devices can be populated"
                    pushover_init()
                }
            }
            section("Test Notifications:") {
                input "pushoverDevices", "enum", title: "Select Pushover Devices", description: "Tap to select", groupedOptions: getPushoverDevices(), multiple: true, required: false, submitOnChange: true
                if(settings?.pushoverDevices) {
                    input "pushoverSound", "enum", title: "Notification Sound (Optional)", description: "Tap to select", defaultValue: "pushover", required: false, multiple: false, submitOnChange: true, options: getPushoverSounds()
                    input "testMessage", "text", title: "Message to Send:", description: "Enter message to send...", required: false, submitOnChange: true
                    if(settings?.testMessage && settings?.testMessage?.length() > 0) {
                        href "sendMessagePage", title: "Send Message", description: ""
                    }
                }
            }
        } else { section() { paragraph "New Install Detected!!!\n\n1. Press Done to Finish the Install.\n2. Goto the Automations Tab at the Bottom\n3. Tap on the SmartApps Tab above\n4. Select ${app?.getLabel()} and Resume configuration", state: "complete" } }
        state?.testMessageSent = false
    }
}

def sendMessagePage() {
    return dynamicPage(name: "sendMessagePage", title: "Notification Test", install: false, uninstall: false) {
        section() {
            if(state?.testMessageSent == true) {
                paragraph title: "Oops", "Message Already Sent...\nGo Back to MainPage to Send again..."
            } else {
                paragraph title: "Sending Message: ", "${settings?.testMessage}", state: "complete"
                paragraph "Device(s): ${settings?.pushoverDevices}" 
                Map msgObj = [
                    title: app?.getLabel() + " (Demo Test)", //Optional and can be what ever
                    html: false, //Optional see: https://pushover.net/api#html
                    message: settings?.testMessage, //Required (HTML markup requires html: true, parameter)
                    priority: 0,  //Optional
                    retry: 30, //Requried only when sending with High-Priority
                    expire: 10800, //Requried only when sending with High-Priority
                    sound: settings?.pushoverSound //Optional
                ]
                /* buildPushMessage(List param1, Map param2, Boolean param3)
                    Param1: List of pushover Device Names
                    Param2: Map msgObj above
                    Param3: Boolean add timeStamp
                */
                buildPushMessage(settings?.pushoverDevices, msgObj, true) // This method is part of the required code block
            }
            state?.testMessageSent = true
        }
    }
}

//PushOver-Manager Input Generation Functions
private getPushoverSounds(){return (Map) atomicState?.pushoverManager?.sounds?:[:]}
private getPushoverDevices(){List opts=[];Map pmd=atomicState?.pushoverManager?:[:];pmd?.apps?.each{k,v->if(v&&v?.devices&&v?.appId){Map dm=[:];v?.devices?.sort{}?.each{i->dm["${i}_${v?.appId}"]=i};addInputGrp(opts,v?.appName,dm);}};return opts;}
private inputOptGrp(List groups,String title){def group=[values:[],order:groups?.size()];group?.title=title?:"";groups<<group;return groups;}
private addInputValues(List groups,String key,String value){def lg=groups[-1];lg["values"]<<[key:key,value:value,order:lg["values"]?.size()];return groups;}
private listToMap(List original){original.inject([:]){r,v->r[v]=v;return r;}}
private addInputGrp(List groups,String title,values){if(values instanceof List){values=listToMap(values)};values.inject(inputOptGrp(groups,title)){r,k,v->return addInputValues(r,k,v)};return groups;}
private addInputGrp(values){addInputGrp([],null,values)}
//PushOver-Manager Location Event Subscription Events, Polling, and Handlers
public pushover_init(){subscribe(location,"pushoverManager",pushover_handler);pushover_poll()}
public pushover_cleanup(){state?.remove("pushoverManager");unsubscribe("pushoverManager");}
public pushover_poll(){sendLocationEvent(name:"pushoverManagerCmd",value:"poll",data:[empty:true],isStateChange:true,descriptionText:"Sending Poll Event to Pushover-Manager")}
public pushover_msg(List devs,Map data){if(devs&&data){sendLocationEvent(name:"pushoverManagerMsg",value:"sendMsg",data:data,isStateChange:true,descriptionText:"Sending Message to Pushover Devices: ${devs}");}}
public pushover_handler(evt){Map pmd=atomicState?.pushoverManager?:[:];switch(evt?.value){case"refresh":def ed = evt?.jsonData;String id = ed?.appId;Map pA = pmd?.apps?.size() ? pmd?.apps : [:];if(id){pA[id]=pA?."${id}"instanceof Map?pA[id]:[:];pA[id]?.devices=ed?.devices?:[];pA[id]?.appName=ed?.appName;pA[id]?.appId=id;pmd?.apps = pA;};pmd?.sounds=ed?.sounds;break;case "reset":pmd=[:];break;};atomicState?.pushoverManager=pmd;}
//Builds Map Message object to send to Pushover Manager
private buildPushMessage(List devices,Map msgData,timeStamp=false){if(!devices||!msgData){return};Map data=[:];data?.appId=app?.getId();data.devices=devices;data?.msgData=msgData;if(timeStamp){data?.msgData?.timeStamp=new Date().getTime()};pushover_msg(devices,data);}

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
