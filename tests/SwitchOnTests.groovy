package joelwetzel.dimmer_minimums.tests

import joelwetzel.dimmer_minimums.mockDeviceFactories.MockDimmerFactory
import joelwetzel.dimmer_minimums.utils.SubscribingAppExecutor

import me.biocomp.hubitat_ci.api.app_api.AppExecutor
import me.biocomp.hubitat_ci.api.app_api.Subscription
import me.biocomp.hubitat_ci.api.common_api.Log
import me.biocomp.hubitat_ci.app.HubitatAppSandbox
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
* Tests of behavior when an ON event is received.
*/
class SwitchOnTests extends Specification {
    private HubitatAppSandbox sandbox = new HubitatAppSandbox(new File('dimmer-minimums.groovy'))

    def log = Mock(Log)

    def api = Spy(SubscribingAppExecutor) {
        _*getLog() >> log
    }

    def dimmerFactory = new MockDimmerFactory()

    void "switchOnHandler() ensures minimum level"() {
        given:
        // Define a virtual dimmer device
        def dimmerDevice = dimmerFactory.constructDevice('n')

        // Run the app sandbox, passing the virtual dimmer device in.
        def script = sandbox.run(api: api,
            userSettingValues: [dimmers: [dimmerDevice], minimumLevel: 5, enableLogging: true],
            )
        api.setScript(script)

        dimmerFactory.attachBehavior(dimmerDevice, api, script, [switch: "off", level: 0])

        when:
        script.installed()
        dimmerDevice.on()

        then:
        1 * log.debug('n (0) ON detected')
        1 * log.debug('n setLevel(5)')
        1 * api.sendEvent(dimmerDevice, [name: "level", value: 5])
        dimmerDevice.state.switch == "on"
        dimmerDevice.state.level == 5
    }

    void "switchOnHandler() does not change level if above the minimum"() {
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
        dimmerDevice.on()

        then:
        1 * log.debug('n (99) ON detected')
        dimmerDevice.state.switch == "on"
        dimmerDevice.state.level == 99
    }

    void "switchOnHandler() adjusts correct dimmer from among multiple devices"() {
        given:
        // Define two virtual dimmer devices
        def dimmerDevice1 = dimmerFactory.constructDevice('n1')
        def dimmerDevice2 = dimmerFactory.constructDevice('n2')

        // Run the app sandbox, passing the virtual dimmer devices in.
        def script = sandbox.run(api: api,
            userSettingValues: [dimmers: [dimmerDevice1, dimmerDevice2], minimumLevel: 5, enableLogging: true],
            )
        api.setScript(script)

        dimmerFactory.attachBehavior(dimmerDevice1, api, script, [switch: "off", level: 0])
        dimmerFactory.attachBehavior(dimmerDevice2, api, script, [switch: "off", level: 0])

        when:
        script.installed()
        dimmerDevice2.on()
        script.switchOnHandler([deviceId: dimmerDevice2.deviceId])

        then:
        1 * log.debug('n2 (0) ON detected')
        1 * log.debug('n2 setLevel(5)')
        dimmerDevice2.state.switch == "on"
        dimmerDevice2.state.level == 5
        dimmerDevice1.state.switch == "off"
        dimmerDevice1.state.level == 0
    }
}
