package org.mrumrocks.towerdefgen;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import net.miginfocom.layout.CC;
import net.miginfocom.swing.MigLayout;

import org.mrumrocks.towerdefgen.TowerDefGen.ProgressIndicator.ProgressState;
import org.mrumrocks.towerdefgen.core.Aspect;
import org.mrumrocks.towerdefgen.core.CodeGenerator;
import org.mrumrocks.towerdefgen.core.CodeGenerator.CodeProgress;
import org.mrumrocks.towerdefgen.core.Data;
import org.mrumrocks.towerdefgen.core.GameData;
import org.mrumrocks.towerdefgen.core.Invalid;
import org.mrumrocks.towerdefgen.editors.EnemiesConfigurationPanel;
import org.mrumrocks.towerdefgen.editors.GeneralConfigurationPanel;
import org.mrumrocks.towerdefgen.editors.LevelsConfigurationPanel;
import org.mrumrocks.towerdefgen.editors.ProjectilesConfigurationPanel;
import org.mrumrocks.towerdefgen.editors.ShopConfigurationPanel;
import org.mrumrocks.towerdefgen.editors.TowersConfigurationPanel;

public class TowerDefGen extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private File lastSave = null;

	private GeneralConfigurationPanel gcp;
	private ShopConfigurationPanel scp;
	private LevelsConfigurationPanel lcp;
	private TowersConfigurationPanel tcp;
	private EnemiesConfigurationPanel ecp;
	private ProjectilesConfigurationPanel pcp;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		TowerDefGen gen = new TowerDefGen();
		gen.pack();
		gen.setLocationRelativeTo(null);
		gen.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gen.setVisible(true);
	}

	public TowerDefGen() {
		super("Tower Defense Game Creator");
		JTabbedPane jtp = new JTabbedPane();
		setContentPane(jtp);

		gcp = new GeneralConfigurationPanel();
		scp = new ShopConfigurationPanel();
		lcp = new LevelsConfigurationPanel();
		tcp = new TowersConfigurationPanel();
		ecp = new EnemiesConfigurationPanel();
		pcp = new ProjectilesConfigurationPanel();

		jtp.addTab(Aspect.GENERAL.toString(), gcp);
		jtp.addTab(Aspect.SHOP.toString(), scp);
		jtp.addTab(Aspect.LEVELS.toString(), lcp);
		jtp.addTab(Aspect.TOWERS.toString(), tcp);
		jtp.addTab(Aspect.ENEMIES.toString(), ecp);
		jtp.addTab(Aspect.PROJECTILES.toString(), pcp);

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		mnFile.setMnemonic(KeyEvent.VK_F);

		JMenuItem miSave = new JMenuItem("Save");
		mnFile.add(miSave);
		miSave.setMnemonic('S');
		miSave.setAccelerator(getAccelerator(KeyEvent.VK_S));
		miSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				performSave();
			}
		});

		JMenuItem miSaveAs = new JMenuItem("Save As...");
		mnFile.add(miSaveAs);
		miSaveAs.setMnemonic('A');
		miSaveAs.setAccelerator(getAccelerator(KeyEvent.VK_S,
				KeyEvent.SHIFT_DOWN_MASK));
		miSaveAs.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				final File oldLastSave = lastSave;
				lastSave = null;
				performSave();
				if (lastSave == null) {
					// save was cancelled; restore
					lastSave = oldLastSave;
				}
			}
		});

		JMenuItem miOpen = new JMenuItem("Open...");
		mnFile.add(miOpen);
		miOpen.setMnemonic('O');
		miOpen.setAccelerator(getAccelerator(KeyEvent.VK_O));
		miOpen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				performOpen();
			}
		});

		JMenu mnCode = new JMenu("Game");
		menuBar.add(mnCode);
		mnCode.setMnemonic('G');

		JMenuItem miValidate = new JMenuItem("Validate");
		mnCode.add(miValidate);
		miValidate.setMnemonic('V');
		miValidate.setAccelerator(getAccelerator(KeyEvent.VK_F10));
		miValidate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				displayValidationErrorDialog(false, validateData());
			}
		});

		JMenuItem miGenerate = new JMenuItem("Generate");
		mnCode.add(miGenerate);
		miGenerate.setMnemonic('G');
		miGenerate.setAccelerator(getAccelerator(KeyEvent.VK_F11, 0));
		miGenerate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				generateCode();
			}
		});

	}

	protected void generateCode() {
		List<Invalid> errors = validateData();
		if (!errors.isEmpty()) {
			displayValidationErrorDialog(true, errors);
			return;
		}

		JFileChooser save = new JFileChooser();
		save.setAcceptAllFileFilterUsed(false);
		save.setFileFilter(new FileNameExtensionFilter("JAR files", "jar"));
		int result = save.showSaveDialog(this);
		if (result != JFileChooser.APPROVE_OPTION) {
			return;
		}

		File selected = save.getSelectedFile();
		if (!selected.getName().toLowerCase().endsWith(".jar")) {
			selected = new File(selected.getAbsolutePath() + ".jar");
		}

		final Map<Aspect, ProgressIndicator> boxes = new LinkedHashMap<>();
		boxes.put(Aspect.GENERAL, new ProgressIndicator(
				"Generating general and shop code"));
		boxes.put(Aspect.LEVELS, new ProgressIndicator("Generating level code"));
		boxes.put(Aspect.TOWERS, new ProgressIndicator("Generating tower code"));
		boxes.put(Aspect.ENEMIES,
				new ProgressIndicator("Generating enemy code"));
		boxes.put(Aspect.PROJECTILES, new ProgressIndicator(
				"Generating projectile code"));
		boxes.put(null, new ProgressIndicator("Compiling and packaging code"));

		final JPanel pnlDialog = new JPanel(new MigLayout());

		JLabel lblTitle = new JLabel("Code generation in progress.");
		JLabel lblSubtitle = new JLabel("Please wait...");
		lblTitle.setFont(lblTitle.getFont().deriveFont(24f)
				.deriveFont(Font.BOLD));
		lblSubtitle.setFont(lblSubtitle.getFont().deriveFont(18f));

		CC labels = new CC().growX().pushX().wrap(); // looks nicer without span

		pnlDialog.add(lblTitle, labels);
		pnlDialog.add(lblSubtitle, labels);
		pnlDialog.add(new JSeparator(JSeparator.HORIZONTAL), new CC().growX()
				.pushX().spanX());

		for (ProgressIndicator indicator : boxes.values()) {
			indicator.addTo(pnlDialog);
		}

		JProgressBar pb = new JProgressBar();
		pb.setIndeterminate(true);
		pnlDialog.add(pb, new CC().newline().growX().pushX().spanX());

		final JDialog dialog = new JDialog(TowerDefGen.this,
				"Code Generation Progress");
		dialog.setContentPane(pnlDialog);
		dialog.pack();
		dialog.setLocationRelativeTo(TowerDefGen.this);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.setVisible(true);
		dialog.setModal(true);

		final File finalFile = selected;
		new Thread(new Runnable() {
			@Override
			public void run() {
				CodeGenerator.generateJar(getDataObject(), finalFile,
						new CodeProgress() {
							private CodeGenerationState lastState = null;

							@Override
							public void progressUpdated(
									CodeGenerationState newState) {
								if (lastState != null) {
									boxes.get(lastState.aspect).setState(
											ProgressState.COMPLETED);
								}
								lastState = newState;
								boxes.get(newState.aspect).setState(
										ProgressState.IN_PROGRESS);
							}

						});
				dialog.setVisible(false);
				dialog.dispose();

				int ret = JOptionPane
						.showConfirmDialog(
								TowerDefGen.this,
								"Your game has been exported. Would you like to launch it now?",
								"Export Complete", JOptionPane.YES_NO_OPTION);
				if (ret == JOptionPane.YES_OPTION) {
					ProcessBuilder pb = new ProcessBuilder("java", "-jar",
							finalFile.getAbsolutePath());
					try {
						pb.start();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}).start();

	}

	public static class ProgressIndicator {
		private JLabel message;
		private JLabel status;

		public void addTo(JComponent container) {
			container.add(message, new CC().newline().pushX().growX());
			container.add(status, new CC().pushX().growX().minWidth("100"));
		}

		public ProgressIndicator(String message) {
			this.message = new JLabel(message);
			this.message.setHorizontalAlignment(JLabel.LEFT);
			this.status = new JLabel();
			setState(ProgressState.NOT_STARTED);
		}

		public void setState(ProgressState state) {
			status.setText(state.name);
			switch (state) {
			case IN_PROGRESS:
				message.setFont(message.getFont().deriveFont(Font.BOLD));
				break;
			case NOT_STARTED:
			case COMPLETED:
				message.setFont(message.getFont().deriveFont(Font.PLAIN));
				break;
			}
		}

		public enum ProgressState {
			NOT_STARTED(""), IN_PROGRESS("In progress"), COMPLETED("Completed");
			public final String name;

			public String getName() {
				return name;
			}

			private ProgressState(String name) {
				this.name = name;
			}

		}

	}

	private KeyStroke getAccelerator(int vk, int maskAdditions) {
		return KeyStroke.getKeyStroke(vk, Toolkit.getDefaultToolkit()
				.getMenuShortcutKeyMask() | maskAdditions);
	}

	private KeyStroke getAccelerator(int vk) {
		return getAccelerator(vk, 0);
	}

	private void performOpen() {
		JFileChooser chooser = new JFileChooser(lastSave);
		chooser.setFileFilter(getFileFilter());
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.showOpenDialog(this);
		File file = chooser.getSelectedFile();
		String errorMessage = null;
		if (file != null && file.isFile() && file.exists()) {
			try (FileInputStream fis = new FileInputStream(file);
					ObjectInputStream ois = new ObjectInputStream(fis)) {
				Object o = ois.readObject();
				if (o instanceof GameData) {
					GameData d = (GameData) o;

					gcp.setContents(d.general);
					scp.setContents(d.shop);
					lcp.setContents(d.levels);
					tcp.setContents(d.towers);
					ecp.setContents(d.enemies);
					pcp.setContents(d.projectiles);

					lastSave = file;
				} else {
					errorMessage = "The file does not contain valid game data.";
				}
			} catch (IOException ie) {
				errorMessage = "The file could not be read from the drive.";
			} catch (ClassNotFoundException e) {
				errorMessage = "The contents of the file are incompatible with this computer.";
			} finally {
				if (errorMessage != null) {
					JOptionPane.showMessageDialog(this, errorMessage, "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

	private void performSave() {
		if (lastSave == null) {
			chooseSaveLocation();
		}
		if (lastSave == null) {
			// It was null and the user cancelled again.
			return;
		}

		try (FileOutputStream fos = new FileOutputStream(lastSave);
				ObjectOutputStream oos = new ObjectOutputStream(fos)) {
			GameData data = getDataObject();
			oos.writeObject(data);
		} catch (IOException ie) {
			JOptionPane.showMessageDialog(this,
					"An error occurred while writing to the file.", "Error",
					JOptionPane.ERROR_MESSAGE);
		}

	}

	private void chooseSaveLocation() {
		JFileChooser chooser = new JFileChooser(lastSave);
		chooser.setFileFilter(getFileFilter());
		chooser.showSaveDialog(this);
		File chosen = chooser.getSelectedFile();
		if (chosen != null) {
			if (chosen.getName().toLowerCase().endsWith("tdg")) {
				lastSave = chosen;
			} else {
				lastSave = new File(chosen.getAbsolutePath() + ".tdg");
			}
		}
	}

	private FileNameExtensionFilter getFileFilter() {
		return new FileNameExtensionFilter("Tower Defense game setups", "tdg");
	}

	private List<Invalid> validateData() {
		GameData gameData = getDataObject();

		List<Invalid> errors = new ArrayList<>();

		Data[] data = { gameData.general, gameData.shop, gameData.levels,
				gameData.towers, gameData.enemies, gameData.projectiles };
		for (Data d : data) {
			errors.addAll(d.validate(gameData));
		}

		return errors;
	}

	private void displayValidationErrorDialog(boolean creationAttempted,
			List<Invalid> errors) {
		if (!errors.isEmpty()) {
			JPanel content = new JPanel(new BorderLayout(0, 8));

			JLabel label = new JLabel(
					(creationAttempted ? "Your game could not be created due to the following "
							: "Your game has the following ")
							+ errors.size()
							+ " "
							+ (errors.size() == 1 ? "error" : "errors") + ":");
			content.add(label, BorderLayout.NORTH);

			DefaultTableModel model = new DefaultTableModel(errors.size(), 2);
			model.setColumnIdentifiers(new String[] { "Aspect", "Message" });
			JTable table = new JTable(model);
			for (int i = 0; i < errors.size(); i++) {
				Invalid ve = errors.get(i);
				model.setValueAt(ve.aspect, i, 0);
				model.setValueAt(ve.message, i, 1);
			}
			table.setEnabled(false);
			table.getColumn("Aspect").setPreferredWidth(100);
			table.getColumn("Aspect").setMaxWidth(100);

			Dimension oldScrollSize = table
					.getPreferredScrollableViewportSize();
			oldScrollSize.height = Math.min(table.getPreferredSize().height,
					oldScrollSize.height);
			table.setPreferredScrollableViewportSize(oldScrollSize);
			content.add(new JScrollPane(table), BorderLayout.CENTER);

			JOptionPane.showMessageDialog(this, content, "Validation Failed",
					JOptionPane.ERROR_MESSAGE);
		} else {
			if (!creationAttempted) {
				JOptionPane
						.showMessageDialog(
								this,
								"The validation completed successfully. Your game has no errors.",
								"Validation Successful",
								JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}

	private GameData getDataObject() {
		GameData gameData = new GameData();
		gameData.general = gcp.getContents();
		gameData.shop = scp.getContents();
		gameData.levels = lcp.getContents();
		gameData.towers = tcp.getContents();
		gameData.enemies = ecp.getContents();
		gameData.projectiles = pcp.getContents();
		return gameData;
	}
}
