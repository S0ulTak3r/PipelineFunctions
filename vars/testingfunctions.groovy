
def executeWithRetry(Closure action, int maxRetries = 30, int waitTime = 10, String description) 
{
    int retries = 0
    while (retries < maxRetries) 
    {
        try 
        {
            action()
            return
        } 
        catch (Exception e) 
        {
            retries++
            if (retries >= maxRetries) 
            {
                error "Failed to execute action for ${description} after ${maxRetries} attempts. Error: ${e.getMessage()}"
            }
            sleep(waitTime)
        }
    }
}

def curlTest(String server, String port="")
{
    executeWithRetry({
        echo "Sending curl request to: http://${server}:${port}..."
        sh "curl http://${server}:${port}"
    }, 30, 10, "curl test to server: ${server} on port: ${port}")
}

// This is the important part. It makes the functions accessible.
return this
