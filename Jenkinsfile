pipeline {
    agent {
        label "master"
    }
    tools {
        jdk "JAVA8"
    }
    stages {
        stage("Notify Discord") {
            steps {
                discordSend webhookURL: env.FDD_WH_ADMIN,
                        title: "Build Started: SDLink-Lib #${BUILD_NUMBER}",
                        link: env.BUILD_URL,
                        result: 'SUCCESS',
                        description: "Build: [${BUILD_NUMBER}](${env.BUILD_URL})"
            }
        }
        stage("Publish 116") {
            steps {
                sh "chmod +x ./gradlew"
                sh "./gradlew clean publish -Poshi_hack=true"
            }
        }
        stage("Publish Newer") {
            steps {
                sh "./gradlew clean publish"
            }
        }
    }
    post {
        always {
            sh "./gradlew --stop"
            deleteDir()

            discordSend webhookURL: env.FDD_WH_ADMIN,
                    title: "Build Finished: SDLink-Lib #${BUILD_NUMBER}",
                    link: env.BUILD_URL,
                    result: currentBuild.currentResult,
                    description: "Build: [${BUILD_NUMBER}](${env.BUILD_URL})\nStatus: ${currentBuild.currentResult}"
        }
    }
}
