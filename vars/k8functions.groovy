def rollout(String deployment)
{
    try
    {
        echo 'Initiating Kubernetes rollout restart for deployment: ${deployment}...'
        sh "kubectl rollout restart ${deployment}"
    }
    catch (Exception e)
    {
        echo "[ERROR]: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
        error "Kubernetes rollout restart failed for deployment: ${deployment}. Refer to logs for more details."
    }
}

def uninstallHelmRelease(String releaseName)
{
    try
    {
        echo "Uninstalling Helm release: ${releaseName}"
        sh "helm uninstall ${releaseName}"
    }
    catch (Exception e)
    {
        echo "[ERROR]: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
        error "Failed to uninstall Helm release: ${releaseName}"
    }
}

def installHelmRelease(String releaseName, String chartPath)
{
    try
    {
        echo "Installing Helm release: ${releaseName} using chart: ${chartPath}"
        sh "helm install ${releaseName} ${chartPath}"
    }
    catch (Exception e)
    {
        echo "[ERROR]: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
        error "Failed to install Helm release: ${releaseName}"
    }
}

def upgradeHelmRelease(String releaseName, String chartPath)
{
    try
    {
        echo "Upgrading Helm release: ${releaseName} using chart: ${chartPath}"
        sh "helm upgrade ${releaseName} ${chartPath}"
    }
    catch (Exception e)
    {
        echo "[ERROR]: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
        error "Failed to upgrade Helm release: ${releaseName}"
    }
}


def deleteWithFile(String filepath)
{
    try
    {
        echo 'Deleting...'
        sh "kubectl delete -f ${filepath}"
    }
    catch (Exception e)
    {
        echo "[ERROR]: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
        error "Failed To Delete: ${filepath}"
    }
}

def applyWithFile(String filepath)
{
    try
    {
        echo 'Applying...'
        sh "kubectl apply -f ${filepath}"
    }
    catch (Exception e)
    {
        echo "[ERROR]: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
        error "Failed To Apply: ${filepath}"
    }
}

def changeContext(String context)
{
    try
    {
        echo 'Changing Context...'
        sh "kubectl config use-context ${context}"
    }
    catch (Exception e)
    {
        echo "[ERROR]: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
        error "Failed To Change Context: ${context}"
    }
}

def getAllPods()
{
    try
    {
        echo 'Getting All Pods...'
        sh "kubectl get pods"
    }
    catch (Exception e)
    {
        echo "[ERROR]: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
        error "Failed To Get All Pods"
    }
}

def getAllInfo()
{
    try
    {
        echo 'Getting All Info...'
        sh "kubectl get all"
    }
    catch (Exception e)
    {
        echo "[ERROR]: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
        error "Failed To Get All Info"
    }
}

def startProxyRemote(String remoteHost)
{
    try
    {
        echo 'Starting Proxy...'
        sh """
            nohup ssh -o StrictHostKeyChecking=no creed@${remoteHost} "kubectl proxy --address=0.0.0.0 --port=8080 --accept-hosts=.*" &
        """
    }
    catch (Exception e)
    {
        echo "[ERROR]: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
        error "Failed To Open Proxy"
    }
}

def closeProxyRemote(String remoteHost)
{
    try
    {
        echo 'Getting All Info...'
        sh """
            ssh -o StrictHostKeyChecking=no creed@${remoteHost} "taskkill /F /IM kubectl.exe"
        """
    }
    catch (Exception e)
    {
        echo "[ERROR]: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
        error "Failed To Close Proxy"
    }
}

// This is the important part. It makes the functions accessible.
return this