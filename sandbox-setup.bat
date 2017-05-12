@ECHO OFF
CLS
ECHO 1.Create Sandbox
ECHO 2.Delete Sandbox
ECHO 3.Update Configmap, App Secret
ECHO 4.Delete All Elements and Recreate (Much faster than Delete and Create Sandbox)
ECHO.

CHOICE /C 1234 /M "Enter your choice: "

IF ERRORLEVEL 4 GOTO RecreateSandbox
IF ERRORLEVEL 3 GOTO UpdateConfigSecret
IF ERRORLEVEL 2 GOTO DeleteSandbox
IF ERRORLEVEL 1 GOTO CreateSandbox

:CreateSandbox
set /p username= Openshift UserName:
set /p password= Openshift Password:
set /p project= Openshift Project (Not your App!) Name:
set /p artifact= Artifact (App) Name:
set /p ghusername= GitHub UserName:
set /p ghpassword= GitHub Password:
oc login https://console.dev-inmarsat.openshift.com:443 -u %username% -p %password%
oc new-project %project%
oc secrets new-basicauth sshsecret --username=%ghusername% --password=%ghpassword%
oc secrets add serviceaccount/builder secrets/sshsecret
oc create configmap %artifact% --from-file=environments\sandbox\configmap
oc create secret generic %artifact% --from-file=environments\sandbox\secret
oc create -f kube\build-deploy-sandbox.yml
pause
GOTO End

:DeleteSandbox
set /p username= Openshift UserName:
set /p password= Openshift Password:
set /p project= Openshift Project (Not your App!) Name:
oc login https://console.dev-inmarsat.openshift.com:443 -u %username% -p %password%
oc delete project %project%
pause
GOTO End

:UpdateConfigSecret
set /p username= Openshift UserName:
set /p password= Openshift Password:
set /p project= Openshift Project (Not your App!) Name:
set /p artifact= Artifact (App) Name:
oc login https://console.dev-inmarsat.openshift.com:443 -u %username% -p %password%
oc project %project%
oc delete configmap %artifact%
oc delete secret %artifact% 
oc create configmap %artifact% --from-file=environments\sandbox\configmap
oc create secret generic %artifact% --from-file=environments\sandbox\secret
pause
GOTO End

:RecreateSandbox
set /p username= Openshift UserName:
set /p password= Openshift Password:
set /p project= Openshift Project (Not your App!) Name:
set /p artifact= Artifact (App) Name:
oc login https://console.dev-inmarsat.openshift.com:443 -u %username% -p %password%
oc project %project%
oc delete all --all
oc delete configmap %artifact%
oc delete secret %artifact% 
oc delete template %artifact%-sandbox
oc create configmap %artifact% --from-file=environments\sandbox\configmap
oc create secret generic %artifact% --from-file=environments\sandbox\secret
oc create -f kube\build-deploy-sandbox.yml
pause
GOTO End
