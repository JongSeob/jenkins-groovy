pipeline {
    agent any
    
    environment {
        PAR_GRP_SIZE = "${params.PAR_GRP_SIZE ?: '2'}"
    }
    
    stages {
        stage('Initialize') {
            steps {
                script {
                    echo "Current job: ${env.JOB_NAME}"
                    jobs = getJobsInFolder(true)
                    echo "Jobs including sub-folders: ${jobs}"
                    echo "Parallel group size: ${env.PAR_GRP_SIZE}"
                }
            }
        }
        
        stage('Build Jobs') {
            steps {
                script {
                    def jobGroups = jobs.collate(env.PAR_GRP_SIZE.toInteger())
                    
                    jobGroups.each { jobGroup ->
                        def parallelStages = jobGroup.collectEntries { job ->
                            ["Build ${job}": {
                                stage("Build ${job}") {
                                    echo "Running ${job}"
                                    // Add actual job invocation code here if needed
                                    // build job: job
                                }
                            }]
                        }
                        parallel parallelStages
                    }
                }
            }
        }
    }
}

// 함수 정의는 pipeline 블록 외부에 위치
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