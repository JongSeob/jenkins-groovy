def jobs = ["job1", "job2", "job3", "job4"] // 실행할 Jenkins job item 이름
def stages_to_run = [:] // stage 객체를 저장할 map

pipeline {
    agent any
    stages {
        stage("Prepare Stages") {
            steps {
                script {
                    // 각 job에 대해 stage 객체 생성 및 저장
                    jobs.each { jobName ->
                        stages_to_run[jobName] = {
                            stage("Build ${jobName}") {
                                echo "Triggering build for ${jobName}"
                                build job: jobName, wait: false // 비동기 실행.
                            }
                        }
                    }

                    echo "Stages prepared: ${stages_to_run.keySet()}"
                }
            }
        }

        stage("Execute in Parallel") {
            steps {
                script {
                    // 저장된 stage 객체를 parallel로 실행
                    parallel stages_to_run
                }
            }
        }
    }
}
