def curlTest(String server,String port="")
{
    try
    {
        echo "testing test environment..."
        retry(30) 
        {
            sleep 10 // Wait for 10 seconds between retries
            sh "curl http://${server}:${port}"
        }
    }
    catch (Exception e)
    {
        echo "[ERROR]: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
        error "Failed To Curl Test"
    }
}

// This is the important part. It makes the functions accessible.
return this