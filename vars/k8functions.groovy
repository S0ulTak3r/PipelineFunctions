
def executeK8Command(String command, String description) 
{
    try
    {
        echo "Executing Kubernetes command for: ${description}..."
        sh "${command}"
    }
    catch (Exception e)
    {
        error "Failed to execute Kubernetes command for ${description}. Error: ${e.getMessage()}"
    }
}

def rollout(String deployment)
{
    executeK8Command("kubectl rollout restart ${deployment}", "rollout restart for deployment: ${deployment}")
}

def uninstallHelmRelease(String releaseName)
{
    executeK8Command("helm uninstall ${releaseName}", "uninstalling Helm release: ${releaseName}")
}

def installHelmRelease(String releaseName, String chartPath)
{
    executeK8Command("helm install ${releaseName} ${chartPath}", "installing Helm release: ${releaseName} using chart: ${chartPath}")
}

def upgradeHelmRelease(String releaseName, String chartPath)
{
    executeK8Command("helm upgrade ${releaseName} ${chartPath}", "upgrading Helm release: ${releaseName} using chart: ${chartPath}")
}

def deleteWithFile(String filepath)
{
    executeK8Command("kubectl delete -f ${filepath}", "deleting Kubernetes resources using file: ${filepath}")
}

def applyWithFile(String filepath)
{
    executeK8Command("kubectl apply -f ${filepath}", "applying Kubernetes resources using file: ${filepath}")
}

// This is the important part. It makes the functions accessible.
return this
