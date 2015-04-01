package com.timepath.hl2.hudeditor

import com.apple.OSXAdapter
import com.timepath.plaf.OS
import com.timepath.plaf.mac.Application.*
import com.timepath.swing.BlendedToolBar
import com.timepath.swing.StatusBar
import java.awt.BorderLayout
import java.awt.Image
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.UnsupportedFlavorException
import java.awt.dnd.DnDConstants
import java.awt.dnd.DropTarget
import java.awt.dnd.DropTargetDropEvent
import java.awt.dnd.InvalidDnDOperationException
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.File
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import java.util.StringTokenizer
import java.util.logging.Level
import java.util.logging.Logger
import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

/**
 * @author TimePath
 */
SuppressWarnings("serial")
public abstract class Application : JFrame() {
    var tabbedContent: JTabbedPane
    var fileSystemRoot: DefaultMutableTreeNode
    var status: StatusBar
    var root: DefaultMutableTreeNode
    var fileTree: ProjectTree
    var archiveRoot: DefaultMutableTreeNode
    var fileModel: DefaultTreeModel
    var propTable: PropertyTable

    init {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                quit()
            }
        })
        getRootPane().putClientProperty("apple.awt.brushMetalLook", java.lang.Boolean.TRUE) // Mac tweak
        setDropTarget(object : DropTarget() {
            synchronized override fun drop(dtde: DropTargetDropEvent) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE)
                    val t = dtde.getTransferable()
                    var file: File? = null
                    if (OS.isLinux()) {
                        val nixFileDataFlavor = DataFlavor("text/uri-list;class=java.lang.String")
                        val data = t.getTransferData(nixFileDataFlavor) as String
                        run {
                            val st = StringTokenizer(data, "\r\n")
                            while (st.hasMoreTokens()) {
                                val token = st.nextToken().trim()
                                if (token.startsWith("#") || token.isEmpty()) continue // comment line, by RFC 2483
                                try {
                                    file = File(URI(token))
                                } catch (e: URISyntaxException) {
                                    return
                                }

                            }
                        }
                    } else {
                        val data = t.getTransferData(DataFlavor.javaFileListFlavor)
                        if (data is Iterable<*>) {
                            for (o in data) {
                                if (o is File) file = o
                            }
                        }
                    }
                    file?.let { fileDropped(it) }
                } catch (e: ClassNotFoundException) {
                    LOG.log(Level.SEVERE, null, e)
                } catch (e: InvalidDnDOperationException) {
                    LOG.log(Level.SEVERE, null, e)
                } catch (e: UnsupportedFlavorException) {
                    LOG.log(Level.SEVERE, null, e)
                } catch (e: IOException) {
                    LOG.log(Level.SEVERE, null, e)
                } finally {
                    dtde.dropComplete(true)
                    repaint()
                }
            }
        })
        val tools = BlendedToolBar()
        getContentPane().add(tools, BorderLayout.PAGE_START)
        val rootSplit = JSplitPane()
        rootSplit.setDividerLocation(180)
        rootSplit.setContinuousLayout(true)
        rootSplit.setOneTouchExpandable(true)
        val sideSplit = object : JSplitPane() {
            init {
                setBorder(null)
                setOrientation(JSplitPane.VERTICAL_SPLIT)
                setResizeWeight(0.5)
                setContinuousLayout(true)
                setOneTouchExpandable(true)
            }
        }
        rootSplit.setLeftComponent(sideSplit)
        tabbedContent = JTabbedPane()
        rootSplit.setRightComponent(tabbedContent)
        getContentPane().add(rootSplit, BorderLayout.CENTER)
        status = StatusBar()
        getContentPane().add(status, BorderLayout.PAGE_END)
        tools.setWindow(this)
        tools.putClientProperty("Quaqua.ToolBar.style", "title")
        status.putClientProperty("Quaqua.ToolBar.style", "bottom")
        archiveRoot = DefaultMutableTreeNode("Archives")
        fileSystemRoot = DefaultMutableTreeNode("Projects")
        fileTree = ProjectTree()
        root = DefaultMutableTreeNode()
        root.add(archiveRoot)
        root.add(fileSystemRoot)
        fileModel = fileTree.getModel()
        fileModel.setRoot(root)
        fileModel.reload()
        sideSplit.setTopComponent(object : JScrollPane(fileTree) {
            init {
                setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS)
            }
        })
        propTable = PropertyTable()
        sideSplit.setBottomComponent(JScrollPane(propTable))
    }

    public abstract fun preferences()

    public abstract fun about()

    public abstract val dockIconImage: Image

    public abstract fun fileDropped(f: File)

    override fun setJMenuBar(menubar: JMenuBar) {
        LOG.log(Level.INFO, "Setting menubar for {0}", OS.get())
        super.setJMenuBar(menubar)
        if (OS.isMac()) {
            try {
                OSXAdapter.setQuitHandler(this, javaClass.getDeclaredMethod("quit"))
                OSXAdapter.setAboutHandler(this, javaClass.getDeclaredMethod("about"))
                OSXAdapter.setPreferencesHandler(this, javaClass.getDeclaredMethod("preferences"))
                val app = com.timepath.plaf.mac.Application.getApplication()
                app.setAboutHandler(object : AboutHandler {
                    override fun handleAbout(e: AboutEvent) {
                        about()
                    }
                })
                app.setPreferencesHandler(object : PreferencesHandler {
                    override fun handlePreferences(e: PreferencesEvent) {
                        preferences()
                    }
                })
                app.setQuitHandler(object : QuitHandler {
                    override fun handleQuitRequestWith(qe: QuitEvent, qr: QuitResponse) {
                        quit()
                    }
                })
                app.setDockIconImage(dockIconImage)
            } catch (e: Exception) {
                LOG.severe(e.toString())
            }

        }
    }

    public fun quit() {
        LOG.info("Closing...")
        dispose()
    }

    public fun error(msg: Any) {
        error(msg, Main.getString("Error"))
    }

    public fun error(msg: Any, title: String) {
        LOG.log(Level.SEVERE, "{0}:{1}", array<Any>(title, msg))
        JOptionPane.showMessageDialog(this, msg, title, JOptionPane.ERROR_MESSAGE)
    }

    public fun info(msg: Any) {
        info(msg, Main.getString("Info"))
    }

    public fun info(msg: Any, title: String) {
        LOG.log(Level.INFO, "{0}:{1}", array<Any>(title, msg))
        JOptionPane.showMessageDialog(this, msg, title, JOptionPane.INFORMATION_MESSAGE)
    }

    companion object {

        private val LOG = Logger.getLogger(javaClass<Application>().getName())
    }

}
