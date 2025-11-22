pipeline {
    agent any

    // 1. Define tools to install automatically based on UI names
    tools {
        jdk 'JDK'
        maven 'Maven'
    }

    environment {
        // Your SonarQube server name defined in 'System' settings
        SONAR_SERVER = 'SonarQube'
        // Token secret ID from Credentials in Jenkins
        SONAR_TOKEN = credentials('cargotracker-token-sonarqube')
        // Your Docker image name
        DOCKER_IMAGE = 'smollcoco/cargotracker-app:latest'
        // The ID of the username/password credential you created in Jenkins for Docker Hub
        DOCKER_CREDS_ID = 'docker-hub-creds'
    }

    stages {
        stage('Checkout') {
            steps {
                // Clones the repo [cite: 4, 23]
                checkout scm
            }
        }

        stage('Compile & Test') {
            steps {
                // Uses the Maven tool installed above to compile and test [cite: 24, 25, 26]
                sh 'mvn clean package'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                // Connects to SonarQube using the environment defined [cite: 33]
                withSonarQubeEnv(SONAR_SERVER) {
                    // Analysis via Maven (preferred for JEE) [cite: 6, 27]
                    sh 'mvn sonar:sonar -Dsonar.exclusions=**/*.js,**/*.css,**/*.html,**/*.ts'
                }
            }
        }

        stage('Quality Gate') {
            steps {
                // Pauses pipeline until SonarQube returns quality status [cite: 34]
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
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

        // Optional: Stage for K8s Deployment would go here [cite: 8, 50]
    }

    post {
        always {
            // Clean up workspace to save disk space
            cleanWs()
        }
    }
}
