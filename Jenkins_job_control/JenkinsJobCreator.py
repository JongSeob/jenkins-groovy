import xmltodict
import jenkins


import jenkins
import xmltodict


class JenkinsJobCreater:
    def __init__(self, jenkins_url, username, password):
        """
        JenkinsJobCreater 초기화
        :param jenkins_url: Jenkins 서버 URL
        :param username: Jenkins 사용자 이름
        :param password: Jenkins 비밀번호 또는 API 토큰
        """
        # Jenkins 서버 연결
        self.server = jenkins.Jenkins(
            jenkins_url, username=username, password=password)

        # 기본 빈 Jenkins job XML을 딕셔너리로 저장
        self.default_job_config = xmltodict.parse("""
        <project>
            <actions/>
            <description/>
            <keepDependencies>false</keepDependencies>
            <properties/>
            <scm class="hudson.scm.NullSCM"/>
            <canRoam>true</canRoam>
            <disabled>false</disabled>
            <blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding>
            <blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding>
            <triggers/>
            <concurrentBuild>false</concurrentBuild>
            <builders/>
            <publishers/>
            <buildWrappers/>
        </project>
        """)

    def create_job(self, job_name):
        """
        Jenkins 서버에 새로운 Job 생성
        :param job_name: 생성할 Job의 이름
        """
        # Job 이름을 description에 추가
        self.default_job_config['project']['description'] = f"Job: {job_name}"

        # XML 문자열로 변환
        job_config_xml = xmltodict.unparse(
            self.default_job_config, pretty=True)

        # Jenkins 서버에 Job 생성 요청
        try:
            self.server.create_job(job_name, job_config_xml)
            print(f"Job '{job_name}'이 성공적으로 생성되었습니다.")
        except jenkins.JenkinsException as e:
            print(f"Job 생성 중 오류가 발생했습니다: {e}")


# 사용 예제
if __name__ == "__main__":
    # Jenkins 서버 정보 입력

    jenkins_url = "http://localhost:18080"
    username = "admin"
    password = "11d65a72be11533292fcb8e66da8a6969c"

    # JenkinsJobCreater 객체 생성 및 Job 생성
    manager = JenkinsJobCreater(jenkins_url, username, password)
    manager.create_job("job5")
