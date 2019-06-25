package struct

import base.ModuleBase

abstract class ModuleStruct : ModuleBase {

    var isEnabled = false
        private set


    protected abstract fun onEnable()

    protected abstract fun onDisable()


    final override fun enable() {

        if (isEnabled) return

        onEnable()
        isEnabled = true
    }

    final override fun disable() {

        if (!isEnabled) return

        onDisable()
        isEnabled = false
    }

}