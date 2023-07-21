def PullDockerCompose(String instanceip, String sshkey)
{
    try 
    {
        // function body
        echo "Pulling Docker-Compose..."
        sh "ssh -o StrictHostKeyChecking=no -i ${sshkey} ec2-user@${instanceip} 'docker-compose pull'"
    } 
    catch (Exception e) 
    {
        echo "Error: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
        error "Failed To Pull Images on: ${instanceip}"
    }
}

def StartDockerCompose(String instanceip, String sshkey)
{
    try 
    {
        // function body
        echo "Running Docker-Compose..."
        sh "ssh -o StrictHostKeyChecking=no -i ${sshkey} ec2-user@${instanceip} 'docker-compose up --no-build -d'"
    } 
    catch (Exception e) 
    {
        echo "Error: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
        error "Failed To Run Containers On: ${instanceip}"
    }
}

def cleanDockerContainers(String instanceip, String sshkey)
{
    try 
    {
        // function body
        echo "Cleaning Docker Containers..."
        sh "ssh -o StrictHostKeyChecking=no -i ${sshkey} ec2-user@${instanceip} 'sudo docker container prune --force'"
    } 
    catch (Exception e) 
    {
        echo "Error: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
        error "Failed To Clean Containers On: ${instanceip}"
    }

}

def StopDockerCompose(String instanceip, String sshkey)
{
    try 
    {
        echo "Stopping Docker-Compose..."
        sh "ssh -o StrictHostKeyChecking=no -i ${sshkey} ec2-user@${instanceip} 'docker-compose down --no-build'"
    } 
    catch (Exception e) 
    {
        echo "Error: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
        error "Failed To Stop Docker-Compose on: ${instanceip}"
    }
}



def BuildAndPush(String project, String location)
{
    try
    {
        dir("${location}") 
        {
            // Stage building
            echo "Building ${project} Docker Image..."
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


def loginDockerHub()
{
    try
    {
        echo "Logging into DockerHub..."
        withCredentials([usernamePassword(credentialsId: 'DockerLogin', usernameVariable: 'DOCKERHUB_USERNAME', passwordVariable: 'DOCKERHUB_PASSWORD')]) 
        {
            sh "docker login -u ${DOCKERHUB_USERNAME} -p ${DOCKERHUB_PASSWORD}"
        }

    }
    catch (Exception e)
    {
        echo "Error: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
        error "Failed to login to DockerHub"
    }
}
// This is the important part. It makes the functions accessible.
return this

