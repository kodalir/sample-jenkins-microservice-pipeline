package io.fourfinanceit.pipeline.example.jobs

import com.ofg.job.component.JobComponent
import com.ofg.pipeline.core.JenkinsVariables
import com.ofg.pipeline.core.JobType
import io.fourfinanceit.pipeline.example.MicroserviceProject
import io.fourfinanceit.pipeline.example.common.BuildConstants
import io.fourfinanceit.pipeline.example.common.Environment
import io.fourfinanceit.pipeline.example.common.MicroserviceVersion
import javaposse.jobdsl.dsl.Job

/**
 * @author Tomasz Uliński
 * @author Artur Gajowy
 */
abstract class AbstractRunSmokeTests extends MicroserviceJobDefinition {
    
    protected final MicroserviceVersion version
    protected final Environment environment = Environment.SMOKE_TESTING
    
    protected AbstractRunSmokeTests(MicroserviceVersion version) {
        this.version = version
    }
    
    @Override
    final JobType getJobType() {
        return new JobType(camelCaseToJobTypeFormat("RunSmokeTestsFor${version.label}"))
    }
    
    @Override
    final void configure(Job job, MicroserviceProject project, JenkinsVariables jenkinsVariables) {
        (
            configureScm(project) &
                configureSteps(project) &
                configurePublishers()
        ).applyOn(job)
    }
    
    protected abstract JobComponent<Job> configureScm(MicroserviceProject project)
    
    protected abstract JobComponent<Job> configureSteps(MicroserviceProject project)
    
    protected final Closure runSmokeTestsGradleStep(MicroserviceProject project) {
        return {
            def realm = project.realmDomainPart()
            def env = environment.getFrom(project)
            def port = project.port
            
            gradle("clean smokeTest -DsmokeTestAppUrl=\"http://app.${realm}.${env}.dev.4finance.net:${port}\"")
        }
    }
    
    protected abstract JobComponent<Job> configurePublishers()
    
    protected final Closure archiveJunitTestResults() {
        return {
            archiveJunit BuildConstants.DEFAULT_GRADLE_JUNIT_XML_REPORTS_PATH
        }
    }
}
