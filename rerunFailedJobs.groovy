def failedJobs = []
def jobs = []

pipeline {
    agent any
    
    stages {
        stage('Initialize') {
            steps {
                script {
                    echo "Current job: ${env.JOB_NAME}"
                    jobs = getJobsInFolder(true)
                    echo "Jobs including sub-folders: ${jobs}"
                }
            }
        }
        
        stage('Build Jobs') {
            steps {
                script {
                    jobs.each { job ->
                        stage("Build ${job}") {
                            try {
                                echo "Running ${job}"
                                build job: job
                                echo "Job ${job} completed successfully."
                            } catch (Exception e) {
                                echo "Job ${job} failed with error: ${e.message}"
                                failedJobs.add(job)
                                echo "Added ${job} to rerun queue."
                            }
                        }
                    }
                }
            }
        }
    }
    
    post {
        always {
            script {
                if (failedJobs.size() > 0) {
                    echo "Failed jobs to rerun: ${failedJobs}"
                    
                    stage('Rerun Failed Jobs') {
                        failedJobs.each { job ->
                            stage("rerun - ${job}") {
                                echo "Rerunning: ${job}"
                                retry(3) {
                                    build job: job
                                }
                            }
                        }
                    }
                } else {
                    echo "All jobs completed successfully."
                }
            }
        }
    }
}

// Function defined outside the pipeline block
def getJobsInFolder(boolean recursive = false) {
    // Split the full job name into parts by '/' to handle nested folder structures
    def jobNameParts = env.JOB_NAME.split('/')
    
    // Extract the current folder path excluding the job name
    def currFolderPath = jobNameParts.size() > 1 ? jobNameParts[0..-2].join('/') : jobNameParts[0]
    def currentJobName = jobNameParts.last()
    
    echo "Current folder path: ${currFolderPath}"
    def currFolder = Jenkins.get().getItemByFullName(currFolderPath)
    if (!currFolder) return []

    def jobs = []
    // Initialize queue for BFS (Breadth-First Search) traversal
    def queue = [currFolder]

    // Use BFS to traverse the folder structure
    while (queue) {
        def currentFolder = queue.remove(0)
        // Iterate through all items in the current folder
        currentFolder.getItems().each { item ->
            if (item instanceof Job) {
                def relativePath = item.fullName.replaceFirst("^${currFolderPath}/", '')
                jobs.add(relativePath)
            } else if (recursive && item instanceof Folder) {
                // Add subfolder to queue for processing if recursive flag is true
                queue << item
            }
        }
    }

    // Filter out the current job from the list
    return jobs.findAll { it != currentJobName }
}

import jenkins.model.*
import com.cloudbees.hudson.plugins.folder.Folder