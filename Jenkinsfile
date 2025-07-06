pipeline {
    agent any

    tools {
        maven 'Maven-3.8'
    }

    environment {
        MAVEN_OPTS = '-Dmaven.test.failure.ignore=true'
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out source code...'
                checkout scm
            }
        }

        stage('Clean') {
            steps {
                echo 'Cleaning previous builds...'
                script {
                    if (isUnix()) {
                        sh 'mvn clean'
                    } else {
                        bat 'mvn clean'
                    }
                }
            }
        }

        stage('Compile') {
            steps {
                echo 'Compiling source code...'
                script {
                    if (isUnix()) {
                        sh 'mvn compile'
                    } else {
                        bat 'mvn compile'
                    }
                }
            }
        }

        stage('Test') {
            steps {
                echo 'Running tests...'
                script {
                    if (isUnix()) {
                        sh 'mvn test'
                    } else {
                        bat 'mvn test'
                    }
                }
                echo 'Tests completed'
            }
            post {
                always {
                    // Publish test results using the correct step name
                    junit(
                        allowEmptyResults: true,
                        testResults: 'target/surefire-reports/*.xml'
                    )
                }
                failure {
                    echo 'Tests failed but continuing pipeline...'
                }
            }
        }

        stage('Package') {
            steps {
                echo 'Packaging application...'
                script {
                    if (isUnix()) {
                        sh 'mvn package -DskipTests'
                    } else {
                        bat 'mvn package -DskipTests'
                    }
                }
            }
        }

        stage('Archive Artifacts') {
            steps {
                echo 'Archiving artifacts...'
                archiveArtifacts(
                    artifacts: 'target/*.jar',
                    allowEmptyArchive: true,
                    fingerprint: true
                )
            }
        }

        stage('Build Summary') {
            steps {
                echo 'Build completed successfully!'
                script {
                    echo "Build Number: ${env.BUILD_NUMBER}"
                    echo "Build URL: ${env.BUILD_URL}"
                    echo "Job Name: ${env.JOB_NAME}"
                    echo "Workspace: ${env.WORKSPACE}"
                }
            }
        }
    }

    post {
        always {
            echo 'Cleaning up workspace...'
            cleanWs()
        }
        success {
            echo 'Pipeline completed successfully!'
            echo 'All stages passed without errors.'
        }
        failure {
            echo 'Pipeline failed!'
            echo 'Check the console output for errors'
        }
        unstable {
            echo 'Pipeline completed with warnings'
            echo 'Some tests may have failed'
        }
    }
}