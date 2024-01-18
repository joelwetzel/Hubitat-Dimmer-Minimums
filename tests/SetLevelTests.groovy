package joelwetzel.dimmer_minimums.tests

import me.biocomp.hubitat_ci.util.device_fixtures.DimmerFixtureFactory
import me.biocomp.hubitat_ci.util.AppExecutorWithEventForwarding

import me.biocomp.hubitat_ci.api.app_api.AppExecutor
import me.biocomp.hubitat_ci.api.common_api.Log
import me.biocomp.hubitat_ci.app.HubitatAppSandbox
import me.biocomp.hubitat_ci.app.HubitatAppScript
import me.biocomp.hubitat_ci.api.common_api.DeviceWrapper
import me.biocomp.hubitat_ci.app.preferences.DeviceInputValueFactory
import me.biocomp.hubitat_ci.capabilities.GeneratedCapability
import me.biocomp.hubitat_ci.capabilities.Switch
import me.biocomp.hubitat_ci.capabilities.SwitchLevel
import me.biocomp.hubitat_ci.util.NullableOptional
import me.biocomp.hubitat_ci.validation.DefaultAndUserValues
import me.biocomp.hubitat_ci.validation.Flags
import me.biocomp.hubitat_ci.validation.GeneratedDeviceInputBase

import spock.lang.Specification

/**
* Tests of behavior when a LEVEL event is received.
*/
class SetLevelTests extends Specification {
    private HubitatAppSandbox sandbox = new HubitatAppSandbox(new File('dimmer-minimums.groovy'))

    def log = Mock(Log)

    def appExecutor = Spy(AppExecutorWithEventForwarding) {
        _*getLog() >> log
    }

    void "levelHandler() ensures minimum level"() {
        given:
        // Define a dimmer fixture
        def dimmerFixture = DimmerFixtureFactory.create('n')

        // Run the app sandbox, passing the dimmer fixture in.
        def appScript = sandbox.run(api: appExecutor,
            userSettingValues: [dimmers: [dimmerFixture], minimumLevel: 5, enableLogging: true],
            )
        appExecutor.setSubscribingScript(appScript)

        dimmerFixture.initialize(appExecutor, [switch: "on", level: 99])

        when:
        appScript.installed()
        dimmerFixture.setLevel(2)

        then:
        dimmerFixture.state.switch == "on"
        dimmerFixture.state.level == 5
    }

    void "setLevel() can turn on the dimmer"() {
        given:
        // Define a dimmer fixture
        def dimmerFixture = DimmerFixtureFactory.create('n')

        // Run the app sandbox, passing the dimmer fixture in.
        def appScript = sandbox.run(api: appExecutor,
            userSettingValues: [dimmers: [dimmerFixture], minimumLevel: 5, enableLogging: true],
            )
        appExecutor.setSubscribingScript(appScript)

        dimmerFixture.initialize(appExecutor, [switch: "off", level: 99])

        when:
        appScript.installed()
        dimmerFixture.setLevel(2)

        then:
        dimmerFixture.state.switch == "on"
        dimmerFixture.state.level == 5
    }

    void "setLevel() does not turn on dimmer if zero"() {
        given:
        // Define a dimmer fixture
        def dimmerFixture = DimmerFixtureFactory.create('n')

        // Run the app sandbox, passing the dimmer fixture in.
        def appScript = sandbox.run(api: appExecutor,
            userSettingValues: [dimmers: [dimmerFixture], minimumLevel: 5, enableLogging: true],
            )
        appExecutor.setSubscribingScript(appScript)

        dimmerFixture.initialize(appExecutor, [switch: "off", level: 99])

        when:
        appScript.installed()
        dimmerFixture.setLevel(0)

        then:
        dimmerFixture.state.switch == "off"
        dimmerFixture.state.level == 0
    }

    void "levelHandler() does not change level if above the minimum"() {
        given:
        // Define a dimmer fixture
        def dimmerFixture = DimmerFixtureFactory.create('n')

        // Run the app sandbox, passing the dimmer fixture in.
        def appScript = sandbox.run(api: appExecutor,
            userSettingValues: [dimmers: [dimmerFixture], minimumLevel: 5, enableLogging: true],
            )
        appExecutor.setSubscribingScript(appScript)

        dimmerFixture.initialize(appExecutor, [switch: "on", level: 99])

        when:
        appScript.installed()
        dimmerFixture.setLevel(80)

        then:
        dimmerFixture.state.switch == "on"
        dimmerFixture.state.level == 80
    }

    void "levelHandler() adjusts correct dimmer from among multiple devices"() {
        given:
        // Define two dimmer fixtures
        def dimmerFixture1 = DimmerFixtureFactory.create('n1')
        def dimmerFixture2 = DimmerFixtureFactory.create('n2')

        // Run the app sandbox, passing the dimmer fixtures in.
        def appScript = sandbox.run(api: appExecutor,
            userSettingValues: [dimmers: [dimmerFixture1, dimmerFixture2], minimumLevel: 5, enableLogging: true],
            )
        appExecutor.setSubscribingScript(appScript)

        dimmerFixture1.initialize(appExecutor, [switch: "on", level: 99])
        dimmerFixture2.initialize(appExecutor, [switch: "on", level: 99])

        when:
        appScript.installed()
        dimmerFixture2.setLevel(2)

        then:
        dimmerFixture2.state.switch == "on"
        dimmerFixture2.state.level == 5
        dimmerFixture1.state.switch == "on"
        dimmerFixture1.state.level == 99
    }
}
