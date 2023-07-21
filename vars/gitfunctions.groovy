def clonegit(String gitUrl)
{
    try
    {
        //clonning from github to workspace
        echo 'Cloning Repository...'
        sh "git clone ${gitUrl}"
        sh 'ls'
    }
    catch (Exception e)
    {
        echo "Error: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
        error "Failed To Clone Repository"
    }
}


// This is the important part. It makes the functions accessible.
return this