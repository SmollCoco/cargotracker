pipeline {
    agent any
    environment {
        DOCKER_IMAGE = 'smollcoco/cargotracker-app:latest'
        SONAR_TOKEN = credentials('cargotracker-token-sonarqube') // ID from Phase 3
    }
    stages {
        stage('Compile & Test') { // [cite: 24, 25]
            steps {
                sh 'mvn clean package'
            }
        }
        stage('SonarQube Analysis') { // [cite: 27, 34]
            steps {
                withSonarQubeEnv('sonar-server') {
                    sh 'mvn sonar:sonar'
                }
            }
        }
        stage('Build Docker Image') { // [cite: 40]
            steps {
                sh "docker build -t ${DOCKER_IMAGE} ."
            }
        }
        stage('Push to Docker Hub') { // [cite: 41]
            steps {
                withCredentials([usernamePassword(credentialsId: 'docker-hub-creds', passwordVariable: 'PASS', usernameVariable: 'USER')]) {
                    sh "docker login -u $USER -p $PASS"
                    sh "docker push ${DOCKER_IMAGE}"
                }
            }
        }
    }
}
