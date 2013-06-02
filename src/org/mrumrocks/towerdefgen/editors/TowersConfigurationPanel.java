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

import org.mrumrocks.towerdefgen.data.TowersData;

import tools.customizable.AbstractProperty;
import tools.customizable.CounterProperty;
import tools.customizable.FileProperty;
import tools.customizable.PropertyPanel;
import tools.customizable.PropertySet;
import tools.customizable.TextProperty;

public class TowersConfigurationPanel extends
		MultiEditorPanel<TowersData, TowersData.SingleTowerData> {

	public class TowerEditor extends EditorPanel<TowersData.SingleTowerData> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		protected class TowerLevelsEditor
				extends
				MultiEditorPanel<TowersData.SingleTowerData, TowersData.TowerLevelData> {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			protected ListCellRenderer<TowersData.TowerLevelData> createCellRenderer() {
				return new TowerLevelRenderer();
			}

			@Override
			protected TowersData.TowerLevelData createNewSub() {
				return new TowersData.TowerLevelData();
			}

			@Override
			protected EditorPanel<TowersData.TowerLevelData> createSubEditor() {
				return new TowerLevelEditor();
			}

			@Override
			protected List<TowersData.TowerLevelData> getSubItems() {
				return contents.levels;
			}

			@Override
			protected TowersData.SingleTowerData createData() {
				return new TowersData.SingleTowerData();
			}
		}

		private TowerLevelsEditor internal = new TowerLevelsEditor();
		private final Runnable rUpdate;

		@Override
		protected void updatePanel() {
			internal.setContents(contents);
			cl.show(this, Boolean.toString(contents != null));
			if (contents != null) {
				rUpdate.run();
			}
		}

		public TowerEditor() {
			super();
			setLayout(cl = new CardLayout());

			JLabel lblNothing = new JLabel(" [ No tower selected ]");
			lblNothing.setHorizontalAlignment(JLabel.CENTER);
			add(lblNothing, Boolean.toString(false));

			JPanel pnlEditor = new JPanel(new BorderLayout());
			add(pnlEditor, Boolean.toString(true));

			PropertySet ps = new PropertySet();

			final TextProperty tpName = new TextProperty("Tower name", null);

			ps.add(tpName);

			tpName.setDescription("The name of the turret.");

			tpName.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent ce) {
					if (!isUpdating()) {
						contents.name = tpName.getValue();
						TowersConfigurationPanel.this.repaint();
					}
				}
			});

			rUpdate = new Runnable() {
				@Override
				public void run() {
					tpName.setValue(contents.name);
				}
			};

			pnlEditor.add(new PropertyPanel(ps, true, false),
					BorderLayout.NORTH);

			pnlEditor.add(internal, BorderLayout.CENTER);
		}

		private final CardLayout cl;

	}

	public class TowerLevelEditor extends
			EditorPanel<TowersData.TowerLevelData> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		private final Runnable rUpdate;

		private final CardLayout cl;

		@Override
		protected void updatePanel() {
			cl.show(this, Boolean.toString(contents != null));
			if (contents != null) {
				rUpdate.run();
			}
		}

		public TowerLevelEditor() {
			super();
			setLayout(cl = new CardLayout());

			JLabel lblNothing = new JLabel("[ No upgrade level selected ]");
			lblNothing.setHorizontalAlignment(JLabel.CENTER);
			add(lblNothing, Boolean.toString(false));

			JPanel pnlEditor = new JPanel(new BorderLayout());
			add(pnlEditor, Boolean.toString(true));

			PropertySet ps = new PropertySet();

			final FileProperty fpImage = new FileProperty(
					"Image (64 \u00d7 64)", null);
			final CounterProperty cpCost = new CounterProperty("Cost", 0, 100,
					Integer.MAX_VALUE);
			final CounterProperty cpFireDelay = new CounterProperty(
					"Fire delay", 0, 48, Integer.MAX_VALUE);
			final CounterProperty cpRange = new CounterProperty("Tower range",
					1, 250, Integer.MAX_VALUE);
			final TextProperty tpProjectileType = new TextProperty(
					"Projectile type", null);
			final CounterProperty cpLaunchSpeed = new CounterProperty(
					"Launch speed", 1, 10, Integer.MAX_VALUE);

			ps.add(fpImage);
			ps.add(null);
			ps.add(cpCost);
			ps.add(cpFireDelay);
			ps.add(cpRange);
			ps.add(null);

			fpImage.setFilter(ffImage);
			ps.add(tpProjectileType);
			ps.add(cpLaunchSpeed);

			cpCost.setDescription("The cost to purchase the turret or upgrade to this level.");
			cpFireDelay
					.setDescription("The delay (in frames) between firings.");
			cpRange.setDescription("The range of this tower (in pixels).");
			tpProjectileType
					.setDescription("The type of projectile fired by this tower. This must match the name of one of the projectiles on the Projectile configuration page.");
			cpLaunchSpeed
					.setDescription("The speed (in pixels/frame) at which the projectiles move after being fired.");

			ChangeListener cl = new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent ce) {
					if (!isUpdating()) {
						contents.image = fpImage.getValue();
						contents.cost = cpCost.getValue();
						contents.fireDelay = cpFireDelay.getValue();
						contents.range = cpRange.getValue();
						contents.projectileType = tpProjectileType.getValue();
						contents.launchSpeed = cpLaunchSpeed.getValue();
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
					fpImage.setValue(contents.image);
					cpCost.setValue(contents.cost);
					cpFireDelay.setValue(contents.fireDelay);
					cpRange.setValue(contents.range);
					tpProjectileType.setValue(contents.projectileType);
					cpLaunchSpeed.setValue(contents.launchSpeed);
				}
			};
		}
	}

	public TowersConfigurationPanel() {
		super();

	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private class SingleTowerDataRenderer implements
			ListCellRenderer<TowersData.SingleTowerData> {

		private final DefaultListCellRenderer dlcr = new DefaultListCellRenderer();

		@Override
		public Component getListCellRendererComponent(
				JList<? extends TowersData.SingleTowerData> list,
				TowersData.SingleTowerData value, int index,
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
					def.setText("Unnamed Tower");
				}
			} else {
				def.setText("Empty Tower");
			}
			return def;
		}
	}

	private class TowerLevelRenderer implements
			ListCellRenderer<TowersData.TowerLevelData> {

		private final DefaultListCellRenderer dlcr = new DefaultListCellRenderer();

		@Override
		public Component getListCellRendererComponent(
				JList<? extends TowersData.TowerLevelData> list,
				TowersData.TowerLevelData value, int index, boolean isSelected,
				boolean cellHasFocus) {
			JLabel def = (JLabel) dlcr.getListCellRendererComponent(list,
					value, index, isSelected, cellHasFocus);
			def.setText(index == 0 ? ("Level 0 (no upgrades)") : "Level "
					+ index);
			return def;
		}
	}

	@Override
	protected ListCellRenderer<TowersData.SingleTowerData> createCellRenderer() {
		return new SingleTowerDataRenderer();
	}

	@Override
	protected TowersData.SingleTowerData createNewSub() {
		return new TowersData.SingleTowerData();
	}

	@Override
	protected EditorPanel<TowersData.SingleTowerData> createSubEditor() {
		return new TowerEditor();
	}

	@Override
	protected List<TowersData.SingleTowerData> getSubItems() {
		return contents.towers;
	}

	@Override
	protected TowersData createData() {
		return new TowersData();
	}

}
