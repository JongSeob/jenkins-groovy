import jenkins
import xmltodict
import copy


class JenkinsJobManager:
    def __init__(self, jenkins_url, username, password):
        """
        JenkinsJobManager 초기화
        :param jenkins_url: Jenkins 서버 URL
        :param username: Jenkins 사용자 이름
        :param password: Jenkins 비밀번호 또는 API 토큰
        """
        # Jenkins 서버 연결
        self.server = jenkins.Jenkins(
            jenkins_url, username=username, password=password)

        # 초기 기본 Jenkins job XML.
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

        # 편집할 Job config
        self.job_config = copy.deepcopy(self.default_job_config)

    def get_job_config(self, job_name):
        """
        지정된 Job의 config.xml을 읽어와 self.job_config에 저장합니다.
        :param job_name: 읽어올 Job의 이름
        """
        try:
            # Jenkins 서버에서 Job 설정 가져오기
            config_xml = self.server.get_job_config(job_name)

            # XML 문자열을 딕셔너리 형태로 변환하여 저장
            self.job_config = xmltodict.parse(config_xml)
            print(f"Job '{job_name}'의 config.xml이 성공적으로 로드되었습니다.")

        except jenkins.JenkinsException as e:
            print(f"Job 설정을 가져오는 중 오류가 발생했습니다: {e}")

    def reconfig_job(self, job_name):
        config_xml = xmltodict.unparse(self.job_config, pretty=True)
        self.server.reconfig_job(job_name, config_xml)

    # def add_parameter(self, param_name, default_value=''):
    #     """
    #     XML에 새로운 파라미터를 추가합니다.
    #     :param param_name: 파라미터 이름
    #     :param default_value: 파라미터 기본값 (기본값은 빈 문자열)
    #     """

    #     # `properties` 태그가 비어 있다면 초기화
    #     if 'properties' not in self.job_config['project'] or not self.job_config['project']['properties']:
    #         self.job_config['project']['properties'] = {}

    #     # `hudson.model.ParametersDefinitionProperty`가 없으면 추가
    #     if 'hudson.model.ParametersDefinitionProperty' not in self.job_config['project']['properties']:
    #         self.job_config['project']['properties']['hudson.model.ParametersDefinitionProperty'] = {
    #             'parameterDefinitions': []
    #         }

    #     # 파라미터 정의 추가
    #     parameter_definitions = self.job_config['project']['properties'][
    #         'hudson.model.ParametersDefinitionProperty']['parameterDefinitions']

    #     # `parameterDefinitions`가 리스트가 아닌 경우 리스트로 변환 (xmltodict의 특성 때문)
    #     if not isinstance(parameter_definitions, list):
    #         parameter_definitions = [parameter_definitions]

    #     # 새로운 파라미터 추가
    #     parameter_definitions.append({
    #         'hudson.model.StringParameterDefinition': {
    #             'name': param_name,
    #             'defaultValue': default_value,
    #             'trim': 'false'
    #         }
    #     })

    #     # 업데이트된 리스트를 다시 할당
    #     self.job_config['project']['properties']['hudson.model.ParametersDefinitionProperty']['parameterDefinitions'] = parameter_definitions
    # def add_parameter(self, param_name, default_value=''):
    #     """
    #     XML에 새로운 파라미터를 추가합니다.
    #     :param param_name: 파라미터 이름
    #     :param default_value: 파라미터 기본값 (기본값은 빈 문자열)
    #     """
    #     if self.job_config is None:
    #         print("먼저 get_job_config()를 호출하여 Job 설정을 로드하세요.")
    #         return

    #     # `properties` 태그가 비어 있다면 초기화
    #     if 'properties' not in self.job_config['project'] or not self.job_config['project']['properties']:
    #         self.job_config['project']['properties'] = {}

    #     # `hudson.model.ParametersDefinitionProperty`가 없으면 추가
    #     if 'hudson.model.ParametersDefinitionProperty' not in self.job_config['project']['properties']:
    #         self.job_config['project']['properties']['hudson.model.ParametersDefinitionProperty'] = {
    #             'parameterDefinitions': []
    #         }

    #     # 파라미터 정의 가져오기
    #     parameter_definitions = self.job_config['project']['properties'][
    #         'hudson.model.ParametersDefinitionProperty']['parameterDefinitions']

    #     # `parameterDefinitions`가 리스트가 아닌 경우 리스트로 변환 (xmltodict의 특성 때문)
    #     if not isinstance(parameter_definitions, list):
    #         parameter_definitions = [parameter_definitions]

    #     # 새로운 파라미터 추가
    #     parameter_definitions.append({
    #         'hudson.model.StringParameterDefinition': {
    #             'name': param_name,
    #             'defaultValue': default_value,
    #             'trim': 'false'
    #         }
    #     })

    #     # 업데이트된 리스트를 다시 할당
    #     self.job_config['project']['properties']['hudson.model.ParametersDefinitionProperty']['parameterDefinitions'] = parameter_definitions

    def add_parameter(self, param_name, default_value=''):
        """
        XML에 새로운 파라미터를 추가합니다.
        :param param_name: 파라미터 이름
        :param default_value: 파라미터 기본값 (기본값은 빈 문자열)
        """

        # `properties` 태그가 비어 있다면 초기화
        if 'properties' not in self.job_config['project'] or not self.job_config['project']['properties']:
            self.job_config['project']['properties'] = {}

        # `hudson.model.ParametersDefinitionProperty`가 없으면 추가
        if 'hudson.model.ParametersDefinitionProperty' not in self.job_config['project']['properties']:
            self.job_config['project']['properties']['hudson.model.ParametersDefinitionProperty'] = {
                'parameterDefinitions': []
            }

        # 파라미터 정의 가져오기
        parameter_definitions = self.job_config['project']['properties'][
            'hudson.model.ParametersDefinitionProperty']['parameterDefinitions']

        # `parameterDefinitions`가 리스트인지 확인하고, 아니면 리스트로 변환
        if isinstance(parameter_definitions, dict):  # 단일 항목일 경우
            parameter_definitions = [parameter_definitions]

        # 새로운 파라미터 추가
        new_parameter = {
            'name': param_name,
            'defaultValue': default_value,
            'trim': 'false'
        }

        parameter_definitions.append(new_parameter)

        # 업데이트된 리스트를 다시 할당
        self.job_config['project']['properties']['hudson.model.ParametersDefinitionProperty']['parameterDefinitions'] = parameter_definitions

    def store_job_config(self, file_name="job_config.xml"):
        # self.job_config (딕셔너리)를 XML 문자열로 변환
        job_config_xml = xmltodict.unparse(self.job_config, pretty=True)

        # XML 문자열을 파일에 저장
        with open(file_name, "w", encoding="utf-8") as file:
            file.write(job_config_xml)
            print(f"'{file_name}' 파일이 성공적으로 생성되었습니다.")

    def create_job(self, job_name):
        """
        Jenkins 서버에 새로운 Job 생성 및 XML 파일 저장
        :param job_name: 생성할 Job의 이름
        """
        # Job 이름을 description에 추가 (예시로 사용)
        self.job_config['project']['description'] = f"Job: {job_name}"

        # XML 문자열로 변환
        job_config_xml = xmltodict.unparse(
            self.job_config, pretty=True)

        try:
            # Jenkins 서버에 Job 생성 요청
            self.server.create_job(job_name, job_config_xml)
            print(f"Job '{job_name}'이 성공적으로 생성되었습니다.")

            self.store_job_config()

        except jenkins.JenkinsException as e:
            print(f"Job 생성 중 오류가 발생했습니다: {e}")


# 사용 예제
if __name__ == "__main__":
    # Jenkins 서버 정보 입력
    jenkins_url = "http://localhost:18080"
    username = "admin"
    password = "11d65a72be11533292fcb8e66da8a6969c"

    # JenkinsJobManager 객체 생성 및 작업 수행
    manager = JenkinsJobManager(jenkins_url, username, password)

    # 기존 Job 설정 로드 및 편집 예제
    manager.get_job_config("job5")

    # 새로운 Job 생성 예제
    manager.add_parameter("PARAM_Z", "ZZZZ")
    manager.reconfig_job("job5")

    manager.store_job_config("job5_config.xml")

    # manager.create_job("job6")
