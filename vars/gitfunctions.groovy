def clonegit(String gitUrl, String branchName = 'master', String destDir = '')
{
    try
    {
        echo 'Cloning Repository...'
        
        // Construct the git clone command
        def gitCmd = "git clone --depth 1 --branch ${branchName} --single-branch ${gitUrl}"
        
        // Append destination directory if provided
        if (destDir) {
            gitCmd += " ${destDir}"
        }
        
        sh gitCmd
        sh "git log -1"
    }
    catch (Exception e)
    {
        echo "[ERROR]: Failed to clone repository from URL: ${gitUrl}. Error: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
        error "Failed To Clone Repository"
    }
}



// This is the important part. It makes the functions accessible.
return this