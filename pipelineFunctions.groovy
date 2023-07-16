def pullFlaskAndDB(String server)
{
    try 
    {
        // function body
        echo "cleaning"
        sh "ssh -o StrictHostKeyChecking=no -i ${env.SSHKEYLOCATION} ec2-user@${server} 'sudo docker container prune --force'"
        echo 'pull +run docker'
        sh "ssh -o StrictHostKeyChecking=no -i ${env.SSHKEYLOCATION} ec2-user@${server} 'docker-compose pull'"
        sh "ssh -o StrictHostKeyChecking=no -i ${env.SSHKEYLOCATION} ec2-user@${server} 'docker-compose up --no-build -d'"
    } 
    catch (Exception e) 
    {
        echo "Error: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
        error "Failed to pull and run docker on ${server}"
    }
}

def checkChanges()
{
    try
    {
        def changeSets = currentBuild.changeSets
        if (changeSets.size() == 0)
        {
            println('No commits have been made. Proceeding with pipeline execution...')
            env.NO_CHANGES = "false"
        } 
        else 
        {
            def modifiedFiles = []
            for(changeSet in changeSets) 
            {
                for(item in changeSet) 
                {
                    modifiedFiles += item.getAffectedPaths()
                }
            }
            modifiedFiles = modifiedFiles.minus('Jenkinsfile-dockercompose2')
            
            if (modifiedFiles.isEmpty()) 
            {
                println('Skipping pipeline execution as the only change is to the Jenkinsfile.')
                env.NO_CHANGES = "true"
            }
            else
            {
                env.NO_CHANGES = "false"
            }
        }
    }
    catch (Exception e) 
    {
        echo "Error: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
        error "Failed to check changes"
    }
}
def BuildAndPush(String project, String location)
{
    try
    {
        dir("${location}") 
        {
            // Stage building
            echo 'Building flask'
            sh "docker build -t ${project}:latest -t ${project}:1.${BUILD_NUMBER} ."
            sh "docker push --all-tags ${project}"
        }
    }
    catch  (Exception e)
    {
        echo "Error: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
        error "Failed to build and push ${project}"
    }
}

def TransferFile(String server, String fileLocation, String sshkey)
{
    try
    {
        echo "transferring docker-compose to ${server}"
        sh "scp -o StrictHostKeyChecking=no -i ${sshkey} ${fileLocation} ec2-user@${server}:."
    }
    catch (Exception e)
    {
        echo "Error: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
        error "Failed to transfer file to ${server}"
    }
}

def clone(String gitUrl)
{
    try
    {
        //clonning from github to workspace
        echo 'Cloning repository...'
        sh "git clone ${gitUrl}"
        sh 'ls'
    }
    catch (Exception e)
    {
        echo "Error: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
        error "Failed to clone repository"
    }
}

def installDependenciesSystemLvl()
{
    try
    {
        //Install dependencies
        echo 'Installing dependencies...'
        sh "ansible-playbook ${ANSIBLEFOLDER}/installLocal.yml"
        sh "pip install -r ${REQLOCATION}"
    }
    catch (Exception e)
    {
        echo "Error: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
        error "Failed to install dependencies system level"
    }
}

def GetInstanceDetails()
{
    try
    {
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
        echo "Error: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
        error "Failed to get instance details"
    }
    
}

def installDockerRemote(String ansible_folder , String playbook)
{
    try
    {
        dir("${ansible_folder}")
        {
            echo "installing docker"
            sh "ansible-playbook -i aws_ec2.yml ${playbook}"
        }
    }
    catch (Exception e)
    {
        echo "Error: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
        error "Failed to install docker"
    }
}


def closeInstance(String instanceid,String instanceip,String sshkey)
{
    try
    {
        echo "closing container"
        sh "ssh -o StrictHostKeyChecking=no -i ${sshkey} ec2-user@${instanceip} 'docker-compose down --no-build'"
        sh "aws ec2 stop-instances --region eu-north-1 --instance-ids ${instanceid}"
        sh "aws ec2 wait instance-stopped --region eu-north-1 --instance-ids ${instanceid}"
    }
    catch (Exception e)
    {
        echo "Error: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
        error "Failed to close instance"
    }
}