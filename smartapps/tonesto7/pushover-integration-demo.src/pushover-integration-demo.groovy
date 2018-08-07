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
def appDate() { return "8-07-2018" }

definition(
    name: "Pushover-Integration-Demo",
    namespace: "tonesto7",
    author: "Anthony Santilli",
    description: "Demo App to demonstrate using the Pushover Location Integration",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@3x.png")


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
        paragraph str, image: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@3x.png"
    }
}

def mainPage() {
    return dynamicPage(name: "mainPage", title: "", install: true, uninstall: true) {
        appInfoSect()
        if(state?.isInstalled == true) {
            // log.debug "pushDevices: ${getPushoverDevices()}"
            section("Test Notifications:", hideable: true, hidden: false) {
                input "pushoverDevices", "enum", title: "Select Devices", description: "Select Pushover Devices to use", groupedOptions: getPushoverDevices(), multiple: true, required: false, submitOnChange: true
                if(settings?.pushoverDevices) {
                    input "testSound", "enum", title: "Notification Sound:", description: "Select the Notification Sound", defaultValue: "pushover", required: false, multiple: false, submitOnChange: true, options: getPushoverSounds()
                    input "testMessage", "text", title: "Message to Send:", description: "Enter message to send...", required: false, submitOnChange: true
                    if(settings?.testMessage && settings?.testMessage?.length() > 0) {
                        href "sendMessagePage", title: "Send Message", description: ""
                    }
                }
            }
        } else {
            section() { paragraph "This is a new install... Press done to install and then come back into the app to configure" }
        }
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
                    title: app?.getLabel()+"Test", //Optional
                    html: false, //Optional
                    message: settings?.testMessage, //Required
                    priority: 0,  //Optional
                    retry: 30, //Requried for High-Priority
                    expire: 10800, //Requried for High-Priority
                    sound: settings?.testSound, //Optional (Defaults to Pushover sound)
                    url: "https://www.foreverbride.com/files/6414/7527/3346/test.png", //Optional
                    url_title: "Test Image" //Optional
                    // timestamp: new Date().getTime(), //Optional
                ]
                buildPushMessage(settings?.pushoverDevices, msgObj, true)
            }
            state?.testMessageSent = true
        }
    }
}

//PushOver-Manager Input Generation Functions
private getPushoverSounds(){return (Map) state?.pushoverManagerData?.sounds?:[:]}
private getPushoverDevices(){List opts=[];Map pd=state?.pushoverManagerData?:[:];pd?.apps?.each{k,v->if(v&&v?.devices&&v?.appId){Map dm=[:];v?.devices?.sort{}?.each{i->dm["${i}_${v?.appId}"]=i};addInputGrp(opts,v?.appName,dm);}};return opts;}
private inputOptGrp(List groups,String title){def group=[values:[],order:groups?.size()];group?.title=title?:"";groups<<group;return groups;}
private addInputValues(List groups,String key,String value){def lg=groups[-1];lg["values"]<<[key:key,value:value,order:lg["values"]?.size()];return groups;}
private listToMap(List original){original.inject([:]){r,v->r[v]=v;return r;}}
private addInputGrp(List groups,String title,values){if(values instanceof List){values=listToMap(values)};values.inject(inputOptGrp(groups,title)){r,k,v->return addInputValues(r,k,v)};return groups;}
private addInputGrp(values){addInputGrp([],null,values)}

//PushOver-Manager Location Event Subscription Events, Polling, and Handlers
public pushover_init(){subscribe(location,"pushoverManager",pushover_handler);pushover_poll()}
public pushover_cleanup(){state?.remove("pushoverManagerData");unsubscribe("pushoverManager");}
public pushover_poll(){sendLocationEvent(name:"pushoverManagerPoll",value:"poll",data:[empty:true],isStateChange:true,descriptionText:"Sending Poll Event to Pushover-Manager")}
public pushover_msg(List devs,Map data){if(devs&&data){sendLocationEvent(name:"pushoverManagerMsg",value:"sendMsg",data:data,isStateChange:true,descriptionText:"Sending Message to Pushover Devices: ${devs}");}}
public pushover_handler(evt){switch(evt?.value){case"refresh":Map pD=state?.pushoverManagerData?:[:];pD?.apps=[:];pD?.apps["${evt?.jsonData?.id}"]=[:];pD?.apps["${evt?.jsonData?.id}"]?.devices=evt?.jsonData?.devices?:[];pD?.apps["${evt?.jsonData?.id}"]?.appName=evt?.jsonData?.appName;pD?.apps["${evt?.jsonData?.id}"]?.appId=evt?.jsonData?.id;pD?.sounds=evt?.jsonData?.sounds?:[];state?.pushoverManagerData=pD;break;case "reset":state?.pushoverManagerData=[:];break;}}

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
    state?.remove("pushSounds")
}

def uninstalled() {
    log.warn "Uninstalled called..."    
}
