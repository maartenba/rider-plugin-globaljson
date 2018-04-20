package be.maartenballiauw.rider.globaljson.util

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.jetbrains.rider.model.dotNetActiveRuntimeModel
import com.jetbrains.rider.projectView.solution
import java.io.File

class DotNetCoreSdkDetector(private val project: Project) {
    private fun resolveDotNetRootPath() : File? {
        val cliExePath = project.solution.dotNetActiveRuntimeModel.activeRuntime
                .valueOrNull?.dotNetCliExePath ?: return null

        val cliExeFile = File(cliExePath)
        return cliExeFile.parentFile
    }

    fun getInstalledSdkVersions() : Array<String> {
        val dotnetRootPath = resolveDotNetRootPath()
        if (dotnetRootPath == null || !dotnetRootPath.exists() || !dotnetRootPath.isDirectory) {
            return emptyArray()
        }

        val dotnetSdkRootPath = dotnetRootPath.resolve("sdk")
        val dotnetSdkRootFolder = LocalFileSystem.getInstance()
                .refreshAndFindFileByIoFile(dotnetSdkRootPath)

        if (dotnetSdkRootFolder != null && dotnetSdkRootFolder.isDirectory) {
            return dotnetSdkRootFolder.children
                    .filter { it.isDirectory && it.name.substring(0, 1).toIntOrNull() != null }
                    .map { it.name }
                    .toTypedArray()
        }

        return emptyArray()
    }
}