
def detectChangeType(String folder) 
{
    def chartChanges = sh(script: "git diff --name-only HEAD~1 HEAD | grep mynewchart || true", returnStdout: true).trim()
    if (chartChanges.contains("Chart.yaml")) 
    {
        return 'major'
    } 
    else if (chartChanges.contains("templates/")) 
    {
        return 'minor'
    } 
    else 
    {
        return 'patch'
    }
}

def packageHelmChart(String folder, String bucket, String bucketFolder) 
{
    try
    {
        echo "Checking for changes in the mynewchart directory..."
        def chartChanges = sh(script: "git diff --name-only HEAD~1 HEAD | grep mynewchart || true", returnStdout: true).trim()

        echo "Fetching the latest chart from GCS..."
        def latestChart = sh(script: "gsutil ls gs://${bucket}/${bucketFolder}/myproject*.tgz | sort -V | tail -n 1", returnStdout: true).trim()
        sh "gsutil cp ${latestChart} ${folder}/"

        if (chartChanges) 
        {
            echo "Changes detected in the chart. Processing modifications..."
            sh "mkdir -p ${folder}/unpackedChart"
            sh "tar -xzvf ${folder}/myproject*.tgz -C ${folder}/unpackedChart"
            sh "rsync -av --exclude='Chart.yaml' ${folder}/mynewchart/ ${folder}/unpackedChart/myproject/"

            def changeType = detectChangeType(folder)
            sh "bash ${folder}/scripts/versionBump.sh ${changeType} ${folder}/unpackedChart/myproject/Chart.yaml"

            def newVersion = sh(script: "awk '/name: myproject/{getline; print \$2}' ${folder}/unpackedChart/myproject/Chart.yaml", returnStdout: true).trim()
            def packagedChartName = "myproject-${newVersion}.tgz"

            sh "helm package ${folder}/unpackedChart/myproject -d ${folder}"
            sh "gsutil cp ${folder}/${packagedChartName} gs://${bucket}/${bucketFolder}/"
            sh "rm -rf ${folder}/unpackedChart"
        }
        else
        {
            echo "No changes detected in the Helm chart."
        }
    }
    catch (Exception e)
    {
        error "Failed to package and upload Helm chart. Error: ${e.getMessage()}"
    }
}

// This is the important part. It makes the functions accessible.
return this
