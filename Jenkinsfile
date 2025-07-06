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
                echo 'Checking out source code from GitLab'
                checkout scm
                echo '✅ Source code checked out successfully'
            }
        }

        stage('Environment Info') {
            steps {
                echo 'Environment Information:'
                script {
                    if (isUnix()) {
                        sh 'java -version || echo "Java not found"'
                        sh './mvnw -version || echo "Maven Wrapper not found"'
                        sh 'echo "Build Number: ${BUILD_NUMBER}"'
                        sh 'echo "Branch: ${BRANCH_NAME}"'
                    } else {
                        bat 'java -version || echo "Java not found"'
                        bat 'mvnw.cmd -version || echo "Maven Wrapper not found"'
                        bat 'echo Build Number: %BUILD_NUMBER%'
                        bat 'echo Branch: %BRANCH_NAME%'
                    }
                }
            }
        }

        stage('Make Maven Wrapper Executable') {
            when {
                expression { isUnix() }
            }
            steps {
                sh 'chmod +x mvnw'
                echo 'Maven wrapper is now executable'
            }
        }

        stage('Clean') {
            steps {
                script {
                    if (isUnix()) {
                        sh './mvnw clean'
                    } else {
                        bat 'mvnw.cmd clean'
                    }
                }
                echo 'Clean completed'
            }
        }

        stage('Compile') {
            steps {
                script {
                    if (isUnix()) {
                        sh './mvnw compile'
                    } else {
                        bat 'mvnw.cmd compile'
                    }
                }
                echo 'Compilation successful'
            }
        }

        stage('Test') {
            steps {
                script {
                    if (isUnix()) {
                        sh './mvnw test'
                    } else {
                        bat 'mvnw.cmd test'
                    }
                }
                echo 'Tests completed'
            }
            post {
                always {
                    publishTestResults testResultsPattern: 'target/surefire-reports/*.xml'
                    echo 'Test results published'
                }
                failure {
                    echo 'Tests failed but continuing pipeline...'
                }
            }
        }

        stage('Package') {
            steps {
                script {
                    if (isUnix()) {
                        sh './mvnw package -DskipTests'
                    } else {
                        bat 'mvnw.cmd package -DskipTests'
                    }
                }
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
                script {
                    if (isUnix()) {
                        sh 'ls -la target/ || echo "Target directory not found"'
                        sh 'find target -name "*.jar" -type f || echo "No JAR files found"'
                    } else {
                        bat 'dir target || echo "Target directory not found"'
                        bat 'dir target\\*.jar || echo "No JAR files found"'
                    }
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
            echo "Build: ${BUILD_NUMBER}"
            echo "Application: ${APP_NAME}"
            script {
                if (isUnix()) {
                    sh 'echo "JAR file created: $(find target -name "*.jar" -type f)"'
                } else {
                    bat 'echo JAR file created in target directory'
                }
            }
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