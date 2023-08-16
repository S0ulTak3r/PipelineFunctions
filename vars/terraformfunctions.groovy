def terraformApply(String directory,String clustername,String zone,String project)
{
    try
    {
        // Change the current working directory to where your Terraform files are
        dir(${directory}) 
        {
            // Initialize Terraform
            sh 'terraform init'
            sh 'terraform apply -auto-approve'
            echo 'Updating kubectl context...'
            sh "gcloud container clusters get-credentials ${clustername} --zone ${zone} --project ${project}"
        }
    }
    catch (Exception e)
    {
        echo "[ERROR]: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
        error "Failed To Apply Terraform"
    }
}

// This is the important part. It makes the functions accessible.
return this