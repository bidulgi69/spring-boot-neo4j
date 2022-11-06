# spring-data-neo4j
spring boot + spring-data-neo4j<br>
그래프 기반의 데이터베이스인 [neo4j](https://neo4j.com/)를 활용해 단순한 요구사항들을 구현해보는 프로젝트 

## 목표(요구사항) 정의
> 언론사가 사용한 명사는 어떤 종류가 있을까?<br>

> 특정 명사에 대해 어느 언론사가 기사를 작성했을까?

## 사용한 라이브러리
- [Selenium](https://www.selenium.dev/)
- [Komoran](https://docs.komoran.kr/)
- Spring Batch
- Spring Data Neo4j

## Work Flow (Spring Batch)
- 뉴스 **uri** 수집<br><br>
<img src="https://user-images.githubusercontent.com/17774927/200138166-119a04c4-bcc1-41e6-ba27-f82ba5ddb9f3.png"><br>
<img src="https://user-images.githubusercontent.com/17774927/200138163-415fe131-ed7c-4ae4-ab9f-1d05c4b5fafc.png">
    1. Selenium, [Chrome Driver](https://chromedriver.chromium.org/downloads) 를 이용해 스크래핑을 수행합니다.
    2. 데이터는 [네이버 뉴스](https://news.naver.com/)의 정치, 경제, 사회 카테고리의 뉴스 기사들을 활용합니다.
    3. 헤드라인 뉴스와 아래 페이지가 적용된 섹션 부분의 뉴스 기사들을 수집합니다. (1~10 페이지, 헤드라인 뉴스는 고정됨)
    4. 뉴스 기사들에 대해 링크 값(href)을 중복되지 않도록 **Set** 에 저장합니다.
    5. 1 번의 Job 수행 시 720개의 링크가 수집됩니다.
<br><br>
- 헤드라인(기사 제목), 언론사, 기자 데이터 수집
    1. 위 step 에서 넘겨받은 기사 url 들에 접속해 헤드라인, 언론사, 기자 값을 수집합니다.
    2. html 이 로딩될 수 있도록 적당한 시간을 멈춰줘야 합니다. (_implicitlyWait 또는 Thread.sleep 을 통해 구현할 수 있습니다._)
<br><br>
- 헤드라인 형태소 분석
    1. Komoran 라이브러리를 활용해 헤드라인에서 명사(**NNP**) pos 를 갖는 단어들을 추출합니다.
    2. 추출된 단어들은 Keyword Node 로 활용됩니다.
<br><br>
- 데이터베이스 갱신<br><br>
<img src="https://user-images.githubusercontent.com/17774927/200138533-cea46651-d294-426f-b2fe-a5f1cf029753.png">
    1. 그래프 데이터베이스는 다음과 같은 구조를 갖습니다.<br>
  Node 는 언론사(**Press**), 명사(**Keyword**)로 구성되며 두 Node 간의 관계는 **Written** 이라는 Relationship 으로 연결됩니다.<br>
  (Keyword)-[:Written]->(Press) 의 형태로 나타낼 수 있습니다.
    2. `Neo4jTemplate` 클래스를 이용해 전처리된 데이터를 위와 같은 구조로 데이터베이스에 저장합니다.
<br><br>
- cleanup
    1. 스크래핑에 사용된 chromedriver 는 리소스를 많이 사용하기 때문에, 사용 후에 정리해줘야 합니다. `driver.close()`

## 요구사항 처리
> 언론사가 사용한 명사는 어떤 종류가 있을까?
```shell
curl -XGET localhost:8080/press/$언론사 -s | jq '.articles[].keyword.morph'
```
<img src="https://user-images.githubusercontent.com/17774927/200144341-fb9f7589-8db2-463b-8a44-2eb879615faa.png">

> 특정 명사에 대해 어느 언론사가 기사를 작성했을까?
```shell
curl -XGET localhost:8080/press/keyword/$명사 -s | jq
```
<img src="https://user-images.githubusercontent.com/17774927/200144275-ece6078e-c869-4497-921c-01f1c9e9f356.png">

## 사용법 (Intellij IDE)
1. [Chromedriver](https://chromedriver.chromium.org/downloads) 다운로드 후, `/src/main/resources/selenium/` 에 위치시켜 주세요.
2. 도커 이미지 빌드 및 실행, `mysql` 컨테이너는 entrypoint 스크립트 적용 후 재실행으로 인해 적당히 기다려줘야 합니다.
```shell
docker-compose build && docker-compose up -d && sleep 30
```
3. _(Optional)_ spring batch 에서 사용할 테이블이 정상적으로 생성됐는지 확인합니다.<br>
`BATCH_JOB_EXECUTION_CONTEXT` 등 총 9개의 테이블이 존재해야 합니다.
```shell
# 컨테이너 접속
docker-compose exec -it mysql mysql -uroot -p
root
```
```mysql
# mysql
use batch_job;
show tables;
```
4. `http://localhost:7474` 로 접속해 neo4j 브라우저를 실행시킵니다.<br>로그인은 기본값인 `id: neo4j, password: neo4j` 로 접속하면 되고,<br>password 를 변경한 뒤 application.yml 의 `spring:neo4j:authentication:password` 값을 바꿔주면 됩니다.
5. `SpringNeo4jApplication.kt` 의 main 함수를 실행시킵니다. Chrome 인스턴스가 실행되는 화면을 보고자 한다면,<br>`SeleniumConfiguration` 의 `ChromeOptions().setHeadless()` 값을 false 로 변경해주세요.
6. 요청을 보내 Batch Job 을 실행시킵니다. 약 15~30 분 정도의 시간이 소요됩니다. 
    ```shell
    curl -XGET localhost:8080/job/launch
    ```
8. 이후, **요구사항 처리**에 있는 요청을 통해 요구사항에 대해 알아볼 수 있습니다.
9. `make clean` 커맨드를 입력해 docker 컨테이너 및 이미지를 정리합니다.