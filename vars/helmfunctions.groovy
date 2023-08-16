def packageHelmChart(String folder, String bucket, String bucketFolder) {
    // Check for changes in the mynewchart directory
    def chartChanges = sh(script: "git diff --name-only HEAD~1 HEAD | grep mynewchart || true", returnStdout: true).trim()

    // Fetch latest chart from GCS
    def latestChart = sh(script: "gsutil ls gs://${bucket}/${bucketFolder}/myproject*.tgz | sort -V | tail -n 1", returnStdout: true).trim()
    sh "gsutil cp ${latestChart} ${folder}/"

    if (chartChanges) {
        // Unpack the chart
        sh "mkdir -p ${folder}/unpackedChart"
        sh "tar -xzvf ${folder}/myproject*.tgz -C ${folder}/unpackedChart"

        // Copy changes from static mynewchart to the unpacked version, excluding Chart.yaml
        sh "rsync -av --exclude='Chart.yaml' ${folder}/mynewchart/ ${folder}/unpackedChart/myproject/"

        // Determine the type of change
        def changeType = 'patch'  // default to patch

        if (chartChanges.contains("Chart.yaml")) {
            changeType = 'major'
        } else if (chartChanges.contains("templates/")) {
            changeType = 'minor'
        }

        // Bump version in the unpacked chart's Chart.yaml
        sh "bash ${folder}/scripts/versionBump.sh ${changeType} ${folder}/unpackedChart/myproject/Chart.yaml"

        // Extract the new version from Chart.yaml using awk
        def newVersion = sh(script: "awk '/name: myproject/{getline; print \$2}' ${folder}/unpackedChart/myproject/Chart.yaml", returnStdout: true).trim()

        // Predict the packaged chart name
        def packagedChartName = "myproject-${newVersion}.tgz"

        // Repackage the chart
        sh "helm package ${folder}/unpackedChart/myproject -d ${folder}"

        // Upload the repackaged chart to GCS
        sh "gsutil cp ${folder}/${packagedChartName} gs://${bucket}/${bucketFolder}/"

        // Cleanup
        sh "rm -rf ${folder}/unpackedChart"
    }
}

def deployToK8s(String k8context,String packagename,String approotfolder,String environment,String deploymentName) 
{
    echo 'Making Sure I am in the right context...'
    k8functions.changeContext(${k8context})
    
    echo "Fetching the latest chart version..."
    def latestChart = sh(script: "ls ${approotfolder}/${packagename}*.tgz | sort -V | tail -n 1", returnStdout: true).trim()
    env.LATEST_CHART_PATH = latestChart

    echo "Deploying application using Helm..."
    def releaseName = ${deploymentName}
    sh "helm upgrade --install ${releaseName} ${env.LATEST_CHART_PATH} --set global.env=${environment}"

}




// This is the important part. It makes the functions accessible.
return this
