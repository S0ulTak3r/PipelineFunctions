def installDependenciesSystemLvl(String ANSIBLEFOLDER, String playbook)
{
    try
    {
        dir("${ANSIBLEFOLDER}")
        {
            echo "instalsssssling Dependencies..."
            sh "ansible-playbook ${playbook}"
        }
    }

    catch (Exception e)
    {
        echo "[ERROR]: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
        error "Failed To Install Dependencies System Level"
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
        echo "[ERROR]: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
        error "Failed To Install Docker"
    }
}


// This is the important part. It makes the functions accessible.
return this