package syndie.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.i2p.util.RandomSource;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;

import syndie.Constants;
import syndie.data.Timer;
import syndie.db.ManageForumExecutor;
import syndie.gui.Wizard.Page;

/**
 *
 */
public class WelcomeScreen extends Wizard {
    private Display _display;
    private Browser _browser;
    private CompleteListener _lsnr;
    
    private Page _welcomePage;
    private Label _welcomeMessage;
    
    private Page _identityPage;
    private Label _description;
    private Label _nameLabel;
    private Text _name;
    private Label _avatarLabel;
    private Button _avatar;
    private Label _authenticationLabel;
    private Button _authenticatePublic;
    private Button _authenticateReplies;
    private Button _authenticateAuth;
    
    private Menu _avatarMenu;
    private MenuItem _avatarItems[];
    private List _avatarImages;
    
    private Image _avatarImage;
    
    private Page _archiveExplanationPage;
    private Label _archiveExplanationMessage;
    
    private Page _archiveDefaultsPage;
    private ArchiveDefaults _archiveDefaults;
    private Label _archiveInstructions;
    
    private Page _finishPage;
    private Label _finishMessage;
    
    public WelcomeScreen(Display display, Browser browser, CompleteListener lsnr, Timer timer) {
        super(display);
        _display = display;
        _browser = browser;
        _lsnr = lsnr;
        _avatarImages = new ArrayList();
        ImageUtil.init(browser.getClient().getTempDir(), timer);
        initComponents();
    }
    
    public void open() {
        super.open();
        Splash.dispose();
    }
    
    void close() {
        _browser.getTranslationRegistry().unregister(this);
        _browser.getThemeRegistry().unregister(this);
        _archiveDefaults.dispose();
        super.close();
        
        _lsnr.complete();
    }
    
    void save() {
        ManageForumExecutor exec = new ManageForumExecutor(_browser.getClient(), _browser.getUI(), new ManageForumExecutor.ManageForumState() {
            public byte[] getAvatarData() {
                Image avatar = _avatarImage;
                if (avatar != null) {
                    try {
                        return ImageUtil.serializeImage(avatar);
                    } catch (SWTException se) {
                        _browser.getUI().errorMessage("Internal error serializing image", se);
                        return null;
                    }
                } else {
                    return null;
                }
            }
        
            public String getName() { return _name.getText().trim(); }
            public String getDescription() { return ""; }
            /** 
             * note the assumption that the newly created forum is version 0.  not sure when this
             * wouldn't be the case, since this is only run on install
             */
            public long getChannelId() { return 0; }
            public long getLastEdition() { return _browser.getClient().getChannelVersion(getChannelId()); }
            public boolean getAllowPublicPosts() { return _authenticatePublic.getSelection(); }
            public boolean getAllowPublicReplies() { return _authenticateReplies.getSelection() || _authenticatePublic.getSelection(); }
            public Set getPublicTags() { return Collections.EMPTY_SET; }
            public Set getPrivateTags() { return Collections.EMPTY_SET; }
            public Set getAuthorizedPosters() { return Collections.EMPTY_SET; }
            public Set getAuthorizedManagers() { return Collections.EMPTY_SET; }
            public String getReferences() { return ""; }
            public Set getPublicArchives() { return Collections.EMPTY_SET; }
            public Set getPrivateArchives() { return Collections.EMPTY_SET; }
            public boolean getEncryptContent() { return false; }
            public boolean getPBE() { return false; }
            public String getPassphrase() { return null; }
            public String getPassphrasePrompt() { return null; }
            public List getCurrentReadKeys() { return Collections.EMPTY_LIST; }
            public boolean getCreateReadKey() { return false; }
            public boolean getCreatePostIdentity() { return false; }
            public boolean getCreateManageIdentity() { return false; }
            public boolean getCreateReplyKey() { return false; }
            public List getCancelledURIs() { return new ArrayList(); }
        });
        exec.execute();
        String errs = exec.getErrors();
        if ( (errs != null) && (errs.length() > 0) )
            _browser.getUI().errorMessage("Error updating the forum: " + errs);
        
        _archiveDefaults.save();
    }
    
    private void initComponents() {
        // Create welcome page
        _welcomePage = new Page();
        _welcomeMessage = new Label(_welcomePage, SWT.WRAP);
        
        // Create identity page
        _identityPage = new Page();
        _identityPage.setLayout(new GridLayout(2, false));
        
        _description = new Label(_identityPage, SWT.WRAP);
        _description.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 2, 1));
        
        _nameLabel = new Label(_identityPage, SWT.NONE);
        _nameLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));
        
        _name = new Text(_identityPage, SWT.SINGLE | SWT.BORDER);
        _name.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
        _name.addTraverseListener(new TraverseListener() {
            public void keyTraversed(TraverseEvent evt) {
                if (evt.detail == SWT.TRAVERSE_RETURN) {
                    next();
                }
            }
        });
        
        _avatarLabel = new Label(_identityPage, SWT.NONE);
        _avatarLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));
        
        _avatar = new Button(_identityPage, SWT.PUSH);
        GridData avatarGD = new GridData(GridData.BEGINNING, GridData.CENTER, true, false);
        avatarGD.widthHint = 52;
        avatarGD.heightHint = 52;
        _avatar.setLayoutData(avatarGD);
        _avatarMenu = new Menu(_avatar);
        _avatar.setMenu(_avatarMenu);
        _avatar.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent selectionEvent) { _avatarMenu.setVisible(true); }
            public void widgetSelected(SelectionEvent selectionEvent) { _avatarMenu.setVisible(true); }
        });
        
        _authenticationLabel = new Label(_identityPage, SWT.WRAP);
        _authenticationLabel.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 2, 1));
        
        _authenticatePublic = new Button(_identityPage, SWT.RADIO);
        _authenticatePublic.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false, 2, 1));
        
        _authenticateReplies = new Button(_identityPage, SWT.RADIO);
        _authenticateReplies.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false, 2, 1));
        
        _authenticateAuth = new Button(_identityPage, SWT.RADIO);
        _authenticateAuth.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false, 2, 1));

        _authenticateReplies.setSelection(true);
        
        _identityPage.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent evt) {
                if (evt.character == '=')
                    _browser.getThemeRegistry().increaseFont();
                if (evt.character == '-')
                    _browser.getThemeRegistry().decreaseFont();
            }
            public void keyReleased(KeyEvent keyEvent) {}
        });
        
        populateAvatarMenu();
        
        // Create archive explanation page
        _archiveExplanationPage = new Page();
        _archiveExplanationMessage = new Label(_archiveExplanationPage, SWT.WRAP);
        
        // Create archive page
        _archiveDefaultsPage = new Page(0, 0);
        _archiveDefaultsPage.setLayout(new GridLayout(1, false));
        
        _archiveDefaults = new ArchiveDefaults(_archiveDefaultsPage, _browser.getClient(), _browser.getUI(), _browser.getThemeRegistry(), _browser.getTranslationRegistry());
        _archiveDefaults.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        _archiveInstructions = new Label(_archiveDefaultsPage, SWT.WRAP);
        _archiveInstructions.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        // Create finish page
        _finishPage = new Page();
        _finishMessage = new Label(_finishPage, SWT.WRAP);
        
        _browser.getTranslationRegistry().register(this);
        _browser.getThemeRegistry().register(this);
        
        update();
    }
    
    private void populateAvatarMenu() {
        int i = 0;
        while (true) {
            Image img = ImageUtil.createImageFromResource("iconAvatar" + i + ".png");
            if (img != null) {
                _avatarImages.add(img);
                i++;
            } else {
                break;
            }
        }
        
        _avatarItems = new MenuItem[_avatarImages.size() + 1];
        for (i = 0; i < _avatarItems.length-1; i++) {
            final Image img = (Image)_avatarImages.get(i);
            _avatarItems[i] = new MenuItem(_avatarMenu, SWT.PUSH);
            _avatarItems[i].setImage(img);
            _avatarItems[i].addSelectionListener(new SelectionListener() {
                public void widgetDefaultSelected(SelectionEvent selectionEvent) { 
                    _avatarImage = img;
                    _avatar.setImage(img);
                }
                public void widgetSelected(SelectionEvent selectionEvent) {
                    _avatarImage = img;
                    _avatar.setImage(img);
                }
            });
        }
        _avatarItems[_avatarItems.length-1] = new MenuItem(_avatarMenu, SWT.PUSH);
        _avatarItems[_avatarItems.length-1].setText(_browser.getTranslationRegistry().getText(T_AVATAR_OTHER, "Other..."));
        _avatarItems[_avatarItems.length-1].addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent selectionEvent) { pickAvatar(); }
            public void widgetSelected(SelectionEvent selectionEvent) { pickAvatar(); }
        });
        
        _avatarImage = (Image)_avatarImages.get(0);
        _avatar.setImage(_avatarImage);
    }
    private static final String T_AVATAR_OTHER = "syndie.gui.welcomescreen.avatar.other";
    
    private void pickAvatar() {
        FileDialog dialog = new FileDialog(getShell(), SWT.SINGLE | SWT.OPEN);
        dialog.setText(_browser.getTranslationRegistry().getText(T_AVATAR_OPEN_NAME, "Select a 48x48 pixel PNG image"));
        dialog.setFilterExtensions(new String[] { "*.png" });
        dialog.setFilterNames(new String[] { _browser.getTranslationRegistry().getText(T_AVATAR_OPEN_TYPE, "PNG image") });
        String filename = dialog.open();
        if (filename != null) {
            Image img = ImageUtil.createImageFromFile(filename);
            if (img != null) {
                Rectangle bounds = img.getBounds();
                int width = bounds.width;
                int height = bounds.height;
                if (width > Constants.MAX_AVATAR_WIDTH)
                    width = Constants.MAX_AVATAR_WIDTH;
                if (height > Constants.MAX_AVATAR_HEIGHT)
                    height = Constants.MAX_AVATAR_HEIGHT;
                if ( (height != bounds.height) || (width != bounds.width) ) {
                    img = ImageUtil.resize(img, width, height, true);
                    System.out.println("resizing avatar from " + bounds + " to " + width +"/"+ height);
                } else {
                    System.out.println("keeping avatar at " + bounds);
                }
                ImageUtil.dispose(_avatarImage);
                _avatarImage = img;
                _avatar.setImage(_avatarImage);
            }
        }
    }
    
    private static final String T_AVATAR_OPEN_NAME = "syndie.gui.welcomescreen.avatar.name";
    private static final String T_AVATAR_OPEN_TYPE = "syndie.gui.welcomescreen.avatar.type";
    
    public void applyTheme(Theme theme) {
        super.applyTheme(theme);
        
        _welcomeMessage.setFont(theme.DEFAULT_FONT);
        
        _description.setFont(theme.DEFAULT_FONT);
        _nameLabel.setFont(theme.DEFAULT_FONT);
        _name.setFont(theme.DEFAULT_FONT);
        _avatarLabel.setFont(theme.DEFAULT_FONT);
        _authenticationLabel.setFont(theme.DEFAULT_FONT);
        _authenticatePublic.setFont(theme.DEFAULT_FONT);
        _authenticateReplies.setFont(theme.DEFAULT_FONT);
        _authenticateAuth.setFont(theme.DEFAULT_FONT);
    }
    
    private static final String T_WELCOME = "syndie.gui.welcomescreen.welcome";
    private static final String T_DESC = "syndie.gui.welcomescreen.desc";
    private static final String T_NAME = "syndie.gui.welcomescreen.name";
    private static final String T_NAME_DEFAULT = "syndie.gui.welcomescreen.name.default";
    private static final String T_AVATAR_LABEL = "syndie.gui.welcomescreen.avatar.label";
    private static final String T_AUTH_LABEL = "syndie.gui.welcomescreen.auth.label";
    private static final String T_AUTH_PUBLIC = "syndie.gui.welcomescreen.auth.public";
    private static final String T_AUTH_REPLY = "syndie.gui.welcomescreen.auth.reply";
    private static final String T_AUTH_AUTH = "syndie.gui.welcomescreen.auth.auth";
    private static final String T_ARCHIVEEXPLANATION = "syndie.gui.welcomescreen.archive.explanation";
    private static final String T_ARCHIVEINSTRUCTIONS = "syndie.gui.welcomescreen.archive.instructions";
    private static final String T_FINISH = "syndie.gui.welcomescreen.finish";
    
    public void translate(TranslationRegistry registry) {
        super.translate(registry);
        
        _welcomeMessage.setText(registry.getText(T_WELCOME, reflow(new String [] {
                "Welcome to Syndie!\n\n",
                "This wizard will help you set up your new Syndie installation. First we'll set up your identity,",
                "and then we'll configure some archives for you to syndicate with.\n\n",
                "It is strongly recommended that you have I2P running before you start Syndie.",
                "If I2P is not running, please start it and wait 5 minutes before proceeding."
        })));
        
        _description.setText(registry.getText(T_DESC, reflow(new String [] {
                "Syndie will create a new identity for you to use with which to post messages in other forums and to run",
                "your own blog/forum"})));
        _nameLabel.setText(registry.getText(T_NAME, "What name would you like to use for your new identity?"));
        _name.setText(registry.getText(T_NAME_DEFAULT, "Syndie user") + ' ' + (1001 + RandomSource.getInstance().nextInt(98888)));
        _avatarLabel.setText(registry.getText(T_AVATAR_LABEL, "What avatar would you like to use?"));
        _authenticationLabel.setText(registry.getText(T_AUTH_LABEL, "In your new identity's blog/forum, would  you like to allow other people to post?"));
        _authenticatePublic.setText(registry.getText(T_AUTH_PUBLIC, "Yes, let anyone reply to existing posts and post new topics"));
        _authenticateReplies.setText(registry.getText(T_AUTH_REPLY, "Yes, let anyone reply to existing posts"));
        _authenticateAuth.setText(registry.getText(T_AUTH_AUTH, "No"));
        
        _archiveExplanationMessage.setText(registry.getText(T_ARCHIVEEXPLANATION, reflow(new String [] {
                "Next it's time to select some archives to syndicate with.\n\n",
                "Syndie messages are propagated from one Syndie instance to another by a process called 'syndication'.",
                "Each client connects to one or more archives and uploads any messages which the client has, but the",
                "archive does not, and downloads any messages which the archive has, but the client does not. In this",
                "way messages are propagated from client to client and archive to archive within a Syndie community.\n\n",
                "To join a Syndie community, you need to syndicate with one or more archives of that community."})));
        
        _archiveInstructions.setText(registry.getText(T_ARCHIVEINSTRUCTIONS, reflow(new String [] {
                "The default archives shipped with your Syndie install are listed above. Double-click a field to edit it.",
                "Please make any necessary changes and uncheck any archives that you don't want."})));
        
        _finishMessage.setText(registry.getText(T_FINISH, reflow(new String [] {
                "Congratulations! Your Syndie installation is configured!\n\n",
                "Click Finish to start exploring Syndie."})));
    }
    
    private String reflow(String [] str) {
        String sep = SWT.getPlatform().equals("win32") ? "\n" : " ";
        String r = null;
        
        for (int c = 0; c < str.length;c ++) {
            if (r == null)
                r = str[c];
            else if (r.endsWith("\n")) // manual break
                r = r.concat(str[c]);
            else
                r = r.concat(sep.concat(str[c]));
        }
        
        return r;
    }
    
    public static interface CompleteListener { public void complete(); }
}
