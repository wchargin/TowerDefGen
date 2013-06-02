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

import org.mrumrocks.towerdefgen.data.ProjectilesData;

import tools.customizable.AbstractProperty;
import tools.customizable.CounterProperty;
import tools.customizable.FileProperty;
import tools.customizable.PropertyPanel;
import tools.customizable.PropertySet;
import tools.customizable.TextProperty;

public class ProjectilesConfigurationPanel extends
		MultiEditorPanel<ProjectilesData, ProjectilesData.SingleProjectileData> {

	private class SingleProjectileEditor extends
			EditorPanel<ProjectilesData.SingleProjectileData> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		private final CardLayout cl;

		private Runnable rUpdate;

		public SingleProjectileEditor() {
			super();
			setLayout(cl = new CardLayout());

			JLabel lblNothing = new JLabel(" [ No projectile selected ]");
			lblNothing.setHorizontalAlignment(JLabel.CENTER);
			add(lblNothing, Boolean.toString(false));

			JPanel pnlEditor = new JPanel(new BorderLayout());
			add(pnlEditor, Boolean.toString(true));

			PropertySet ps = new PropertySet();

			final TextProperty tpName = new TextProperty("Name", null);
			final FileProperty fpImage = new FileProperty(
					"Image (48 \u00d7 48 or smaller)", null);
			final CounterProperty cpHealth = new CounterProperty("Health", 1,
					10, Integer.MAX_VALUE);

			ps.add(tpName);
			ps.add(fpImage);
			ps.add(cpHealth);

			fpImage.setFilter(ffImage);

			tpName.setDescription("The projectile name (not displayed in the game).");
			fpImage.setDescription("The image for the projectile. The projectile should be facing to the right.");
			cpHealth.setDescription("The amount of damage this projectile can perform.");

			final ChangeListener cl = new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent ce) {
					if (!isUpdating()) {
						contents.name = tpName.getValue();
						contents.image = fpImage.getValue();
						contents.health = cpHealth.getValue();
						ProjectilesConfigurationPanel.this.repaint();
					}
				}
			};

			for (AbstractProperty<?> p : ps) {
				if (p != null) {
					p.addChangeListener(cl);
				}
			}

			rUpdate = new Runnable() {
				@Override
				public void run() {
					tpName.setValue(contents.name);
					fpImage.setValue(contents.image);
					cpHealth.setValue(contents.health);
				}
			};

			pnlEditor.add(new PropertyPanel(ps, true, false),
					BorderLayout.CENTER);
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

	private class SingleProjectileDataRenderer implements
			ListCellRenderer<ProjectilesData.SingleProjectileData> {

		private final DefaultListCellRenderer dlcr = new DefaultListCellRenderer();

		@Override
		public Component getListCellRendererComponent(
				JList<? extends ProjectilesData.SingleProjectileData> list,
				ProjectilesData.SingleProjectileData value, int index,
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
					def.setText("Unnamed Projectile");
				}
			} else {
				def.setText("Empty Projectile");
			}
			return def;
		}
	}

	@Override
	protected ListCellRenderer<ProjectilesData.SingleProjectileData> createCellRenderer() {
		return new SingleProjectileDataRenderer();
	}

	@Override
	protected ProjectilesData.SingleProjectileData createNewSub() {
		return new ProjectilesData.SingleProjectileData();
	}

	@Override
	protected EditorPanel<ProjectilesData.SingleProjectileData> createSubEditor() {
		return new SingleProjectileEditor();
	}

	@Override
	protected List<ProjectilesData.SingleProjectileData> getSubItems() {
		return contents.projectiles;
	}

	@Override
	protected ProjectilesData createData() {
		return new ProjectilesData();
	}

}
