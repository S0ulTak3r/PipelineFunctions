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
        echo "[ERROR]: ${e.getMessage()}"
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
        echo "[ERROR]: ${e.getMessage()}"
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
        sh "ssh -o StrictHostKeyChecking=no -i ${sshkey} ec2-user@${instanceip} 'sudo docker image prune --force'"
    } 
    catch (Exception e) 
    {
        echo "[ERROR]: ${e.getMessage()}"
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
        echo "[ERROR]: ${e.getMessage()}"
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
        echo "[ERROR]: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
        error "Failed to build and push ${project}"
    }
}

def BuildCheckAndPush(String project, String rootFolder, String applocation) {
    try {
        // Check for changes in the given location using git diff
        def changedFiles = sh(script: "git diff --name-only HEAD~1..HEAD", returnStdout: true).trim().split("\n")

        
        // Check if any of the changed files are from the checkLocation
        def hasRelevantChanges = changedFiles.any { it.startsWith(applocation) }

        if (!hasRelevantChanges) {
            echo "No changes detected in ${location}. Skipping build and push for ${project}."
            return
        }

        dir("${rootFolder}/${applocation}") { // Using original location here
            // Stage building
            echo "Building ${project} Docker Image..."
            sh "docker build -t ${project}:latest -t ${project}:1.${BUILD_NUMBER} ."
            sh "docker push --all-tags ${project}"
        }
    } catch (Exception e) {
        echo "[ERROR]: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
        error "Failed to build and push ${project}"
    }
}


def BuildCheckAndPushV2(String project, String rootFolder, String applocation) {
    try {
        // Fetch changes using changeSets
        def changeSets = currentBuild.changeSets
        def modifiedFiles = []
        
        for(changeSet in changeSets) {
            for(item in changeSet) {
                echo "Changes in ${item.getAffectedPaths()}"
                modifiedFiles += item.getAffectedPaths()
            }
        }
        
        // Check for changes in the given location
        def hasRelevantChanges = modifiedFiles.any { it.startsWith(applocation) }
        
        if (!hasRelevantChanges) {
            echo "No changes detected in the ${rootFolder}/${applocation}. Skipping build and push for ${project}."
            return
        }

        dir("${rootFolder}/${applocation}") 
        {
            // Stage building
            echo "Building ${project} Docker Image..."
            sh "docker build -t ${project}:latest -t ${project}:1.${BUILD_NUMBER} ."
            sh "docker push --all-tags ${project}"
        }
    } catch (Exception e) {
        echo "[ERROR]: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
        error "Failed to build and push ${project}"
    }
}






def loginDockerHub()
{
    try
    {
        //MAKE SURE TO ADD CREDENTIALS TO JENKINS
        echo "Logging into DockerHub..."
        withCredentials([usernamePassword(credentialsId: 'DockerLogin', usernameVariable: 'DOCKERHUB_USERNAME', passwordVariable: 'DOCKERHUB_PASSWORD')]) 
        {
            sh "docker login -u ${DOCKERHUB_USERNAME} -p ${DOCKERHUB_PASSWORD}"
        }

    }
    catch (Exception e)
    {
        echo "[ERROR]: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
        error "Failed to login to DockerHub"
    }
}


def deleteImageVersion(String image)
{
    try 
    {
        echo "attempting Cleanup"
        sh "docker image prune -f"
        sh "docker images | grep -w '${image}' | grep -w 1\\.[0-9]* | awk '{print \$2}' | xargs -I {} docker rmi ${image}:{}"
    } 
    catch (Exception e)
    {
        echo "[ERROR]: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
        error "Failed to cleanup"
    }
}
// This is the important part. It makes the functions accessible.
return this

