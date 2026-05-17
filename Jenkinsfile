pipeline {
    agent any

    environment {
        // You can set production environment variables here if needed
        COMPOSE_PROJECT_NAME = "edulearn"
    }

    stages {
        stage('Checkout') {
            steps {
                // Jenkins will pull the code from your GitHub repository automatically
                checkout scm
            }
        }

        stage('Compile Backend') {
            steps {
                // Compiles all 10 microservices into JAR files on the AWS server
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Deploy Services') {
            steps {
                // This builds the Docker images and starts all containers directly on the EC2
                // --build ensures it uses the fresh JAR files we just compiled
                sh 'docker-compose up -d --build'
            }
        }

        stage('Cleanup') {
            steps {
                // Remove old, unused Docker images to save disk space on your EC2
                sh 'docker image prune -f'
            }
        }
    }
}
