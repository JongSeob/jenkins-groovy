import jenkins.model.*
import com.cloudbees.hudson.plugins.folder.Folder // required to check if the item is a folder (instanceof Folder)

// Example) 
// return [job0, job1, job2, sub_folder/job0, sub_folder/job1]
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

node {
    echo "Current job: ${env.JOB_NAME}"
    def jobs = getJobsInFolder()
    echo "Jobs including sub-folders: ${jobs}"
}

node {
    def jobs = getJobsInFolder()

    jobs.each { job ->
        stage("Run ${job}") {
            echo "Running ${job}"
        }
    }
}