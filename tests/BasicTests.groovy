package joelwetzel.dimmer_minimums.tests

import me.biocomp.hubitat_ci.util.device_fixtures.DimmerFixtureFactory
import me.biocomp.hubitat_ci.util.integration.IntegrationAppSpecification
import me.biocomp.hubitat_ci.util.integration.TimeKeeper

import spock.lang.Specification

/**
* Basic tests for dimmer-minimums.groovy
*/
class BasicTests extends IntegrationAppSpecification {
    def dimmerFixture = DimmerFixtureFactory.create('n')

    @Override
    def setup() {
        super.initializeEnvironment(appScriptFilename: "dimmer-minimums.groovy",
                                    userSettingValues: [dimmers: [dimmerFixture], minimumLevel: 5, enableLogging: true])
    }

    void "installed() logs the settings"() {
        when:
        appScript.installed()

        then:
        1 * log.info('Installed with settings: [dimmers:[GeneratedDevice(input: n, type: t)], minimumLevel:5, enableLogging:true]')
    }

    void "initialize() subscribes to events"() {
        when:
        appScript.initialize()

        then:
        // Expect that events are subscribe to
        1 * appExecutor.subscribe([dimmerFixture], 'level', 'levelHandler')
        1 * appExecutor.subscribe([dimmerFixture], 'switch.on', 'switchOnHandler')
    }
}
