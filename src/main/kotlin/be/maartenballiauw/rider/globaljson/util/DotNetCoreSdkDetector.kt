package be.maartenballiauw.rider.globaljson.util

import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.util.io.exists
import org.apache.commons.lang.SystemUtils
import java.nio.file.Path
import java.nio.file.Paths

class DotNetCoreSdkDetector {
    private fun detectRootInstallPath() : Path? {
        val fallbackPaths = ArrayList<Path>()

        if (SystemUtils.IS_OS_WINDOWS ) {
            fallbackPaths.add(Paths.get(System.getenv("ProgramFiles"), "dotnet"))
        }
        else
        {
            fallbackPaths.add(Paths.get("usr", "local", "share", "dotnet"))
            fallbackPaths.add(Paths.get("usr", "share", "dotnet"))
        }

        return fallbackPaths.firstOrNull {
            it.exists()
        }
    }

    fun getInstalledSdkVersions() : Array<String> {
        val dotnetRootPath = detectRootInstallPath()
        if (dotnetRootPath != null) {
            val dotnetSdkRootPath = Paths.get(dotnetRootPath.toString(), "sdk")
            val dotnetSdkRootFolder = LocalFileSystem.getInstance()
                .findFileByPath(dotnetSdkRootPath.toString())

            if (dotnetSdkRootFolder != null && dotnetSdkRootFolder.isDirectory) {
                return dotnetSdkRootFolder.children
                        .filter { it.isDirectory }
                        .map { it.name }
                        .toTypedArray()
            }
        }

        return emptyArray()
    }
}