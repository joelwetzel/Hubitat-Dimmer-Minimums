# Dimmer Minimums app for Hubitat
An app for Hubitat to enforce a minimum level for smart switches/lights that don't have a config setting for it.

## Use Case
Some dimmable lights (such as some LEDs) will dim to very low levels, but will not initially turn on at those levels.  For example, an LED that can be dimmed to 1%, but must be at 5% or more to turn on from off.  This app enforces a minimum dimming level for dimmers/lights that don't have their own minimum brightness settings, to ensure fast and accurate startup.

It does this in two ways:
1. If the lights are on and you dim them below the minimum, it raises them back up to the minimum.
2. If the lights are off and are below the minimum when you turn them on, it will raise them up to the minimum.

This is to prevent the situation where the switch turns on at 1% but the lights don't actually come on, confusing the user.

## Installation

The best way to install this code is by using [Hubitat Package Manager](https://community.hubitat.com/t/beta-hubitat-package-manager).

However, it can also be installed manually.
