package be.maartenballiauw.rider.globaljson.actions

import be.maartenballiauw.rider.globaljson.dialogs.ManageDotNetCoreSdkDialog
import be.maartenballiauw.rider.globaljson.util.DotNetCoreSdkDetector
import be.maartenballiauw.rider.globaljson.util.RiderSolutionUtil
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.impl.LoadTextUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.jetbrains.rider.projectView.actions.ProjectViewActionBase
import com.jetbrains.rider.projectView.nodes.ProjectModelNode
import com.jetbrains.rider.projectView.nodes.isSolution
import org.json.JSONObject
import java.io.OutputStreamWriter

class ManageDotNetCoreSdkAction : ProjectViewActionBase(
        "Manage .NET Core SDK",
        "Manage .NET Core SDK for solution") {

    override fun getItemInternal(item: ProjectModelNode) = when {
        item.isSolution() -> item
        else -> null
    }

    override fun updatePresentation(e: AnActionEvent, items: Array<ProjectModelNode>) {
        val solutionItem = items.firstOrNull()
        e.presentation.isEnabledAndVisible = solutionItem != null &&
                RiderSolutionUtil.hasDotNetCoreProjects(solutionItem.getChildren())
    }

    override fun actionPerformedInternal(item: ProjectModelNode, project: Project) {
        val application = ApplicationManager.getApplication()

        var currentSdkVersion : String? = null
        var globalJsonObject: JSONObject? = null

        // Read current SDK version
        val globalJsonFile = item.getVirtualFile()?.parent?.findFileByRelativePath("global.json")
        if (globalJsonFile != null && !globalJsonFile.isDirectory) {
            globalJsonObject = JSONObject(LoadTextUtil.loadText(globalJsonFile).toString())
            if (globalJsonObject.has("sdk")) {
                val sdk = globalJsonObject.getJSONObject("sdk")
                if (sdk.has("version")) {
                    currentSdkVersion = sdk.getString("version")
                }
            }
        }

        // Detect available SDK's
        val detector = DotNetCoreSdkDetector(project)

        // Show dialog
        val dialog = ManageDotNetCoreSdkDialog(detector.getInstalledSdkVersions(), currentSdkVersion)
        dialog.show()

        // Set SDK version
        if (dialog.exitCode == DialogWrapper.OK_EXIT_CODE) {
            // Update JSON value
            val versionJsonObject = JSONObject()
            if (dialog.selectedValue != ManageDotNetCoreSdkDialog.ITEM_UNSPECIFIED) {
                versionJsonObject.put("version", dialog.selectedValue)
            }

            if (globalJsonObject == null) {
                globalJsonObject = JSONObject()
            }
            globalJsonObject.put("sdk", versionJsonObject)

            // Write to global.json
            val capturedGlobalJson = globalJsonObject
            application.runWriteAction {
                var file = globalJsonFile
                if (file == null) {
                    file = item.getVirtualFile()?.parent?.createChildData(this, "global.json")
                }
                if (file != null && !file.isDirectory) {
                    val writer = OutputStreamWriter(file.getOutputStream(this))
                    capturedGlobalJson.write(writer)
                    writer.flush()
                    writer.close()
                }
            }

            // Refresh our solution and projects
            RiderSolutionUtil.reloadEntireSolution(item, project, application)
        }
    }
}