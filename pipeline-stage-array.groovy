def jobs = ["job1", "job2", "job3", "job4"] // 실행할 job 목록
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
                            stage(jobName) {
                                echo "Executing ${jobName}"
                                // sh "echo Running ${jobName}" // 실제 작업 명령어를 여기에 추가
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
