pipeline {
    agent any

    tools {
        maven 'Maven-3.8'
        jdk 'Java-17'
    }

    environment {
        MAVEN_OPTS = '-Dmaven.compiler.source=17 -Dmaven.compiler.target=17'
        APP_NAME = 'tesis-yorum'
        APP_VERSION = "${BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out source code from GitLab'
                checkout scm
                echo 'Source code checked out successfully'
            }
        }

        stage('Environment Info') {
            steps {
                echo 'Environment Information:'
                sh 'java -version'
                sh 'mvn -version'
                sh 'echo "Build Number: ${BUILD_NUMBER}"'
                sh 'echo "Branch: ${BRANCH_NAME}"'
            }
        }

        stage('Clean') {
            steps {
                echo 'Cleaning previous builds'
                sh 'mvn clean'
                echo 'Clean completed'
            }
        }

        stage('Compile') {
            steps {
                echo 'Compiling source code'
                sh 'mvn compile'
                echo 'Compilation successful'
            }
        }

        stage('Test') {
            steps {
                echo 'Running tests'
                sh 'mvn test'
                echo 'Tests completed'
            }
            post {
                always {
                    echo 'Publishing test results'
                    publishTestResults testResultsPattern: 'target/surefire-reports/*.xml'
                    echo 'Test results published'
                }
            }
        }

        stage('Package') {
            steps {
                echo 'Packaging application'
                sh 'mvn package -DskipTests'
                echo 'Packaging completed'
            }
        }

        stage('Code Quality Check') {
            steps {
                echo 'Running code quality checks'
                sh 'mvn verify -DskipTests'
                echo 'Code quality check completed'
            }
        }

        stage('Archive Artifacts') {
            steps {
                echo 'Archiving build artifacts'
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                echo 'Artifacts archived successfully'
            }
        }

        stage('Deploy to Test Environment') {
            when {
                anyOf {
                    branch 'main'
                    branch 'master'
                    branch 'develop'
                }
            }
            steps {
                echo 'Deploying to test environment...'
                script {
                    sh '''
                        echo "Stopping previous application instance..."
                        pkill -f "tesis_yorum" || true

                        echo "Starting new application instance..."
                        nohup java -jar target/tesis_yorum-*.jar --server.port=8081 > application.log 2>&1 &

                        echo "Waiting for application to start..."
                        sleep 30

                        echo "Health check..."
                        curl -f http://localhost:8081/api/users || exit 1

                        echo "Application deployed successfully to test environment"
                    '''
                }
                echo 'Deployment to test environment completed'
            }
        }
    }

    post {
        always {
            cleanWs()
        }

        success {
            echo 'Pipeline completed successfully!'
        }

        failure {
            echo 'Pipeline failed!'
        }

        unstable {
            echo 'Pipeline completed with problems'
        }
    }
}