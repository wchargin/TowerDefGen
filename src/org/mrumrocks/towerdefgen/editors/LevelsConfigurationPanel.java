package org.mrumrocks.towerdefgen.editors;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.layout.CC;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;

import org.mrumrocks.towerdefgen.data.LevelsData;

import tools.customizable.AbstractProperty;
import tools.customizable.AbstractSwingProperty;
import tools.customizable.ColorProperty;
import tools.customizable.CounterProperty;
import tools.customizable.FileProperty;
import tools.customizable.PropertyPanel;
import tools.customizable.PropertySet;
import tools.customizable.TextProperty;

public class LevelsConfigurationPanel extends
		MultiEditorPanel<LevelsData, LevelsData.SingleLevelData> {

	private class SingleLevelDataRenderer implements
			ListCellRenderer<LevelsData.SingleLevelData> {

		private final DefaultListCellRenderer dlcr = new DefaultListCellRenderer();

		@Override
		public Component getListCellRendererComponent(
				JList<? extends LevelsData.SingleLevelData> list,
				LevelsData.SingleLevelData value, int index,
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
					def.setText("Unnamed Level");
				}
			} else {
				def.setText("Empty Level");
			}
			return def;
		}
	}

	private class SingleLevelEditor extends
			EditorPanel<LevelsData.SingleLevelData> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		private CardLayout cl;

		private final Runnable rUpdate;

		public SingleLevelEditor() {
			super();
			setLayout(cl = new CardLayout());

			JLabel lblNothing = new JLabel(" [ No level selected ]");
			lblNothing.setHorizontalAlignment(JLabel.CENTER);
			add(lblNothing, Boolean.toString(false));

			JPanel pnlEditor = new JPanel(new BorderLayout());
			add(pnlEditor, Boolean.toString(true));

			PropertySet ps = new PropertySet();

			final TextProperty tpName = new TextProperty("Level name", null);
			final FileProperty fpImage = new FileProperty(
					"Level image (640 \u00d7 480)", null);
			final FileProperty fpIcon = new FileProperty(
					"Thumbnail icon (240 \u00d7 180)", null);
			final WaypointProperty wp = new WaypointProperty("Enemy path",
					null, fpImage);
			final CounterProperty cpPathWidth = new CounterProperty(
					"Path width", 1, 40, 200);

			ps.add(tpName);
			ps.add(fpImage);
			ps.add(fpIcon);
			ps.add(null);
			ps.add(wp);
			ps.add(cpPathWidth);

			fpImage.setFilter(ffImage);
			fpIcon.setFilter(ffImage);

			tpName.setDescription("The level name (not displayed in the game).");
			fpImage.setDescription("The background image for the level.");
			fpIcon.setDescription("The small icon for the level select screen.");
			wp.setDescription("The path along which the enemies travel.");
			cpPathWidth
					.setDescription("The average width of the path, in pixels.");

			ChangeListener cl = new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent ce) {
					if (!isUpdating()) {
						contents.name = tpName.getValue();
						contents.backgroundImage = fpImage.getValue();
						contents.iconImage = fpIcon.getValue();
						contents.waypoints = wp.getValue();
						contents.pathWidth = cpPathWidth.getValue();
						LevelsConfigurationPanel.this.repaint();
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
					fpImage.setValue(contents.backgroundImage);
					fpIcon.setValue(contents.iconImage);
					wp.setValue(contents.waypoints);
					cpPathWidth.setValue(contents.pathWidth);
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

	private class WaypointDialog extends JDialog {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private WaypointPanel wp;

		public WaypointDialog(Polygon polygon, File ref) {
			super(SwingUtilities
					.getWindowAncestor(LevelsConfigurationPanel.this),
					"Configure Waypoint Path");
			setModal(true);

			JPanel pnlContent = new JPanel(new BorderLayout());
			setContentPane(pnlContent);
			JPanel pnlNorth = new JPanel(new MigLayout(new LC().flowY()));
			add(pnlNorth, BorderLayout.NORTH);

			PropertySet ps = new PropertySet();

			final FileProperty fpImage = new FileProperty("Background image",
					null);
			ps.add(fpImage);
			fpImage.setFilter(ffImage);

			final ColorProperty cpWaypoints = new ColorProperty(
					"Waypoint preview color", Color.BLACK);
			ps.add(cpWaypoints);

			final CounterProperty cpReso = new CounterProperty("Resolution", 1,
					25, 50);
			ps.add(cpReso);

			pnlNorth.add(new PropertyPanel(ps, true, false), new CC().growX()
					.pushX());

			wp = new WaypointPanel();
			wp.setPolygon(polygon);
			wp.setImageDisplayed(ref);

			fpImage.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					wp.setImageDisplayed(fpImage.getValue());
				}
			});

			cpWaypoints.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					wp.setWaypointColor(cpWaypoints.getValue());
				}
			});

			cpReso.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent arg0) {
					wp.setResolution(cpReso.getValue());
				}
			});

			pnlContent.add(wp, BorderLayout.CENTER);

			JScrollPane scroll = new JScrollPane(wp,
					JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
					JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			scroll.setPreferredSize(new Dimension(500, 500));
			pnlContent.add(scroll, BorderLayout.CENTER);

			JPanel pnlSouth = new JPanel(new GridLayout(1, 2));
			add(pnlSouth, BorderLayout.SOUTH);

			@SuppressWarnings("serial")
			JButton btnClear = new JButton(new AbstractAction(
					"Clear All Waypoints") {
				@Override
				public void actionPerformed(ActionEvent ae) {
					if (!wp.waypoints.isEmpty()) {
						wp.waypoints.clear();
						wp.repaint();
					} else {
						JOptionPane
								.showMessageDialog(
										LevelsConfigurationPanel.this,
										"There are no waypoints to remove.",
										"No Waypoints",
										JOptionPane.INFORMATION_MESSAGE);
					}
				}
			});
			pnlSouth.add(btnClear);

			@SuppressWarnings("serial")
			JButton btnRemove = new JButton(new AbstractAction(
					"Pop Last Waypoint") {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (!wp.waypoints.isEmpty()) {
						wp.waypoints.remove(wp.waypoints.size() - 1);
						wp.repaint();
					} else {
						JOptionPane
								.showMessageDialog(
										LevelsConfigurationPanel.this,
										"There are no waypoints to remove.",
										"No Waypoints",
										JOptionPane.INFORMATION_MESSAGE);
					}
				}
			});
			pnlSouth.add(btnRemove);
		}

		public Polygon getPolygon() {
			int size = wp.waypoints.size();
			int[] x = new int[size];
			int[] y = new int[size];
			for (int i = 0; i < size; i++) {
				Point point = wp.waypoints.get(i);
				x[i] = point.x;
				y[i] = point.y;
			}
			Polygon p = new Polygon(x, y, size);
			return p;
		}

	}

	private class WaypointPanel extends JPanel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The background image.
		 */
		private BufferedImage image;

		/**
		 * The waypoint color.
		 */
		private Color waypointColor = Color.BLACK;

		/**
		 * The list of waypoints.
		 */
		private List<Point> waypoints;

		/**
		 * The resolution for drawing.
		 */
		private int resolution = 25;

		/**
		 * The last known mouse position.
		 */
		private Point mousePos;

		public WaypointPanel() {
			waypoints = new ArrayList<Point>();
			final MouseAdapter adapter = new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent me) {
					if (waypoints.isEmpty()
							|| me.getPoint().distanceSq(
									waypoints.get(waypoints.size() - 1)) > 0) {
						waypoints.add(me.getPoint());
						repaint();
					}
				}

				@Override
				public void mouseDragged(MouseEvent me) {
					if (waypoints.isEmpty()
							|| waypoints.get(waypoints.size() - 1).distance(
									me.getPoint()) > resolution) {
						mousePos = me.getPoint();
						mouseClicked(me);
					}
				}

				@Override
				public void mouseExited(MouseEvent e) {
					mousePos = null;
					repaint();
				}

				@Override
				public void mouseMoved(MouseEvent me) {
					mousePos = me.getPoint();
					repaint();
				}

			};
			addMouseListener(adapter);
			addMouseMotionListener(adapter);
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (image != null) {
				g.drawImage(image, 0, 0, this);
			}
			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setColor(waypointColor);

			for (Point p : waypoints) {
				Ellipse2D e2d = new Ellipse2D.Double(p.x - 2, p.y - 2, 5, 5);

				if (mousePos != null && e2d.contains(mousePos)) {
					final String text = String.format("(%d, %d)", p.x, p.y);
					FontMetrics fm = g2d.getFontMetrics();
					int width = fm.stringWidth(text);
					int height = fm.getAscent();
					int x = p.x + width > getWidth() ? p.x - width : p.x;
					int y = p.y < height ? p.y + height : p.y;

					final ColorSpace cs = ColorSpace
							.getInstance(ColorSpace.CS_sRGB);
					float[] rgb = g2d.getColor().getColorComponents(cs,
							new float[3]);
					for (int i = 0; i < rgb.length; i++) {
						rgb[i] = 1 - rgb[i];
					}
					Color inverse = new Color(cs, rgb, g2d.getColor()
							.getAlpha() / 255f / 2);
					g2d.setColor(inverse);
					g2d.fill(e2d);

					g2d.fillRect(x - 2, y - height - 2, width + 4, height + 4);
					g2d.setColor(waypointColor);
					g2d.drawString(text, x, y);
				} else {
					g2d.fill(e2d);
				}
			}
		}

		public void setImageDisplayed(File f) {
			if (f != null) {
				try {
					image = ImageIO.read(f);
					setPreferredSize(new Dimension(image.getWidth(),
							image.getHeight()));
					setSize(getPreferredSize());
				} catch (IOException ie) {
					JOptionPane.showMessageDialog(
							LevelsConfigurationPanel.this,
							"The image file could not be loaded.");
				}
				repaint();
			}
		}

		public void setPolygon(Polygon polygon) {
			waypoints.clear();
			if (polygon != null) {
				for (int i = 0; i < polygon.npoints; i++) {
					waypoints.add(new Point(polygon.xpoints[i],
							polygon.ypoints[i]));
				}
			}
		}

		/**
		 * Sets the resolution.
		 * 
		 * @param resolution
		 *            the new resolution
		 */
		public void setResolution(int resolution) {
			this.resolution = resolution;
			repaint();
		}

		/**
		 * Sets the preview color for waypoints.
		 * 
		 * @param value
		 */
		public void setWaypointColor(Color value) {
			waypointColor = value;
			repaint();
		}
	}

	public class WaypointProperty extends
			AbstractSwingProperty<Polygon, JButton, JButton> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		private FileProperty imageLocation;

		public WaypointProperty(String name, Polygon value, FileProperty fpImage) {
			super(name, value);
			this.imageLocation = fpImage;
		}

		@Override
		protected JButton createEditor() {
			JButton btn = new JButton();
			btn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ae) {
					WaypointDialog dialog = new WaypointDialog(value,
							imageLocation.getValue());
					dialog.pack();
					dialog.setLocationRelativeTo(SwingUtilities
							.getWindowAncestor(LevelsConfigurationPanel.this));
					dialog.setVisible(true);
					setValue(dialog.getPolygon());
				}
			});
			return btn;
		}

		@Override
		protected JButton createViewer() {
			return new JButton();
		}

		@Override
		protected void updateEditor(JButton editor) {
			editor.setText(value == null ? "Create path" : ("Edit path ("
					+ value.npoints + " points)"));
		}

		@Override
		protected void updateViewer(JButton viewer) {
			if (value == null || value.npoints == 0) {
				viewer.setEnabled(false);
				viewer.setText("No points in path");
			} else {
				viewer.setEnabled(true);
				viewer.setText("View path (" + value.npoints + " points)");
			}
		}

	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected ListCellRenderer<LevelsData.SingleLevelData> createCellRenderer() {
		return new SingleLevelDataRenderer();
	}

	@Override
	protected LevelsData.SingleLevelData createNewSub() {
		return new LevelsData.SingleLevelData();
	}

	@Override
	protected EditorPanel<LevelsData.SingleLevelData> createSubEditor() {
		return new SingleLevelEditor();
	}

	@Override
	protected List<LevelsData.SingleLevelData> getSubItems() {
		return contents.levels;
	}

	@Override
	protected LevelsData createData() {
		return new LevelsData();
	}

}
