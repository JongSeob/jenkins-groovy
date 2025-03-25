import jenkins.model.*
import com.cloudbees.hudson.plugins.folder.Folder // required to check if the item is a folder (instanceof Folder)

def getJobsInFolder(String folderPath, boolean recursive = false) {
    def folder = Jenkins.get().getItemByFullName(folderPath)
    if (!folder) return []

    def jobs = []
    def queue = [folder]

    while (queue) {
        def currentFolder = queue.remove(0)
        currentFolder.getItems().each { item ->
            if (item instanceof Job) {
                jobs.add(item.fullName.replaceFirst("^${folderPath}/", ''))
            } else if (recursive && item instanceof Folder) {
                queue << item // Add the sub-folder to the queue to process its items
            }
        }
    }

    // Exclude the current job from the list
    def currentJobName = env.JOB_NAME.split('/')[1]
    jobs.findAll { it != currentJobName }
}


// Test code: echo all jobs from the function
node {
    echo "Current job: ${env.JOB_NAME}"
    def folderName = env.JOB_NAME.split('/')[0]
    def jobs = getJobsInFolder(folderName, true)
    echo "Jobs including sub-folders: ${jobs}"
}

// Test code: Run all jobs from the function in each stage
node {
    def folderName = env.JOB_NAME.split('/')[0]
    def jobs = getJobsInFolder(folderName, true)

    jobs.each { job ->
        stage("Run ${job}") {
            echo "Running ${job}"
        }
    }
}