# Pushover-Manager
![][https://raw.githubusercontent.com/tonesto7/pushover-manager/master/images/icon-72.png "Pushover logo"]

## Important Note

**This app by itself is basically useless!!!  
It requires other community apps to make use of it by using the required locationEvents.**

## Summary

Pushover Manager is a SmartThings SmartApp that allows you to expose your Pushover devices to any 3rd-Party SmartApp that adds support for the locationEvents this SmartApp uses to Send customizable rich push notifications.
It was designed in the wake of SmartThings removing support for ContactBook which allowed selecting specific users to receive certain push notifications.

***Category:*** My Apps

***Author***: Anthony Santilli

***ST Community handle***: <https://community.smartthings.com/u/tonesto7/summary>

-----------
## Latest Versions

|**Application**|**Version**|**Location**|
|---------------|-----------|------------|
|**Pushover Manager**|*1.0.0*|[Pushover Manage Code](https://raw.githubusercontent.com/tonesto7/pushover-manager/master/smartapps/tonesto7/pushover-manager.src/pushover-manager.groovy)|
||

-----------
## Open Source License/Trademarks

Licensed under the Apache License, Version 2.0 (the "License"); you may not use **Pushover Manager** code except in compliance with the License. You may obtain a copy of the License at:

<http://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

All product and company names are trademarks™ or registered® trademarks of their respective holders. Use of them does not imply any affiliation with or endorsement by them.

------------
## Requirements

The following are the basic requirements to use **Pushover Manager**:
- A mobile device running the SmartThings Classic mobile application
- Access to your SmartThings IDE
- A community SmartApp with Pushover Manager Support

-------------
## Privacy/Usage Statement

The authors of this application will not be responsible for any damages that may occur as a result of any missed Notifications.

**Use Pushover Manager at your own risk!**

-------------
## App Code Installation

### Automated Community Installer (Highly Recommended)
This is the simplest way to Install Pushover Manager as well as other community apps

Please visit here for more info: [Things That Are Smart Wiki: SmartThings Community Installer](http://thingsthataresmart.wiki/index.php?title=Community_Installer_(Free_Marketplace))


### IDE Github Integration
The raw code for the SmartThings SmartApp is found on the GitHub site:

[(RAW) pushover-manager.groovy](https://raw.githubusercontent.com/tonesto7/pushover-manager/master/smartapps/tonesto7/pushover-manager.src/pushover-manager.groovy)

While on the GitHub site, find the **Raw** button and click it. This will bring up a non-formatted page with just the code present. Select all of the code (typically ***CTRL+A***) and copy It (typically ***CTRL+C***).

![](https://raw.githubusercontent.com/tonesto7/pushover-manager/master/images/ST-GitHub.png "ST-GitHub.png")

- Next, point your browser to you SmartThings IDE for your country (https://account.smartthings.com/login) and **Log In**.

    ![](https://raw.githubusercontent.com/tonesto7/pushover-manager/master/images/ST-Loginscreen.jpg "ST-Loginscreen.jpg")

- Once you are logged in, find the **My SmartApps** link on the top of the page. Clicking **My SmartApps** will allow you to produce a new SmartApp.

    ![](https://raw.githubusercontent.com/tonesto7/pushover-manager/master/images/700px-MySmartApps.png "ST-MySmartApps.png")

- Find the button on this page labeled **+New SmartApp** and click it.

    ![](https://raw.githubusercontent.com/tonesto7/pushover-manager/master/images/ST-NewSmartApp.png "ST-NewSmartApp.png")

- Since you already have the code in your computer’s clipboard, find the tab along the top section called **From Code**. In the area provided, paste (typically ***CTRL+V***) the code you copied from GitHub. Click **Create** in the bottom left corner of the page.

    ![](https://raw.githubusercontent.com/tonesto7/pushover-manager/master/images/ST-NewSmartAppCreate.png "ST-NewSmartAppCreate.png")

- This will bring up another page, with the code now formatted within the IDE. If the code was copied correctly, there are no other steps except to save and publish the code. In the upper right corner of the page, find and click **Save**. Now, click **Publish (For Me)**, and you should receive a confirmation that the code has been published successfully.

    ![](https://raw.githubusercontent.com/tonesto7/pushover-manager/master/images/ST-SavePublish.png "ST-SavePublish.png")

### Advanced Installation

For advanced users who have their SmartThings IDE integrated with GitHub, the installation and maintaining of SmartThings SmartApp code become's very simple. This manual will not go into detail about setting up your IDE with GitHub; those instructions can be found on the SmartThings website <http://docs.smartthings.com/en/latest/tools-and-ide/github-integration.html?highlight=git>

Once you have integration, the code you need will be available to you to download and keep in sync with the latest versions.

-   First, find the **Settings** button at the top of your SmartThings IDE page (this will only appear after you integrate with GitHub)
![](https://raw.githubusercontent.com/tonesto7/pushover-manager/master/images/ST-IDE-Settings.jpg "ST-IDE-Settings.jpg")

-   Clicking this button will open the GitHub Repository Integration page. To find the **Pushover Manager** SmartApp code, enter the information as you see it below:

    |**Owner**|**Name**|**Branch**|
    |---------|--------|----------|
    |tonesto7|pushover-manager|master|

    ![](https://raw.githubusercontent.com/tonesto7/pushover-manager/master/images/PM-AddRepoLink.png "PM-AddRepoLink.png")

The final step is to press the **Update** button at the bottom left corner of the screen, or go back to your code by using the button in the upper-right region of the page, then **Save**, then **Publish** the SmartApp again.

![](https://raw.githubusercontent.com/tonesto7/pushover-manager/master/images/ST-SavePublish.png "ST-SavePublish.png")

-------------
## SmartApp Install and Configuration


### SmartApp Install
Once you finished following the installation procedure above you will need to actually install the Pushover Manager SmartApp using the SmartThings Classic MobileApp

- Open the SmartThings Mobile App (It is important to use the Classic app, as we have reports the new mobile app does not play well with custom SmartApps):
- Go to "**Marketplace**" and select "**SmartApps**" tab.
- Marketplace is the "building" icon second from the right at the bottom of the mobile app
- Scroll to the bottom of the list, select"**My Apps**"
- Select"**Pushover Manager**" from the list.
- You will see this page on your screen:

    ![](https://raw.githubusercontent.com/tonesto7/pushover-manager/master/images/PM-AppNewInstall.png "PM-AppNewInstall.png")
- Tap on "**Done**" to complete the install
- Proceed to SmartApp Configuration


### SmartApp Configuration
Once you finished following the installation procedure above you will need to actually configure the smartapp.

- Tap on the "**Automation**" Tap at the Bottom of the page and select "**SmartApps**" tab
- Scroll down and Select "**Pushover Manager**" from the list.

    ![](https://raw.githubusercontent.com/tonesto7/pushover-manager/master/images/PM-SmartAppList.png "PM-SmartAppList.png")

- You will see this page on your screen:

    ![](https://raw.githubusercontent.com/tonesto7/pushover-manager/master/images/PM-AppHomeAuth.png "PM-AppHomeAuth.png")

- Tap on "**Configure your Pushover Keys**"
- Use the appropriate inputs to get both your User/Group Key and App Keys

    ![](https://raw.githubusercontent.com/tonesto7/pushover-manager/master/images/PM-AppAuthPage.png "PM-AppAuthPage.png")
- Copy/Paste each key into the inputs at the bottom of the page
- Tap on "**Done**"
- You will be returned to the main home page:

    ![](https://raw.githubusercontent.com/tonesto7/pushover-manager/master/images/PM-AppHomeConfigured.png "AppHomeConfigured.png")
    ![](https://raw.githubusercontent.com/tonesto7/pushover-manager/master/images/PM-AppHomeConfigured2.png "AppHomeConfigured2.png")
- Make sure to update the App Name if you plan on installing multiple versions to handle multiple user/group keys

-------------
## Community SmartApps with Pushover Manager Integration

- [NST Manager](https://community.smartthings.com/t/release-nst-manager-v5-0/)

-------------
## Developers: Adding Pushover Manager Support to your SmartApps

### Demo SmartApp
- [Pushover Integration Demo SmartApp](https://github.com/tonesto7/pushover-manager/blob/master/smartapps/tonesto7/pushover-integration-demo.src/pushover-integration-demo.groovy)


### Code Required: Add this Code block to your SmartApp (DO NOT Modify)

```groovy
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
```

### Example Code: Adding Device Input to Select Pushover Devices and Notification Sound
```groovy
input ("pushoverEnabled", "bool", title: "Use Pushover Integration", required: false, submitOnChange: true)
if(state?.isInstalled) {
    if(settings?.pushoverEnabled == true) {
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
}
```
- ***INFO***: By using the groupedOptions: parameter over options: this allows support for multiple Pushover-Manager SmartApp installs each with different user keys and devices.  So it groups the devices by the pushover manager SmartApp label in one list.  All you need to add for a supported input is the ```type: "enum", groupedOptions: getPushoverDevices()``` to any input to get the pushdevices available on your ST account.

    ![](https://raw.githubusercontent.com/tonesto7/pushover-manager/master/images/PM-DeviceAppGroups.png "DeviceAppGroups.png")

- ***INFO***: The input names can be whatever you want and there can be as many instances as you want.  Basically it just generates the list of device names to send any message to.

### Example Code: Adding to your Installed(), Updated(), and Initialize() Methods

```groovy
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
```

### Example Code: Sending a Pushover message

```groovy
def sendPushoverMessage() {
    Map msgObj = [
        title: app?.getLabel(), //Optional and can be what ever
        html: false, //Optional see: https://pushover.net/api#html
        message: "some message string here", //Required (HTML markup requires html: true, parameter)
        priority: 0,  //Optional
        retry: 30, //Requried only when sending with High-Priority
        expire: 10800, //Requried only when sending with High-Priority
        sound: settings?.pushoverSound, //Optional
        url: "https://www.foreverbride.com/files/6414/7527/3346/test.png", //Optional
        url_title: "Test Image" //Optional
    ]
    /* buildPushMessage()
        Param1: List of pushover Device Names
        Param2: Map msgObj above
        Param3: Boolean add timeStamp
    */
    buildPushMessage(settings?.pushoverDevices, msgObj, true) // This method is part of the required code block
}
```

## API References

[Pushover API Documentation](https://pushover.net/api)
- [Message Priority](https://pushover.net/api#priority)
- [HTML Message Markup](https://pushover.net/api#html)
- [Notification Sounds](https://pushover.net/api#sounds)
- [Message Count Limitations](https://pushover.net/api#limits)

---------------
