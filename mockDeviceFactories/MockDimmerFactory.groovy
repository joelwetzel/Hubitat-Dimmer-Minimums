package joelwetzel.dimmer_minimums.mockDeviceFactories

import me.biocomp.hubitat_ci.app.preferences.DeviceInputValueFactory
import me.biocomp.hubitat_ci.validation.DefaultAndUserValues

import me.biocomp.hubitat_ci.capabilities.Switch
import me.biocomp.hubitat_ci.capabilities.SwitchLevel

class MockDimmerFactory {
    private DeviceInputValueFactory dimmerFactory

    MockDimmerFactory() {
        this.dimmerFactory = new DeviceInputValueFactory([Switch, SwitchLevel])
    }

    def constructDevice(String name) {
        def dimmerDevice = this.dimmerFactory.makeInputObject(name, 't',  DefaultAndUserValues.empty(), false)
        return dimmerDevice
    }

    def attachBehavior(dimmerDevice, api, script, state) {
        def dimmerMetaClass = dimmerDevice.getMetaClass()
        dimmerMetaClass.state = state
        dimmerMetaClass.on = {
            state.switch = "on"
            api.sendEvent(dimmerDevice, [name: "switch.on", value: "on"])
        }
        dimmerMetaClass.off = {
            state.switch = "off"
            api.sendEvent(dimmerDevice, [name: "switch.off", value: "off"])
        }
        dimmerMetaClass.setLevel = {
            int level ->
                state.level = level

                api.sendEvent(dimmerDevice, [name: "level", value: level])
        }

        return dimmerDevice
    }

}
