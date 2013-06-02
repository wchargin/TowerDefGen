package org.mrumrocks.towerdefgen.editors;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.mrumrocks.towerdefgen.core.Data;
import org.mrumrocks.towerdefgen.core.Editor;

public abstract class EditorPanel<T extends Data> extends JPanel implements
		Editor<T> {

	protected static final FileFilter ffImage = new FileNameExtensionFilter(
			"Image files", ImageIO.getReaderFileSuffixes());
	protected static final FileFilter ffSoundWav = new FileNameExtensionFilter(
			"Waveform audio files", "wav", "aiff", "au");
	protected static final FileFilter ffSoundOgg = new FileNameExtensionFilter(
			"Ogg vorbis audio files", "ogg");

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The contents for this panel.
	 */
	protected T contents;
	private transient boolean updating;

	@Override
	public T getContents() {
		return contents;
	}

	@Override
	public void setContents(T contents) {
		this.contents = contents;
		updating = true;
		updatePanel();
		updating = false;
	}

	protected abstract void updatePanel();

	protected boolean isUpdating() {
		return updating;
	}

}
