pipeline {
    agent any
    
    stages {
        stage('Initialize') {
            steps {
                script {
                    echo "Current job: ${env.JOB_NAME}"
                    jobs = getJobsInFolderWithName("1", true)
                    echo "Jobs including sub-folders: ${jobs}"
                }
            }
        }
        
        stage('Build Jobs') {
            steps {
                script {
                    jobs.each { job ->
                        stage("Build ${job}") {
                            echo "Running ${job}"
                        }
                    }
                }
            }
        }
    }
}

// Example) 
// return [job0, job1, job2, sub_folder/job0, sub_folder/job1]
def getJobsInFolderWithName(String pattern, boolean recursive = false) {
    def currFolderName = env.JOB_NAME.split('/')[0] // type: string
    def currFolder = Jenkins.get().getItemByFullName(currFolderName) // type: com.cloudbees.hudson.plugins.folder.Folder
    if (!currFolder) return []

    def jobs = []
    def queue = [currFolder]

    while (queue) {
        def currentFolder = queue.remove(0)
        currentFolder.getItems().each { item ->
            if (item instanceof Job) {
                def jobName = item.fullName.replaceFirst("^${currFolderName}/", '')
                if (jobName.contains(pattern)) {
                    jobs.add(jobName)
                }
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
import com.cloudbees.hudson.plugins.folder.Folder // required to check if the item is a folder (instanceof Folder)