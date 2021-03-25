package de.hans.google.upload.data;

import java.io.File;

public class VideoMetaData {

	private String title;
	private String description;
	private File thumbnail;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public File getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(File thumbnail) {
		this.thumbnail = thumbnail;
	}

}
