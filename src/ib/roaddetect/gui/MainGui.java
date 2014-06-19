package ib.roaddetect.gui;

import ib.roaddetect.processing.ProcessRoad;
import ib.roaddetect.utilities.LoadSWT;
import ib.utilities.libloader.LibraryLoad;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

public class MainGui {

	protected Shell shell;
	
	private File file;

	
	private static final String[] FILTER_NAMES = {
		"Video files (*.avi;*.mkv;)",
	    "AVI (*.avi)",
	    "MKV (*.mkv)",
	    "All Files (*.*)"};

	  private static final String[] FILTER_EXTS = {
		"*.avi;*.mkv;",
		"*.avi",
	  	"*.mkv",
	  	"*.*"};
	  private ProcessRoad pr;
	  Thread processAndShow;
	  

		/**
		 * Opens file dialog for choosing a file to open.
		 * @return 
		 */
		private File getFilename()
		{
			FileDialog fd = new FileDialog(shell);
			
			fd.setFilterNames(FILTER_NAMES);
			fd.setFilterExtensions(FILTER_EXTS);
			fd.setFilterPath("C:\\");
			
			String path = fd.open();
			
			if (path != null) {			
				System.out.println("File found.\n");
				File file = new File(path);
				return file;
			} else {
				System.out.println("File not found!\n");
				return null;
			}
			
		}
	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		LoadSWT ll = new LoadSWT();
		LibraryLoad.loadOpenCVLibrary();
		(new Thread(ll)).start();
		try {
			MainGui window = new MainGui();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setSize(838, 454);
		shell.setText("SWT Application");
		shell.setLayout(new FormLayout());
		
		Composite composite_1 = new Composite(shell, SWT.NONE);
		FillLayout fl_composite_1 = new FillLayout(SWT.HORIZONTAL);
		fl_composite_1.spacing = 10;
		fl_composite_1.marginWidth = 10;
		fl_composite_1.marginHeight = 10;
		composite_1.setLayout(fl_composite_1);
		FormData fd_composite_1 = new FormData();
		fd_composite_1.bottom = new FormAttachment(100, -39);
		fd_composite_1.top = new FormAttachment(0, 55);
		fd_composite_1.left = new FormAttachment(0, 10);
		fd_composite_1.right = new FormAttachment(100, -10);
		composite_1.setLayoutData(fd_composite_1);
		
		final Composite comOrigImage = new Composite(composite_1, SWT.BORDER | SWT.NO_BACKGROUND | SWT.EMBEDDED);
		
		final Composite comProcessed = new Composite(composite_1, SWT.BORDER | SWT.NO_BACKGROUND | SWT.EMBEDDED);
		
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayout(new FormLayout());
		FormData fd_composite = new FormData();
		fd_composite.top = new FormAttachment(0, 10);
		fd_composite.left = new FormAttachment(composite_1, 0, SWT.LEFT);
		fd_composite.right = new FormAttachment(100, -10);
		composite.setLayoutData(fd_composite);
		
		Button btnOpen = new Button(composite, SWT.NONE);
		btnOpen.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				file = getFilename();
				
			}
		});
		FormData fd_btnOpen = new FormData();
		btnOpen.setLayoutData(fd_btnOpen);
		btnOpen.setText("Open");
		
		Button btnStart = new Button(composite, SWT.NONE);
		fd_btnOpen.top = new FormAttachment(btnStart, 0, SWT.TOP);
		fd_btnOpen.right = new FormAttachment(btnStart, -6);
		btnStart.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				if (pr == null)
				{
					pr = new ProcessRoad(comOrigImage, comProcessed, file);
					pr.start();					
				} 
				else if (!pr.isRunning()) 
				{
					pr.resumeThread();
				} else if (pr.isRunning()) {}
			}
		});
		FormData fd_btnStart = new FormData();
		fd_btnStart.top = new FormAttachment(0, 10);
		btnStart.setLayoutData(fd_btnStart);
		btnStart.setText("Start");
		
		Button btnPause = new Button(composite, SWT.NONE);
		btnPause.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (processAndShow != null)
				{
					try {
						pr.pauseThread();
						pr.interrupt();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		fd_btnStart.right = new FormAttachment(btnPause, -6);
		FormData fd_btnPause = new FormData();
		fd_btnPause.top = new FormAttachment(0, 10);
		fd_btnPause.right = new FormAttachment(100, -10);
		btnPause.setLayoutData(fd_btnPause);
		btnPause.setText("Pause");

	}
}
