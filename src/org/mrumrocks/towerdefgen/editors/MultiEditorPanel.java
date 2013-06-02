package org.mrumrocks.towerdefgen.editors;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.mrumrocks.towerdefgen.core.Data;
import org.mrumrocks.towerdefgen.core.MultiData;
import org.mrumrocks.towerdefgen.core.SimpleListModel;

public abstract class MultiEditorPanel<T extends MultiData<U>, U extends Data>
		extends EditorPanel<T> {

	private static final int MAX_WIDTH = 150;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private SimpleListModel<U> model;

	private final Runnable rUpdateContents;

	private final Runnable rUpdateButtons;

	public MultiEditorPanel() {
		super();
		setLayout(new BorderLayout());

		contents = createData();

		JPanel pnlLevelSelect = new JPanel(new BorderLayout());
		add(pnlLevelSelect, BorderLayout.WEST);

		model = new SimpleListModel<>();
		final JList<U> list = new JList<U>(model);
		list.setCellRenderer(createCellRenderer());
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scroll = new JScrollPane(list) {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public Dimension getMaximumSize() {
				Dimension s = super.getMaximumSize();
				s.width = Math.min(s.width, MAX_WIDTH);
				return s;
			}

			@Override
			public Dimension getPreferredSize() {
				Dimension s = super.getPreferredSize();
				s.width = Math.min(s.width, MAX_WIDTH);
				return s;
			}

		};
		pnlLevelSelect.add(scroll, BorderLayout.CENTER);

		JPanel pnlControls = new JPanel(new GridLayout(2, 2));
		pnlLevelSelect.add(pnlControls, BorderLayout.SOUTH);

		final JButton btnAdd = new JButton("Add");
		pnlControls.add(btnAdd);

		final JButton btnRemove = new JButton("Remove");
		pnlControls.add(btnRemove);

		btnAdd.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				U data = createNewSub();
				model.add(data);
				contents.getSubs().add(data);
				list.setSelectedValue(data, true);
				rUpdateButtons.run();
			}
		});
		btnRemove.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				U data = list.getSelectedValue();
				if (data != null) {
					model.remove(list.getSelectedIndex());
				}
			}
		});

		final JButton btnUp = new JButton("Move Up");
		final JButton btnDown = new JButton("Move Down");

		pnlControls.add(btnUp);
		pnlControls.add(btnDown);

		btnUp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				model.moveUp(list.getSelectedIndex());
				list.setSelectedIndex(list.getSelectedIndex() - 1);
			}
		});

		btnDown.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				model.moveDown(list.getSelectedIndex());
				list.setSelectedIndex(list.getSelectedIndex() + 1);
			}
		});

		final EditorPanel<U> subEditor = createSubEditor();
		add(subEditor, BorderLayout.CENTER);

		list.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent lse) {
				subEditor.setContents(list.getSelectedValue());
				rUpdateButtons.run();
			}
		});

		rUpdateButtons = new Runnable() {
			@Override
			public void run() {
				btnRemove.setEnabled(!model.isEmpty());
				btnUp.setEnabled(!list.isSelectionEmpty()
						&& list.getSelectedIndex() > 0);
				btnDown.setEnabled(!list.isSelectionEmpty()
						&& list.getSelectedIndex() + 1 < model.size());
			}
		};

		rUpdateContents = new Runnable() {
			@Override
			public void run() {
				model.clear();
				if (contents != null) {
					List<U> sub = getSubItems();
					if (sub != null) {
						for (U u : sub) {
							model.add(u);
						}
					}
				}
			}
		};

		updatePanel();
	}

	protected abstract T createData();

	protected abstract ListCellRenderer<U> createCellRenderer();

	protected abstract U createNewSub();

	protected abstract EditorPanel<U> createSubEditor();

	protected abstract List<U> getSubItems();

	@Override
	public void updatePanel() {
		rUpdateContents.run();
		rUpdateButtons.run();
	}

}
