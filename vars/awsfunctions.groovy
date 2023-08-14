def GetInstanceDetails()
{
    try
    {
        echo "Getting Instance Details..."

        def testInstanceId = sh(script: "aws ec2 describe-instances --region eu-north-1 --filters 'Name=tag:servernumber,Values=flask1' 'Name=instance-state-name,Values=stopped' | jq -r .Reservations[].Instances[].InstanceId", returnStdout: true).trim()
        def prodInstanceId = sh(script: "aws ec2 describe-instances --region eu-north-1 --filters 'Name=tag:servernumber,Values=flask2' 'Name=instance-state-name,Values=stopped' | jq -r .Reservations[].Instances[].InstanceId", returnStdout: true).trim()
                        
        if(testInstanceId)
        {
            //Test Not Running
                echo "Starting stopped EC2 instances..."
                sh (script: "aws ec2 start-instances --region eu-north-1 --instance-ids ${testInstanceId}")
                sh (script: "aws ec2 wait instance-running --region eu-north-1 --instance-ids ${testInstanceId}")
        }
        else
        {
            //Gather Id with Running state
            testInstanceId= sh(script: "aws ec2 describe-instances --region eu-north-1 --filters 'Name=tag:servernumber,Values=flask1' 'Name=instance-state-name,Values=running' | jq -r .Reservations[].Instances[].InstanceId", returnStdout: true).trim()
        }
        def publicTestIp = sh(script: "aws ec2 describe-instances --region eu-north-1 --instance-ids ${testInstanceId} | jq -r .Reservations[].Instances[].PublicIpAddress", returnStdout: true).trim()
        env.testInstanceId = testInstanceId
        env.publicTestIp = publicTestIp


        if(prodInstanceId)
        {
            //prod not running
            sh (script: "aws ec2 start-instances --region eu-north-1 --instance-ids ${prodInstanceId}")
            sh (script: "aws ec2 wait instance-running --region eu-north-1 --instance-ids ${prodInstanceId}")
        }
        else
        {
            //Gather Id with Running state
            prodInstanceId= sh (script: "aws ec2 describe-instances --region eu-north-1 --filters 'Name=tag:servernumber,Values=flask2' 'Name=instance-state-name,Values=running' | jq -r .Reservations[].Instances[].InstanceId", returnStdout: true).trim()
        }
        def publicProdIp =sh(script: "aws ec2 describe-instances --region eu-north-1 --instance-ids ${prodInstanceId} | jq -r .Reservations[].Instances[].PublicIpAddress", returnStdout: true).trim() 
        env.prodInstanceId=prodInstanceId
        env.publicProdIp=publicProdIp
    }
    catch (Exception e) {
    error "Failed to retrieve or start EC2 instances. Error: ${e.getMessage()}"
    {
        echo "Error: ${e.getMessage()}"
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
    catch (Exception e) {
    error "Failed to retrieve or start EC2 instances. Error: ${e.getMessage()}"
    {
        echo "Error: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
        error "Failed To Close Instance"
    }
}


def startInstance(String instanceid)
{
    try
    {
        echo "Starting Instance..."
        sh "aws ec2 start-instances --region eu-north-1 --instance-ids ${instanceid}"
        sh "aws ec2 wait instance-running --region eu-north-1 --instance-ids ${instanceid}"
    }
    catch (Exception e) {
    error "Failed to retrieve or start EC2 instances. Error: ${e.getMessage()}"
    {
        echo "Error: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
        error "Failed To Start Instance"
    }
}


// This is the important part. It makes the functions accessible.
return this