pipeline {
    agent any

    stages {
        stage('Checkout Code') {
            steps {
                script {
                    def branchToCheckout = env.BRANCH_NAME ?: 'main'
                    echo "Checkout branch: ${branchToCheckout}"
                    git branch: branchToCheckout, 
                        url: 'https://github.com/Alrmendo/spring-petclinic-microservices.git'
                }
            }
        }

        stage('Detect Changes') {
            steps {
                script {
                    echo "Detecting changes in source code..."
                    def changedFiles = sh(script: 'git diff --name-only HEAD~1', returnStdout: true).trim()
                    echo "Changed files:\n${changedFiles}"
                    env.CHANGED_FILES = changedFiles
                }
            }
        }

        stage('Test Services') {
            parallel {
                stage('Test - Customers Service') {
                    when {
                        expression { env.CHANGED_FILES.contains('spring-petclinic-customers-service/') }
                    }
                    steps {
                        echo "Running tests for Customers Service..."
                        dir('spring-petclinic-customers-service') {
                            sh 'chmod +x mvnw' // Cấp quyền thực thi nếu chưa có
                            sh './mvnw clean test'
                        }
                    }
                }

                stage('Test - Visits Service') {
                    when {
                        expression { env.CHANGED_FILES.contains('spring-petclinic-visits-service/') }
                    }
                    steps {
                        echo "Running tests for Visits Service..."
                        dir('spring-petclinic-visits-service') {
                            sh 'chmod +x mvnw' 
                            sh './mvnw clean test'
                        }
                    }
                }
            }
        }

        stage('Debug') {
            steps {
                echo "Checking test report files..."
                sh 'find . -name "*.xml"'
            }
        }

        stage('Build Services') {
            parallel {
                stage('Build - Customers Service') {
                    when {
                        expression { env.CHANGED_FILES.contains('spring-petclinic-customers-service/') }
                    }
                    steps {
                        echo "Building Customers Service..."
                        dir('spring-petclinic-customers-service') {
                            sh 'chmod +x mvnw' 
                            sh './mvnw clean install -DskipTests'
                        }
                    }
                }
                
                stage('Build - Visits Service') {
                    when {
                        expression { env.CHANGED_FILES.contains('spring-petclinic-visits-service/') }
                    }
                    steps {
                        echo "Building Visits Service..."
                        dir('spring-petclinic-visits-service') {
                            sh 'chmod +x mvnw' 
                            sh './mvnw clean install -DskipTests'
                        }
                    }
                }
            }
        }
    }

    post {
        success {
            script {
                def commitId = env.GIT_COMMIT
                echo "Sending 'success' status to GitHub for commit: ${commitId}"
                def response = httpRequest(
                    url: "https://api.github.com/repos/Alrmendo/spring-petclinic-microservices/statuses/${commitId}",
                    httpMode: 'POST',
                    contentType: 'APPLICATION_JSON',
                    requestBody: """{
                        \"state\": \"success\",
                        \"description\": \"Build passed\",
                        \"context\": \"ci/jenkins-pipeline\",
                        \"target_url\": \"${env.BUILD_URL}\"
                    }""",
                    authentication: 'github-token'
                )
                echo "GitHub Response: ${response.status}"
            }
        }

        failure {
            script {
                def commitId = env.GIT_COMMIT
                echo "Sending 'failure' status to GitHub for commit: ${commitId}"
                def response = httpRequest(
                    url: "https://api.github.com/repos/Alrmendo/spring-petclinic-microservices/statuses/${commitId}",
                    httpMode: 'POST',
                    contentType: 'APPLICATION_JSON',
                    requestBody: """{
                        \"state\": \"failure\",
                        \"description\": \"Build failed\",
                        \"context\": \"ci/jenkins-pipeline\",
                        \"target_url\": \"${env.BUILD_URL}\"
                    }""",
                    authentication: 'github-token'
                )
                echo "GitHub Response: ${response.status}"
            }
        }

        always {
            echo "Pipeline finished."
        }
    }
}
