@Library('civ_pipeline_lib')_

pipeline {
    agent any
    tools {
        maven 'Maven 3.6.3'
        jdk 'Java 8'
    }
    environment {
    	civ_dependent_plugins = "Citadel BanStick CivChat2"
    }
     stages {
        stage ('Build') {
            steps {
                civ_build_plugin()
            }
        }
        stage ('Archive binaries') {
            steps {
                civ_archive_artifacts()
            }
        }
//        stage ('Archive javadoc') {
//            steps {
//                civ_archive_javadoc()
//            }
//        }
        stage ('Aggregate reports') {
            steps {
                civ_aggregate_reports()
            }
        }
    }

    post {
        always {
           civ_post_always()
        }
        success {
           civ_post_success()
        }
        failure {
           civ_post_failure()
        }
    }
}
