package joelwetzel.dimmer_minimums.tests

import me.biocomp.hubitat_ci.util.device_fixtures.DimmerFixtureFactory
import me.biocomp.hubitat_ci.util.integration.IntegrationAppSpecification
import me.biocomp.hubitat_ci.util.integration.TimeKeeper

import spock.lang.Specification

/**
* Tests of behavior when a LEVEL event is received.
*/
class SetLevelTests extends IntegrationAppSpecification {
    def dimmerFixture1 = DimmerFixtureFactory.create('d1')
    def dimmerFixture2 = DimmerFixtureFactory.create('d2')

    @Override
    def setup() {
        super.initializeEnvironment(appScriptFilename: "dimmer-minimums.groovy",
                                    userSettingValues: [dimmers: [dimmerFixture1, dimmerFixture2], minimumLevel: 5, enableLogging: true])
        appScript.installed()
    }

    void "levelHandler() ensures minimum level"() {
        given:
        dimmerFixture1.initialize(appExecutor, [switch: "on", level: 99])

        when:
        dimmerFixture1.setLevel(2)

        then:
        dimmerFixture1.currentValue('switch') == "on"
        dimmerFixture1.currentValue('level') == 5
    }

    void "setLevel() can turn on the dimmer"() {
        given:
        dimmerFixture1.initialize(appExecutor, [switch: "off", level: 99])

        when:
        dimmerFixture1.setLevel(2)

        then:
        dimmerFixture1.currentValue('switch') == "on"
        dimmerFixture1.currentValue('level') == 5
    }

    void "setLevel() does not turn on dimmer if zero"() {
        given:
        dimmerFixture1.initialize(appExecutor, [switch: "off", level: 99])

        when:
        dimmerFixture1.setLevel(0)

        then:
        dimmerFixture1.currentValue('switch') == "off"
        dimmerFixture1.currentValue('level') == 0
    }

    void "levelHandler() does not change level if above the minimum"() {
        given:
        dimmerFixture1.initialize(appExecutor, [switch: "on", level: 99])

        when:
        dimmerFixture1.setLevel(80)

        then:
        dimmerFixture1.currentValue('switch') == "on"
        dimmerFixture1.currentValue('level') == 80
    }

    void "levelHandler() adjusts correct dimmer from among multiple devices"() {
        given:
        dimmerFixture1.initialize(appExecutor, [switch: "on", level: 99])
        dimmerFixture2.initialize(appExecutor, [switch: "on", level: 99])

        when:
        dimmerFixture2.setLevel(2)

        then:
        dimmerFixture2.currentValue('switch') == "on"
        dimmerFixture2.currentValue('level') == 5
        dimmerFixture1.currentValue('switch') == "on"
        dimmerFixture1.currentValue('level') == 99
    }
}
