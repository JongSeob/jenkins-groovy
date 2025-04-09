import jenkins

# server = jenkins.Jenkins('http://localhost:18080', username='admin',
#                          password='11d65a72be11533292fcb8e66da8a6969c')

# Jenkins 서버 정보 설정
jenkins_url = "http://localhost:18080"
username = "admin"
password = "11d65a72be11533292fcb8e66da8a6969c"

# Jenkins 서버 연결
server = jenkins.Jenkins(jenkins_url, username=username, password=password)

# 모든 Job 가져오기
jobs = server.get_jobs()

# Job 개수 출력
print(f"총 Job 개수: {len(jobs)}")
