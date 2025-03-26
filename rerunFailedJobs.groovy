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
    def currFolderName = env.JOB_NAME.split('/')[0] // type: string
    def currFolder = Jenkins.get().getItemByFullName(currFolderName) // type: com.cloudbees.hudson.plugins.folder.Folder
    if (!currFolder) return []

    def jobs = []
    def queue = [currFolder]

    while (queue) {
        def currentFolder = queue.remove(0)
        currentFolder.getItems().each { item ->
            if (item instanceof Job) {
                jobs.add(item.fullName.replaceFirst("^${currFolderName}/", ''))
            } else if (recursive && item instanceof Folder) {
                queue << item // Add the sub-folder to the queue to process its items
            }
        }
    }

    // Exclude the current job from the list
    def currentJobName = env.JOB_NAME.split('/')[1]
    jobs.findAll { it != currentJobName }
}

import jenkins.model.*
import com.cloudbees.hudson.plugins.folder.Folder