
def checkChanges(String jenkinsfile)
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
            def modifiedFiles = []
            for(changeSet in changeSets) 
            {
                for(item in changeSet) 
                {
                    modifiedFiles += item.getAffectedPaths()
                }
            }
            modifiedFiles = modifiedFiles.minus("${jenkinsfile}")
            
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
        echo "[ERROR]: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
        error "Failed To Check Changes"
    }
}

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
            def modifiedFiles = []
            for(changeSet in changeSets) 
            {
                for(item in changeSet) 
                {
                    modifiedFiles += item.getAffectedPaths()
                }
            }
            modifiedFiles = modifiedFiles.minus("jenkinsfile")
            
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
        echo "[ERROR]: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
        error "Failed To Check Changes"
    }
}


def cleanupWorkspace() {
    try {
        cleanWs()
    } catch (Exception e) {
        echo "[ERROR]: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
        error "Failed To Cleanup Workspace"
    }
}

// This is the important part. It makes the functions accessible.
return this
