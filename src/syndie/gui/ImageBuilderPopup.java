package syndie.gui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Text;

/**
 *
 */
public class ImageBuilderPopup {
    private PageEditor _page;
    private Shell _shell;
    private Composite _choices;
    private Button _choiceAttach;
    private Combo _choiceAttachCombo;
    private Button _choiceFile;
    private Text _choiceFileText;
    private Button _choiceFileBrowse;

    private FileDialog _choiceFileDialog;
    private boolean _choiceUpdated;
    
    private Composite _config;
    private Button _configPreview;
    private ScrolledComposite _configPreviewScroll;
    private Image _configPreviewImage;
    private Label _configPreviewLabel;
    private Label _configResizeTo;
    private Combo _configResizeToCombo;
    private Label _configResizeAlt;
    private Text _configResizeWidth;
    private boolean _configResizeWidthModified;
    private Label _configResizeWxH;
    private Text _configResizeHeight;
    private boolean _configResizeHeightModified;
    private Label _configResizePx;
    private Button _configThumbnail;
    private Button _configStrip;
    private Label _configSize;
    private Label _configSizeAmount;
    
    private Button _ok;
    private Button _cancel;
    
    public ImageBuilderPopup(PageEditor page) {
        _page = page;
        initComponents();
    }
    
    private void initComponents() {
        _shell = new Shell(_page.getControl().getShell(), SWT.SHELL_TRIM | SWT.PRIMARY_MODAL);
        _shell.setText("Include image...");
        _shell.setLayout(new GridLayout(1, true));
        
        _choices = new Composite(_shell, SWT.NONE);
        _choices.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
        _choices.setLayout(new GridLayout(3, false));
        
        _choiceAttach = new Button(_choices, SWT.RADIO);
        _choiceAttach.setText("Attachment:");
        _choiceAttach.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));
        _choiceAttach.addSelectionListener(new ChoiceUpdateListener());
        
        _choiceAttachCombo = new Combo(_choices, SWT.SIMPLE | SWT.READ_ONLY);
        _choiceAttachCombo.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false, 2, 1));
        _choiceAttachCombo.addSelectionListener(new ChoiceUpdateListener());
        
        _choiceFile = new Button(_choices, SWT.RADIO);
        _choiceFile.setText("File:");
        _choiceFile.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));
        _choiceFile.addSelectionListener(new ChoiceFileUpdateListener());
        
        _choiceFileText = new Text(_choices, SWT.SINGLE | SWT.BORDER);
        _choiceFileText.setText("");
        _choiceFileText.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
        _choiceFileText.addModifyListener(new ChoiceFileUpdateListener());
        
        _choiceFileBrowse = new Button(_choices, SWT.PUSH);
        _choiceFileBrowse.setText("Browse...");
        _choiceFileBrowse.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false));
        _choiceFileBrowse.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent selectionEvent) { browse(); }
            public void widgetDefaultSelected(SelectionEvent selectionEvent) { browse(); }
        });
        
        _choiceFileDialog = new FileDialog(_shell, SWT.OPEN | SWT.SINGLE);
        _choiceFileDialog.setFilterExtensions(new String[] { "*.png; *.jpeg; *.jpg; *.gif; *.ico", "*.*" });
        _choiceFileDialog.setFilterNames(new String[] { "Images", "All files" });
        
        new Label(_shell, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
        
        _config = new Composite(_shell, SWT.NONE);
        _config.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
        _config.setLayout(new GridLayout(6, false));
        
        _configPreview = new Button(_config, SWT.CHECK);
        _configPreview.setText("Preview");
        _configPreview.setLayoutData(new GridData(GridData.BEGINNING, GridData.FILL, true, false, 7, 1));
        _configPreview.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent selectionEvent) { refreshPreview(); }
            public void widgetSelected(SelectionEvent selectionEvent) { refreshPreview(); }
        });
        
        _configPreviewScroll = new ScrolledComposite(_config, SWT.H_SCROLL | SWT.V_SCROLL);
        _configPreviewScroll.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 1, 5));
        _configPreviewLabel = new Label(_configPreviewScroll, SWT.BORDER);
        _configPreviewScroll.setAlwaysShowScrollBars(false);
        _configPreviewScroll.setContent(_configPreviewLabel);
        _configPreviewScroll.setSize(_configPreviewLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        
        _configResizeTo = new Label(_config, SWT.NONE);
        _configResizeTo.setText("Resize to:");
        _configResizeTo.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));
        
        _configResizeToCombo = new Combo(_config, SWT.SIMPLE);
        _configResizeToCombo.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false, 4, 1));
        _configResizeToCombo.add("25%");
        _configResizeToCombo.add("50%");
        _configResizeToCombo.add("75%");
        _configResizeToCombo.add("100%");
        _configResizeToCombo.add("125%");
        _configResizeToCombo.add("150%");
        _configResizeToCombo.add("175%");
        _configResizeToCombo.add("200%");
        _configResizeToCombo.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent evt) { resizePct(); }
        });
        
        _configResizeAlt = new Label(_config, SWT.NONE);
        _configResizeAlt.setText("or:");
        _configResizeAlt.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));
        
        _configResizeWidth = new Text(_config, SWT.SINGLE | SWT.BORDER);
        _configResizeWidth.setText("");
        GridData gd = new GridData(GridData.FILL, GridData.FILL, false, false);
        gd.widthHint = 50;
        _configResizeWidth.setLayoutData(gd);
        _configResizeWidth.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent keyEvent) { _configResizeWidthModified = true; }
            public void keyReleased(KeyEvent keyEvent) {}
        });
        _configResizeWidth.addTraverseListener(new TraverseListener() {
            public void keyTraversed(TraverseEvent traverseEvent) { if (_configResizeWidthModified) resizePx(false); }
        });
        
        _configResizeWxH = new Label(_config, SWT.NONE);
        _configResizeWxH.setText("x");
        
        _configResizeHeight = new Text(_config, SWT.SINGLE | SWT.BORDER);
        _configResizeHeight.setText("");
        gd = new GridData(GridData.FILL, GridData.FILL, false, false);
        gd.widthHint = 50;
        _configResizeHeight.setLayoutData(gd);
        _configResizeHeight.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent keyEvent) { _configResizeHeightModified = true; }
            public void keyReleased(KeyEvent keyEvent) {}
        });
        _configResizeHeight.addTraverseListener(new TraverseListener() {
            public void keyTraversed(TraverseEvent traverseEvent) { if (_configResizeHeightModified) resizePx(true); }
        });
        
        _configResizePx = new Label(_config, SWT.NONE);
        _configResizePx.setText("px");

        _configSize = new Label(_config, SWT.NONE);
        _configSize.setText("Size:");
        _configSize.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));
        
        _configSizeAmount = new Label(_config, SWT.NONE);
        _configSizeAmount.setText("0KB");
        _configSizeAmount.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, false, false, 4, 1));
        
        _configThumbnail = new Button(_config, SWT.CHECK);
        _configThumbnail.setText("show a thumbnail in the page");
        _configThumbnail.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, false, false, 5, 1));
        
        _configStrip = new Button(_config, SWT.CHECK);
        _configStrip.setText("strip exif data from the image");
        _configStrip.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, false, false, 5, 1));
        
        Composite actions = new Composite(_shell, SWT.NONE);
        actions.setLayout(new FillLayout(SWT.HORIZONTAL));
        actions.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
        
        _ok = new Button(actions, SWT.PUSH);
        _ok.setText("ok");
        _ok.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent selectionEvent) { insertImage(); hide(); }
            public void widgetSelected(SelectionEvent selectionEvent) { insertImage(); hide(); }
        });
        
        _cancel = new Button(actions, SWT.PUSH);
        _cancel.setText("cancel");
        _cancel.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent selectionEvent) { hide(); }
            public void widgetSelected(SelectionEvent selectionEvent) { hide(); }
        });
        
        // intercept the shell closing, since that'd cause the shell to be disposed rather than just hidden
        _shell.addShellListener(new ShellListener() {
            public void shellActivated(ShellEvent shellEvent) {}
            public void shellClosed(ShellEvent evt) { evt.doit = false; hide(); }
            public void shellDeactivated(ShellEvent shellEvent) {}
            public void shellDeiconified(ShellEvent shellEvent) {}
            public void shellIconified(ShellEvent shellEvent) {}
        });
        _shell.pack();
    }
    
    private class ChoiceUpdateListener implements SelectionListener, ModifyListener {
        public void widgetSelected(SelectionEvent selectionEvent) { choiceUpdated(); }
        public void widgetDefaultSelected(SelectionEvent selectionEvent) { choiceUpdated(); }
        public void modifyText(ModifyEvent modifyEvent) { choiceUpdated(); }
        private void choiceUpdated() { _choiceUpdated = true; _choiceFile.setSelection(false); _choiceAttach.setSelection(true); showConfig(true); }
    }
    private class ChoiceFileUpdateListener implements SelectionListener, ModifyListener {
        public void widgetSelected(SelectionEvent selectionEvent) { choiceUpdated(); }
        public void widgetDefaultSelected(SelectionEvent selectionEvent) { choiceUpdated(); }
        public void modifyText(ModifyEvent modifyEvent) { choiceUpdated(); }
        private void choiceUpdated() { _choiceUpdated = true; _choiceFile.setSelection(true); _choiceAttach.setSelection(false); showConfig(true); }
    }
    
    public void showPopup() {
        List attachments = _page.getAttachmentDescriptions(true);
        if ( (attachments != null) && (attachments.size() > 0) ) {
            _choiceAttach.setEnabled(true);
            _choiceAttachCombo.setRedraw(false);
            _choiceAttachCombo.removeAll();
            for (int i = 0; i < attachments.size(); i++)
                _choiceAttachCombo.add((String)attachments.get(i));
            _choiceAttachCombo.setEnabled(true);
            _choiceAttachCombo.setRedraw(true);
        } else {
            _choiceAttach.setEnabled(false);
            _choiceAttach.setSelection(false);
            _choiceAttachCombo.removeAll();
            _choiceAttachCombo.setEnabled(false);
        }
        
        showConfig(false);
        
        _shell.open();
    }
    public void hide() {
        _shell.setVisible(false);
        if ( (_configPreviewImage != null) && (!_configPreviewImage.isDisposed()) )
            _configPreviewImage.dispose();
        _configPreviewImage = null;
    }
    
    private void insertImage() {
        // add the attachment if necessary (perhaps resizing it), then insert the
        // image html into the page
        Image img = _configPreviewLabel.getImage();
        if ( (img != null) && (!img.isDisposed()) ) {
            ImageLoader loader = new ImageLoader();
            ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
            loader.data = new ImageData[] { img.getImageData() };
            loader.save(outBuf, SWT.IMAGE_JPEG);
            byte written[] = outBuf.toByteArray();
            System.out.println("image size: " + written.length + " bytes");
        }
    }
    
    private Image getImage() {
        if (!_choiceUpdated) return _configPreviewImage;
        if (_choiceFile.getSelection()) {
            File fname = new File(_choiceFileText.getText().trim());
            if (fname.exists()) {
                Image rv = new Image(_shell.getDisplay(), fname.getPath());
                _configPreviewImage = rv;
                return rv;
            } else {
                return null;
            }
        } else if (_choiceAttach.getSelection()) {
            int idx = _choiceAttachCombo.getSelectionIndex();
            if (idx >= 0) {
                byte attachment[] = _page.getImageAttachment(idx);
                if (attachment != null) {
                    Image rv = new Image(_shell.getDisplay(), new ByteArrayInputStream(attachment));
                    _configPreviewImage = rv;
                    return rv;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
    
    private int getImageSize() {
        if (_choiceFile.getSelection()) {
            File fname = new File(_choiceFileText.getText().trim());
            if (fname.exists())
                return (int)fname.length();
        } else if (_choiceAttach.getSelection()) {
            int idx = _choiceAttachCombo.getSelectionIndex();
            if (idx >= 0) {
                byte attachment[] = _page.getImageAttachment(idx);
                if (attachment != null)
                    return attachment.length;
            }
        }
        return 0;
    }
    
    private void showConfig(boolean visible) {
        Image img = null;
        if (visible)
            img = getImage();
        System.out.println("Showing config [" + visible + "] / " + _configPreviewImage);
        if (visible && (img != null)) {
            int pxWidth = img.getBounds().width;
            int pxHeight = img.getBounds().height;
            int size = getImageSize();
            System.out.println("image found [" + pxWidth + "x" + pxHeight + ", " + size + " bytes]");
            
            _configPreview.setEnabled(false);
            _configPreview.setSelection(true);
            _configPreviewLabel.setRedraw(false);
            Image old = _configPreviewLabel.getImage();
            if ( (old != null) && (old != _configPreviewImage) )
                old.dispose();
            _configPreviewLabel.setImage(img);
            _configPreviewLabel.setRedraw(true);

            _configResizeTo.setEnabled(true);
            _configResizeToCombo.setEnabled(true);
            _configResizeToCombo.select(3); // 100%
            _configResizeAlt.setEnabled(true);
            _configResizeWidth.setText(pxWidth + "");
            _configResizeWidth.setEnabled(true);
            _configResizeWxH.setEnabled(true);
            _configResizeHeight.setText(pxHeight + "");
            _configResizeHeight.setEnabled(true);
            _configResizePx.setEnabled(true);
            _configSize.setEnabled(true);
            _configSizeAmount.setText(((size+1023)/1024)+"KB");
            _configSizeAmount.setEnabled(true);
            _configThumbnail.setEnabled(true);
            _configThumbnail.setSelection(false);
            _configStrip.setEnabled(true);
            _configStrip.setSelection(false);
        } else {
            System.out.println("image not found or not visible");
            _configPreview.setEnabled(false);
            _configPreview.setSelection(false);
            Image old = _configPreviewLabel.getImage();
            if ( (old != null) && (old != _configPreviewImage) )
                old.dispose();

            _configResizeTo.setEnabled(false);
            _configResizeToCombo.setEnabled(false);
            _configResizeToCombo.select(3); // 100%
            _configResizeAlt.setEnabled(false);
            _configResizeWidth.setText("");
            _configResizeWidth.setEnabled(false);
            _configResizeWxH.setEnabled(false);
            _configResizeHeight.setText("");
            _configResizeHeight.setEnabled(false);
            _configResizePx.setEnabled(false);
            _configSize.setEnabled(false);
            _configSizeAmount.setText("0KB");
            _configSizeAmount.setEnabled(false);
            _configThumbnail.setEnabled(false);
            _configThumbnail.setSelection(false);
            _configStrip.setEnabled(false);
            _configStrip.setSelection(false);
        }
        
        _choiceUpdated = false;
        refreshPreview();
    }
    
    private void refreshPreview() {
        if (_configPreview.getSelection()) {
            Image img = _configPreviewLabel.getImage();
            if ( (img != null) && (!img.isDisposed()) ) {
                redrawPreview(img);
            } else {
                _configPreviewLabel.setVisible(false);
                _configPreviewScroll.setSize(50, 50);
            }
        } else {
            _configPreviewLabel.setVisible(false);
            _configPreviewScroll.setSize(50, 50);
        }
        _shell.layout(true, true);
    }
    
    private void browse() {
        String selected = _choiceFileDialog.open();
        if (selected != null) {
            _choiceAttach.setSelection(false);
            //_choiceFile.setSelection(true);
            _choiceFileText.setText(selected);
            _choiceUpdated = true;
            showConfig(true);
        } else {
            System.out.println("browse picked nothing");
        }
    }
    
    private void redrawPreview(Image img) {
        if (img == null) {
            _configPreviewLabel.setVisible(false);
            _configPreviewLabel.setSize(50, 50);
            _configPreviewScroll.setSize(50, 50);
        } else {
            _configPreviewLabel.setVisible(true);
            int width = Math.min(600, img.getBounds().width);
            int height = Math.min(600, img.getBounds().height);
            Point size = _configPreviewScroll.computeSize(SWT.DEFAULT, SWT.DEFAULT);
            _configPreviewLabel.setSize(img.getBounds().width, img.getBounds().height);//width, height);
            _configPreviewScroll.setSize(width, height);
            System.out.println("redrawing preview w/ size=" + size + " width=" + width + " height=" + height);
            if ( (size.x > width) && (size.y > height) )
                _shell.pack();
        }
    }
    
    private void resizePct() {
        Image img = _configPreviewImage;
        System.out.println("resize pct [" + img + "]");
        if ( (img != null) && (!img.isDisposed()) ) {
            ImageData data = img.getImageData();
            float scale = 1.0f;
            try {
                String str = _configResizeToCombo.getText();
                if (str == null) return;
                int end = str.indexOf('%');
                if (end < 0)
                    end = str.length();
                scale = Float.parseFloat(str.substring(0,end)) * .01f;
            } catch (NumberFormatException nfe) {
                return;
            }
            int width = (int)(((float)data.width) * scale);
            int height = (int)(((float)data.height) * scale);
            System.out.println("scaling to " + width + "x" + height + " [" + _configResizeToCombo.getText() + "]");
            resize(data, width, height);
            _shell.layout(true, true);
        }
    }
    private void resizePx(boolean resizeHeight) {
        if (!_configResizeHeightModified && !_configResizeWidthModified) return;
        Image img = _configPreviewImage;
        if ( (img != null) && (!img.isDisposed()) ) {
            int width = 0;
            int height = 0;
            try { width = Integer.parseInt(_configResizeWidth.getText().trim()); } catch (NumberFormatException nfe) { return; }
            try { height = Integer.parseInt(_configResizeHeight.getText().trim()); } catch (NumberFormatException nfe) { return; }
            if ( (width <= 0) || (height <= 0) ) return;
        
            ImageData data = img.getImageData();
            
            float scale = 1.0f;
            if (resizeHeight && data.height > 0) {
                scale = height/(float)data.height;
            } else if (data.width > 0) {
                scale = width/(float)data.width;
            }
            
            width = (int)(((float)data.width) * scale);
            height = (int)(((float)data.height) * scale);
            
            System.out.println("scaling to " + width + "x" + height);
            resize(data, width, height);
            _configResizeHeightModified = false;
            _configResizeWidthModified = false;
            _shell.layout(true, true);
        }
    }
    private void resize(ImageData data, int width, int height) {
        if ( (width < 1) || (height < 1) ) return; // too small
        ImageData scaled = null;
        Image scaledImage = null;
        Image old = _configPreviewLabel.getImage();
        try {
            scaled = data.scaledTo(width, height);
            scaledImage = new Image(_configPreviewLabel.getDisplay(), scaled);
            _configPreviewLabel.setImage(scaledImage);
            if ( (old != null) && (old != _configPreviewImage) )
                old.dispose();
            redrawPreview(scaledImage);
            _configResizeWidth.setText(width+"");
            _configResizeHeight.setText(height+"");
        } catch (OutOfMemoryError oom) {
            System.out.println("Image size is too large (OOMed): " + width + "x" + height + ": " + oom.getMessage());
            if (scaledImage != null) {
                if (!scaledImage.isDisposed())
                    scaledImage.dispose();
                scaledImage = null;
            }
            scaled = null;
            if ( (old != null) && (!old.isDisposed()) ) {
                _configPreviewLabel.setImage(old);
                redrawPreview(old);
            } else {
                _configPreviewLabel.setImage(null);
                redrawPreview(null);
            }
        }
    }
}
