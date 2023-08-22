def PullDockerCompose(String instanceip, String sshkey, String tag,String dockercomposefile)
{
    try 
    {
        // function body
        echo "Pulling Docker-Compose..."
        sh "ssh -o StrictHostKeyChecking=no -i ${sshkey} ec2-user@${instanceip} 'RELEVANT_DOCKER_TAG=${tag} docker-compose -f ${dockercomposefile} pull'"
    } 
    catch (Exception e) 
    {
        echo "[ERROR]: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
        error "Failed To Pull Images on: ${instanceip}"
    }
}

def StartDockerCompose(String instanceip, String sshkey, String tag,String dockercomposefile)
{
    


    try 
    {
        // function body
        echo "Running Docker-Compose..."
        sh "ssh -o StrictHostKeyChecking=no -i ${sshkey} ec2-user@${instanceip} 'RELEVANT_DOCKER_TAG=${tag} docker-compose -f ${dockercomposefile} up --no-build -d'"
    } 
    catch (Exception e) 
    {
        echo "[ERROR]: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
        error "Failed To Run Containers On: ${instanceip}"
    }
}

def cleanDockerContainersAndImages(String instanceip, String sshkey)
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


def StopDockerCompose(String instanceip, String sshkey, String dockercomposefile)
{
    try 
    {
        echo "Stopping Docker-Compose..."
        sh "ssh -o StrictHostKeyChecking=no -i ${sshkey} ec2-user@${instanceip} 'docker-compose -f ${dockercomposefile} down'"
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
        echo "Building ${project} Docker Image... in location ${location}"
        dir("${location}") 
        {
            // Stage sbuilding
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
        echo "Checking for changes in ${rootFolder}/${applocation}"

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
            def newTag = "1.${BUILD_NUMBER}"  // This is a placeholder. Later, you can implement a logic for semantic versioning here.
            sh "docker build -t ${project}:latest -t ${project}:${newTag} ."
            sh "docker push --all-tags ${project}"
            env.RELEVANT_DOCKER_TAG = newTag // Set the environmental variable
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


def retainLatestImageVersionOnly(String image) {
    try {
        // Get all tags for the given image
        def tags = sh(script: "docker images ${image} --format '{{.Tag}}'", returnStdout: true).trim().split('\n')
        
        // Sort the tags. This assumes that the tags are semver compliant (or at least lexically sortable).
        tags.sort()

        // Keep the latest tag (last one in the sorted list)
        def latestTag = tags[-1]

        for (tag in tags) {
            if (tag != latestTag) {
                sh "docker rmi ${image}:${tag}"
            }
        }
    } catch (Exception e) {
        echo "[ERROR]: Failed to retain only the latest image version for ${image}. Error: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
        error "Failed to process image tags"
    }
}



def pruneDockerImages() {
    try {
        sh "docker image prune -f"
    } catch (Exception e) {
        echo "[ERROR]: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
        error "Failed to prune Docker images"
    }
}

def pruneDockerContainers() {
    try {
        sh "docker container prune -f"
    } catch (Exception e) {
        echo "[ERROR]: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
        error "Failed to prune Docker containers"
    }
}
// This is the important part. It makes the functions accessible.
return this

