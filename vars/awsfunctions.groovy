def getInstanceDetails(String tagValue) {
    def instanceId = sh(script: "aws ec2 describe-instances --region eu-north-1 --filters 'Name=tag:servernumber,Values=${tagValue}' 'Name=instance-state-name,Values=stopped' | jq -r .Reservations[].Instances[].InstanceId", returnStdout: true).trim()

    if (instanceId) {
        // If instance not running, start it
        sh(script: "aws ec2 start-instances --region eu-north-1 --instance-ids ${instanceId}")
        sh(script: "aws ec2 wait instance-running --region eu-north-1 --instance-ids ${instanceId}")
    } else {
        // If instance already running, fetch its ID
        instanceId = sh(script: "aws ec2 describe-instances --region eu-north-1 --filters 'Name=tag:servernumber,Values=${tagValue}' 'Name=instance-state-name,Values=running' | jq -r .Reservations[].Instances[].InstanceId", returnStdout: true).trim()
    }
    
    def publicIp = sh(script: "aws ec2 describe-instances --region eu-north-1 --instance-ids ${instanceId} | jq -r .Reservations[].Instances[].PublicIpAddress", returnStdout: true).trim()
    return [instanceId: instanceId, publicIp: publicIp]
}

def GetInstanceDetails() {
    try {
        echo "Getting Instance Details..."

        def testDetails = getInstanceDetails("flask1")
        def prodDetails = getInstanceDetails("flask2")

        return [testInstance: testDetails.instanceId, testIp: testDetails.publicIp, prodInstance: prodDetails.instanceId, prodIp: prodDetails.publicIp]
    } catch (Exception e) {
        echo "[ERROR]: Failed to get instance details due to: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
        error "Failed to get instance details"
    }
}



def closeInstance(String instanceid)
{
    try
    {
        echo "Closing Instance..."
        sh "aws ec2 stop-instances --region eu-north-1 --instance-ids ${instanceid}"
        sh "aws ec2 wait instance-stopped --region eu-north-1 --instance-ids ${instanceid}"
    }
    catch (Exception e)
    {
        echo "[ERROR]: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
        error "Failed To Close Instance"
    }
}


def startInstance(String instanceid)
{
    try
    {
        echo "[INFO] Starting Instance..."
        sh "aws ec2 start-instances --region eu-north-1 --instance-ids ${instanceid}"
        sh "aws ec2 wait instance-running --region eu-north-1 --instance-ids ${instanceid}"
    }
    catch (Exception e)
    {
        echo "[ERROR]: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
        error "Failed To Start Instance"
    }
}


// This is the important part. It makes the functions accessible.
return this