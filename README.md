**Pushover-Manager**

And since this is run by the community, by the community, new applications or device will show up as more and more people discover the power of SmartThings.

Summary
-------

![](https://raw.githubusercontent.com/tonesto7/pushover-manager/master/images/icon-72.png "https://raw.githubusercontent.com/tonesto7/pushover-manager/master/images/icon-72.png")

**Category:** My Apps

**Author**: Anthony Santilli

**ST Community handle**: <https://community.smartthings.com/u/tonesto7/summary>

**Documentation contributions:** Michael Struck

### Latest Versions

|**Application**|**Version**|**Location**|
|---------------|-----------|------------|
|**Pushover Manager**|*1.0.0*|<https://raw.githubusercontent.com/tonesto7/pushover-manager/master/smartapps/tonesto7/pushover-manager.src/pushover-manager.groovy>|
||

### Open Source License/Trademarks 

Licensed under the Apache License, Version 2.0 (the "License"); you may not use **Pushover Manager** code except in compliance with the License. You may obtain a copy of the License at:

  
  
<http://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

All product and company names are trademarks™ or registered® trademarks of their respective holders. Use of them does not imply any affiliation with or endorsement by them.

Requirements
------------

The following are the basic requirements to use **Pushover Manager**:

-   A mobile device running the SmartThings mobile application
-   Access to your SmartThings IDE
-   A community SmartApp with Pushover Manager Support

Privacy/Usage Statement
----------------------- 

The authors of this application will not be responsible for any damages that may occur as a result of any missed Notifications.

**Use Pushover Manager at your own risk!**

Installation
------------

The code for the SmartThings SmartApp is found on the GitHub site:

`  `[`https://raw.githubusercontent.com/tonesto7/pushover-manager/master/smartapps/tonesto7/pushover-manager.src/pushover-manager.groovy`](https://raw.githubusercontent.com/tonesto7/pushover-manager/master/smartapps/tonesto7/pushover-manager.src/pushover-manager.groovy)

While on the GitHub site, find the **Raw** button and click it. This will bring up a non-formatted page with just the code present. Select all of the code (typically CTRL+A) and copy It (typically CTRL+C).

![](https://raw.githubusercontent.com/tonesto7/pushover-manager/master/images/700px-GitHub.png "GitHub.png")

-   Next, point your browser to you SmartThings IDE for your country (https://account.smartthings.com/login) and **Log In**.

![](https://raw.githubusercontent.com/tonesto7/pushover-manager/master/images/700px-Loginscreen.jpg "loginscreen.jpg")

-   Once you are logged in, find the **My SmartApps** link on the top of the page. Clicking **My SmartApps** will allow you to produce a new SmartApp.

![](https://raw.githubusercontent.com/tonesto7/pushover-manager/master/images/700px-MySmartApps.png "MySmartApps.png")

-   Find the button on this page labeled **+New SmartApp** and click it.

![](https://raw.githubusercontent.com/tonesto7/pushover-manager/master/images/700px-NewSmartApp.png "NewSmartApp.png")

-   Since you already have the code in your computer’s clipboard, find the tab along the top section called **From Code**. In the area provided, paste (typically CTRL+V) the code you copied from GitHub. Click **Create** in the bottom left corner of the page.

![](https://raw.githubusercontent.com/tonesto7/pushover-manager/master/images/700px-NewSmartAppCreate.png "NewSmartAppCreate.png")

-    This will bring up another page, with the code now formatted within the IDE. If the code was copied correctly, there are no other steps except to save and publish the code. In the upper right corner of the page, find and click **Save**. Now, click **Publish (For Me)**, and you should receive a confirmation that the code has been published successfully.
![](https://raw.githubusercontent.com/tonesto7/pushover-manager/master/images/SavePublish.png "SavePublish.png")

### Advanced Installation

For advanced users who have their SmartThings IDE integrated with GitHub, the installation and maintaining of SmartThings SmartApp code become's very simple. This manual will not go into detail about setting up your IDE with GitHub; those instructions can be found on the SmartThings website <http://docs.smartthings.com/en/latest/tools-and-ide/github-integration.html?highlight=git>

Once you have integration, the code you need will be available to you to download and keep in sync with the latest versions.

-   First, find the **Settings** button at the top of your SmartThings IDE page (this will only appear after you integrate with GitHub)
![](https://raw.githubusercontent.com/tonesto7/pushover-manager/master/images/IdeSettings.jpg "IdeSettings.jpg")

-   Clicking this button will open the GitHub Repository Integration page. To find the **Pushover Manager** SmartApp code, enter the information as you see it below:

    |**Owner**|**Name**|**Branch**|
    |---------|--------|----------|
    |tonesto7|pushover-manager|master|

![](https://raw.githubusercontent.com/tonesto7/pushover-manager/master/images/CI-IDELink.png "CI-IDELink.png")

The final step is to press the **Update** button at the bottom left corner of the screen, or go back to your code by using the button in the upper-right region of the page, then **Save**, then **Publish** the SmartApp again.

![](https://raw.githubusercontent.com/tonesto7/pushover-manager/master/images/SavePublish.png "SavePublish.png")

Usage
-----

Once in follow the installation procedure above, along with enabling OAuth, you will be presented with the following screen:

![](https://raw.githubusercontent.com/tonesto7/pushover-manager/master/images/250px-CI-OpenScreen.png "CI-OpenScreen.png")

On this page you will be required to choose your specific login to the SmartThings environment. Valid choices are either **SmartThings** or **Samsung**.

Once selected, tap \<<Installer Home>\>\> and you will be prompted to login to the SmartThings.

`   `**`Please` `Note:`**
`   As mentioned in the privacy statement, your data is not uploaded`
`   to any third-party server. Instead, `**`Community` `Installer` `(Free` `Marketplace)`**
`   queries your SmartThings configuration, building you an installer`
`   HTML page custom to you.`

### Main Menu (Home)

Once you successfully login, you will see the main home page:

![](https://raw.githubusercontent.com/tonesto7/pushover-manager/master/images/250px-CI-Home.png "CI-Home.png")

Here, you can scroll through the available applications submitted to be included with **Community Installer (Free Marketplace)**. Please note not all available SmartThings developers submit their applications through this installer; if you are a developer and wish to include your SmartApp here, please see the section [Community\_Installer\_(Free\_Marketplace)\#Developers:\_How\_To\_Add\_Your\_Apps\_To\_Community\_Installer](Community_Installer_(Free_Marketplace)#Developers:_How_To_Add_Your_Apps_To_Community_Installer "wikilink")

From the home page, each application is listed with the name, the number of registered installs (from the installer) along with a cumulative rating from users across the SmartThings eco-system. You may also see a banner on the applications indicating whether the SmartApp or device has been installed or if an upgrade is available.

`   `**`Warning:`**
`   If you are an active developer for the SmartThings environment`
`   and regularly do development within the SmartThings IDE, you may`
`   see an notification (likely on your own SmartApps) that an upgrade`
`   is available when in reality, this flag is indicating there is a `
`   difference between the GitHub version and your IDE's version. Please`
`   note that upgrading from the GitHub will overwrite your current `
`   application code.`

### Search

Also included on the web page is a search feature. Use this area to search for specific programs or device type handlers (DTHs) that you are looking for.

![](CI-Search.png "CI-Search.png")

### Application Information

Tapping on any of the application boxes in the list on the home page will bring additional information about the application, including the author, project links, and even documentation and the GitHub information. In addition, you can rank the application.

![](CI-AboutApp.png "CI-AboutApp.png")

As you scroll down through the information you will also see information about individual components of the Smartapp, their version numbers, and the ability to select or deselect optional items to install.

![](CI-Install.png "CI-Install.png")

### Installation/Updates

At the bottom of the information page for each application you will have the ability to install the application. In addition, if you already have a listed application installed and an upgrade is available, you will be given the ability to upgrade your code in your SmartThings IDE automatically using the installer.

![](CI-AboutApp2.png "fig:CI-AboutApp2.png") ![](CI-Install2.png "fig:CI-Install2.png")

The installation will handle adding the SmartApp and/or devices to your SmartThings IDE, and even add the links (if available) to sync the developer's GitHub with your SmartThings IDE. Finally, if the application required OAuth to be enabled the installer will do that as well as publishing the application (it the author deemed it necessary).

Security
--------

Security is at the backbone of the **Community Installer (Free Marketplace)**. While you are required to log in to SmartThings to integrate this program, no information about your account or IDE is revealed to anyone.

![](CI-Refresh.png "fig:CI-Refresh.png")![](CI-AuthExpire.png "fig:CI-AuthExpire.png")

In addition if you leave your the **Community Installer (Free Marketplace)** open and go to other screens on your mobile device you may be required to refresh the page or even log back in. This is to ensure the security of your environment.

Current Featured Apps
---------------------

-   List to come soon (There is 15 apps currently)

Developers: How To Add Your Apps To Community Installer
-------------------------------------------------------

To maintain integrity of the marketplace, developers can not post directly to **Community Installer (Free Marketplace)**. Instead, the list of available applications is maintained by a small group of individuals (including the author of this application). This prevents rouge applications from being place into your IDE.

If you are a developer and would like to have your application available to the community through the **Community Installer (Free Marketplace)**, please follow the following steps:

-   The developers must fill out a JSON manifest that allows the installer to present the necessary information to the users.
-   TBD
-   TBD
-   TBD
-   TBD
