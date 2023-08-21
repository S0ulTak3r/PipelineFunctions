def clonegit(String gitUrl)
{
    try
    {
        //clonning frosm github to workspace
        echo 'Cloning Reposiitory...'
        sh "git clone ${gitUrl}"
        sh 'ls'
    }
    catch (Exception e)
    {
        echo "[ERROR]: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
        error "Failed To Clone Repository"
    }
}


// This is the important part. It makes the functions accessible.
return this