
def executeDockerCommandOnInstance(String command, String instanceip, String sshkey, String description) 
{
    try
    {
        echo "Executing Docker command for: ${description} on instance: ${instanceip}..."
        sh "ssh -o StrictHostKeyChecking=no -i ${sshkey} ec2-user@${instanceip} '${command}'"
    }
    catch (Exception e)
    {
        error "Failed to execute Docker command for ${description} on instance: ${instanceip}. Error: ${e.getMessage()}"
    }
}

def PullDockerCompose(String instanceip, String sshkey)
{
    executeDockerCommandOnInstance("docker-compose pull", instanceip, sshkey, "Pulling Docker images using Docker Compose")
}

def StartDockerCompose(String instanceip, String sshkey)
{
    executeDockerCommandOnInstance("docker-compose up --no-build -d", instanceip, sshkey, "Starting Docker containers using Docker Compose")
}

def cleanDockerContainers(String instanceip, String sshkey)
{
    executeDockerCommandOnInstance("sudo docker container prune --force", instanceip, sshkey, "Cleaning Docker containers")
    executeDockerCommandOnInstance("sudo docker image prune --force", instanceip, sshkey, "Cleaning Docker images")
}

def StopDockerCompose(String instanceip, String sshkey)
{
    executeDockerCommandOnInstance("docker-compose down --no-build", instanceip, sshkey, "Stopping Docker containers using Docker Compose")
}

def BuildAndPush(String project, String location)
{
    try
    {
        dir("\${location}") 
        {
            // Stage building
            echo "Building Docker image for project: ${project}..."
            sh "docker build -t ${project} ."
            
            // Stage pushing
            echo "Pushing Docker image for project: ${project} to Docker registry..."
            sh "docker push ${project}"
        }
    }
    catch (Exception e)
    {
        error "Failed to build and push Docker image for project: ${project}. Error: ${e.getMessage()}"
    }
}

// This is the important part. It makes the functions accessible.
return this
