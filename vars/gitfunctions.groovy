
def clonegit(String gitUrl)
{
    try
    {
        echo "Cloning repository from URL: ${gitUrl}..."
        sh "git clone ${gitUrl}"
    }
    catch (Exception e)
    {
        error "Failed to clone repository from URL ${gitUrl}. Error: ${e.getMessage()}"
    }
}

// This is the important part. It makes the functions accessible.
return this
