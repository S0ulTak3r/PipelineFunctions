def GetInstanceDetails()
{
    try
    {
        echo "Getting Instancssse Dessstails..."

        def instanceTestId = sh(script: "aws ec2 describe-instances --region eu-north-1 --filters 'Name=tag:servernumber,Values=flask1' 'Name=instance-state-name,Values=stopped' | jq -r .Reservations[].Instances[].InstanceId", returnStdout: true).trim()
        def instanceProdId = sh(script: "aws ec2 describe-instances --region eu-north-1 --filters 'Name=tag:servernumber,Values=flask2' 'Name=instance-state-name,Values=stopped' | jq -r .Reservations[].Instances[].InstanceId", returnStdout: true).trim()
                        
        if(instanceTestId)
        {
            //Test Not Running
                sh (script: "aws ec2 start-instances --region eu-north-1 --instance-ids ${instanceTestId}")
                sh (script: "aws ec2 wait instance-running --region eu-north-1 --instance-ids ${instanceTestId}")
        }
        else
        {
            //Gather Id with Running state
            instanceTestId= sh(script: "aws ec2 describe-instances --region eu-north-1 --filters 'Name=tag:servernumber,Values=flask1' 'Name=instance-state-name,Values=running' | jq -r .Reservations[].Instances[].InstanceId", returnStdout: true).trim()
        }
        def publicTestIp = sh(script: "aws ec2 describe-instances --region eu-north-1 --instance-ids ${instanceTestId} | jq -r .Reservations[].Instances[].PublicIpAddress", returnStdout: true).trim()
        env.instanceTestId = instanceTestId
        env.publicTestIp = publicTestIp


        if(instanceProdId)
        {
            //prod not running
            sh (script: "aws ec2 start-instances --region eu-north-1 --instance-ids ${instanceProdId}")
            sh (script: "aws ec2 wait instance-running --region eu-north-1 --instance-ids ${instanceProdId}")
        }
        else
        {
            //Gather Id with Running state
            instanceProdId= sh (script: "aws ec2 describe-instances --region eu-north-1 --filters 'Name=tag:servernumber,Values=flask2' 'Name=instance-state-name,Values=running' | jq -r .Reservations[].Instances[].InstanceId", returnStdout: true).trim()
        }
        def publicProdIp =sh(script: "aws ec2 describe-instances --region eu-north-1 --instance-ids ${instanceProdId} | jq -r .Reservations[].Instances[].PublicIpAddress", returnStdout: true).trim() 
        env.instanceProdId=instanceProdId
        env.publicProdIp=publicProdIp
    }
    catch (Exception e)
    {
        echo "[ERROR]: ${e.getMessage()}"
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