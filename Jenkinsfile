pipeline {
    agent any

    tools {
        maven 'Maven_Home'
    }

    environment {
        COMPOSE_PATH = "${WORKSPACE}/docker"
        SELENIUM_GRID = "true"
    }

    stages {

        stage('Start Selenium Grid via Docker Compose') {
            steps {
                echo "Starting Selenium Grid..."
                bat "docker compose -f ${COMPOSE_PATH}\\docker-compose.yml up -d"
                echo "Waiting for Grid to be ready..."
                sleep 30
            }
        }

        stage('Checkout Code') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/sadiq123hkgn/Selenium-Test-Framework.git'
            }
        }

        stage('Build & Execute Tests') {
            steps {
                bat 'mvn clean test -DseleniumGrid=true'
            }
        }

        stage('Stop Selenium Grid') {
            steps {
                echo "Stopping Selenium Grid..."
                bat "docker compose -f ${COMPOSE_PATH}\\docker-compose.yml down"
            }
        }

        stage('Publish Extent Report') {
            steps {
                publishHTML(target: [
                    reportDir: 'src/test/resources/ExtentReport',
                    reportFiles: 'SparkReport.html',
                    reportName: 'Extent Report',
                    keepAll: true,
                    alwaysLinkToLastBuild: true
                ])
            }
        }
    }

    post {
        always {
            junit 'target/surefire-reports/*.xml'
            archiveArtifacts artifacts: '**/src/test/resources/ExtentReport/*.html', fingerprint: true
        }

        success {
            echo "Build SUCCESS ✅"
        }

        failure {
            echo "Build FAILED ❌"
        }
    }
}
