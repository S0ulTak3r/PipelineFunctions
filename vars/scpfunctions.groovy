
def TransferFile(String server, String fileLocation, String sshkey)
{
    try
    {
        echo "Initiating file transfer..."
        echo "Source File: ${fileLocation}"
        echo "Destination Server: ${server}"
        sh "scp -o StrictHostKeyChecking=no -i ${sshkey} ${fileLocation} ec2-user@${server}:."
        echo "File transfer completed successfully."
    }
    catch (Exception e)
    {
        error "Failed to transfer file to ${server}. Error: ${e.getMessage()}"
    }
}

// This is the important part. It makes the functions accessible.
return this
