package com.aific.finances.ui;

import java.awt.Component;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;

import com.aific.finances.Document;
import com.aific.finances.Main;
import com.aific.finances.util.FileExtensionFilter;
import com.aific.finances.util.FileExtensionGroupFilter;


/**
 * A collection of file choosers
 * 
 * @author Peter Macko
 */
public class FileChoosers {
	
	private static FileExtensionFilter csvFilter;
	private static FileExtensionFilter ofxFilter;
	private static FileExtensionGroupFilter importFilter;
	private static FileExtensionFilter documentFilter;
	
	private static File lastChosenTransactionsImportFile = null;
	private static File lastChosenDocumentFile = null;
	private static File lastChosenExportDirectory = null;


	/**
	 * Initialize
	 */
	static {
		csvFilter = new FileExtensionFilter("CSV file (*.csv)", "csv");
		ofxFilter = new FileExtensionFilter("OFX file (*.ofx, *.qfx)", "ofx", "qfx");
		importFilter = new FileExtensionGroupFilter(ofxFilter);
		
		documentFilter = new FileExtensionFilter(Main.PROGRAM_NAME + " file (*." + Document.FILE_EXTENSION + ")", Document.FILE_EXTENSION);
	}
	
	
	/**
	 * Choose a transactions file to open or save
	 */
	public static File chooseTransactionsImportFile(Component parent, String title, boolean open)
	{
		Frame frame = parent instanceof Frame ? (Frame) parent : null;
		FileDialog fd = new FileDialog(frame, title, open ? FileDialog.LOAD : FileDialog.SAVE);
		
		if (lastChosenTransactionsImportFile != null) {
			fd.setDirectory(lastChosenTransactionsImportFile.getParentFile().getAbsolutePath());
			fd.setFile(lastChosenTransactionsImportFile.getName());
		}
		fd.setFilenameFilter(importFilter);
		
		fd.setVisible(true);
		
		String r = fd.getFile();
		if (r == null) return null;
		lastChosenTransactionsImportFile = new File(new File(fd.getDirectory()), r);
		return lastChosenTransactionsImportFile;
	}
	
	
	/**
	 * Choose one or more transaction files to open
	 */
	public static File[] chooseTransactionsImportFiles(Component parent, String title)
	{
		Frame frame = parent instanceof Frame ? (Frame) parent : null;
		FileDialog fd = new FileDialog(frame, title, FileDialog.LOAD);
		
		if (lastChosenTransactionsImportFile != null) {
			fd.setDirectory(lastChosenTransactionsImportFile.getParentFile().getAbsolutePath());
			fd.setFile(lastChosenTransactionsImportFile.getName());
		}
		fd.setFilenameFilter(importFilter);
		fd.setMultipleMode(true);
		
		fd.setVisible(true);
		
		File[] f = fd.getFiles();
		if (f == null || f.length == 0) return null;
		lastChosenTransactionsImportFile = f[0];
		return f;
	}
	
	
	/**
	 * Choose a document file to open or save
	 */
	public static File chooseDocumentFile(Component parent, String title, boolean open)
	{
		Frame frame = parent instanceof Frame ? (Frame) parent : null;
		FileDialog fd = new FileDialog(frame, title, open ? FileDialog.LOAD : FileDialog.SAVE);
		if (lastChosenDocumentFile != null) {
			fd.setDirectory(lastChosenDocumentFile.getParentFile().getAbsolutePath());
			fd.setFile(lastChosenDocumentFile.getName());
		}
		fd.setFilenameFilter(documentFilter);
		
		fd.setVisible(true);
		
		String r = fd.getFile();
		if (r == null) return null;
		lastChosenDocumentFile = new File(new File(fd.getDirectory()), r);
		return lastChosenDocumentFile;
	}
	
	
	/**
	 * Choose a CSV file to export
	 */
	public static File chooseExportCsvFile(Component parent, String title)
	{
		Frame frame = parent instanceof Frame ? (Frame) parent : null;
		FileDialog fd = new FileDialog(frame, title, FileDialog.SAVE);
		
		if (lastChosenExportDirectory != null) {
			fd.setDirectory(lastChosenExportDirectory.getAbsolutePath());
		}
		fd.setFilenameFilter(csvFilter);
		
		fd.setVisible(true);
		
		String r = fd.getFile();
		if (r == null) return null;
		lastChosenExportDirectory = new File(fd.getDirectory());
		return new File(lastChosenExportDirectory, r);
	}
}
