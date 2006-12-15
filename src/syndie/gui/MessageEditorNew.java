package syndie.gui;

import com.swabunga.spell.engine.Word;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import syndie.Constants;

/**
 *
 */
public class MessageEditorNew implements Themeable, Translatable, ImageBuilderPopup.ImageBuilderSource {
    private BrowserControl _browser;
    private Composite _parent;
    private Composite _root;
    private Composite _toolbar;
    private Label _subjectLabel;
    private Text _subject;
    private Label _tagLabel;
    private Text _tag;
    private Label _replyToLabel;
    private Label _replyTo;
    private StackLayout _stack;
    private Composite _pageRoot;
    private Button _post;
    private Button _postpone;
    private Button _cancel;
    
    // now for the toolbar...
    
    // forum control
    private Group _forumGroup;
    private Button _forumButton;
    private Menu _forumMenu;
    private MenuItem _forumMenuOther;
    // author control
    private Group _authorGroup;
    private Button _authorButton;
    private Menu _authorMenu;
    private MenuItem _authorMenuOther;
    // privacy control
    private Group _privGroup;
    private Button _privButton;
    private Menu _privMenu;
    private MenuItem _privPublic;
    private MenuItem _privAuthorized;
    private MenuItem _privPBE;
    private MenuItem _privReply;
    // page control
    private Group _pageGroup;
    private Button _pageButton;
    private Menu _pageMenu;
    private MenuItem _pageAddHTML;
    private MenuItem _pageAddText;
    private MenuItem _pageAddWebRip;
    private MenuItem _pageRemove;
    // attachment control
    private Group _attachGroup;
    private Button _attachButton;
    private Menu _attachMenu;
    private MenuItem _attachAdd;
    // link control
    private Group _linkGroup;
    private Button _linkButton;
    private Menu _linkMenu;
    private MenuItem _linkWeb;
    private MenuItem _linkPage;
    private MenuItem _linkAttach;
    private MenuItem _linkForum;
    private MenuItem _linkMsg;
    private MenuItem _linkEepsite;
    private MenuItem _linkI2P;
    private MenuItem _linkFreenet;
    private MenuItem _linkArchive;
    private MenuItem _linkOther;
    // style control
    private Group _styleGroup;
    private Button _styleButton;
    private Menu _styleMenu;
    private MenuItem _styleText;
    private MenuItem _styleBGColor;
    private Menu _styleBGColorMenu;
    private MenuItem _styleBGColorDefault;
    private MenuItem _styleBGImage;
    private MenuItem _styleListOrdered;
    private MenuItem _styleListUnordered;
    private MenuItem _styleHeading;
    private Menu _styleHeadingMenu;
    private MenuItem _styleHeading1;
    private MenuItem _styleHeading2;
    private MenuItem _styleHeading3;
    private MenuItem _styleHeading4;
    private MenuItem _styleHeading5;
    private MenuItem _stylePre;
    // resources control
    private Group _resourcesGroup;
    private Button _resourcesButton;
    private Menu _resourcesMenu;
    private MenuItem _resourcesAdd;
    // spellcheck control
    private Group _spellGroup;
    private Button _spellButton;
    // search control
    private Group _searchGroup;
    private Button _searchButton;
    
    // state info
    private List _pageEditors;
    private List _pageTypes;
    private int _currentPage;
    private String _currentPageType;
    
    private List _attachmentConfig;
    private List _attachmentData;
    private List _attachmentSummary;
    private String _selectedPageBGColor;
    private String _selectedPageBGImage;
    /** has it been modified */
    private boolean _modified;
    
    private MessageEditorFind _finder;
    private MessageEditorSpell _spellchecker;
    private MessageEditorStyler _styler;
    private ImageBuilderPopup _imagePopup;
    
    /** Creates a new instance of MessageEditorNew */
    public MessageEditorNew(BrowserControl browser, Composite parent) {
        _browser = browser;
        _parent = parent;
        _currentPage = -1;
        _currentPageType = null;
        _pageEditors = new ArrayList(1);
        _pageTypes = new ArrayList();
        _attachmentConfig = new ArrayList();
        _attachmentData = new ArrayList();
        _attachmentSummary = new ArrayList();
        _modified = true;
        initComponents();
    }
    
    public void dispose() {
        _styler.dispose();
        _spellchecker.dispose();
        _finder.dispose();
        _browser.getTranslationRegistry().unregister(this);
        _browser.getThemeRegistry().unregister(this);
    }
    
    // PageEditors ask for these:
    Composite getPageRoot() { return _pageRoot; }
    void modified() { _modified = true; }
    /** save the state of the message so if there is a crash / exit / etc, it is resumeable */
    void saveState() {}
    
    BrowserControl getBrowser() { return _browser; }
    
    /** current search term used */
    String getSearchTerm() { return _finder.getSearchTerm(); }
    /** current replacement for the search term used */
    String getSearchReplacement() { return _finder.getSearchReplacement(); }
    /** are searches case sensitive? */
    boolean getSearchCaseSensitive() { return _finder.getSearchCaseSensitive(); }
    /** do we want to search backwards? */
    boolean getSearchBackwards() { return _finder.getSearchBackwards(); }
    /** do we want to search around the end/beginning of the page? */
    boolean getSearchWrap() { return _finder.getSearchWrap(); }
    /** fire up the search/replace dialog w/ empty values */
    void search() { 
        // don't open unless there's a page to search...
        if (getPageEditor() != null)
            _finder.open();
    }
    
    // these four are proxies from the finder to the current page editor */
    void findNext() {
        PageEditorNew editor = getPageEditor();
        if (editor != null)
            editor.findNext();
    }
    void findReplace() {
        PageEditorNew editor = getPageEditor();
        if (editor != null)
            editor.findReplace();
    }
    void findReplaceAll() {
        PageEditorNew editor = getPageEditor();
        if (editor != null)
            editor.findReplaceAll();
    }
    void cancelFind() {
        _finder.hide();
        PageEditorNew editor = getPageEditor();
        if (editor != null)
            editor.cancelFind();
    }

    // spellcheck proxies
    void spellIgnore() {
        PageEditorNew editor = getPageEditor();
        if (editor != null)
            editor.spellIgnore();
    } 
    void resetSpellcheck() {
        PageEditorNew editor = getPageEditor();
        if (editor != null)
            editor.resetSpellcheck();
    }
    void spellReplaceWord(boolean allOccurrences) {
        PageEditorNew editor = getPageEditor();
        if (editor != null)
            editor.spellReplaceWord(allOccurrences);
    }
    void spellNext() {
        PageEditorNew editor = getPageEditor();
        if (editor != null)
            editor.spellNext();
    }
    // from editor to spellchecker
    String getSpellWordOrig() { return _spellchecker.getSpellWordOrig(); }
    String getSpellWordSuggestion() { return _spellchecker.getSuggestion(); }
    List getSpellIgnoreAllList() { return _spellchecker.getIgnoreAllList(); }
    /** tuning parameter for how close a word has to be to serve as a suggestion.  5 was arbitrary */
    private static final int SEARCH_CLOSENESS = 5;
    ArrayList getSuggestions(String word, String lcword, String lineText) {
        if (!SpellUtil.getDictionary().isCorrect(lcword)) {
            ArrayList rv = new ArrayList();
            for (Iterator iter = SpellUtil.getDictionary().getSuggestions(word, SEARCH_CLOSENESS).iterator(); iter.hasNext(); ) {
                Word suggestedWord = (Word)iter.next();
                rv.add(suggestedWord.getWord());
            }
            
            _spellchecker.updateSuggestions(rv, lineText, word);
            return rv;
        }
        return null;
        
    }
    void showSpell(boolean wordSet) { _spellchecker.showSpell(wordSet); }
    
    void styleText() {
        if ((getPageType() != null) && (TYPE_HTML.equals(getPageType())))
            _styler.open();
    }
    void cancelStyle() {
        PageEditorNew editor = getPageEditor();
        if (editor != null)
            editor.cancelStyle();
    }
    String getSelectedText() {
        PageEditorNew editor = getPageEditor();
        if (editor != null)
            return editor.getSelectedText();
        else
            return null;
    }
    void insertStyle(String buf, boolean insert, int begin, int end) {
        PageEditorNew editor = getPageEditor();
        if (editor != null)
            editor.insertStyle(buf, insert, begin, end);
    }
    
    
    // gui stuff..
    private void initComponents() {
        _root = new Composite(_parent, SWT.NONE);
        _root.setLayout(new GridLayout(1, true));
        
        initToolbar();
        initHeader();
        initPage();
        initFooter();
        
        _finder = new MessageEditorFind(this);
        _spellchecker = new MessageEditorSpell(this);
        _styler = new MessageEditorStyler(this);
        
        _browser.getTranslationRegistry().register(this);
        _browser.getThemeRegistry().register(this);
    }
    
    private void initFooter() {
        Composite c = new Composite(_root, SWT.NONE);
        c.setLayout(new FillLayout(SWT.HORIZONTAL));
        c.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
        
        _post = new Button(c, SWT.PUSH);
        _postpone = new Button(c, SWT.PUSH);
        _cancel = new Button(c, SWT.PUSH);
        
        _post.setText("Post the message");
        _postpone.setText("Save the message for later");
        _cancel.setText("Cancel the message");
    }
    
    private static final String TYPE_HTML = "text/html";
    
    private void addPage(String type) {
        PageEditorNew ed = new PageEditorNew(_browser, this, TYPE_HTML.equals(type));
        _pageEditors.add(ed);
        _pageTypes.add(type);
        
        viewPage(_pageEditors.size()-1);
    }
    
    /** current page */
    private PageEditorNew getPageEditor() { return getPageEditor(_currentPage); }
    /** grab the given (0-indexed) page */
    private PageEditorNew getPageEditor(int pageNum) {
        return (PageEditorNew)_pageEditors.get(pageNum);
    }
    /** current page */
    private String getPageType() { return getPageType(_currentPage); }
    /** grab the content type of the given (0-indexed) page */
    private String getPageType(int pageNum) {
        if (_pageTypes.size() > pageNum)
            return (String)_pageTypes.get(pageNum);
        else
            return "";
    }
    
    /** view the given (0-indexed) page */
    private void viewPage(int pageNum) {
        PageEditorNew ed = (PageEditorNew)_pageEditors.get(pageNum);
        String type = (String)_pageTypes.get(pageNum);
        
        _currentPage = pageNum;
        _currentPageType = type;
        
        _stack.topControl = ed.getControl();
        _pageRoot.layout();
        updateToolbar();
    }
    
    /**
     * go through the toolbar to adjust the available options for the current page
     */
    private void updateToolbar() {
        
    }
    
    void setBodyTags() { setBodyTags(null); }
    public void setBodyTags(String bgImageURL) {
        _selectedPageBGImage = bgImageURL;
        PageEditorNew editor = getPageEditor();
        if (editor != null)
            editor.setBodyTags(_selectedPageBGImage, _selectedPageBGColor);
    }
    void setPageBGColor(String name) {
        _selectedPageBGColor = name;
        setBodyTags(_selectedPageBGImage);
    }
    private void showImagePopup(boolean forBodyBackground) { 
        if (_imagePopup == null)
            _imagePopup = new ImageBuilderPopup(_root.getShell(), this);
        _imagePopup.showPopup(forBodyBackground); 
    }
    
    private void initPage() {
        _pageRoot = new Composite(_root, SWT.BORDER);
        _stack = new StackLayout();
        _pageRoot.setLayout(_stack);
        _pageRoot.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
        
        addPage(TYPE_HTML);
        //PageEditor ed = new PageEditor(_browser.getClient(), _pageRoot, null, "text/html", _browser);
        //_stack.topControl = ed.getControl();
    }
    
    private void initHeader() {
        Composite header = new Composite(_root, SWT.NONE);
        header.setLayout(new GridLayout(2, false));
        header.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
        
        _subjectLabel = new Label(header, SWT.NONE);
        _subjectLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));
        
        _subject = new Text(header, SWT.BORDER | SWT.SINGLE);
        _subject.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
        
        _tagLabel = new Label(header, SWT.NONE);
        _tagLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));
        
        _tag = new Text(header, SWT.BORDER | SWT.SINGLE);
        _tag.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
        
        _replyToLabel = new Label(header, SWT.NONE);
        GridData gd = new GridData(GridData.END, GridData.CENTER, false, false);
        gd.exclude = true;
        _replyToLabel.setLayoutData(gd);
        
        _replyTo = new Label(header, SWT.NONE);
        gd = new GridData(GridData.FILL, GridData.FILL, true, false);
        gd.exclude = true;
        _replyTo.setLayoutData(gd);
        
        _subjectLabel.setText("Subject:");
        _tagLabel.setText("Tags:");
        _replyToLabel.setText("In reply to:");
    }
    
    private void initToolbar() {
        _toolbar = new Composite(_root, SWT.NONE);
        RowLayout rl = new RowLayout(SWT.HORIZONTAL);
        rl.wrap = false;
        rl.fill = true;
        _toolbar.setLayout(rl);
        _toolbar.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
        
        initForumControl();
        initAuthorControl();
        initPrivacyControl();
        initPageControl();
        initAttachControl();
        initLinkControl();
        initStyleControl();
        initResourcesControl();
        initSpellControl();
        initSearchControl();
    }
    
    private void initForumControl() {
        _forumGroup = new Group(_toolbar, SWT.SHADOW_ETCHED_IN);
        //ctl.setLayoutData(new RowData(48, 48));
        _forumGroup.setLayout(new FillLayout());
        
        _forumButton = new Button(_forumGroup, SWT.PUSH);
        _forumButton.setImage(ImageUtil.resize(ImageUtil.ICON_REF_SYNDIE, 48, 48, false));
        
        _forumMenu = new Menu(_forumButton);
        _forumGroup.setMenu(_forumMenu);
        _forumButton.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent selectionEvent) { _forumMenu.setVisible(true); }
            public void widgetSelected(SelectionEvent selectionEvent) { _forumMenu.setVisible(true); }
        });
        
        _forumMenuOther = new MenuItem(_forumMenu, SWT.PUSH);
        _forumMenuOther.setText("other...");
        
        _forumGroup.setText("Forum:");
        _forumGroup.setToolTipText("Select the forum to post in");
    }
    
    private void initAuthorControl() {
        _authorGroup = new Group(_toolbar, SWT.SHADOW_ETCHED_IN);
        _authorGroup.setLayout(new FillLayout());
        
        _authorButton = new Button(_authorGroup, SWT.PUSH);
        _authorButton.setImage(ImageUtil.resize(ImageUtil.ICON_QUESTION, 48, 48, false));
        
        _authorMenu = new Menu(_authorButton);
        _authorGroup.setMenu(_authorMenu);
        _authorButton.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent selectionEvent) { _authorMenu.setVisible(true); }
            public void widgetSelected(SelectionEvent selectionEvent) { _authorMenu.setVisible(true); }
        });
        
        _authorMenuOther = new MenuItem(_authorMenu, SWT.PUSH);
        _authorMenuOther.setText("other...");
        
        _authorGroup.setText("Author:");
        _authorGroup.setToolTipText("Who do you want to sign the post as?");
    }
    
    private void initPrivacyControl() {
        _privGroup = new Group(_toolbar, SWT.SHADOW_ETCHED_IN);
        _privGroup.setLayout(new FillLayout());
        
        _privButton = new Button(_privGroup, SWT.PUSH);
        _privButton.setImage(ImageUtil.resize(ImageUtil.ICON_MSG_FLAG_PUBLIC, 48, 48, false));
        
        _privMenu = new Menu(_privButton);
        _privGroup.setMenu(_privMenu);
        _privButton.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent selectionEvent) { _privMenu.setVisible(true); }
            public void widgetSelected(SelectionEvent selectionEvent) { _privMenu.setVisible(true); }
        });
        
        _privPublic = new MenuItem(_privMenu, SWT.RADIO);
        _privAuthorized = new MenuItem(_privMenu, SWT.RADIO);
        _privPBE = new MenuItem(_privMenu, SWT.RADIO);
        _privReply = new MenuItem(_privMenu, SWT.RADIO);
        
        _privPublic.setText("Anyone can read the post");
        _privAuthorized.setText("Authorized readers of the forum can read the post");
        _privPBE.setText("Passphrase required to read the post...");
        _privReply.setText("Only forum administrators can read the post");
        _privAuthorized.setSelection(true);
        
        _privGroup.setText("Privacy:");
        _privGroup.setToolTipText("Who is allowed to read the post?");
    }
    
    private void initPageControl() {
        _pageGroup = new Group(_toolbar, SWT.SHADOW_ETCHED_IN);
        _pageGroup.setLayout(new FillLayout());
        
        _pageButton = new Button(_pageGroup, SWT.PUSH);
        _pageButton.setImage(ImageUtil.resize(ImageUtil.ICON_MSG_FLAG_PUBLIC, 48, 48, false));
        
        _pageMenu = new Menu(_pageButton);
        _pageGroup.setMenu(_pageMenu);
        _pageButton.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent selectionEvent) { _pageMenu.setVisible(true); }
            public void widgetSelected(SelectionEvent selectionEvent) { _pageMenu.setVisible(true); }
        });
        
        _pageAddHTML = new MenuItem(_pageMenu, SWT.PUSH);
        _pageAddText = new MenuItem(_pageMenu, SWT.PUSH);
        _pageAddWebRip = new MenuItem(_pageMenu, SWT.PUSH);
        _pageRemove = new MenuItem(_pageMenu, SWT.PUSH);
        _pageRemove.setEnabled(false);
        new MenuItem(_pageMenu, SWT.SEPARATOR);
        
        for (int i = 0; i < 10; i++) {
            MenuItem item = new MenuItem(_pageMenu, SWT.PUSH);
            item.setText((i+1)+"");
        }
        
        _pageGroup.setText("Page:");
        _pageGroup.setToolTipText("Manage pages in this post");
        _pageAddHTML.setText("Add a new HTML page");
        _pageAddText.setText("Add a new text page");
        _pageAddWebRip.setText("Add a new web rip");
        _pageRemove.setText("Remove the current page");
    }
    
    private void initAttachControl() {
        _attachGroup = new Group(_toolbar, SWT.SHADOW_ETCHED_IN);
        _attachGroup.setLayout(new FillLayout());
        
        _attachButton = new Button(_attachGroup, SWT.PUSH);
        _attachButton.setImage(ImageUtil.resize(ImageUtil.ICON_MSG_FLAG_PUBLIC, 48, 48, false));
        
        _attachMenu = new Menu(_attachButton);
        _attachGroup.setMenu(_attachMenu);
        _attachButton.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent selectionEvent) { _attachMenu.setVisible(true); }
            public void widgetSelected(SelectionEvent selectionEvent) { _attachMenu.setVisible(true); }
        });
        
        _attachAdd = new MenuItem(_attachMenu, SWT.PUSH);
        
        MenuItem item0 = new MenuItem(_attachMenu, SWT.CASCADE);
        item0.setText("foo.txt");
        Menu sub = new Menu(item0);
        item0.setMenu(sub);
        MenuItem view = new MenuItem(sub, SWT.PUSH);
        view.setText("view");
        MenuItem delete = new MenuItem(sub, SWT.PUSH);
        delete.setText("delete");
        
        _attachGroup.setText("Attach:");
        _attachGroup.setToolTipText("Manage attachments to this post");
        _attachAdd.setText("Add a new attachment");
    }
    
    private void initLinkControl() {
        _linkGroup = new Group(_toolbar, SWT.SHADOW_ETCHED_IN);
        _linkGroup.setLayout(new FillLayout());
        
        _linkButton = new Button(_linkGroup, SWT.PUSH);
        _linkButton.setImage(ImageUtil.resize(ImageUtil.ICON_MSG_FLAG_PUBLIC, 48, 48, false));
        
        _linkMenu = new Menu(_linkButton);
        _linkGroup.setMenu(_linkMenu);
        _linkButton.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent selectionEvent) { _linkMenu.setVisible(true); }
            public void widgetSelected(SelectionEvent selectionEvent) { _linkMenu.setVisible(true); }
        });
        
        _linkWeb = new MenuItem(_linkMenu, SWT.PUSH);
        _linkPage = new MenuItem(_linkMenu, SWT.PUSH);
        _linkAttach = new MenuItem(_linkMenu, SWT.PUSH);
        _linkForum = new MenuItem(_linkMenu, SWT.PUSH);
        _linkMsg = new MenuItem(_linkMenu, SWT.PUSH);
        _linkEepsite = new MenuItem(_linkMenu, SWT.PUSH);
        _linkI2P = new MenuItem(_linkMenu, SWT.PUSH);
        _linkFreenet = new MenuItem(_linkMenu, SWT.PUSH);
        _linkArchive = new MenuItem(_linkMenu, SWT.PUSH);
        _linkOther = new MenuItem(_linkMenu, SWT.PUSH);
        
        _linkPage.setEnabled(false);
        _linkAttach.setEnabled(false);
        
        _linkWeb.setImage(ImageUtil.ICON_REF_URL);
        _linkPage.setImage(ImageUtil.ICON_REF_SYNDIE);
        _linkAttach.setImage(ImageUtil.ICON_REF_SYNDIE);
        _linkForum.setImage(ImageUtil.ICON_REF_FORUM);
        _linkMsg.setImage(ImageUtil.ICON_REF_SYNDIE);
        _linkEepsite.setImage(ImageUtil.ICON_REF_SYNDIE);
        _linkI2P.setImage(ImageUtil.ICON_REF_SYNDIE);
        _linkFreenet.setImage(ImageUtil.ICON_REF_FREENET);
        _linkArchive.setImage(ImageUtil.ICON_REF_ARCHIVE);
        _linkOther.setImage(ImageUtil.ICON_REF_SYNDIE);
        
        _linkWeb.setText("Link to a website");
        _linkPage.setText("Link to a page in this message");
        _linkAttach.setText("Link to an attachment in this message");
        _linkForum.setText("Link to a forum");
        _linkMsg.setText("Link to a particular Syndie message");
        _linkEepsite.setText("Link to an I2P eepsite");
        _linkI2P.setText("Link to an I2P destination");
        _linkFreenet.setText("Link to a Freenet freesite");
        _linkArchive.setText("Link to a Syndie archive");
        _linkOther.setText("Link to another Syndie URI");
        
        _linkGroup.setText("Link:");
        _linkGroup.setToolTipText("Add a new link");
    }
    
    private void initStyleControl() {
        _styleGroup = new Group(_toolbar, SWT.SHADOW_ETCHED_IN);
        _styleGroup.setLayout(new FillLayout());
        
        _styleButton = new Button(_styleGroup, SWT.PUSH);
        _styleButton.setImage(ImageUtil.resize(ImageUtil.ICON_MSG_FLAG_PUBLIC, 48, 48, false));
        
        _styleMenu = new Menu(_styleButton);
        _styleGroup.setMenu(_styleMenu);
        _styleButton.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent selectionEvent) { _styleMenu.setVisible(true); }
            public void widgetSelected(SelectionEvent selectionEvent) { _styleMenu.setVisible(true); }
        });
        
        _styleText = new MenuItem(_styleMenu, SWT.PUSH);
        _styleText.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent selectionEvent) { styleText(); }
            public void widgetSelected(SelectionEvent selectionEvent) { styleText(); }
        });
        new MenuItem(_styleMenu, SWT.SEPARATOR);
        
        _styleBGColor = new MenuItem(_styleMenu, SWT.CASCADE);
        _styleBGColorMenu = new Menu(_styleBGColor);
        _styleBGColor.setMenu(_styleBGColorMenu);
        _styleBGColorDefault = new MenuItem(_styleBGColorMenu, SWT.PUSH);
        _styleBGColorDefault .addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent selectionEvent) { setPageBGColor(null); }
            public void widgetSelected(SelectionEvent selectionEvent) { setPageBGColor(null); }
        });
        ColorUtil.init();
        List names = ColorUtil.getSystemColorNames();
        for (int i = 0; i < names.size(); i++) {
            final String name = (String)names.get(i);
            Color color = ColorUtil.getColor(name);
            MenuItem item = new MenuItem(_styleBGColorMenu, SWT.PUSH);
            item.setImage(ColorUtil.getSystemColorSwatch(color));
            item.setText(name);
            item.addSelectionListener(new SelectionListener() {
                public void widgetDefaultSelected(SelectionEvent selectionEvent) { setPageBGColor(name); }
                public void widgetSelected(SelectionEvent selectionEvent) { setPageBGColor(name); }
            });
        }
        _styleBGImage = new MenuItem(_styleMenu, SWT.PUSH);
        _styleBGImage.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent selectionEvent) { showImagePopup(true); }
            public void widgetSelected(SelectionEvent selectionEvent) { showImagePopup(true); }
        });
        new MenuItem(_styleMenu, SWT.SEPARATOR);
        
        _styleListOrdered = new MenuItem(_styleMenu, SWT.PUSH);
        _styleListOrdered.addSelectionListener(new InsertListener("<ol>\n\t<li>first list item</li>\n</ol>\n", true));
        _styleListUnordered = new MenuItem(_styleMenu, SWT.PUSH);
        _styleListUnordered.addSelectionListener(new InsertListener("<ul>\n\t<li>first list item</li>\n</ul>\n", true));
        
        new MenuItem(_styleMenu, SWT.SEPARATOR);
        
        _styleHeading = new MenuItem(_styleMenu, SWT.CASCADE);
        _styleHeadingMenu = new Menu(_styleHeading);
        _styleHeading.setMenu(_styleHeadingMenu);
        _styleHeading1 = new MenuItem(_styleHeadingMenu, SWT.PUSH);
        _styleHeading1.addSelectionListener(new InsertListener("<h1>TEXT</h1>", true));
        _styleHeading2 = new MenuItem(_styleHeadingMenu, SWT.PUSH);
        _styleHeading2.addSelectionListener(new InsertListener("<h2>TEXT</h2>", true));
        _styleHeading3 = new MenuItem(_styleHeadingMenu, SWT.PUSH);
        _styleHeading3.addSelectionListener(new InsertListener("<h3>TEXT</h3>", true));
        _styleHeading4 = new MenuItem(_styleHeadingMenu, SWT.PUSH);
        _styleHeading4.addSelectionListener(new InsertListener("<h4>TEXT</h4>", true));
        _styleHeading5 = new MenuItem(_styleHeadingMenu, SWT.PUSH);
        _styleHeading5.addSelectionListener(new InsertListener("<h5>TEXT</h5>", true));
        _stylePre = new MenuItem(_styleMenu, SWT.PUSH);
        _stylePre.addSelectionListener(new InsertListener("<pre>first line\n\tindented line</pre>", true));
        
        _styleGroup.setText("Style:");
        _styleButton.setToolTipText("Insert style elements");
        
        _styleText.setText("Styled text...");
        _styleBGColor.setText("Page background color");
        _styleBGColorDefault.setText("standard");
        _styleBGImage.setText("Page background image");
        _styleListOrdered.setText("List (ordered)");
        _styleListUnordered.setText("List (unordered)");
        _styleHeading.setText("Heading");
        _styleHeading1.setText("Heading 1 (largest)");
        _styleHeading2.setText("Heading 2");
        _styleHeading3.setText("Heading 3");
        _styleHeading4.setText("Heading 4");
        _styleHeading5.setText("Heading 5 (smallest)");
        _stylePre.setText("Preformatted text");
    }
    
    private void initResourcesControl() {
        _resourcesGroup = new Group(_toolbar, SWT.SHADOW_ETCHED_IN);
        _resourcesGroup.setLayout(new FillLayout());
        
        _resourcesButton = new Button(_resourcesGroup, SWT.PUSH);
        _resourcesButton.setImage(ImageUtil.resize(ImageUtil.ICON_MSG_FLAG_PUBLIC, 48, 48, false));
        
        _resourcesMenu = new Menu(_resourcesButton);
        _resourcesGroup.setMenu(_resourcesMenu);
        _resourcesButton.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent selectionEvent) { _resourcesMenu.setVisible(true); }
            public void widgetSelected(SelectionEvent selectionEvent) { _resourcesMenu.setVisible(true); }
        });
        
        _resourcesAdd = new MenuItem(_resourcesMenu, SWT.PUSH);
        
        MenuItem item0 = new MenuItem(_resourcesMenu, SWT.CASCADE);
        item0.setImage(ImageUtil.ICON_REF_URL);
        item0.setText("www.i2p.net");
        Menu sub = new Menu(item0);
        item0.setMenu(sub);
        MenuItem view = new MenuItem(sub, SWT.PUSH);
        view.setText("view");
        MenuItem edit = new MenuItem(sub, SWT.PUSH);
        edit.setText("edit");
        MenuItem delete = new MenuItem(sub, SWT.PUSH);
        delete.setText("delete");
        
        _resourcesGroup.setText("Refs:");
        _resourcesGroup.setToolTipText("Manage references to other resources");
        _resourcesAdd.setText("Add a new resource");
    }
    
    private void initSpellControl() {
        _spellGroup = new Group(_toolbar, SWT.SHADOW_ETCHED_IN);
        _spellGroup.setLayout(new FillLayout());
        
        _spellButton = new Button(_spellGroup, SWT.PUSH);
        _spellButton.setImage(ImageUtil.resize(ImageUtil.ICON_MSG_FLAG_PUBLIC, 48, 48, false));
        _spellButton.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent selectionEvent) { spellNext(); }
            public void widgetSelected(SelectionEvent selectionEvent) { spellNext(); }
        });
        
        _spellGroup.setText("Spell:");
        _spellButton.setToolTipText("Check the spelling in the current page");
    }
    
    private void initSearchControl() {
        _searchGroup = new Group(_toolbar, SWT.SHADOW_ETCHED_IN);
        _searchGroup.setLayout(new FillLayout());
        
        _searchButton = new Button(_searchGroup, SWT.PUSH);
        _searchButton.setImage(ImageUtil.resize(ImageUtil.ICON_MSG_FLAG_PUBLIC, 48, 48, false));
        _searchButton.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent selectionEvent) { search(); }
            public void widgetSelected(SelectionEvent selectionEvent) { search(); }
        });
        
        _searchGroup.setText("Find:");
        _searchButton.setToolTipText("Find or replace text in the current page");
    }
    
    public void applyTheme(Theme theme) {
        _subjectLabel.setFont(theme.DEFAULT_FONT);
        _subject.setFont(theme.DEFAULT_FONT);
        _tagLabel.setFont(theme.DEFAULT_FONT);
        _tag.setFont(theme.DEFAULT_FONT);
        _replyToLabel.setFont(theme.DEFAULT_FONT);
        _replyTo.setFont(theme.DEFAULT_FONT);
        _post.setFont(theme.BUTTON_FONT);
        _postpone.setFont(theme.BUTTON_FONT);
        _cancel.setFont(theme.BUTTON_FONT);
    
        _forumGroup.setFont(theme.DEFAULT_FONT);
        _authorGroup.setFont(theme.DEFAULT_FONT);
        _privGroup.setFont(theme.DEFAULT_FONT);
        _pageGroup.setFont(theme.DEFAULT_FONT);
        _attachGroup.setFont(theme.DEFAULT_FONT);
        _linkGroup.setFont(theme.DEFAULT_FONT);
        _styleGroup.setFont(theme.DEFAULT_FONT);
        _resourcesGroup.setFont(theme.DEFAULT_FONT);
        _spellGroup.setFont(theme.DEFAULT_FONT);
        _searchGroup.setFont(theme.DEFAULT_FONT);
        
        _root.layout(true);
    }
    
    private static final String T_ATTACHMENTS_NONE = "syndie.gui.messageeditor.attachments.none";
    
    public void translate(TranslationRegistry registry) {}

    // image popup stuff
    public int addAttachment(String contentType, String name, byte[] data) {
        saveState();
        modified();
        int rv = -1;
        Properties cfg = new Properties();
        cfg.setProperty(Constants.MSG_ATTACH_CONTENT_TYPE, contentType);
        cfg.setProperty(Constants.MSG_ATTACH_NAME, name);
        _attachmentConfig.add(cfg);
        _attachmentData.add(data);
        rv = _attachmentData.size();
        rebuildAttachmentSummaries();
        return rv;
    }

    public List getAttachmentDescriptions() { return getAttachmentDescriptions(false); }
    public List getAttachmentDescriptions(boolean imagesOnly) {
        ArrayList rv = new ArrayList();
        for (int i = 0; i < _attachmentConfig.size(); i++) {
            if (imagesOnly) {
                Properties cfg = (Properties)_attachmentConfig.get(i);
                String type = cfg.getProperty(Constants.MSG_ATTACH_CONTENT_TYPE);
                if ( (type == null) || (!type.startsWith("image")) )
                    continue;
            }
            String item = (String)_attachmentSummary.get(i);
            rv.add(item);
        }
        return rv;
    }
    public List getAttachmentNames() {
        ArrayList rv = new ArrayList();
        for (int i = 0; i < _attachmentConfig.size(); i++) {
            Properties cfg = (Properties)_attachmentConfig.get(i);
            rv.add(cfg.getProperty(Constants.MSG_ATTACH_NAME));
        }
        return rv;
    }
    public byte[] getAttachmentData(int attachment) {
        if ( (attachment <= 0) || (attachment > _attachmentData.size()) ) return null;
        return (byte[])_attachmentData.get(attachment-1);
    }
    public byte[] getImageAttachment(int idx) {
        int cur = 0;
        for (int i = 0; i < _attachmentConfig.size(); i++) {
            Properties cfg = (Properties)_attachmentConfig.get(i);
            String type = cfg.getProperty(Constants.MSG_ATTACH_CONTENT_TYPE);
            if ( (type == null) || (!type.startsWith("image")) )
                continue;
            if (cur + 1 == idx)
                return (byte[])_attachmentData.get(i);
            cur++;
        }
        return null;
    }
    public int getImageAttachmentNum(int imageNum) { 
        int cur = 0;
        for (int i = 0; i < _attachmentConfig.size(); i++) {
            Properties cfg = (Properties)_attachmentConfig.get(i);
            String type = cfg.getProperty(Constants.MSG_ATTACH_CONTENT_TYPE);
            if ( (type == null) || (!type.startsWith("image")) )
                continue;
            if (cur == imageNum)
                return cur+1;
            cur++;
        }
        return -1;
    }
    public void updateImageAttachment(int imageNum, String contentType, byte data[]) { 
        modified();
        int cur = 0;
        for (int i = 0; i < _attachmentConfig.size(); i++) {
            Properties cfg = (Properties)_attachmentConfig.get(i);
            String type = cfg.getProperty(Constants.MSG_ATTACH_CONTENT_TYPE);
            if ( (type == null) || (!type.startsWith("image")) )
                continue;
            if (cur == imageNum) {
                cfg.setProperty(Constants.MSG_ATTACH_CONTENT_TYPE, contentType);
                _attachmentConfig.set(cur, data);
                rebuildAttachmentSummaries();
                return;
            }
            cur++;
        }
    }
    private void rebuildAttachmentSummaries() {
        _attachmentSummary.clear();
        if (_attachmentData.size() > 0) {
            for (int i = 0; i < _attachmentData.size(); i++) {
                byte data[] = (byte[])_attachmentData.get(i);
                Properties cfg = (Properties)_attachmentConfig.get(i);
                StringBuffer buf = new StringBuffer();
                buf.append((i+1) + ": ");
                String name = cfg.getProperty(Constants.MSG_ATTACH_NAME);
                if (name != null)
                    buf.append(name).append(" ");
                String type = cfg.getProperty(Constants.MSG_ATTACH_CONTENT_TYPE);
                if (type != null)
                    buf.append('(').append(type).append(") ");
                buf.append("[" + data.length + " bytes]");
                _attachmentSummary.add(buf.toString());
            }
        } else {
            _attachmentSummary.add(_browser.getTranslationRegistry().getText(T_ATTACHMENTS_NONE, "none"));
        }
    }

    public void insertAtCaret(String html) {
        PageEditorNew editor = getPageEditor();
        if (editor != null)
            editor.insertAtCaret(html);
    }
    
    /** simple hook to inert a buffer at the caret */
    private class InsertListener implements SelectionListener {
        private boolean _onNewline;
        private String _toInsert;
        public InsertListener(String toInsert, boolean onNewline) {
            _onNewline = onNewline;
            _toInsert = toInsert;
        }
        public void widgetSelected(SelectionEvent selectionEvent) { insert(); }
        public void widgetDefaultSelected(SelectionEvent selectionEvent) { insert(); }
        private void insert() {
            PageEditorNew editor = getPageEditor();
            if (editor != null)
                editor.insert(_toInsert, _onNewline);
        }
    }
}

class MessageEditorFind implements Translatable, Themeable {
    private MessageEditorNew _editor;
    private Shell _findShell;
    private Label _findTextLabel;
    private Text _findText;
    private Label _findReplaceLabel;
    private Text _findReplace;
    private Button _findMatchCase;
    private Button _findWrapAround;
    private Button _findBackwards;
    private Button _findNext;
    private Button _close;
    private Button _replace;
    private Button _replaceAll;
    
    public MessageEditorFind(MessageEditorNew editor) {
        _editor = editor;
        initComponents();
    }
    
    public void hide() { _findShell.setVisible(false); }
    public void open() {
        _findText.setText("");
        _findReplace.setText("");
        _findBackwards.setSelection(false);
        _findMatchCase.setSelection(false);
        _findWrapAround.setSelection(false);
        _findShell.open();
        _findText.forceFocus();
    }
    public void dispose() {
        _editor.getBrowser().getTranslationRegistry().unregister(this);
        _editor.getBrowser().getThemeRegistry().unregister(this);
        _findShell.dispose();
        
    }
    
    /** current search term used */
    public String getSearchTerm() { return _findText.getText(); }
    /** current replacement for the search term used */
    public String getSearchReplacement() { return _findReplace.getText(); }
    /** are searches case sensitive? */
    public boolean getSearchCaseSensitive() { return _findMatchCase.getSelection(); }
    /** do we want to search backwards? */
    public boolean getSearchBackwards() { return _findBackwards.getSelection(); }
    /** do we want to search around the end/beginning of the page? */
    public boolean getSearchWrap() { return _findWrapAround.getSelection(); }
    
    private void initComponents() {
        _findShell = new Shell(_editor.getPageRoot().getShell(), SWT.DIALOG_TRIM);
        GridLayout gl = new GridLayout(2, false);
        _findShell.setLayout(gl);
    
        _findTextLabel = new Label(_findShell, SWT.NONE);
        _findTextLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));
        _findText = new Text(_findShell, SWT.BORDER | SWT.SINGLE);
        _findText.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
        
        _findReplaceLabel = new Label(_findShell, SWT.NONE);
        _findReplaceLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));
        _findReplace = new Text(_findShell, SWT.BORDER | SWT.SINGLE);
        _findReplace.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
        
        _findMatchCase = new Button(_findShell, SWT.CHECK | SWT.LEFT);
        _findMatchCase.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false, 2, 1));
        
        _findWrapAround = new Button(_findShell, SWT.CHECK | SWT.LEFT);
        _findWrapAround.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false, 2, 1));
        
        _findBackwards = new Button(_findShell, SWT.CHECK | SWT.LEFT);
        _findBackwards.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false, 2, 1));
        
        Composite actionRow = new Composite(_findShell, SWT.NONE);
        actionRow.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false, 2, 1));
        actionRow.setLayout(new FillLayout(SWT.HORIZONTAL));
        
        _findNext = new Button(actionRow, SWT.PUSH);
        _findNext.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent selectionEvent) { _editor.findNext(); }
            public void widgetSelected(SelectionEvent selectionEvent) { _editor.findNext(); }
        });
        
        _close = new Button(actionRow, SWT.PUSH);
        _close.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent selectionEvent) { _editor.cancelFind(); }
            public void widgetSelected(SelectionEvent selectionEvent) { _editor.cancelFind(); }
        });
        
        _replace = new Button(actionRow, SWT.PUSH);
        _replace.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent selectionEvent) { _editor.findReplace(); }
            public void widgetSelected(SelectionEvent selectionEvent) { _editor.findReplace(); }
        });
        
        _replaceAll = new Button(actionRow, SWT.PUSH);
        _replaceAll.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent selectionEvent) { _editor.findReplaceAll(); }
            public void widgetSelected(SelectionEvent selectionEvent) { _editor.findReplaceAll(); }
        });

        _findText.addTraverseListener(new TraverseListener() {
            public void keyTraversed(TraverseEvent evt) {
                if (evt.detail == SWT.TRAVERSE_RETURN) {
                    _findNext.forceFocus();
                    _editor.findNext();
                }
            }
        });
        _findReplace.addTraverseListener(new TraverseListener() {
            public void keyTraversed(TraverseEvent evt) {
                if (evt.detail == SWT.TRAVERSE_RETURN) {
                    _replace.forceFocus();
                    _editor.findReplace();
                }
            }
        });
        
        _findShell.addShellListener(new ShellListener() {
            public void shellActivated(ShellEvent shellEvent) {}
            public void shellClosed(ShellEvent evt) { evt.doit = false; _editor.cancelFind(); }
            public void shellDeactivated(ShellEvent shellEvent) {}
            public void shellDeiconified(ShellEvent shellEvent) {}
            public void shellIconified(ShellEvent shellEvent) {}
        });
        
        _editor.getBrowser().getTranslationRegistry().register(this);
        _editor.getBrowser().getThemeRegistry().register(this);
    }
 
    public void applyTheme(Theme theme) {
        _findShell.setFont(theme.SHELL_FONT);
        _findTextLabel.setFont(theme.DEFAULT_FONT);
        _findText.setFont(theme.DEFAULT_FONT);
        _findReplaceLabel.setFont(theme.DEFAULT_FONT);
        _findReplace.setFont(theme.DEFAULT_FONT);
        _findMatchCase.setFont(theme.DEFAULT_FONT);
        _findWrapAround.setFont(theme.DEFAULT_FONT);
        _findBackwards.setFont(theme.DEFAULT_FONT);
        _findNext.setFont(theme.BUTTON_FONT);
        _close.setFont(theme.BUTTON_FONT);
        _replace.setFont(theme.BUTTON_FONT);
        _replaceAll.setFont(theme.BUTTON_FONT);
        
        _findShell.pack();
    }
    
    private static final String T_FIND_ROOT = "syndie.gui.messageeditorfind.root";
    private static final String T_FIND_TEXT = "syndie.gui.messageeditorfind.text";
    private static final String T_FIND_REPLACE = "syndie.gui.messageeditorfind.replace";
    private static final String T_FIND_MATCH = "syndie.gui.messageeditorfind.match";
    private static final String T_FIND_WRAP = "syndie.gui.messageeditorfind.wrap";
    private static final String T_FIND_BACKWARDS = "syndie.gui.messageeditorfind.backwards";
    private static final String T_FIND_NEXT = "syndie.gui.messageeditorfind.next";
    private static final String T_FIND_NEXT_TOOLTIP = "syndie.gui.messageeditorfind.nexttooltip";
    private static final String T_FIND_CLOSE = "syndie.gui.messageeditorfind.close";
    private static final String T_FIND_CLOSE_TOOLTIP = "syndie.gui.messageeditorfind.closetooltip";
    private static final String T_FIND_REPLACE_ACTION = "syndie.gui.messageeditorfind.replace.action";
    private static final String T_FIND_REPLACE_ACTION_TOOLTIP = "syndie.gui.messageeditorfind.replace.actiontooltip";
    private static final String T_FIND_REPLACE_ALL_ACTION = "syndie.gui.messageeditorfind.replaceall.action";
    private static final String T_FIND_REPLACE_ALL_ACTION_TOOLTIP = "syndie.gui.messageeditorfind.replaceall.actiontooltip";
    
    public void translate(TranslationRegistry registry) {
        _findShell.setText(registry.getText(T_FIND_ROOT, "Search and replace"));
        _findTextLabel.setText(registry.getText(T_FIND_TEXT, "Find what: "));
        _findReplaceLabel.setText(registry.getText(T_FIND_REPLACE, "Replace with: "));
        _findMatchCase.setText(registry.getText(T_FIND_MATCH, "match case"));
        _findWrapAround.setText(registry.getText(T_FIND_WRAP, "wrap around"));
        _findBackwards.setText(registry.getText(T_FIND_BACKWARDS, "backwards"));
        _findNext.setText(registry.getText(T_FIND_NEXT, "Find next"));
        _findNext.setToolTipText(registry.getText(T_FIND_NEXT_TOOLTIP, "Find the next occurrence of the word"));
        _close.setText(registry.getText(T_FIND_CLOSE, "Close"));
        _close.setToolTipText(registry.getText(T_FIND_CLOSE_TOOLTIP, "Finish searching"));
        _replace.setText(registry.getText(T_FIND_REPLACE_ACTION, "Replace"));
        _replace.setToolTipText(registry.getText(T_FIND_REPLACE_ACTION_TOOLTIP, "Replace the current occurrence of the word"));
        _replaceAll.setText(registry.getText(T_FIND_REPLACE_ALL_ACTION, "Replace all"));
        _replaceAll.setToolTipText(registry.getText(T_FIND_REPLACE_ALL_ACTION_TOOLTIP, "Replace all remaining occurrences of the word"));
    }
}

class MessageEditorSpell implements Themeable, Translatable {
    private MessageEditorNew _editor;
    private Shell _spellShell;
    private StyledText _spellContext;
    private Label _spellWordLabel;
    private Text _spellWord;
    private Label _spellSuggestionsLabel;
    private Combo _spellSuggestions;
    private Button _spellReplace;
    private Button _spellReplaceAll;
    private Button _spellIgnore;
    private Button _spellIgnoreAll;
    private Button _spellAdd;
    private Button _spellCancel;
    /** list of words we are ignoring for the current spellcheck iteration */
    private ArrayList _spellIgnoreAllList;
    
    public MessageEditorSpell(MessageEditorNew editor) {
        _editor = editor;
        initComponents();
    }
 
    public void dispose() {
        _editor.getBrowser().getTranslationRegistry().unregister(this);
        _editor.getBrowser().getThemeRegistry().unregister(this);
        _spellShell.dispose();
    }
    
    public String getSpellWordOrig() { return _spellWord.getText().trim(); }
    public String getSuggestion() { return _spellSuggestions.getText().trim(); }
    public List getIgnoreAllList() { return _spellIgnoreAllList; }
    public void updateSuggestions(ArrayList suggestions, String lineText, String word) {
        _spellWord.setText(word);
        _spellSuggestions.removeAll();
        for (int i = 0; i < suggestions.size(); i++)
            _spellSuggestions.add((String)suggestions.get(i));
        _spellSuggestions.select(0);
        _spellContext.setText(lineText);
    }
    public void showSpell(boolean wordSet) {
        if (wordSet) {
            _spellContext.setLineBackground(0, 1, null);
            _spellWord.setEnabled(true);
            _spellSuggestions.setEnabled(true);
            _spellAdd.setEnabled(true);
            _spellCancel.setEnabled(true);
            _spellCancel.setText(_editor.getBrowser().getTranslationRegistry().getText(T_SPELL_CANCEL, "cancel"));
            _spellIgnore.setEnabled(true);
            _spellIgnoreAll.setEnabled(true);
            _spellReplace.setEnabled(true);
            _spellReplaceAll.setEnabled(true);
        } else {
            _spellContext.setText(_editor.getBrowser().getTranslationRegistry().getText(T_SPELL_END, "End of content reached"));
            _spellContext.setLineBackground(0, 1, ColorUtil.getColor("red", null));
            _spellWord.setText("");
            _spellWord.setEnabled(false);
            _spellSuggestions.removeAll();
            _spellSuggestions.setEnabled(false);
            _spellAdd.setEnabled(false);
            _spellCancel.setEnabled(true);
            _spellCancel.setText(_editor.getBrowser().getTranslationRegistry().getText(T_SPELL_END_OK, "ok"));
            _spellIgnore.setEnabled(false);
            _spellIgnoreAll.setEnabled(false);
            _spellReplace.setEnabled(false);
            _spellReplaceAll.setEnabled(false);
        }
        _spellShell.open();
    }
    
    private void initComponents() {
        _spellShell = new Shell(_editor.getPageRoot().getShell(), SWT.DIALOG_TRIM);
        GridLayout gl = new GridLayout(2, false);
        _spellShell.setLayout(gl);
        
        _spellIgnoreAllList = new ArrayList();
        
        _spellContext = new StyledText(_spellShell, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.READ_ONLY);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalSpan = 2;
        _spellContext.setLayoutData(gd);
        
        _spellWordLabel = new Label(_spellShell, SWT.NONE);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.grabExcessHorizontalSpace = false;
        _spellWordLabel.setLayoutData(gd);
        _spellWord = new Text(_spellShell, SWT.BORDER | SWT.READ_ONLY | SWT.SINGLE | SWT.LEFT);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.grabExcessHorizontalSpace = true;
        _spellWord.setLayoutData(gd);

        _spellSuggestionsLabel = new Label(_spellShell, SWT.NONE);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.grabExcessHorizontalSpace = false;
        _spellSuggestionsLabel.setLayoutData(gd);
        _spellSuggestions = new Combo(_spellShell, SWT.DROP_DOWN);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.grabExcessHorizontalSpace = true;
        _spellSuggestions.setLayoutData(gd);
        
        Composite actionLine = new Composite(_spellShell, SWT.NONE);
        actionLine.setLayout(new FillLayout(SWT.HORIZONTAL));
        _spellReplace = new Button(actionLine, SWT.PUSH);
        _spellReplaceAll = new Button(actionLine, SWT.PUSH);
        _spellIgnore = new Button(actionLine, SWT.PUSH);
        _spellIgnoreAll = new Button(actionLine, SWT.PUSH);
        _spellAdd = new Button(actionLine, SWT.PUSH);
        _spellCancel = new Button(actionLine, SWT.PUSH);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalSpan = 2;
        actionLine.setLayoutData(gd);
        
        _spellIgnore.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent selectionEvent) { _editor.spellIgnore(); }
            public void widgetSelected(SelectionEvent selectionEvent) { _editor.spellIgnore(); }
        });
        _spellIgnoreAll.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent selectionEvent) { _spellIgnoreAllList.add(_spellWord.getText().trim().toLowerCase()); _editor.spellIgnore(); }
            public void widgetSelected(SelectionEvent selectionEvent) { _spellIgnoreAllList.add(_spellWord.getText().trim().toLowerCase()); _editor.spellIgnore(); }
        });
        _spellReplace.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent selectionEvent) { _editor.spellReplaceWord(false); _editor.spellNext(); }
            public void widgetSelected(SelectionEvent selectionEvent) { _editor.spellReplaceWord(false); _editor.spellNext(); }
        });
        _spellReplaceAll.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent selectionEvent) { _editor.spellReplaceWord(true); _editor.spellNext(); }
            public void widgetSelected(SelectionEvent selectionEvent) { _editor.spellReplaceWord(true); _editor.spellNext(); }
        });

        _spellCancel.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent selectionEvent) { cancelSpell(); }
            public void widgetSelected(SelectionEvent selectionEvent) { cancelSpell(); }
        });
        
        _editor.getBrowser().getTranslationRegistry().register(this);
        _editor.getBrowser().getThemeRegistry().register(this);
    }
    
    void resetSpellcheck() {
        _spellIgnoreAllList.clear();
        _editor.resetSpellcheck();
    }
    void cancelSpell() {
        resetSpellcheck();
        _spellShell.setVisible(false); 
    }
    
    public void applyTheme(Theme theme) {
        _spellShell.setFont(theme.SHELL_FONT);
        _spellWordLabel.setFont(theme.DEFAULT_FONT);
        _spellWord.setFont(theme.DEFAULT_FONT);
        _spellSuggestionsLabel.setFont(theme.DEFAULT_FONT);
        _spellSuggestions.setFont(theme.DEFAULT_FONT);
        _spellReplace.setFont(theme.BUTTON_FONT);
        _spellReplaceAll.setFont(theme.BUTTON_FONT);
        _spellIgnore.setFont(theme.BUTTON_FONT);
        _spellIgnoreAll.setFont(theme.BUTTON_FONT);
        _spellAdd.setFont(theme.BUTTON_FONT);
        _spellCancel.setFont(theme.BUTTON_FONT);
        _spellShell.pack();
    }
    
    private static final String T_SPELL_ROOT = "syndie.gui.messageeditorspell.root";
    private static final String T_SPELL_WORD = "syndie.gui.messageeditorspell.word";
    private static final String T_SPELL_SUGGESTION = "syndie.gui.messageeditorspell.suggestion";
    private static final String T_SPELL_REPLACE = "syndie.gui.messageeditorspell.replace";
    private static final String T_SPELL_REPLACE_ALL = "syndie.gui.messageeditorspell.replaceall";
    private static final String T_SPELL_IGNORE = "syndie.gui.messageeditorspell.ignore";
    private static final String T_SPELL_IGNORE_ALL = "syndie.gui.messageeditorspell.ignoreall";
    private static final String T_SPELL_ADD = "syndie.gui.messageeditorspell.add";
    private static final String T_SPELL_CANCEL = "syndie.gui.messageeditorspell.cancel";
 
    private static final String T_SPELL_END = "syndie.gui.messageeditorspell.end";
    private static final String T_SPELL_END_OK = "syndie.gui.messageeditorspell.end.ok";
    
    public void translate(TranslationRegistry registry) {
        _spellShell.setText(registry.getText(T_SPELL_ROOT, "Spell checker"));
        _spellWordLabel.setText(registry.getText(T_SPELL_WORD, "Word: "));
        _spellSuggestionsLabel.setText(registry.getText(T_SPELL_SUGGESTION, "Suggestions: "));
        _spellReplace.setText(registry.getText(T_SPELL_REPLACE, "replace"));
        _spellReplaceAll.setText(registry.getText(T_SPELL_REPLACE_ALL, "replace all"));
        _spellIgnore.setText(registry.getText(T_SPELL_IGNORE, "ignore"));
        _spellIgnoreAll.setText(registry.getText(T_SPELL_IGNORE_ALL, "ignore all"));
        _spellAdd.setText(registry.getText(T_SPELL_ADD, "add"));
        _spellCancel.setText(registry.getText(T_SPELL_CANCEL, "cancel"));
    }
}

class MessageEditorStyler implements Themeable, Translatable {
    private MessageEditorNew _editor;
    
    private Shell _txtShell;
    private StyledText _sampleText;
    private Button _txtBold;
    private Button _txtItalic;
    private Button _txtUnderline;
    private Button _txtStrikeout;
    private Combo _txtFont;
    private Combo _txtFontSize;
    private Button _txtFGColor;
    private Button _txtBGColor;
    private Button _txtAlignLeft;
    private Button _txtAlignCenter;
    private Button _txtAlignRight;
    private Button _styleOk;
    private Button _styleCancel;
    private Font _sampleFont;
    
    private Group _grpText;
    private Group _grpAlign;
    
    public MessageEditorStyler(MessageEditorNew editor) {
        _editor = editor;
        initComponents();
    }
    
    public void open() {
        resetTextStyle();
        _txtShell.open();
    }
    public void dispose() {
        _editor.getBrowser().getThemeRegistry().unregister(this);
        _editor.getBrowser().getTranslationRegistry().unregister(this);
        _txtShell.dispose();
    }
    
    public Color getControlFGColor() { return ifDiff(_txtFGColor.getBackground(), _txtFGColor.getParent().getBackground()); }
    public Color getControlBGColor() { return ifDiff(_txtBGColor.getBackground(), _txtBGColor.getParent().getBackground()); }
    public boolean getControlBold() { return _txtBold.getSelection(); }
    public boolean getControlItalic() { return _txtItalic.getSelection(); }
    public boolean getControlUnderline() { return _txtUnderline.getSelection(); }
    public boolean getControlStrikeout() { return _txtStrikeout.getSelection(); }
    public String getControlFontName() { return _txtFont.getItem(_txtFont.getSelectionIndex()); }
    public String getControlFontSize() { return _txtFontSize.getItem(_txtFontSize.getSelectionIndex()); }
    
    private Color ifDiff(Color child, Color parent) {
        if (!child.equals(parent)) {
            //System.out.println("diff: " + child + " / " + parent);
            return child;
        } else {
            return null; 
        }
    }
    private Image ifDiff(Image child, Image parent) { if (!child.equals(parent)) return child; else return null; }

    private void initComponents() {
        _txtShell = new Shell(_editor.getPageRoot().getShell(), SWT.DIALOG_TRIM);
        _txtShell.addShellListener(new ShellListener() {
            public void shellActivated(ShellEvent shellEvent) {}
            public void shellClosed(ShellEvent evt) { evt.doit = false; cancelStyle(); }
            public void shellDeactivated(ShellEvent shellEvent) {}
            public void shellDeiconified(ShellEvent shellEvent) {}
            public void shellIconified(ShellEvent shellEvent) {}
        });
        
        RowLayout rl = new RowLayout(SWT.VERTICAL);
        rl.fill = true;
        rl.wrap = false;
        _txtShell.setLayout(rl);
        
        Composite controlBar = new Composite(_txtShell, SWT.NONE);
        controlBar.setLayout(new RowLayout(SWT.HORIZONTAL));
        _grpText = new Group(controlBar, SWT.SHADOW_ETCHED_IN);
        _grpText.setLayout(new RowLayout(SWT.HORIZONTAL));
        _txtBold = new Button(_grpText, SWT.TOGGLE);
        _txtItalic = new Button(_grpText, SWT.TOGGLE);
        _txtUnderline = new Button(_grpText, SWT.TOGGLE);
        _txtStrikeout = new Button(_grpText, SWT.TOGGLE);
        _txtFont = new Combo(_grpText, SWT.DROP_DOWN | SWT.READ_ONLY);
        _txtFont.add("Times");
        _txtFont.add("Courier");
        _txtFont.add("Helvetica");
        _txtFont.select(0);
        _txtFontSize = new Combo(_grpText, SWT.DROP_DOWN | SWT.READ_ONLY);
        _txtFontSize.add("-5");
        _txtFontSize.add("-4");
        _txtFontSize.add("-3");
        _txtFontSize.add("-2");
        _txtFontSize.add("-1");
        _txtFontSize.add("+0");
        _txtFontSize.add("+1");
        _txtFontSize.add("+2");
        _txtFontSize.add("+3");
        _txtFontSize.add("+4");
        _txtFontSize.add("+5");
        _txtFontSize.select(5);
        _txtFGColor = buildColorCombo(_grpText, "color", "Adjust the fg color", "black", true);
        _txtBGColor = buildColorCombo(_grpText, "bgcolor", "Adjust the bg color", "white", true);
        
        _grpAlign = new Group(controlBar, SWT.SHADOW_ETCHED_IN);
        _grpAlign.setLayout(new RowLayout(SWT.HORIZONTAL));
        _txtAlignLeft = new Button(_grpAlign, SWT.RADIO);
        _txtAlignCenter = new Button(_grpAlign, SWT.RADIO);
        _txtAlignRight = new Button(_grpAlign, SWT.RADIO);
        
        _sampleText = new StyledText(_txtShell, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.READ_ONLY);
        
        Composite actionRow = new Composite(_txtShell, SWT.NONE);
        actionRow.setLayout(new FillLayout(SWT.HORIZONTAL));
        _styleOk = new Button(actionRow, SWT.PUSH);
        _styleOk.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent selectionEvent) { insertTextStyle(); _txtShell.setVisible(false); }
            public void widgetSelected(SelectionEvent selectionEvent) { insertTextStyle(); _txtShell.setVisible(false); }
        });
        _styleCancel = new Button(actionRow, SWT.PUSH);
        _styleCancel.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent selectionEvent) { cancelStyle(); }
            public void widgetSelected(SelectionEvent selectionEvent) { cancelStyle(); }
        });
        
        PreviewStyle lsnr = new PreviewStyle();
        _txtBold.addSelectionListener(lsnr);
        _txtItalic.addSelectionListener(lsnr);
        _txtUnderline.addSelectionListener(lsnr);
        _txtStrikeout.addSelectionListener(lsnr);
        _txtFont.addSelectionListener(lsnr);
        _txtFontSize.addSelectionListener(lsnr);
        _txtAlignLeft.addSelectionListener(lsnr);
        _txtAlignCenter.addSelectionListener(lsnr);
        _txtAlignRight.addSelectionListener(lsnr);
        addListeners(lsnr, _txtFGColor);
        addListeners(lsnr, _txtBGColor);
        
        lsnr.redrawSample();
    
        _editor.getBrowser().getTranslationRegistry().register(this);
        _editor.getBrowser().getThemeRegistry().register(this);
    }
    
    private void cancelStyle() {
        _txtShell.setVisible(false);
        resetTextStyle();
        _editor.cancelStyle();
    }
    
    private void addListeners(PreviewStyle lsnr, Button button) {
        Menu menu = button.getMenu();
        if (menu != null) {
            MenuItem items[] = menu.getItems();
            if (items != null) {
                for (int i = 0; i < items.length; i++)
                    items[i].addSelectionListener(lsnr);
            }
        }
        button.addSelectionListener(lsnr);
    }
    
    private class PreviewStyle implements SelectionListener, TraverseListener {
        public void widgetDefaultSelected(SelectionEvent selectionEvent) { redrawSample(); }
        public void widgetSelected(SelectionEvent selectionEvent) { redrawSample(); }
        public void keyTraversed(TraverseEvent traverseEvent) { redrawSample(); }
        public void redrawSample() {
            if ( (_sampleFont != null) && (!_sampleFont.isDisposed()) ) _sampleFont.dispose();
            _sampleFont = null;
            boolean bold = getControlBold();
            boolean italic = getControlItalic();
            boolean underline = getControlUnderline();
            boolean strikeout = getControlStrikeout();
            String font = getControlFontName();
            String sz = getControlFontSize();
            Color bgColor = getControlBGColor();
            Color fgColor = getControlFGColor();
            
            _sampleText.setStyleRanges(null, null);
            StyleRange range = new StyleRange(0, _sampleText.getCharCount(), fgColor, bgColor);
            range.strikeout = strikeout;
            range.underline = underline;
            range.font = getSampleFont(bold, italic, font, sz);
            int align = SWT.LEFT;
            if (_txtAlignCenter.getSelection()) align = SWT.CENTER;
            else if (_txtAlignRight.getSelection()) align = SWT.RIGHT;
            _sampleText.setStyleRange(range);
            _txtShell.pack(true);
            // align has to be after pack, otherwise it gets lost for some reason
            _sampleText.setLineAlignment(0, 1, align);
        }
        
        private Font getSampleFont(boolean bold, boolean italic, String style, String sz) {
            int fontHeight = Theme.getSize(_editor.getBrowser().getThemeRegistry().getTheme().CONTENT_FONT);
            try {
                if (sz.startsWith("+"))
                    sz = sz.substring(1);
                fontHeight += 2*Integer.parseInt(sz);
            } catch (NumberFormatException nfe) {
                // ignore
            }
            int fontStyle = SWT.NORMAL;
            if (bold) fontStyle |= SWT.BOLD;
            if (italic) fontStyle |= SWT.ITALIC;
            return new Font(Display.getDefault(), style, fontHeight, fontStyle);
        }
    }
    
    private void resetTextStyle() {
        _sampleText.setStyleRanges(null, null);
        _sampleText.setLineAlignment(0, 1, SWT.LEFT);
        _txtBold.setSelection(false);
        _txtItalic.setSelection(false);
        _txtUnderline.setSelection(false);
        _txtStrikeout.setSelection(false);
        _txtFont.select(0);
        _txtFontSize.select(5); // +0
        _txtFGColor.setBackground(null);
        _txtBGColor.setBackground(null);
        _txtAlignLeft.setSelection(true);
        _txtAlignCenter.setSelection(false);
        _txtAlignRight.setSelection(false);
    }
    
    private void insertTextStyle() {
        boolean bold = _txtBold.getSelection();
        boolean italic = _txtItalic.getSelection();
        boolean underline = _txtUnderline.getSelection();
        boolean strikeout = _txtStrikeout.getSelection();
        String font = _txtFont.getItem(_txtFont.getSelectionIndex());
        String sz = _txtFontSize.getItem(_txtFontSize.getSelectionIndex());
        String fg = ColorUtil.getSystemColorName(_txtFGColor.getBackground());
        String bg = ColorUtil.getSystemColorName(_txtBGColor.getBackground());
        
        String align = null;
        if (_txtAlignCenter.getSelection())
            align = " align=\"center\"";
        else if (_txtAlignRight.getSelection())
            align = " align=\"right\"";
        
        StringBuffer buf = new StringBuffer();
        if (bold) {
            buf.append("<b");
            if (align != null)
                buf.append(align);
            align = null;
            buf.append(">");
        }
        if (italic) {
            buf.append("<i");
            if (align != null)
                buf.append(align);
            align = null;
            buf.append(">");
        }
        if (underline) {
            buf.append("<u");
            if (align != null)
                buf.append(align);
            align = null;
            buf.append(">");
        }
        if (strikeout) {
            buf.append("<so");
            if (align != null)
                buf.append(align);
            align = null;
            buf.append(">");
        }
        
        boolean fontSet = false;
        if ( (!"Times".equals(font)) || (!"+0".equals(sz)) || (fg != null) || (bg != null) || (align != null) )
            fontSet = true;
        
        if (fontSet) {
            buf.append("<font ");
            if (!"Times".equals(font)) buf.append(" name=\"").append(font).append("\"");
            if (!"+0".equals(sz)) buf.append(" size=\"").append(sz).append("\"");
            if (fg != null) buf.append(" color=\"").append(fg).append("\"");
            if (bg != null) buf.append(" bgcolor=\"").append(bg).append("\"");
            if (align != null) buf.append(align);
            buf.append(">");
        }
        
        int begin = buf.length();
        String sel = _editor.getSelectedText();
        if ( (sel == null) || (sel.trim().length() <= 0) )
            buf.append("CONTENT GOES HERE");
        else
            buf.append(sel);
        int end = buf.length();
        
        if (fontSet) buf.append("</font>");
        if (strikeout) buf.append("</so>");
        if (underline) buf.append("</u>");
        if (italic) buf.append("</i>");
        if (bold) buf.append("</b>");
        
        String str = buf.toString();
        boolean insert = ( (sel == null) || (sel.trim().length() == 0) );
        _editor.insertStyle(str, insert, begin, end);
    }

    private Button buildColorCombo(Group parent, String name, String tooltip, String defaultColor, boolean enable) {
        return buildColorCombo(parent, name, tooltip, defaultColor, enable, null);
    }
    private Button buildColorCombo(Group parent, String name, String tooltip, String defaultColor, boolean enable, Runnable onSelect) {
        final Button rv = new Button(parent, SWT.PUSH);
        ArrayList names = ColorUtil.getSystemColorNames();
        rv.setText(name);
        final Menu colorMenu = new Menu(rv);
        MenuItem none = new MenuItem(colorMenu, SWT.PUSH);
        none.setText(_editor.getBrowser().getTranslationRegistry().getText(T_COLOR_DEFAULT, "default"));
        if ( (defaultColor == null) || (!names.contains(defaultColor)) )
            none.setSelection(true);
        none.addSelectionListener(new ColorMenuItemListener(rv, null, onSelect));
        for (int i = 0; i < names.size(); i++) {
            String colorName = (String)names.get(i);
            Color color = (Color)ColorUtil.getColor(colorName, null);
            MenuItem item = new MenuItem(colorMenu, SWT.PUSH);
            item.setText(colorName);
            item.setImage(ColorUtil.getSystemColorSwatch(color));
            if (colorName.equalsIgnoreCase(defaultColor))
                item.setSelection(true);
            item.addSelectionListener(new ColorMenuItemListener(rv, colorName, onSelect));
        }
        rv.setMenu(colorMenu);
        rv.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent selectionEvent) { colorMenu.setVisible(true); }
            public void widgetSelected(SelectionEvent selectionEvent) { colorMenu.setVisible(true); }
        });
        rv.setToolTipText(tooltip);
        rv.setEnabled(enable);
        return rv;
    }

    private class ColorMenuItemListener implements SelectionListener {
        private Button _button;
        private String _color;
        private Runnable _onSelect;
        public ColorMenuItemListener(Button button, String color, Runnable onSelect) {
            _button = button;
            _color = color;
            _onSelect = onSelect;
        }
        public void widgetDefaultSelected(SelectionEvent selectionEvent) { pickColor(); }
        public void widgetSelected(SelectionEvent selectionEvent) { pickColor(); }
        private void pickColor() {
            Color color = ColorUtil.getColor(_color, null);
            _button.setBackground(color);
            if (color == null) {
                _button.setForeground(ColorUtil.getColor("black", null));
            } else {
                // not the best heuristic, but it seems to work ok
                if ( (color.getRed() <= 128) && (color.getGreen() <= 128) && (color.getBlue() <= 128) )
                    _button.setForeground(ColorUtil.getColor("white", null));
                else
                    _button.setForeground(ColorUtil.getColor("black", null));
            }
            _editor.modified();
            if (_onSelect != null) _onSelect.run();
        }
    }
    
    private static final String T_COLOR_DEFAULT = "syndie.gui.messageeditorstyler.color.default";
    private static final String T_STYLE = "syndie.gui.messageeditorstyler.style";
    private static final String T_TEXT = "syndie.gui.messageeditorstyler.text";
    private static final String T_TEXT_B = "syndie.gui.messageeditorstyler.text.bold";
    private static final String T_TEXT_I = "syndie.gui.messageeditorstyler.text.italic";
    private static final String T_TEXT_U = "syndie.gui.messageeditorstyler.text.underline";
    private static final String T_TEXT_SO = "syndie.gui.messageeditorstyler.text.strikeout";
    private static final String T_TEXT_FONT = "syndie.gui.messageeditorstyler.text.font";
    private static final String T_TEXT_FONTSIZE = "syndie.gui.messageeditorstyler.text.fontsize";
    private static final String T_TEXT_ALIGN = "syndie.gui.messageeditorstyler.text.align";
    private static final String T_TEXT_ALIGN_LEFT = "syndie.gui.messageeditorstyler.text.align.left";
    private static final String T_TEXT_ALIGN_LEFT_TOOLTIP = "syndie.gui.messageeditorstyler.text.align.lefttooltip";
    private static final String T_TEXT_ALIGN_CENTER = "syndie.gui.messageeditorstyler.text.align.center";
    private static final String T_TEXT_ALIGN_CENTER_TOOLTIP = "syndie.gui.messageeditorstyler.text.align.centertooltip";
    private static final String T_TEXT_ALIGN_RIGHT = "syndie.gui.messageeditorstyler.text.align.right";
    private static final String T_TEXT_ALIGN_RIGHT_TOOLTIP = "syndie.gui.messageeditorstyler.text.align.righttooltip";
    private static final String T_TEXT_SAMPLE = "syndie.gui.messageeditorstyler.text.sample";
    private static final String T_TEXT_OK = "syndie.gui.messageeditorstyler.text.ok";
    private static final String T_TEXT_CANCEL = "syndie.gui.messageeditorstyler.text.cancel";
    
    public void translate(TranslationRegistry registry) {
        // todo: translate
        _txtShell.setText(registry.getText(T_STYLE, "Text style chooser"));        
        _grpText.setText(registry.getText(T_TEXT, "Styling"));
        _txtBold.setText(registry.getText(T_TEXT_B, "B"));
        _txtItalic.setText(registry.getText(T_TEXT_I, "I"));
        _txtUnderline.setText(registry.getText(T_TEXT_U, "U"));
        _txtStrikeout.setText(registry.getText(T_TEXT_SO, "SO"));
        _txtFont.setToolTipText(registry.getText(T_TEXT_FONT, "Adjust the font"));
        _txtFontSize.setToolTipText(registry.getText(T_TEXT_FONTSIZE, "Adjust the font size"));
        _grpAlign.setText(registry.getText(T_TEXT_ALIGN, "Alignment"));
        _txtAlignLeft.setText(registry.getText(T_TEXT_ALIGN_LEFT, "left"));
        _txtAlignLeft.setToolTipText(registry.getText(T_TEXT_ALIGN_LEFT_TOOLTIP, "Align the text to the left"));
        _txtAlignCenter.setText(registry.getText(T_TEXT_ALIGN_CENTER, "center"));
        _txtAlignCenter.setToolTipText(registry.getText(T_TEXT_ALIGN_CENTER_TOOLTIP, "Align the text to the center"));
        _txtAlignRight.setText(registry.getText(T_TEXT_ALIGN_RIGHT, "right"));
        _txtAlignRight.setToolTipText(registry.getText(T_TEXT_ALIGN_RIGHT_TOOLTIP, "Align the text to the right"));
        _sampleText.setText(registry.getText(T_TEXT_SAMPLE, "This is the sample text"));
        _styleOk.setText(registry.getText(T_TEXT_OK, "ok"));
        _styleCancel.setText(registry.getText(T_TEXT_CANCEL, "cancel"));
    }
    
    public void applyTheme(Theme theme) {
        _txtShell.setFont(theme.SHELL_FONT);
        _txtBold.setFont(theme.BUTTON_FONT);
        _txtItalic.setFont(theme.BUTTON_FONT);
        _txtUnderline.setFont(theme.BUTTON_FONT);
        _txtStrikeout.setFont(theme.BUTTON_FONT);
        _txtFont.setFont(theme.DEFAULT_FONT);
        _txtFontSize.setFont(theme.DEFAULT_FONT);
        _txtFGColor.setFont(theme.BUTTON_FONT);
        _txtBGColor.setFont(theme.BUTTON_FONT);
        _txtAlignLeft.setFont(theme.BUTTON_FONT);
        _txtAlignCenter.setFont(theme.BUTTON_FONT);
        _txtAlignRight.setFont(theme.BUTTON_FONT);
        _styleOk.setFont(theme.BUTTON_FONT);
        _styleCancel.setFont(theme.BUTTON_FONT);
        _grpText.setFont(theme.DEFAULT_FONT);
        _grpAlign.setFont(theme.DEFAULT_FONT);
        _txtShell.pack();
    }
}