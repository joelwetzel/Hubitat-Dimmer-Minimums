package joelwetzel.dimmer_minimums.tests

import me.biocomp.hubitat_ci.util.device_fixtures.DimmerFixtureFactory
import me.biocomp.hubitat_ci.util.integration.IntegrationAppSpecification
import me.biocomp.hubitat_ci.util.integration.TimeKeeper

import spock.lang.Specification

/**
* Tests of behavior when an ON event is received.
*/
class SwitchOnTests extends IntegrationAppSpecification {
    def dimmerFixture1 = DimmerFixtureFactory.create('d1')
    def dimmerFixture2 = DimmerFixtureFactory.create('d2')

    @Override
    def setup() {
        super.initializeEnvironment(appScriptFilename: "dimmer-minimums.groovy",
                                    userSettingValues: [dimmers: [dimmerFixture1, dimmerFixture2], minimumLevel: 5, enableLogging: true])
        appScript.installed()
    }

    void "switchOnHandler() ensures minimum level"() {
        given:
        dimmerFixture1.initialize(appExecutor, [switch: "off", level: 0])

        when:
        dimmerFixture1.on()

        then:
        dimmerFixture1.currentValue('switch') == "on"
        dimmerFixture1.currentValue('level') == 5
    }

    void "switchOnHandler() does not change level if above the minimum"() {
        given:
        dimmerFixture1.initialize(appExecutor, [switch: "off", level: 99])

        when:
        dimmerFixture1.on()

        then:
        dimmerFixture1.currentValue('switch') == "on"
        dimmerFixture1.currentValue('level') == 99
    }

    void "switchOnHandler() adjusts correct dimmer from among multiple devices"() {
        given:
        dimmerFixture1.initialize(appExecutor, [switch: "off", level: 0])
        dimmerFixture2.initialize(appExecutor, [switch: "off", level: 0])

        when:
        dimmerFixture2.on()

        then:
        dimmerFixture2.currentValue('switch') == "on"
        dimmerFixture2.currentValue('level') == 5
        dimmerFixture1.currentValue('switch') == "off"
        dimmerFixture1.currentValue('level') == 0
    }
}
