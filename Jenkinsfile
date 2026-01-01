pipeline {
    agent any

    tools {
        maven 'maven-3.9.9'
    }

    environment {
        // Selenium Grid
        COMPOSE_PATH = "${WORKSPACE}/docker"
        SELENIUM_GRID = "true"

        // Maven options
        MAVEN_OPTS = "-Xmx1024m"
    }

    stages {

        stage('Start Selenium Grid') {
            steps {
                script {
                    echo "=============================="
                    echo "Starting Selenium Grid via Docker Compose"
                    echo "=============================="
                    bat "docker compose -f ${COMPOSE_PATH}\\docker-compose.yml up -d"
                    echo "Waiting for Grid to stabilize..."
                    sleep 30
                }
            }
        }

        stage('Checkout Source Code') {
            steps {
                echo "Checking out source code from GitHub"
                git branch: 'main',
                    url: 'https://github.com/sadiq123hkgn/Selenium-Test-Framework.git'
            }
        }

        stage('Maven Clean & Compile') {
            steps {
                echo "Running Maven clean & compile"
                bat 'mvn clean compile'
            }
        }

        stage('Execute Tests on Selenium Grid') {
            steps {
                echo "Executing TestNG tests on Selenium Grid"
                bat 'mvn clean test -DseleniumGrid=true'
            }
        }

        stage('Stop Selenium Grid') {
            steps {
                script {
                    echo "Stopping Selenium Grid containers"
                    bat "docker compose -f ${COMPOSE_PATH}\\docker-compose.yml down"
                }
            }
        }

        stage('Publish Extent Report') {
            steps {
                publishHTML(target: [
                    allowMissing: false,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: 'src/test/resources/ExtentReport',
                    reportFiles: 'SparkReport.html',
                    reportName: 'Extent Automation Report'
                ])
            }
        }
    }

    post {

        always {
            echo "Archiving reports & publishing TestNG results"

            archiveArtifacts artifacts: '**/src/test/resources/ExtentReport/*.html',
                             fingerprint: true

            junit 'target/surefire-reports/*.xml'
        }

        success {
            emailext(
                to: 'sadiqmdsyed@gmail.com',
                subject: "✅ BUILD SUCCESS: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                mimeType: 'text/html',
                body: """
                <html>
                <body>
                    <h2 style="color:green;">Build Successful 🎉</h2>
                    <p><b>Project:</b> ${env.JOB_NAME}</p>
                    <p><b>Build Number:</b> ${env.BUILD_NUMBER}</p>
                    <p><b>Status:</b> SUCCESS ✅</p>
                    <p><b>Build URL:</b>
                       <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>
                    <p><b>Extent Report:</b>
                       <a href="http://localhost:8080/job/${env.JOB_NAME}/HTML_20Extent_20Automation_20Report/">
                       View Report</a></p>
                    <br/>
                    <p>Regards,<br/><b>Automation Team</b></p>
                </body>
                </html>
                """,
                attachLog: true
            )
        }

        failure {
            emailext(
                to: 'sadiqmdsyed@gmail.com',
                subject: "❌ BUILD FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                mimeType: 'text/html',
                body: """
                <html>
                <body>
                    <h2 style="color:red;">Build Failed ❌</h2>
                    <p><b>Project:</b> ${env.JOB_NAME}</p>
                    <p><b>Build Number:</b> ${env.BUILD_NUMBER}</p>
                    <p><b>Status:</b> FAILED ❌</p>
                    <p><b>Build URL:</b>
                       <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>
                    <p>Please check Jenkins console logs.</p>
                    <br/>
                    <p>Regards,<br/><b>Automation Team</b></p>
                </body>
                </html>
                """,
                attachLog: true
            )
        }
    }
}
