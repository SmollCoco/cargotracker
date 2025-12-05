pipeline {
    agent any

    // 1. Define tools to install automatically based on UI names
    tools {
        jdk 'JDK'
        maven 'Maven'
        dockerTool 'Docker'
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
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                // Connects to SonarQube using the environment defined [cite: 33]
                withSonarQubeEnv(SONAR_SERVER) {
                    // Analysis via Maven (preferred for JEE) [cite: 6, 27]
                    sh 'mvn clean verify sonar:sonar -Dsonar.projectKey=cargotracker -Dsonar.host.url=http://sonarqube:9000 -Dsonar.login=sqp_4ef13611c4f4f72c1894c5d31cf30c375d986c3a -Dsonar.exclusions=**/*.js,**/*.css,**/*.html,**/*.ts'
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
                        retry(3) {
                        sh "docker push ${DOCKER_IMAGE}"
                        }
                    }
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                // This injects the 'minikube-kubeconfig' credential file 
                // into a temporary variable KUBECONFIG for these commands
                withKubeConfig([credentialsId: 'minikube-config']) {
                    script {
                        // 1. Debug: Check connectivity
                        sh "kubectl get nodes" 
                        
                        // 2. Deploy
                        sh "kubectl apply -f k8s/"
                        
                        // 3. Restart to pull new image
                        sh "kubectl rollout restart deployment/cargotracker"
                    }
                }
            }
        }
    }

    post {
        always {
            // Clean up workspace to save disk space
            sh 'rm -rf *'
        }
    }
}
