/*
 * NotesPanel.java
 *
 * Created on April 7, 2004, 2:13 PM
 */

package plugin.charactersheet.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import pcgen.core.NoteItem;
import pcgen.core.PlayerCharacter;
import pcgen.core.SettingsHandler;
import pcgen.gui.panes.FlippingSplitPane;
import pcgen.gui.utils.Utility;
import pcgen.util.PropertyFactory;
import plugin.charactersheet.CharacterSheetPlugin;

/**
 *
 * @author  ddjone3
 */
public class NotesPanel extends FlippingSplitPane {
	private PlayerCharacter pc;
	private boolean textIsDirty = false;
	private NoteItem bioNote = null;
	private NoteItem companionNote = null;
	private NoteItem currentItem = null;
	private NoteItem descriptionNote = null;
	private NoteItem lastItem = null;
	private NoteItem magicItemsNote = null;
	private NoteItem otherAssetsNote = null;
	private NoteTreeNode rootTreeNode;
	private DefaultTreeModel notesModel;
	private int serial = 0;

	private static final int BIO_NOTEID = -2;
	private static final int DESCRIPTION_NOTEID = -3;
	private static final int COMPANION_NOTEID = -4;
	private static final int OTHERASSETS_NOTEID = -5;
	private static final int MAGICITEMS_NOTEID = -6;

	/** Creates new form NotesPanel */
	public NotesPanel () {
		initComponents();
		addListeners();
		initPrefs();
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	private void initComponents() {//GEN-BEGIN:initComponents
		jScrollPane1 = new javax.swing.JScrollPane();
		notesArea = new javax.swing.JTextArea();
		jPanel1 = new javax.swing.JPanel();
		jScrollPane2 = new javax.swing.JScrollPane();
		notesTree = new javax.swing.JTree();
		jToolBar1 = new javax.swing.JToolBar();
		addButton = new javax.swing.JButton();
		deleteButton = new javax.swing.JButton();
		renameButton = new javax.swing.JButton();
		moveButton = new javax.swing.JButton();

		setDividerSize(1);
		setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
		setPreferredSize(new java.awt.Dimension(360, 78));
		notesArea.setLineWrap(true);
		notesArea.setWrapStyleWord(true);
		notesArea.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusLost(java.awt.event.FocusEvent evt) {
				notesAreaFocusLost();
			}
		});

		jScrollPane1.setViewportView(notesArea);

		setBottomComponent(jScrollPane1);

		jPanel1.setLayout(new java.awt.BorderLayout());

		jScrollPane2.setViewportView(notesTree);

		jPanel1.add(jScrollPane2, java.awt.BorderLayout.CENTER);

		addButton.setText(PropertyFactory.getString("in_add"));
		addButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				addButtonActionPerformed();
			}
		});

		jToolBar1.add(addButton);

		deleteButton.setText(PropertyFactory.getString("in_delete"));
		deleteButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				deleteButtonActionPerformed();
			}
		});

		jToolBar1.add(deleteButton);

		renameButton.setText(PropertyFactory.getString("in_rename"));
		renameButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				renameButtonActionPerformed();
			}
		});

		jToolBar1.add(renameButton);

		moveButton.setText(PropertyFactory.getString("in_move"));
		moveButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				moveButtonActionPerformed();
			}
		});

		jToolBar1.add(moveButton);

		jPanel1.add(jToolBar1, java.awt.BorderLayout.SOUTH);

		setTopComponent(jPanel1);

	}//GEN-END:initComponents

	private void addButtonActionPerformed() {//GEN-FIRST:event_addButtonActionPerformed
		addNode();
	}//GEN-LAST:event_addButtonActionPerformed

	private void deleteButtonActionPerformed() {//GEN-FIRST:event_deleteButtonActionPerformed
		deleteNode();
	}//GEN-LAST:event_deleteButtonActionPerformed

	private void renameButtonActionPerformed() {//GEN-FIRST:event_renameButtonActionPerformed
		renameNode();
	}//GEN-LAST:event_renameButtonActionPerformed

	private void moveButtonActionPerformed() {//GEN-FIRST:event_moveButtonActionPerformed
		moveNode();
	}//GEN-LAST:event_moveButtonActionPerformed

	private void notesAreaFocusLost() {//GEN-FIRST:event_notesAreaFocusLost
		updateNoteItem();
	}//GEN-LAST:event_notesAreaFocusLost


	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JButton addButton;
	private javax.swing.JButton deleteButton;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.JScrollPane jScrollPane2;
	private javax.swing.JToolBar jToolBar1;
	private javax.swing.JButton moveButton;
	private javax.swing.JTextArea notesArea;
	private javax.swing.JTree notesTree;
	private javax.swing.JButton renameButton;
	// End of variables declaration//GEN-END:variables

	private void initPrefs() {
		int iDividerLocation = SettingsHandler.getGMGenOption(CharacterSheetPlugin.LOG_NAME + ".notes.DividerLocation", 100);
		setDividerLocation(iDividerLocation);
	}

	public void flushPrefs() {
		SettingsHandler.setGMGenOption(CharacterSheetPlugin.LOG_NAME + ".notes.DividerLocation", getDividerLocation());
	}

	private void addNode() {
		TreePath selPath = notesTree.getSelectionPath();

		if (selPath == null) {
			return;
		}

		NoteTreeNode parentTreeNode = (NoteTreeNode) selPath.getLastPathComponent();

		parentTreeNode.addNote();
		notesTree.updateUI();
		notesTree.expandPath(selPath);
	}

	private void deleteNode() {
		TreePath selPath = notesTree.getSelectionPath();

		if (selPath == null) {
			return;
		}

		Object o = selPath.getLastPathComponent();

		if ((o == null) || (((NoteTreeNode) o).getItem() == null)) {
			return;
		}

		NoteTreeNode node = (NoteTreeNode) o;
		node.deleteNote();
		notesTree.updateUI();
	}

	private void renameNode() {
		TreePath selPath = notesTree.getSelectionPath();

		if (selPath == null) {
			return;
		}

		notesTree.startEditingAtPath(selPath);
	}

	private void moveNode() {
		lastItem = currentItem;
	}

	private void addListeners() {
		notesArea.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				docChange(e);
			}

			public void insertUpdate(DocumentEvent e) {
				docChange(e);
			}

			public void removeUpdate(DocumentEvent e) {
				docChange(e);
			}
		});

		notesTree.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				int selRow = notesTree.getRowForLocation(e.getX(), e.getY());
				TreePath selPath = notesTree.getPathForLocation(e.getX(), e.getY());

				if (selRow != -1) {
					if ((e.getClickCount() == 1) && (selPath != null)) {
						selectNotesNode(selRow);
					}
				}
				lastItem = null;
			}
		});

		notesTree.getModel();
		notesTree.setEditable(true);
		notesTree.addMouseListener(new NotePopupListener(notesTree, new NotePopupMenu()));
	}

	public void docChange(DocumentEvent e) {
		textIsDirty = true;
	}

	public void setPc(PlayerCharacter pc, Properties pcProperties) {
		if(this.pc != pc) {
			this.pc = pc;
			serial = 0;
			populateNotes();
		}
	}

	public void refresh() {
		if(serial < pc.getSerial()) {
			if (currentItem != null) {
				updateNoteItem();
			}
			establishTreeNodes();
			notesTree.expandRow(0);
			selectNotesNode(getCSNode());
			serial = pc.getSerial();
		}
	}

	public PlayerCharacter getPc() {
		return pc;
	}

	public void clear() {
		rootTreeNode.removeAllChildren();
		bioNote = null;
		companionNote = null;
		currentItem = null;
		descriptionNote = null;
		lastItem = null;
		magicItemsNote = null;
		otherAssetsNote = null;
	}

	private void populateNotes() {
		List notesList = pc.getNotesList();
		NoteItem csNotes = null;
		int newNodeId = 0;
		for(int i = 0; i < notesList.size(); i++) {
			NoteItem note = (NoteItem)notesList.get(i);
			if (note.getId() > newNodeId) {
				newNodeId = note.getId();
			}
			if(note.getName().equals("Character Sheet Notes")) {
				csNotes = note;
			}
		}
		//Add notes item for the notes pane
		if(csNotes == null) {
			newNodeId++;
			csNotes = new NoteItem(newNodeId, -1, "Character Sheet Notes", "");
			pc.addNotesItem(csNotes);
		}
		establishTreeNodes();
	}

	//If there is no CS Notes node, returns the root
	private NoteTreeNode getCSNode() {
		Enumeration e = rootTreeNode.children();
		while (e.hasMoreElements()) {
			NoteTreeNode node = (NoteTreeNode) e.nextElement();
			if (node.toString().equals("Character Sheet Notes")) {
				return node;
			}
		}
		return rootTreeNode;
	}

	/**
	 * This method currently does nothing
	 */
	public void updateProperties() {
		// Do Nothing
	}

	/**
	 * Recursively build up the tree of notes.
	 * The tree is built off the root node rootTreeNode
	 */
	private void establishTreeNodes() {
		List nodesToBeAddedList = new ArrayList();

		int order = 0;
		rootTreeNode = new NoteTreeNode(null, pc);
		notesModel = new DefaultTreeModel(rootTreeNode);
		notesTree.setModel(notesModel);
		List testList = pc.getNotesList();
		for(int i = 0; i < testList.size(); i++) {
			NoteItem testnote = (NoteItem)testList.get(i);
			//Don't fuck with this - I plan on uncommenting this later when I don't need to test the hidden node anymore -DJ
			//if(!testnote.getName().equals("Hidden")) {
				nodesToBeAddedList.add(testnote);
			//}
		}
		bioNote = new NoteItem(BIO_NOTEID, -1, "Bio", pc.getBio());
		nodesToBeAddedList.add(order++, bioNote);
		descriptionNote = new NoteItem(DESCRIPTION_NOTEID, -1, PropertyFactory.getString("in_descrip"),
		 pc.getDescription());
		nodesToBeAddedList.add(order++, descriptionNote);
		companionNote = new NoteItem(COMPANION_NOTEID, -1, PropertyFactory.getString("in_companions"),
		 pc.getMiscList().get(1));
		nodesToBeAddedList.add(order++, companionNote);
		otherAssetsNote = new NoteItem(OTHERASSETS_NOTEID, -1, PropertyFactory.getString("in_otherAssets"),
		 pc.getMiscList().get(0));
		nodesToBeAddedList.add(order++, otherAssetsNote);
		magicItemsNote = new NoteItem(MAGICITEMS_NOTEID, -1, PropertyFactory.getString("in_magicItems"),
		 pc.getMiscList().get(2));
		nodesToBeAddedList.add(order++, magicItemsNote);

		addNodes(rootTreeNode, nodesToBeAddedList);
	}

	private void addNodes(NoteTreeNode node, List nodesToBeAddedList) {
		int index = -1;
		if(node.getItem() != null) {
			index = node.getItem().getId();
		}

		for (int i = 0; i < nodesToBeAddedList.size(); i++) {
			NoteItem ni = (NoteItem) nodesToBeAddedList.get(i);

			if (ni.getParentId() == index) {
				NoteTreeNode childNode = new NoteTreeNode(ni, pc);
				node.add(childNode);
				nodesToBeAddedList.remove(i);
				i--;
			}
		}

		for (int i = 0; i < node.getChildCount(); i++) {
			addNodes((NoteTreeNode) node.getChildAt(i), nodesToBeAddedList);
		}
	}

	private void selectNotesNode(int rowNum) {
		notesTree.requestFocus();
		notesTree.setSelectionRow(rowNum);

		TreePath path = notesTree.getSelectionPath();
		Object o = path.getLastPathComponent();

		if ((o != null) && (o instanceof NoteTreeNode)) {
			selectNotesNode((NoteTreeNode) o);
		}
	}

	private void selectNotesNode(NoteTreeNode node) {
		if (currentItem != null) {
			updateNoteItem();
		}
		notesTree.expandPath(new TreePath(notesModel.getPathToRoot(node)));

		NoteItem selectedItem = node.getItem();
		currentItem = selectedItem;

		if (selectedItem != null) {
			notesArea.setText(currentItem.getValue());

			if (lastItem != null) { // exchange places
				int oldParent = currentItem.getParentId();
				currentItem.setParentId(lastItem.getParentId());
				lastItem.setParentId(oldParent);
				establishTreeNodes();
				notesModel.setRoot(rootTreeNode);
				notesTree.updateUI();
			}

			notesArea.setEnabled(true);
			notesArea.setEditable(true);
		}
		else {
			notesArea.setText(PropertyFactory.getString("in_idNoteEdit"));
			notesArea.setEnabled(false);
			notesArea.setEditable(false);
		}

		notesArea.setCaretPosition(0);
	}

	private void updateNoteItem() {
		if ((currentItem != null) && textIsDirty) {
			int x = pc.getNotesList().indexOf(currentItem);
			currentItem.setValue(notesArea.getText());

			if (x > -1) {
				(pc.getNotesList().get(x)).setValue(notesArea.getText());
				pc.setDirty(true);
			}
			else if (currentItem == bioNote) {
				pc.setBio(notesArea.getText());
				pc.setDirty(true);
			}
			else if (currentItem == descriptionNote) {
				pc.setDescription(notesArea.getText());
				pc.setDirty(true);
			}
			else if (currentItem == otherAssetsNote) {
				pc.getMiscList().set(0, notesArea.getText());
				pc.setDirty(true);
			}
			else if (currentItem == companionNote) {
				pc.getMiscList().set(1, notesArea.getText());
				pc.setDirty(true);
			}
			else if (currentItem == magicItemsNote) {
				pc.getMiscList().set(2, notesArea.getText());
				pc.setDirty(true);
			}

			textIsDirty = false;
		}
	}

	/**
	 * A tree node dedicated to storing notes.
	 */
	private static class NoteTreeNode extends DefaultMutableTreeNode {
		static final long serialVersionUID = -8015559748421397718L;
		private NoteItem item;
		private PlayerCharacter pc;

		NoteTreeNode(NoteItem item, PlayerCharacter pc) {
			this.item = item;
			this.pc = pc;
		}

		public String toString() {
			if (item != null) {
				return item.toString();
			}

			return pc.getDisplayName();
		}

		public void setUserObject(Object userObject) {
			super.setUserObject(userObject);
			renameNote((String)userObject);
		}

		private final NoteItem getItem() {
			return item;
		}

		public void renameNote(String name) {
			getItem().setName(name);
			pc.setDirty(true);
		}

		public void deleteNote() {
			int numChildren = 0;
			int reallyDelete;
			Enumeration allChildren = breadthFirstEnumeration();

			while (allChildren.hasMoreElements()) {
				NoteTreeNode ancestorNode = (NoteTreeNode) allChildren.nextElement();
				if (ancestorNode != this) {
					numChildren++;
				}
			}

			//The following line should be taken out and shot!
			reallyDelete = JOptionPane.showConfirmDialog(null,
			 PropertyFactory.getString("in_delNote1") + " " + toString() +
			 ((numChildren > 0) ?
			 (" " + PropertyFactory.getString("in_delNote2") + " " + (numChildren) + " " + PropertyFactory.getString("in_delNote3")) :
			 " ") + "?",
			 PropertyFactory.getString("in_delNote4"), JOptionPane.OK_CANCEL_OPTION);

			if (reallyDelete == JOptionPane.OK_OPTION) {
				NoteTreeNode aParent = (NoteTreeNode) getParent();

				if (aParent != null) {
					allChildren = breadthFirstEnumeration();

					while (allChildren.hasMoreElements()) {
						NoteTreeNode ancestorNode = (NoteTreeNode) allChildren.nextElement();
						pc.getNotesList().remove(ancestorNode.getItem());
					}

					aParent.remove(this);
					pc.setDirty(true);
				}
			}
		}

		public void addNote() {
			int parentId = -1;
			int newNodeId = 0;

			parentId = getItem().getId();

			Iterator allNotes = pc.getNotesList().iterator();

			while (allNotes.hasNext()) {
				final NoteItem currItem = (NoteItem) allNotes.next();

				if (currItem.getId() > newNodeId) {
					newNodeId = currItem.getId();
				}
			}

			++newNodeId;

			NoteItem note = new NoteItem(newNodeId, parentId, PropertyFactory.getString("in_newItem"),
			 PropertyFactory.getString("in_newValue"));
			NoteTreeNode node = new NoteTreeNode(note, pc);

			add(node);

			pc.addNotesItem(note);
			pc.setDirty(true);
		}
	}

	private class NotePopupListener extends MouseAdapter {
		private JTree aNotesTree;
		private NotePopupMenu notesMenu;

		NotePopupListener(JTree notesTree, NotePopupMenu menu) {
			this.aNotesTree = notesTree;
			this.notesMenu = menu;

			KeyListener myKeyListener = new KeyListener() {
				public void keyTyped(KeyEvent e) {
					dispatchEvent(e);
				}

				public void keyPressed(KeyEvent e) {
					int keyCode = e.getKeyCode();

					if (keyCode != KeyEvent.VK_UNDEFINED) {
						KeyStroke keyStroke = KeyStroke.getKeyStrokeForEvent(e);

						for (int i = 0; i < notesMenu.getComponentCount(); i++) {
							JMenuItem menuItem = (JMenuItem) notesMenu.getComponent(i);
							KeyStroke ks = menuItem.getAccelerator();

							if ((ks != null) && keyStroke.equals(ks)) {
								menuItem.doClick(2);

								return;
							}
						}
					}

					dispatchEvent(e);
				}

				public void keyReleased(KeyEvent e) {
					dispatchEvent(e);
				}
			};

			notesTree.addKeyListener(myKeyListener);
		}

		public void mousePressed(MouseEvent evt) {
			maybeShowPopup(evt);
		}

		public void mouseReleased(MouseEvent evt) {
			maybeShowPopup(evt);
		}

		private void maybeShowPopup(MouseEvent evt) {
			if (evt.isPopupTrigger()) {
				TreePath selPath = aNotesTree.getClosestPathForLocation(evt.getX(), evt.getY());

				if (selPath == null) {
					return;
				}

				aNotesTree.setSelectionPath(selPath);
				notesMenu.show(evt.getComponent(), evt.getX(), evt.getY());
			}
		}
	}

	//Notes popup menu
	private class NotePopupMenu extends JPopupMenu {
		static final long serialVersionUID = -8015559748421397718L;

		NotePopupMenu() {
			NotePopupMenu.this.add(createAddMenuItem(PropertyFactory.getString("in_add"), "shortcut EQUALS"));
			NotePopupMenu.this.add(createRemoveMenuItem(PropertyFactory.getString("in_remove"), "shortcut MINUS"));
			NotePopupMenu.this.add(createRenameMenuItem(PropertyFactory.getString("in_rename"), "alt M"));
		}

		private JMenuItem createAddMenuItem(String label, String accelerator) {
			return Utility.createMenuItem(label, new AddNoteActionListener(), PropertyFactory.getString("in_add"),
			 (char) 0, accelerator, PropertyFactory.getString("in_add"), "Add16.gif", true);
		}

		private JMenuItem createRemoveMenuItem(String label, String accelerator) {
			return Utility.createMenuItem(label, new RemoveNoteActionListener(),
			 PropertyFactory.getString("in_delete"), (char) 0, accelerator, PropertyFactory.getString("in_delete"),
			 "Remove16.gif", true);
		}

		private JMenuItem createRenameMenuItem(String label, String accelerator) {
			return Utility.createMenuItem(label, new RenameNoteActionListener(),
			 PropertyFactory.getString("in_rename"), (char) 0, accelerator, PropertyFactory.getString("in_rename"),
			 "Add16.gif", true);
		}

		private class AddNoteActionListener extends NoteActionListener {
			AddNoteActionListener() {
				super();
			}

			public void actionPerformed(ActionEvent evt) {
				addButton.doClick();
			}
		}

		private class NoteActionListener implements ActionListener {

			NoteActionListener() {
				// Do Nothing
			}

			public void actionPerformed(ActionEvent evt) {
			  // TODO This method currently does nothing?
			}
		}

		private class RemoveNoteActionListener extends NoteActionListener {
			RemoveNoteActionListener() {
				super();
			}

			public void actionPerformed(ActionEvent evt) {
				deleteButton.doClick();
			}
		}

		private class RenameNoteActionListener extends NoteActionListener {
			RenameNoteActionListener() {
				super();
			}

			public void actionPerformed(ActionEvent evt) {
				renameButton.doClick();
			}
		}
	}
}
