package be.maartenballiauw.rider.globaljson.util

import com.intellij.openapi.application.Application
import com.intellij.openapi.project.Project
import com.jetbrains.rider.model.RdProjectDescriptor
import com.jetbrains.rider.model.ReloadCommand
import com.jetbrains.rider.model.UnloadCommand
import com.jetbrains.rider.projectView.nodes.*
import com.jetbrains.rider.projectView.solution
import com.jetbrains.rider.util.idea.syncFromBackend

class RiderSolutionUtil {
    companion object {
        fun hasDotNetCoreProjects(items: ArrayList<ProjectModelNode>) : Boolean = items.any {
            when {
                it.isSolutionFolder() -> hasDotNetCoreProjects(it.getChildren())
                it.isProject() -> ((it.containingProject()?.descriptor as? RdProjectDescriptor)?.isDotNetCore ?: false)
                else -> false
            }
        }

        fun reloadEntireSolution(item: ProjectModelNode, project: Project, application: Application) {
            val projectsCollector = object : ProjectModelNodeVisitor() {
                val projectIds = hashSetOf<Int>()

                override fun visitProject(node: ProjectModelNode): Result {
                    projectIds.add(node.id)
                    return Result.Stop
                }
            }
            projectsCollector.visit(item)

            if (projectsCollector.projectIds.any()) {
                application.saveAll()

                val unloadCommand = UnloadCommand(projectsCollector.projectIds.toList())
                project.solution.projectModelTasks.unloadProjects.syncFromBackend(unloadCommand, project)

                val reloadCommand = ReloadCommand(projectsCollector.projectIds.toList())
                project.solution.projectModelTasks.reloadProjects.syncFromBackend(reloadCommand, project)
            }
        }
    }
}