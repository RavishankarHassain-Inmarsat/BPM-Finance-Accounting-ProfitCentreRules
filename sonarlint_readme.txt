** SONARLINT README **

Installation:

1. Generate a Key for your sonarqube account via the following link:

http://172.31.45.134:9001/account/security


2. Create the following file:

~/.sonarlint/conf/global.json

or

D:\Users\{your_username}\.sonarlint\conf\global.json


3. Paste the following text within global.json

{
  servers: [
    {
      "id": "sonarqube-inmarsat",
      "url": "http://172.31.45.134:9001",
      "token": "YOUR_SONARQUBE_TOKEN_HERE"
    }
  ]
}


4. Update the "projectKey" in the sonarlint.json file in this directory to match the key for your project.


5. Run sonarlint_analysis.bat - an analysis report will be generated within the .sonarlint directory.


6. That should be a start. For additional issues or concerns follow the full howto here:

http://www.sonarlint.org/commandline/
