def installDependenciesSystemLvl(String ANSIBLEFOLDER, String playbook)
{
    try
    {
        dir("${ANSIBLEFOLDER}")
        {
            echo "installing Dependencies..."
            echo "Executing Ansible playbook: ${playbook}..."
            sh "ansible-playbook ${playbook}"
        }
    }

    catch (Exception e)
    {
        echo "Error: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
        error "Failed to install dependencies using playbook: ${playbook}. Error: ${e.getMessage()}"
    }
}

def installDockerRemote(String ANSIBLEFOLDER , String playbook)
{
    try
    {
        dir("${ANSIBLEFOLDER}")
        {
            echo "Installing Docker..."
            sh "ansible-playbook -i aws_ec2.yml ${playbook}"
        }
    }
    catch (Exception e)
    {
        echo "Error: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
        error "Failed to install Docker using playbook: ${playbook}. Error: ${e.getMessage()}"
    }
}


// This is the important part. It makes the functions accessible.
return this