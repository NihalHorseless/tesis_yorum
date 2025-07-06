pipeline {
    agent any

    environment {
        MAVEN_OPTS = '-Dmaven.compiler.source=17 -Dmaven.compiler.target=17'
        APP_NAME = 'tesis-yorum'
        APP_VERSION = "${BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                echo 'Source code checked out successfully'
            }
        }

        stage('Environment Info') {
            steps {
                echo '📋 Environment Information:'
                sh 'java -version || echo "Java not found"'
                sh 'mvn -version || echo "Maven not found"'
                sh 'which java || echo "Java path not found"'
                sh 'which mvn || echo "Maven path not found"'
                sh 'echo "JAVA_HOME: $JAVA_HOME"'
                sh 'echo "Build Number: ${BUILD_NUMBER}"'
                sh 'echo "Branch: ${BRANCH_NAME}"'
            }
        }

        stage('Clean') {
            steps {
                sh 'mvn clean'
                echo 'Clean completed'
            }
        }

        stage('Compile') {
            steps {
                sh 'mvn compile'
                echo 'Compilation successful'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test'
                echo 'Tests completed'
            }
            post {
                always {
                    publishTestResults testResultsPattern: 'target/surefire-reports/*.xml'
                    echo 'Test results published'
                }
                failure {
                    echo 'Tests failed but continuing pipeline'
                }
            }
        }

        stage('Package') {
            steps {
                sh 'mvn package -DskipTests'
                echo 'Packaging completed'
            }
        }

        stage('Archive Artifacts') {
            steps {
                echo 'Archiving build artifacts...'
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true, allowEmptyArchive: true
                echo 'Artifacts archived successfully'
            }
        }

        stage('Build Summary') {
            steps {
                echo 'Build Summary:'
                sh 'ls -la target/ || echo "Target directory not found"'
                sh 'find target -name "*.jar" -type f || echo "No JAR files found"'
            }
        }
    }

    post {
        always {
            echo 'Cleaning up workspace'
            cleanWs()
        }

        success {
            echo 'Pipeline completed successfully!'
        }

        failure {
            echo 'Pipeline failed!'
            echo 'Check the console output for errors'
        }

        unstable {
            echo 'Pipeline completed with warnings'
        }
    }
}