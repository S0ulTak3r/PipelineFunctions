def packageHelmChart(String folder, String bucket, String bucketFolder) {
    // Check for changes in the mynewchart directory
    def chartChanges = sh(script: "git diff --name-only HEAD~1 HEAD | grep mynewchart || true", returnStdout: true).trim()

    if (chartChanges) {
        // Determine the type of change
        def changeType = 'patch'  // default to patch

        if (chartChanges.contains("Chart.yaml")) {
            changeType = 'major'
        } else if (chartChanges.contains("templates/")) {
            changeType = 'minor'
        } 

        // Bump version
        sh "bash ${folder}/scripts/versionBump.sh ${changeType}"

        // Extract the new version from Chart.yaml using awk
        def newVersion = sh(script: "awk '/name: myproject/{getline; print \$2}' ${folder}/mynewchart/Chart.yaml", returnStdout: true).trim()

        // Predict the packaged chart name
        def packagedChartName = "myproject-${newVersion}.tgz"

        // Package helm chart using the mynewchart directory
        sh "helm package ${folder}/mynewchart -d ${folder}"

        // Push the latest chart to GCS bucket, inside the specified folder
        sh "gsutil cp ${folder}/${packagedChartName} gs://${bucket}/${bucketFolder}/"

        // Delete the locally created package
        sh "rm ${folder}/${packagedChartName}"
    }

    // Get the list of all charts, sort them (assuming semantic versioning) to get the latest one
    def latestChart = sh(script: "gsutil ls gs://${bucket}/${bucketFolder}/myproject*.tgz | sort -V | tail -n 1", returnStdout: true).trim()

    // Pull the latest package from the GCS bucket, from the specified folder
    sh "gsutil cp ${latestChart} ${folder}/"
}

// This is the important part. It makes the functions accessible.
return this
