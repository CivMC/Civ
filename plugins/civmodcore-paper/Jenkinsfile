pipeline {
    agent any
    tools {
        maven 'Maven latest'
        jdk 'OpenJDK 11'
    }
    stages {
        stage ('Initialize') {
            steps {
                sh '''
                    echo "PATH = ${PATH}"
                    echo "M2_HOME = ${M2_HOME}"
                '''
            }
        }

        stage ('Build') {
            steps {
                sh 'mvn clean install -U javadoc:jar -DadditionalJOption=-Xdoclint:none -Dmaven.metadata.legacy=true' 
            }
            withMaven() {
             sh "mvn clean install -U javadoc:jar javadoc:javadoc deploy -DadditionalJOption=-Xdoclint:none -Dmaven.metadata.legacy=true"
            }
        }
    }
}