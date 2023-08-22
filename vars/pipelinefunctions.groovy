def checkChanges(String jenkinsfile) 
{
    try 
    {
        echo "Checking for changes..."

        def changeSets = currentBuild.changeSets
        if (changeSets.size() == 0) 
        {
            echo 'No commits have been made. Proceeding with pipeline execution...'
            env.NO_CHANGES = "false"
        } 
        else 
        {
            def modifiedFiles = []
            for (changeSet in changeSets) 
            {
                for (item in changeSet) 
                {
                    modifiedFiles += item.getAffectedPaths()
                }
            }

            // List of files or patterns to exclude
            def excludedFiles = ["${jenkinsfile}"]
            modifiedFiles = modifiedFiles.findAll { !excludedFiles.contains(it) }

            if (modifiedFiles.isEmpty()) 
            {
                echo 'Skipping pipeline execution as the only changes are to excluded files.'
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
