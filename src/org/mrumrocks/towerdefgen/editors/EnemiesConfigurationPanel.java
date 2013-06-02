package org.mrumrocks.towerdefgen.editors;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Font;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.mrumrocks.towerdefgen.data.EnemiesData;

import tools.customizable.AbstractProperty;
import tools.customizable.CounterProperty;
import tools.customizable.FileProperty;
import tools.customizable.PropertyPanel;
import tools.customizable.PropertySet;
import tools.customizable.TextProperty;

public class EnemiesConfigurationPanel extends
		MultiEditorPanel<EnemiesData, EnemiesData.SingleEnemyData> {

	private class SingleEnemyEditor extends
			EditorPanel<EnemiesData.SingleEnemyData> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		private CardLayout cl;

		private final Runnable rUpdate;

		public SingleEnemyEditor() {
			super();
			setLayout(cl = new CardLayout());

			JLabel lblNothing = new JLabel(" [ No enemy selected ]");
			lblNothing.setHorizontalAlignment(JLabel.CENTER);
			add(lblNothing, Boolean.toString(false));

			JPanel pnlEditor = new JPanel(new BorderLayout());
			add(pnlEditor, Boolean.toString(true));

			PropertySet ps = new PropertySet();

			final TextProperty tpName = new TextProperty("Enemy name", null);
			final FileProperty fpImage = new FileProperty(
					"Enemy image (64 \u00d7 64 or smaller)", null);
			final CounterProperty cpSlowness = new CounterProperty("Slowness",
					1, 15, Integer.MAX_VALUE);
			final CounterProperty cpHealth = new CounterProperty(
					"Enemy health", 1, 10, Integer.MAX_VALUE);
			final CounterProperty cpMoney = new CounterProperty("Money", 0, 1,
					Integer.MAX_VALUE);

			ps.add(tpName);
			ps.add(fpImage);
			ps.add(cpSlowness);
			ps.add(cpHealth);
			ps.add(cpMoney);

			fpImage.setFilter(ffImage);

			tpName.setDescription("The level name (not displayed in the game).");
			fpImage.setDescription("The image for the enemy. The enemy should face to the right in the image.");
			cpSlowness
					.setDescription("The enemy's slowness. This is the number of frames it takes to move from one waypoint to the next.");
			cpHealth.setDescription("The enemy's starting health.");
			cpMoney.setDescription("The amount of money the enemy yields when killed.");

			ChangeListener cl = new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent ce) {
					if (!isUpdating()) {
						contents.name = tpName.getValue();
						contents.image = fpImage.getValue();
						contents.slowness = cpSlowness.getValue();
						contents.health = cpHealth.getValue();
						contents.money = cpMoney.getValue();
						EnemiesConfigurationPanel.this.repaint();
					}
				}
			};

			for (AbstractProperty<?> p : ps) {
				if (p != null) {
					p.addChangeListener(cl);
				}
			}

			pnlEditor.add(new PropertyPanel(ps, true, false),
					BorderLayout.CENTER);

			rUpdate = new Runnable() {
				@Override
				public void run() {
					tpName.setValue(contents.name);
					fpImage.setValue(contents.image);
					cpSlowness.setValue(contents.slowness);
					cpHealth.setValue(contents.health);
					cpMoney.setValue(contents.money);
				}
			};

			updatePanel();
		}

		@Override
		protected void updatePanel() {
			cl.show(this, Boolean.toString(contents != null));
			if (contents != null) {
				rUpdate.run();
			}
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private class SingleEnemyDataRenderer implements
			ListCellRenderer<EnemiesData.SingleEnemyData> {

		private final DefaultListCellRenderer dlcr = new DefaultListCellRenderer();

		@Override
		public Component getListCellRendererComponent(
				JList<? extends EnemiesData.SingleEnemyData> list,
				EnemiesData.SingleEnemyData value, int index,
				boolean isSelected, boolean cellHasFocus) {
			JLabel def = (JLabel) dlcr.getListCellRendererComponent(list,
					value, index, isSelected, cellHasFocus);
			Font oldFont = def.getFont();
			def.setFont(def.getFont().deriveFont(Font.ITALIC));

			if (value != null) {
				if (value.name != null && !value.name.trim().isEmpty()) {
					def.setFont(oldFont);
					def.setText(value.name);
				} else {
					def.setText("Unnamed Enemy");
				}
			} else {
				def.setText("Empty Enemy");
			}
			return def;
		}
	}

	@Override
	protected ListCellRenderer<EnemiesData.SingleEnemyData> createCellRenderer() {
		return new SingleEnemyDataRenderer();
	}

	@Override
	protected EnemiesData.SingleEnemyData createNewSub() {
		return new EnemiesData.SingleEnemyData();
	}

	@Override
	protected EditorPanel<EnemiesData.SingleEnemyData> createSubEditor() {
		return new SingleEnemyEditor();
	}

	@Override
	protected List<EnemiesData.SingleEnemyData> getSubItems() {
		return contents.enemies;
	}

	@Override
	protected EnemiesData createData() {
		return new EnemiesData();
	}

}
