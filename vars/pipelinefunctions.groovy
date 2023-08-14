
def checkChanges()
{
    try
    {
        echo "Checking for changes..."
        def changeSets = currentBuild.changeSets
        if (changeSets.size() == 0)
        {
            println('No commits have been made. Proceeding with pipeline execution...')
            env.NO_CHANGES = "false"
        } 
        else 
        {
            def modifiedFiles = changeSets.collectMany { it.collect { item -> item.getAffectedPaths() } }.flatten()
            modifiedFiles = modifiedFiles.minus('Jenkinsfile-dockercompose2')
            
            if (modifiedFiles.isEmpty()) 
            {
                println('Skipping pipeline execution as the only change is to the Jenkinsfile.')
                env.NO_CHANGES = "true"
            }
            else
            {
                env.NO_CHANGES = "false"
            }
        }
    }
    catch (Exception e)
    {
        error "Failed to check changes. Error: ${e.getMessage()}"
    }
}

def cleanupWorkspace()
{
    try
    {
        echo "Cleaning up the workspace..."
        sh 'rm -rf *'
    }
    catch (Exception e)
    {
        error "Failed to clean up workspace. Error: ${e.getMessage()}"
    }
}

// This is the important part. It makes the functions accessible.
return this
