package syndie.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.i2p.data.Hash;
import net.i2p.data.PrivateKey;
import net.i2p.data.SessionKey;
import net.i2p.data.SigningPrivateKey;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import syndie.data.MessageInfo;
import syndie.data.SyndieURI;

/**
 *
 */
public class MessageViewTab extends BrowserTab implements Translatable, Themeable {
    private MessageView _view;
    private String _desc;
    
    public MessageViewTab(BrowserControl browser, SyndieURI uri) {
        super(browser, uri);
    }
    
    protected void initComponents() {
        getRoot().setLayout(new FillLayout());
        _view = new MessageView(getBrowser(), getRoot(), getURI());
        
        getBrowser().getThemeRegistry().register(this);
        getBrowser().getTranslationRegistry().register(this);
    }
    
    protected void disposeDetails() { 
        _view.dispose();
        getBrowser().getTranslationRegistry().unregister(this);
        getBrowser().getThemeRegistry().unregister(this);
    }
    
    public Image getIcon() { return ImageUtil.ICON_TAB_MSG; }
    public String getName() { return _view.getTitle(); }
    public String getDescription() { return getURI().toString(); }

    public void translate(TranslationRegistry registry) {
        // nothing translatable
    }
    public void applyTheme(Theme theme) {
        // nothing themeable
    }
}