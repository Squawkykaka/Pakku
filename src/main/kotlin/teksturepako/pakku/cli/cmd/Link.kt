package teksturepako.pakku.cli.cmd

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.parameters.arguments.argument
import kotlinx.coroutines.runBlocking
import teksturepako.pakku.api.data.PakkuLock

class Link : CliktCommand("Link project to another project")
{
    private val projectOut: String by argument()
    private val projectIn: String by argument()

    override fun run() = runBlocking {
        val pakkuLock = PakkuLock.readToResult().getOrElse {
            terminal.danger(it.message)
            echo()
            return@runBlocking
        }

        val outId = pakkuLock.getProject(projectOut)?.pakkuId ?: return@runBlocking
        val project = pakkuLock.getProject(projectIn) ?: return@runBlocking
        pakkuLock.addPakkuLink(outId, project)
        terminal.success("$projectOut ($outId) linked to ${project.slug}")

        pakkuLock.write()
        echo()
    }
}