package joelwetzel.dimmer_minimums.tests

import joelwetzel.dimmer_minimums.mockDeviceFactories.MockDimmerFactory
import joelwetzel.dimmer_minimums.utils.SubscribingAppExecutor

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

    def api = Spy(SubscribingAppExecutor) {
        _*getLog() >> log
    }

    def dimmerFactory = new MockDimmerFactory()

    void "levelHandler() ensures minimum level"() {
        given:
        // Define a virtual dimmer device
        def dimmerDevice = dimmerFactory.constructDevice('n')

        // Run the app sandbox, passing the virtual dimmer device in.
        def script = sandbox.run(api: api,
            userSettingValues: [dimmers: [dimmerDevice], minimumLevel: 5, enableLogging: true],
            )
        api.setScript(script)

        dimmerFactory.attachBehavior(dimmerDevice, api, script, [switch: "on", level: 99])

        when:
        script.installed()
        dimmerDevice.setLevel(2)

        then:
        1 * log.debug('n LEVEL CHANGE detected (2)')
        1 * log.debug('n setLevel(5)')
        dimmerDevice.state.switch == "on"
        dimmerDevice.state.level == 5
    }

    void "setLevel() can turn on the dimmer"() {
        given:
        // Define a virtual dimmer device
        def dimmerDevice = dimmerFactory.constructDevice('n')

        // Run the app sandbox, passing the virtual dimmer device in.
        def script = sandbox.run(api: api,
            userSettingValues: [dimmers: [dimmerDevice], minimumLevel: 5, enableLogging: true],
            )
        api.setScript(script)

        dimmerFactory.attachBehavior(dimmerDevice, api, script, [switch: "off", level: 99])

        when:
        script.installed()
        dimmerDevice.setLevel(2)

        then:
        1 * log.debug('n LEVEL CHANGE detected (2)')
        dimmerDevice.state.switch == "on"
        dimmerDevice.state.level == 5
    }

    void "setLevel() does not turn on dimmer if zero"() {
        given:
        // Define a virtual dimmer device
        def dimmerDevice = dimmerFactory.constructDevice('n')

        // Run the app sandbox, passing the virtual dimmer device in.
        def script = sandbox.run(api: api,
            userSettingValues: [dimmers: [dimmerDevice], minimumLevel: 5, enableLogging: true],
            )
        api.setScript(script)

        dimmerFactory.attachBehavior(dimmerDevice, api, script, [switch: "off", level: 99])

        when:
        script.installed()
        dimmerDevice.setLevel(0)

        then:
        1 * log.debug('n LEVEL CHANGE detected (0)')
        dimmerDevice.state.switch == "off"
        dimmerDevice.state.level == 0
    }

    void "levelHandler() does not change level if above the minimum"() {
        given:
        // Define a virtual dimmer device
        def dimmerDevice = dimmerFactory.constructDevice('n')

        // Run the app sandbox, passing the virtual dimmer device in.
        def script = sandbox.run(api: api,
            userSettingValues: [dimmers: [dimmerDevice], minimumLevel: 5, enableLogging: true],
            )
        api.setScript(script)

        dimmerFactory.attachBehavior(dimmerDevice, api, script, [switch: "on", level: 99])

        when:
        script.installed()
        dimmerDevice.setLevel(80)

        then:
        1 * log.debug('n LEVEL CHANGE detected (80)')
        dimmerDevice.state.switch == "on"
        dimmerDevice.state.level == 80
    }

    void "levelHandler() adjusts correct dimmer from among multiple devices"() {
        given:
        // Define two virtual dimmer devices
        def dimmerDevice1 = dimmerFactory.constructDevice('n1')
        def dimmerDevice2 = dimmerFactory.constructDevice('n2')

        // Run the app sandbox, passing the virtual dimmer devices in.
        def script = sandbox.run(api: api,
            userSettingValues: [dimmers: [dimmerDevice1, dimmerDevice2], minimumLevel: 5, enableLogging: true],
            )
        api.setScript(script)

        dimmerFactory.attachBehavior(dimmerDevice1, api, script, [switch: "on", level: 99])
        dimmerFactory.attachBehavior(dimmerDevice2, api, script, [switch: "on", level: 99])

        when:
        script.installed()
        dimmerDevice2.setLevel(2)

        then:
        1 * log.debug('n2 LEVEL CHANGE detected (2)')
        1 * log.debug('n2 setLevel(5)')
        dimmerDevice2.state.switch == "on"
        dimmerDevice2.state.level == 5
        dimmerDevice1.state.switch == "on"
        dimmerDevice1.state.level == 99
    }
}
