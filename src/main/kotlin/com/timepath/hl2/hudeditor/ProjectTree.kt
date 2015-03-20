package com.timepath.hl2.hudeditor

import com.timepath.io.utils.ViewableData
import com.timepath.plaf.x.filechooser.BaseFileChooser
import com.timepath.plaf.x.filechooser.NativeFileChooser
import com.timepath.vfs.provider.ExtendedVFile

import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeCellRenderer
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeSelectionModel
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.io.File
import java.io.IOException
import java.util.concurrent.ExecutionException
import java.util.logging.Level
import java.util.logging.Logger

/**
 * @author TimePath
 */
SuppressWarnings("serial")
public class ProjectTree : JTree(), ActionListener, MouseListener {
    private var directoryEntryContext: ExtendedVFile? = null
    private var archiveContext: ExtendedVFile? = null
    private var projectContext: DefaultMutableTreeNode? = null
    private val closeAction: JMenuItem
    private val defaultMenu: JPopupMenu
    private val extractAction: JMenuItem
    private val fileMenu: JPopupMenu
    private val projectMenu: JPopupMenu

    init {
        defaultMenu = JPopupMenu().let { menu ->
            val nullAction = JMenuItem().let {
                it.setText("No action")
                it.setEnabled(false)
                it
            }
            menu.add(nullAction)
            menu
        }
        extractAction = JMenuItem().let {
            it.setText("Extract")
            it.addActionListener(this)
            it
        }
        fileMenu = JPopupMenu().let { menu ->
            menu.add(extractAction)
            menu
        }
        closeAction = JMenuItem().let {
            it.setText("Close")
            it.addActionListener(this)
            it
        }
        projectMenu = JPopupMenu().let { menu ->
            menu.add(closeAction)
            menu
        }
        setBorder(null)
        setModel(DefaultTreeModel(DefaultMutableTreeNode("root")))
        addMouseListener(this)
        setRootVisible(false)
        setShowsRootHandles(true)
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION)
        setCellRenderer(CustomTreeCellRenderer())
    }

    override fun actionPerformed(e: ActionEvent) {
        if (e.getSource() == extractAction) {
            extractActionActionPerformed()
        } else if (e.getSource() == closeAction) {
            closeActionActionPerformed()
        }
    }

    private fun extractActionActionPerformed() {
        if (archiveContext == null) return
        val context = archiveContext
        LOG.log(Level.INFO, "Archive: {0}", context)
        try {
            val fs = NativeFileChooser().setTitle("Extract").setDialogType(BaseFileChooser.DialogType.SAVE_DIALOG).setFileMode(BaseFileChooser.FileMode.DIRECTORIES_ONLY).choose()
            if (fs == null) return
            LOG.log(Level.INFO, "Extracting to {0}", fs[0])
            object : SwingWorker<File, Int>() {
                throws(javaClass<Exception>())
                override fun doInBackground(): File? {
                    var ret: File? = null
                    try {
                        directoryEntryContext!!.extract(fs[0])
                        ret = File(fs[0], directoryEntryContext!!.name)
                    } catch (e: IOException) {
                        LOG.log(Level.SEVERE, null, e)
                    }

                    return ret
                }

                override fun done() {
                    try {
                        LOG.log(Level.INFO, "Extracted {0}", array<Any>(get()))
                    } catch (e: InterruptedException) {
                        LOG.log(Level.SEVERE, null, e)
                    } catch (e: ExecutionException) {
                        LOG.log(Level.SEVERE, null, e)
                    }

                }
            }.execute()
        } catch (ex: IOException) {
            LOG.log(Level.SEVERE, null, ex)
        }

    }

    private fun closeActionActionPerformed() {
        getModel().removeNodeFromParent(projectContext!!)
    }

    override fun mouseClicked(e: MouseEvent) {
        if (e.getSource() == this) formMouseClicked(e)
    }

    override fun mousePressed(e: MouseEvent) {
    }

    override fun mouseReleased(e: MouseEvent) {
    }

    override fun mouseEntered(e: MouseEvent) {
    }

    override fun mouseExited(e: MouseEvent) {
    }

    override fun getModel(): DefaultTreeModel {
        return super<JTree>.getModel() as DefaultTreeModel
    }

    override fun getLastSelectedPathComponent(): DefaultMutableTreeNode? {
        return super<JTree>.getLastSelectedPathComponent() as? DefaultMutableTreeNode
    }

    private fun formMouseClicked(e: MouseEvent) {
        if (!SwingUtilities.isRightMouseButton(e)) return
        val row = getClosestRowForLocation(e.getX(), e.getY())
        if (row != -1) {
            val clicked = getPathForRow(row).getLastPathComponent()
            setSelectionRow(row)
            if (clicked is DefaultMutableTreeNode) {
                val obj = clicked.getUserObject()
                if (obj is ExtendedVFile) {
                    directoryEntryContext = obj
                    archiveContext = directoryEntryContext!!.root
                    fileMenu.show(e.getComponent(), e.getX(), e.getY())
                    return
                } else if (obj is String) {
                    projectContext = clicked
                    projectMenu.show(e.getComponent(), e.getX(), e.getY())
                    return
                } else {
                    LOG.log(Level.WARNING, "Unknown user object {0}", obj.javaClass)
                }
            } else {
                LOG.log(Level.WARNING, "Unknown tree node {0}", clicked.javaClass)
            }
        }
        defaultMenu.show(e.getComponent(), e.getX(), e.getY())
    }

    private class CustomTreeCellRenderer : DefaultTreeCellRenderer() {

        var sameColor = Color.BLACK
        var diffColor = Color.BLUE
        var newColor = Color.GREEN.darker()

        /**
         * Configures the renderer based on the passed in components.
         * The value is set from messaging the tree with
         * {@code convertValueToText}, which ultimately invokes
         * {@code toString} on
         * {@code value}.
         * The foreground color is set based on the selection and the icon
         * is set based on on leaf and expanded.
         */
        override fun getTreeCellRendererComponent(tree: JTree, value: Any, sel: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean): Component {
            var valueText = value.toString()
            if (value is DefaultMutableTreeNode) {
                val nodeValue = value.getUserObject()
                if (nodeValue is String) {
                    setIcon(tree, UIManager.getIcon("FileView.computerIcon"))
                } else if (nodeValue is File) {
                    valueText = nodeValue.getName()
                    setIcon(tree, UIManager.getIcon("FileView.${when {
                        nodeValue.isDirectory() -> "directoryIcon"
                        else -> "fileIcon"
                    }}"))
                } else if (nodeValue is ViewableData) {
                    setIcon(tree, nodeValue.getIcon())
                } else {
                    if (nodeValue != null) {
                        LOG.log(Level.FINE, "Node class: {0}", nodeValue.javaClass)
                    }
                    setIcon(tree, null)
                }
            }
            val stringValue = tree.convertValueToText(valueText, sel, expanded, leaf, row, hasFocus)
            this.hasFocus = hasFocus
            setText(stringValue)
            val tColor: Color? = null
            if (tColor != null) {
                setForeground(if (sel)
                    (if ((tColor != newColor))
                        Color(-tColor.getRed() + 255, -tColor.getGreen() + 255, -tColor.getBlue() + 255)
                    else
                        tColor.brighter())
                else
                    tColor)
            } else {
                setForeground(if (sel) getTextSelectionColor() else getTextNonSelectionColor())
            }
            setEnabled(tree.isEnabled())
            setComponentOrientation(tree.getComponentOrientation())
            selected = sel
            return this
        }

        private fun setIcon(tree: JTree, icon: Icon?) {
            if (tree.isEnabled()) {
                setIcon(icon)
            } else {
                setDisabledIcon(icon)
            }
        }
    }

    companion object {

        private val LOG = Logger.getLogger(javaClass<ProjectTree>().getName())
    }
}
