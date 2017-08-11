package be.maartenballiauw.rider.globaljson.dialogs

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.IdeBorderFactory
import com.jetbrains.rider.protocol.IPermittedModalities
import java.awt.BorderLayout
import java.awt.event.ActionEvent
import javax.swing.*

class ManageDotNetCoreSdkDialog(
        private val installedSdkVersions: Array<String>,
        private val currentSdkVersion: String?)
    : DialogWrapper(true) {

    companion object {
        val ITEM_UNSPECIFIED = "<Not specified>"
    }

    var selectedValue : String? = null

    init {
        title = "Manage .NET Core SDK for solution"
        init()
        IPermittedModalities.getInstance().allowPumpProtocolForComponent(this.window, this.disposable)
    }

    override fun createCenterPanel(): JComponent? {
        val panel = JPanel()
        panel.layout = BorderLayout()
        panel.border = IdeBorderFactory.createEmptyBorder(3)

        val label = JLabel("Select .NET Core SDK:")
        label.border = IdeBorderFactory.createEmptyBorder(0, 0, 2, 2)
        panel.add(label, BorderLayout.LINE_START)

        val valueEditor = JComboBox<String>(installedSdkVersions.sortedArray())
        valueEditor.addItem(ITEM_UNSPECIFIED)
        valueEditor.selectedItem = ITEM_UNSPECIFIED

        if (currentSdkVersion != null) {
            val selectedSdkVersion = installedSdkVersions.firstOrNull { it == currentSdkVersion }
            if (selectedSdkVersion != null) {
                valueEditor.selectedItem = selectedSdkVersion
            }
        }
        selectedValue = valueEditor.selectedItem.toString()
        valueEditor.addActionListener { e: ActionEvent? -> selectedValue = valueEditor.selectedItem.toString() }

        panel.add(valueEditor)

        return panel
    }

    override fun createActions(): Array<Action> {
        return arrayOf(okAction, cancelAction)
    }

    override fun doOKAction() {
        super.doOKAction()
    }

    override fun doCancelAction() {
        selectedValue = null
        super.doCancelAction()
    }
}