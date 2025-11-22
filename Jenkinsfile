pipeline {
    agent any

    tools {
        maven 'Maven'
        jdk 'JDK'
    }

    stages {
        stage('Clone') {
            steps {
                git url: 'https://github.com/SmollCoco/cargotracker'
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean install'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarServer') {
                    sh "mvn sonar:sonar -Dsonar.projectKey=cargotracker"
                }
            }
        }

        stage('Docker Build & Push') {
            steps {
                script {
                    // 1. Build the image [cite: 7, 40]
                    sh "docker build -t ${DOCKER_IMAGE} ."

                    // 2. Securely Login and Push
                    withCredentials([usernamePassword(credentialsId: DOCKER_CREDS_ID, usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                        sh "docker login -u ${DOCKER_USER} -p ${DOCKER_PASS}"
                        sh "docker push ${DOCKER_IMAGE}"
                    }
                }
            }
        }
    }
}



---


