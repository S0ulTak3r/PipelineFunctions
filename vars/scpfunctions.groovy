def TransferFile(String server, String fileLocation, String sshkey)
{
    try
    {
        echo "Initiating SCP transfer to server: ${server}..."
        sh "scp -o StrictHostKeyChecking=no -i ${sshkey} ${fileLocation} ec2-user@${server}:."
    }
    catch (Exception e)
    {
        echo "[ERROR]: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
        error "SCP Transfer failed for server: ${server}. Refer to logs for more details."
    }
}

// This is the important part. It makes the functions accessible.
return this