# Dimmer Minimums app for Hubitat
An app for Hubitat to enforce a minimum level for smart switches/lights that don't have a config setting for it.

## Use Case
I have some LED can lights hooked up to a smart dimmer.  The can lights will dim all the way down to 1% if they are already on.  However, if you then turn them off, they don't want to come back on at 1%.  They need a higher voltage to get started.

This app makes sure that they never go below a minimum.  It does this in two ways:
1. If the lights are on and you dim them below the minimum, it raises them back up to the minimum.
2. If the lights are off and are below the minimum, when you turn them on, it will raise them up to the minimum.

This is to prevent the situation where the switch turns on at 1% but the lights don't actually come on, confusing the user.

## Installation

TODO
