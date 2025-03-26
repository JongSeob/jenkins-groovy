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
                            echo "Running ${job}"
                        }
                    }
                }
            }
        }
    }
}

// 함수 정의는 pipeline 블록 외부에 위치
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