package com.timepath.hl2.hudeditor

import com.timepath.Utils
import com.timepath.hl2.io.VMT
import com.timepath.hl2.io.image.VTF
import com.timepath.plaf.IconList
import com.timepath.plaf.x.filechooser.BaseFileChooser
import com.timepath.plaf.x.filechooser.NativeFileChooser
import com.timepath.steam.io.VDF
import com.timepath.steam.io.VDFNode
import com.timepath.steam.io.storage.ACF
import com.timepath.steam.io.storage.VPK
import com.timepath.vfs.provider.ExtendedVFile
import com.timepath.vfs.provider.local.LocalFileProvider
import com.timepath.vgui.Element
import com.timepath.vgui.VGUIRenderer
import com.timepath.vgui.VGUIRenderer.ResourceLocator
import com.timepath.vgui.swing.VGUICanvas
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.text.MessageFormat
import java.util.LinkedList
import java.util.concurrent.ExecutionException
import java.util.logging.Level
import java.util.logging.Logger
import java.util.regex.Pattern
import javax.swing.*
import javax.swing.event.HyperlinkListener
import javax.swing.event.TreeSelectionEvent
import javax.swing.event.TreeSelectionListener
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreePath

/**
 * @author TimePath
 */
public class HUDEditor : Application() {
    var editorMenuBar: EditorMenuBar
    var canvas: VGUICanvas? = null
    var lastLoaded: File? = null
        set(root) {
            editorMenuBar.reloadItem.setEnabled(root != null)
            if ((root == null) || !root.exists()) return
            lastLoaded = root
            Main.prefs.put("lastLoaded", root.getPath())
        }
    var spinnerWidth: JSpinner? = null
    var spinnerHeight: JSpinner? = null
    var linkListener: HyperlinkListener? = Utils.getLinkListener()

    init {
        setIconImages(IconList("/com/timepath/hl2/hudeditor/res/Icon", "png", *intArray(16, 22, 24, 32, 40, 48, 64, 128, 512, 1024)).getIcons())
        setTitle(Main.getString("Title"))
        editorMenuBar = EditorMenuBar(this)
        setJMenuBar(editorMenuBar)
        val str = Main.prefs.get("lastLoaded", null)
        str?.let { lastLoaded = File(str) }
        object : SwingWorker<Image, Void>() {
            override fun doInBackground(): Image? {
                return BackgroundLoader.fetch()
            }

            override fun done() {
                try {
                    canvas!!.setBackgroundImage(get())
                } catch (e: InterruptedException) {
                    LOG.log(Level.SEVERE, null, e)
                } catch (e: ExecutionException) {
                    LOG.log(Level.SEVERE, null, e)
                }

            }
        }.execute()
        mount(440)
        val gc = getGraphicsConfiguration()
        val screenBounds = gc.getBounds()
        val screenInsets = getToolkit().getScreenInsets(gc)
        val workspace = Dimension(screenBounds.width - screenInsets.left - screenInsets.right, screenBounds.height - screenInsets.top - screenInsets.bottom)
        setMinimumSize(Dimension(Math.max(workspace.width / 2, 640), Math.max((3 * workspace.height) / 4, 480)))
        setPreferredSize(Dimension((workspace.getWidth() / 1.5).toInt(), (workspace.getHeight() / 1.5).toInt()))
        pack()
        setLocationRelativeTo(null)
    }

    init {
        fileTree.addTreeSelectionListener(object : TreeSelectionListener {
            override fun valueChanged(e: TreeSelectionEvent) {
                val node = fileTree.getLastSelectedPathComponent()
                if (node == null) return
                propTable.clear()
                // val model = propTable.getModel()
                val nodeInfo = node.getUserObject()
                // TODO: introspection
                if (nodeInfo is VDFNode) {
                    val element = Element.importVdf(nodeInfo)
                    element.file = (node.getParent().toString()) // TODO
                    loadProps(element)
                    canvas!!.r!!.load(element)
                }
            }
        })
        canvas = VGUICanvas()
        //        canvas = object : VGUICanvas() {
        //            override fun placed() {
        //                val node = fileTree.getLastSelectedPathComponent()
        //                if (node == null) return
        //                val nodeInfo = node.getUserObject()
        //                if (nodeInfo is Element) {
        //                    val element = nodeInfo as Element
        //                    loadProps(element)
        //                }
        //            }
        //        }
        tabbedContent.add(Main.getString("Canvas"), object : JScrollPane(canvas) {
            init {
                //        setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
                getVerticalScrollBar().setBlockIncrement(30)
                getVerticalScrollBar().setUnitIncrement(20)
                //        setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
                getHorizontalScrollBar().setBlockIncrement(30)
                getHorizontalScrollBar().setUnitIncrement(20)
            }
        })
    }

    override fun preferences() {
        info("No app-specific preferences yet", "Preferences")
    }

    override fun about() {
        val pane = JEditorPane("text/html", "")
        pane.setEditable(false)
        pane.setOpaque(false)
        pane.setBackground(Color(0, 0, 0, 0))
        pane.addHyperlinkListener(linkListener)
        var aboutText = "<html><h2>This is to be a What You See Is What You Get HUD Editor for TF2,</h2>"
        aboutText += "for graphically editing TF2 HUDs!"
        val p1 = aboutText
        pane.setText(p1)
        info(pane, "About")
    }

    override fun fileDropped(f: File) {
        loadAsync(f)
    }

    override val dockIconImage: Image
        get() {
            val url = javaClass.getResource("/com/timepath/hl2/hudeditor/res/Icon.png")
            return Toolkit.getDefaultToolkit().getImage(url)
        }

    fun locateHudDirectory() {
        try {
            val selection = NativeFileChooser().setParent(this).setTitle(Main.getString("LoadHudDir")).setDirectory(lastLoaded).setFileMode(BaseFileChooser.FileMode.DIRECTORIES_ONLY).choose()
            selection?.let { loadAsync(it[0]) }
        } catch (ex: IOException) {
            LOG.log(Level.SEVERE, null, ex)
        }

    }

    fun loadAsync(f: File) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR))
        val start = System.currentTimeMillis()
        object : SwingWorker<DefaultMutableTreeNode, Void>() {
            override fun doInBackground(): DefaultMutableTreeNode? {
                return load(f)
            }

            override fun done() {
                try {
                    val project = get()
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR))
                    if (project == null) return
                    LOG.log(Level.INFO, "Loaded hud - took {0}ms", System.currentTimeMillis() - start)
                    fileSystemRoot.add(project)
                    fileTree.expandPath(TreePath(project.getPath()))
                    fileTree.setSelectionRow(fileSystemRoot.getIndex(project))
                    fileTree.requestFocusInWindow()
                } catch (t: Throwable) {
                    LOG.log(Level.SEVERE, null, t)
                }

            }
        }.execute()
    }

    fun load(root: File?): DefaultMutableTreeNode? {
        if (root == null) return null
        if (!root.exists()) {
            error(MessageFormat(Main.getString("FileAccessError")).format(array<Any>(root)))
        }
        lastLoaded = root
        LOG.log(Level.INFO, "You have selected: {0}", root.getAbsolutePath())
        if (root.isDirectory()) {
            var valid = true // TODO: find resource and scripts if there is a parent directory
            for (folder in root.listFiles() ?: arrayOfNulls<File>(0)) {
                if (folder!!.isDirectory() && ("resource".equalsIgnoreCase(folder.getName()) || "scripts".equalsIgnoreCase(folder.getName()))) {
                    valid = true
                    break
                }
            }
            if (!valid) {
                error("Selection not valid. Please choose a folder containing \'resources\' or \'scripts\'.")
                locateHudDirectory()
                return null
            }
            val project = DefaultMutableTreeNode(root.getName())
            recurseDirectoryToNode(LocalFileProvider(root), project)
            return project
        }
        if (root.getName().endsWith("_dir.vpk")) {
            val project = DefaultMutableTreeNode(root.getName())
            recurseDirectoryToNode(VPK.loadArchive(root)!!, project)
            return project
        }
        return null
    }

    fun changeResolution() {
        spinnerWidth?.setEnabled(false)
        spinnerHeight?.setEnabled(false)
        val dropDown = JComboBox<String>()
        val env = GraphicsEnvironment.getLocalGraphicsEnvironment()
        val listItems = LinkedList<String>()
        for (device in env.getScreenDevices()) {
            for (resolution in device.getDisplayModes()) {
                // TF2 has different resolutions
                val item = "${resolution.getWidth().toString()}x${resolution.getHeight()}" // TODO: Work out aspect ratios
                if (item !in listItems) {
                    listItems.add(item)
                }
            }
        }
        dropDown.addItem("Custom")
        for (listItem in listItems) {
            dropDown.addItem(listItem)
        }
        dropDown.setSelectedIndex(1)
        dropDown.addActionListener(object : ActionListener {
            override fun actionPerformed(e: ActionEvent) {
                val item = dropDown.getSelectedItem().toString()
                val isRes = "x" in item
                spinnerWidth?.setEnabled(!isRes)
                spinnerHeight?.setEnabled(!isRes)
                if (isRes) {
                    val xy = item.split("x")
                    spinnerWidth?.setValue(Integer.parseInt(xy[0]))
                    spinnerHeight?.setValue(Integer.parseInt(xy[1]))
                }
            }
        })
        val message = array("Presets: ", dropDown, "Width: ", spinnerWidth!!, "Height: ", spinnerHeight!!)
        val optionPane = JOptionPane(message, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null, null)
        val dialog = optionPane.createDialog(this, "Change resolution...")
        dialog.setContentPane(optionPane)
        dialog.pack()
        dialog.setVisible(true)
        optionPane.getValue()?.let {
            if (it == JOptionPane.YES_OPTION) {
                canvas!!.setPreferredSize(Dimension(Integer.parseInt(spinnerWidth?.getValue().toString()), Integer.parseInt(spinnerHeight?.getValue().toString())))
            }
        }
    }

    fun mount(appID: Int) {
        object : SwingWorker<DefaultMutableTreeNode, Void>() {
            throws(javaClass<Exception>())
            override fun doInBackground(): DefaultMutableTreeNode? {
                LOG.log(Level.INFO, "Mounting {0}", appID)
                val a = ACF.fromManifest(appID)
                VGUIRenderer.registerLocator(object : ResourceLocator() {
                    override fun locate(path: String): InputStream? {
                        [suppress("NAME_SHADOWING")]
                        val path = path.replace('\\', '/').toLowerCase().let {
                            when {
                                it.startsWith("..") -> "vgui/$it"
                                else -> it
                            }
                        }
                        println("Looking for $path")
                        val file = a.query("tf/materials/$path")
                        if (file == null) return null
                        return file.openStream()
                    }

                    override fun locateImage(path: String): Image? {
                        var vtfName = path
                        if (!path.endsWith(".vtf")) {
                            // It could be a vmt
                            vtfName += ".vtf"
                            locate("$path.vmt")?.let {
                                try {
                                    val vmt = VMT.load(it)
                                    val next = vmt.root.getValue("\$baseTexture") as String
                                    if (next != path) return locateImage(next) // Stop recursion
                                } catch (e: IOException) {
                                    LOG.log(Level.SEVERE, null, e)
                                }

                            }
                        }
                        // It's a vtf
                        locate(vtfName)?.let {
                            try {
                                val vtf = VTF.load(it)
                                if (vtf == null) return null
                                return vtf.getImage(0)
                            } catch (e: IOException) {
                                LOG.log(Level.SEVERE, null, e)
                            }

                        }
                        return null
                    }
                })
                val child = DefaultMutableTreeNode(a)
                recurseDirectoryToNode(a, child)
                return child
            }

            override fun done() {
                try {
                    get()?.let {
                        archiveRoot.add(it)
                        fileModel.reload(archiveRoot)
                        LOG.log(Level.INFO, "Mounted {0}", appID)
                    }
                } catch (ex: InterruptedException) {
                    LOG.log(Level.SEVERE, null, ex)
                } catch (ex: ExecutionException) {
                    LOG.log(Level.SEVERE, null, ex)
                }

            }
        }.execute()
    }

    fun loadProps(element: Element) {
        propTable.clear()
        val model = propTable.getModel()
        if (!element.props.isEmpty()) {
            element.validateDisplay()
            for (i in element.props.size().indices) {
                val entry = element.props[i]
                if ("\\n" == entry.getKey()) continue
                model.addRow(array(entry.getKey(), entry.getValue(), entry.info))
            }
            model.fireTableDataChanged()
            propTable.repaint()
        }
    }

    companion object {

        private val LOG = Logger.getLogger(javaClass<HUDEditor>().getName())

        private val VDF_PATTERN = Pattern.compile("^\\.(vdf|pop|layout|menu|styles)")

        public fun recurseDirectoryToNode(ar: ExtendedVFile, project: DefaultMutableTreeNode) {
            project.setUserObject(ar)
            analyze(project, true)
        }

        public fun analyze(top: DefaultMutableTreeNode, leaves: Boolean) {
            if (top.getUserObject() !is ExtendedVFile) return
            val root = top.getUserObject() as ExtendedVFile
            for (n in root.list()) {
                LOG.log(Level.FINE, "Loading {0}", n.name)
                val child = DefaultMutableTreeNode(n)
                if (n.isDirectory) {
                    if (n.list().size() > 0) {
                        top.add(child)
                        analyze(child, leaves)
                    }
                } else if (leaves) {
                    try {
                        n.openStream()!!.use {
                            when {
                                VDF_PATTERN.matcher(n.name).matches() -> VDF.load(it).toTreeNode()
                            // TODO
                            //                                n.name.endsWith(".res") -> RES.load(it).toTreeNode()
                                else -> null
                            }
                        }?.let {
                            child.add(it)
                            top.add(child)
                        }
                    } catch (e: IOException) {
                        LOG.log(Level.SEVERE, null, e)
                    }
                }
            }
        }
    }
}
