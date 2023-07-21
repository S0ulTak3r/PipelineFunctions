def TransferFile(String server, String fileLocation, String sshkey)
{
    try
    {
        echo "Transferring File To: ${server}..."
        sh "scp -o StrictHostKeyChecking=no -i ${sshkey} ${fileLocation} ec2-user@${server}:."
    }
    catch (Exception e)
    {
        echo "Error: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
        error "Failed To Transfer File To ${server}"
    }
}

// This is the important part. It makes the functions accessible.
return this