<img src="https://github.com/haticeerturk/scoper/blob/main/logo.png" width="300">

# scoper
This is a Burp Suite extension that allows users to easily add web addresses to the Burp Suite scope.

## Installation
To install the extension, follow these steps:

- Download the latest release from the GitHub repository.
- You can easily compile the project using the following command. This command creates a .jar file under the `@jar` folder.
    ```
    gradle wrapper --gradle-version=5.1.1 # To ensure compatibility with openJDK
    ./gradlew jar
    ```
- Open Burp Suite and go to the `Extender` tab.
- Click the `Add` button and select `Extension file` from the drop-down menu.
- Select the downloaded/compiled JAR file and click `Next` to install the extension.

## Usage
To use the extension, follow these steps:

- Copy the URLs you want to add to the scope from any source.
- Open the extension and paste the URLs into the input field.
- Click the `Verify` button to extract the web addresses and preview them in the list.
- Select the appropriate scope status for each web address by clicking the (`Full Scope`|`Out of scope`|`Ignore`) buttons.
    - `Full Scope`: Adds all subdomain records of a domain to the Burp Suite scope settings using the '\*' character (in the form "*.target.com").
    - `Out of Scope`: Adds the selected address to the out of scope section of the Burp Suite scope settings.
    - `Ignore`: Ignores the selected record. Does not add to the scope or out of scope page.
- Click the `Add To Scope` button to add the selected web addresses to the Burp Suite scope.
- To see a quick demo of how to use the extension, please refer to the following GIF:

> ![](https://github.com/haticeerturk/scoper/blob/main/gif1.gif)

Additionally, you can select `Full Scope` for any web address and it changes to full scope. To see this feature in action, please refer to the following GIF:

> ![](https://github.com/haticeerturk/scoper/blob/main/gif2.gif)

## Contact
If you have any questions or comments about the extension, please feel free to contact me at haticeerturk27@gmail.com.

---

Big thanks to Midjourney for the logo design. :orange_heart:

Enjoy!
