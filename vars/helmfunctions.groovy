def packageHelmChart(String folder, String bucket, String bucketFolder) {
    // Check for changes in the mynewchart directory
    def chartChanges = sh(script: "git diff --name-only HEAD~1 HEAD | grep mynewchart || true", returnStdout: true).trim()

    if (chartChanges) {
        // Fetch latest chart from GCS
        def latestChart = sh(script: "gsutil ls gs://${bucket}/${bucketFolder}/myproject*.tgz | sort -V | tail -n 1", returnStdout: true).trim()
        sh "gsutil cp ${latestChart} ${folder}/"

        // Unpack the chart
        sh "mkdir -p ${folder}/unpackedChart"
        sh "tar -xzvf ${folder}/myproject*.tgz -C ${folder}/unpackedChart"

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
        sh "rm -rf ${folder}/unpackedChart ${folder}/myproject*.tgz"
    }
}


// This is the important part. It makes the functions accessible.
return this
