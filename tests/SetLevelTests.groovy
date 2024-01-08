package joelwetzel.dimmer_minimums.tests

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
    // Creating a sandbox object for device script from file.
    private HubitatAppSandbox sandbox = new HubitatAppSandbox(new File('dimmer-minimums.groovy'))

    // Create mock log
    def log = Mock(Log)

    // Make AppExecutor return the mock log
    AppExecutor api = Mock {
        _ * getLog() >> log
    }

    def dimmerFactory = new DeviceInputValueFactory([Switch, SwitchLevel])

    private def constructMockDimmerDevice(String name) {
        def dimmerDevice = dimmerFactory.makeInputObject(name, 't',  DefaultAndUserValues.empty(), false)
        return dimmerDevice
    }

    private def attachDimmerBehavior(dimmerDevice, script, state) {
        def dimmerMetaClass = dimmerDevice.getMetaClass()
        dimmerMetaClass.state = state
        dimmerMetaClass.on = {
            state.switch = "on"
            script.switchOnHandler([deviceId: dimmerDevice.deviceId])
        }
        dimmerMetaClass.off = {
            state.switch = "off"
            script.switchOffHandler([deviceId: dimmerDevice.deviceId])
        }
        dimmerMetaClass.setLevel = {
            int level ->
                state.level = level

                // The framework does not yet actually implement event subscriptions, so we have
                // to call the app's handler directly after setting level on the device.
                script.levelHandler([deviceId: dimmerDevice.deviceId, value: level])
        }

        return dimmerDevice
    }

    void "levelHandler() ensures minimum level"() {
        given:
        // Define a virtual dimmer device
        def dimmerDevice = constructMockDimmerDevice('n')

        // Run the app sandbox, passing the virtual dimmer device in.
        def script = sandbox.run(api: api,
            userSettingValues: [dimmers: [dimmerDevice], minimumLevel: 5, enableLogging: true],
            )

        attachDimmerBehavior(dimmerDevice, script, [switch: "on", level: 99])

        when:
        dimmerDevice.setLevel(2)

        then:
        1 * log.debug('n LEVEL CHANGE detected (2)')
        1 * log.debug('n setLevel(5)')
        dimmerDevice.state.level == 5
    }

    void "levelHandler() does not change level if dimmer is off"() {
        given:
        // Define a virtual dimmer device
        def dimmerDevice = constructMockDimmerDevice('n')

        // Run the app sandbox, passing the virtual dimmer device in.
        def script = sandbox.run(api: api,
            userSettingValues: [dimmers: [dimmerDevice], minimumLevel: 5, enableLogging: true],
            )

        attachDimmerBehavior(dimmerDevice, script, [switch: "off", level: 99])

        when:
        dimmerDevice.setLevel(2)

        then:
        1 * log.debug('n LEVEL CHANGE detected (2)')
        dimmerDevice.state.level == 2
    }

    void "levelHandler() does not change level if above the minimum"() {
        given:
        // Define a virtual dimmer device
        def dimmerDevice = constructMockDimmerDevice('n')

        // Run the app sandbox, passing the virtual dimmer device in.
        def script = sandbox.run(api: api,
            userSettingValues: [dimmers: [dimmerDevice], minimumLevel: 5, enableLogging: true],
            )

        attachDimmerBehavior(dimmerDevice, script, [switch: "on", level: 99])

        when:
        dimmerDevice.setLevel(80)

        then:
        1 * log.debug('n LEVEL CHANGE detected (80)')
        dimmerDevice.state.level == 80
    }

    void "levelHandler() adjusts correct dimmer from among multiple devices"() {
        given:
        // Define two virtual dimmer devices
        def dimmerDevice1 = constructMockDimmerDevice('n1')
        def dimmerDevice2 = constructMockDimmerDevice('n2')

        // Run the app sandbox, passing the virtual dimmer devices in.
        def script = sandbox.run(api: api,
            userSettingValues: [dimmers: [dimmerDevice1, dimmerDevice2], minimumLevel: 5, enableLogging: true],
            )

        attachDimmerBehavior(dimmerDevice1, script, [switch: "on", level: 99])
        attachDimmerBehavior(dimmerDevice2, script, [switch: "on", level: 99])

        when:
        dimmerDevice2.setLevel(2)

        then:
        1 * log.debug('n2 LEVEL CHANGE detected (2)')
        1 * log.debug('n2 setLevel(5)')
        dimmerDevice2.state.level == 5
        dimmerDevice1.state.level == 99
    }
}
