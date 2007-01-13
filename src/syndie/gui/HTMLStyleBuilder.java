package syndie.gui;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.*;
import net.i2p.data.Hash;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import syndie.Constants;
import syndie.data.HTMLStateBuilder;
import syndie.data.HTMLTag;
import syndie.data.MessageInfo;
import syndie.data.SyndieURI;
import syndie.db.CommandImpl;
import syndie.db.DBClient;
import syndie.db.NullUI;
import syndie.db.UI;

/**
 *
 */
class HTMLStyleBuilder {
    private UI _ui;
    private PageRendererSource _source;
    private List _htmlTags;
    private String _msgText;
    private MessageInfo _msg;
    private boolean _enableImages;
    private boolean _styled;
    private StyleRange[] _styleRanges;
    private ArrayList _imageIndexes;
    /** Image instances loaded up at the _imageIndexes location */
    private ArrayList _images;
    private ArrayList _linkIndexes;
    private ArrayList _listItemIndexes;
    private ArrayList _linkTags;
    private ArrayList _imageTags;
    
    // default fonts that can be shared across many ranges.  this does not cover
    // every scenario though, so some fonts may need to be built dynamically
    private Font _fontDefault;
    private Font _fontP;
    private Font _fontLI;
    private Font _fontH1;
    private Font _fontH2;
    private Font _fontH3;
    private Font _fontH4;
    private Font _fontH5;
    private Font _fontPRE;
    private Font _fontCODE;
    private Font _fontA;
    /** fonts built for ranges that did not match up with the defaults */
    private List _customFonts;

    /** light grey background for quotes */
    private static Color _bgColorQuote = new Color(Display.getDefault(), 223, 223, 223);
    
    private Map _customColors;
    
    private Color _bgColor;
    private Image _bgImage;
    
    private int _viewSizeModifier;

    public HTMLStyleBuilder(UI ui, PageRendererSource src, List htmlTags, String msgText, MessageInfo msg, boolean enableImages, boolean styled) {
        _ui = ui;
        _source = src;
        _htmlTags = htmlTags;
        _msgText = msgText;
        _msg = msg;
        _enableImages = enableImages;
        _styled = styled;
        _imageIndexes = new ArrayList();
        _linkIndexes = new ArrayList();
        _listItemIndexes = new ArrayList();
        _linkTags = new ArrayList();
        _imageTags = new ArrayList();
        _images = new ArrayList();
        _customFonts = new ArrayList();
        _customColors = new HashMap();
        _bgImage = null;
        _bgColor = null;
        ts("building fonts");
        buildFonts(getFontConfig(src));
        ts("fonts built");
    }
    
    public ArrayList getImageTags() { return _imageTags; }
    public ArrayList getLinkTags() { return _linkTags; }
    public Image getBackgroundImage() { return _bgImage; }
    public Color getBackgroundColor() { return _bgColor; }
    
    public void buildStyles() { buildStyles(0); }
    public void buildStyles(int viewSizeModifier) {
        _viewSizeModifier = viewSizeModifier;
        ts("building styles for " + _htmlTags.size() + " tags");
        
        String bodyBgImage = null;
        String bodyBgColor = null;
        
        // get a list of points where any tag starts or ends
        TreeMap breakPointTags = new TreeMap();
        for (int i = 0; i < _htmlTags.size(); i++) {
            HTMLTag tag = (HTMLTag)_htmlTags.get(i);
            if ("a".equals(tag.name) && (tag.getAttribValue("href") != null)) {
                _linkTags.add(tag);
            } else if ("img".equals(tag.name)) {
                _imageTags.add(tag);
            } else if ("body".equals(tag.name)) {
                bodyBgImage = tag.getAttribValue("bgimage");
                bodyBgColor = tag.getAttribValue("bgcolor");
            }
            List tags = (List)breakPointTags.get(new Integer(tag.startIndex));
            if (tags == null) {
                tags = new ArrayList();
                breakPointTags.put(new Integer(tag.startIndex), tags);
            }
            tags.add(tag);
            
            // now for ends
            tags = (List)breakPointTags.get(new Integer(tag.endIndex));
            if (tags == null) {
                tags = new ArrayList();
                breakPointTags.put(new Integer(tag.endIndex), tags);
            }
            tags.add(tag);
            //ts("breakpoints for tag " + tag + ": " + tag.getStartIndex() + ", " + tag.getEndIndex());
        }
        
        // include special characters
        if (_enableImages)
            insertCharBreakpoints(HTMLStateBuilder.PLACEHOLDER_IMAGE, breakPointTags, _imageIndexes);
        insertCharBreakpoints(HTMLStateBuilder.PLACEHOLDER_LINK_END, breakPointTags, _linkIndexes);
        insertCharBreakpoints(HTMLStateBuilder.PLACEHOLDER_LISTITEM, breakPointTags, _listItemIndexes);
        _ui.debugMessage("list item indexes: " + _listItemIndexes);

        ts("character breakpoints inserted");
        
        // make sure it covers the whole schebang
        List startTags = (List)breakPointTags.get(new Integer(0));
        if (startTags == null)
            breakPointTags.put(new Integer(0), new ArrayList());
        
        // dont need the end anymore
        breakPointTags.remove(new Integer(_msgText.length()));
        
        int bps = breakPointTags.size();
        _ui.debugMessage("breakpoints: " + bps);
        // now go through the breakpoints and check what other tags are applicable there.
        // the list of tags will contain all tags that are in that set, but it can contain
        // tags that should not be
        for (Iterator iter = breakPointTags.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry entry = (Map.Entry)iter.next();
            Integer bp = (Integer)entry.getKey();
            List tags = (List)entry.getValue();
            int orig = tags.size();
            for (int i = 0; i < tags.size(); i++) {
                HTMLTag tag = (HTMLTag)tags.get(i);
                while (tag.parent != null) {
                    HTMLTag cParent = tag.parent;
                    if (!tags.contains(cParent)) {
                        if ( (cParent.startIndex <= bp.intValue()) && (cParent.endIndex >= bp.intValue()) ) {
                            tags.add(cParent);
                        } else {
                            break;
                        }
                    }
                    tag = cParent;
                }
            }
            /*
            int num = tags.size();
            if (num < 10) {
                //System.out.print("0");
            } else if (num < 20) {
                System.out.println("\n# bp @ " + bp.intValue() + " tags: " + num + "/" + orig + " tags: " + tags);
                System.out.print("1");
            } else if (num < 30) {
                System.out.println("\n# bp @ " + bp.intValue() + " tags: " + num + "/" + orig + " tags: " + tags);
                System.out.print("2");
            } else if (num < 40) {
                System.out.println("\n# bp @ " + bp.intValue() + " tags: " + num + "/" + orig + " tags: " + tags);
                System.out.print("3");
            } else if (num < 50) {
                System.out.println("\n# bp @ " + bp.intValue() + " tags: " + num + "/" + orig + " tags: " + tags);
                System.out.print("4");
            } else {
                System.out.println("\n# bp @ " + bp.intValue() + " tags: " + num + "/" + orig + " tags: " + tags);
            }
             */
        }
        
        ts("tag children found in thread: " + Thread.currentThread().getName());
        
        setBodyOptions(bodyBgImage, bodyBgColor);
        
        long buildTime = 0;
        int buildInstances = 0;
        // iterate across those points, building a new StyleRange out of all tags applicable there
        _styleRanges = new StyleRange[breakPointTags.size()];
        int rangeIndex = 0;
        Iterator iter = breakPointTags.keySet().iterator();
        Integer nextIndex = null;
        for (;;) {
            long t1 = System.currentTimeMillis();
            //ts("iterating over breakpoint");
            Integer curBreakPoint = nextIndex;
            if (nextIndex == null) {
                if (iter.hasNext())
                    curBreakPoint = (Integer)iter.next();
                else
                    break;
            }
            nextIndex = null;
            int start = -1;
            int length = 0;
            if (rangeIndex == 0) {
                start = 0;
            } else {
                start = _styleRanges[rangeIndex-1].start + _styleRanges[rangeIndex-1].length;
            }
            if (iter.hasNext()) {
                nextIndex = (Integer)iter.next();
                length = nextIndex.intValue() - start;
            } else {
                length = _msgText.length() - start;
            }
            
            long t2 = System.currentTimeMillis();
            // now trim the set of applicable tags
            List tags = (List)breakPointTags.get(curBreakPoint);
            for (int i = 0; i < tags.size(); i++) {
                HTMLTag tag = (HTMLTag)tags.get(i);
                if ( (tag.startIndex <= start) && (tag.endIndex >= start+length) ) {
                    // ok, applicable
                } else {
                    tags.remove(i);
                    i--;
                }
            }
            
            long t3 = System.currentTimeMillis();
            List bpt = (List)breakPointTags.get(curBreakPoint);
            _styleRanges[rangeIndex] = buildStyle(bpt, start, length);
            rangeIndex++;
            long t4 = System.currentTimeMillis();
            if (t4-t1 > 10) {
                //ts("breakpoint: " + rangeIndex + " " + (t2-t1) +"/"+(t3-t2)+"/"+(t4-t3) + ": " + bpt);
                buildInstances++;
                buildTime += (t4-t3);
            }
        }
        
        ts("done iterating over breakpoints: " + breakPointTags.size() + " build:" + buildInstances+"/"+buildTime);
        
        if (_enableImages) {
            // put images in for all the <img> tags
            for (int i = 0; i < _imageTags.size(); i++) {
                HTMLTag imgTag = (HTMLTag)_imageTags.get(i);
                for (int j = 0; j < _styleRanges.length; j++) {
                    if (_styleRanges[j].start == imgTag.startIndex) {
                        //System.out.println("img in range @ " + _styleRanges[j].start + ": " + imgTag);
                        includeImage(_styleRanges[j], imgTag);
                        break;
                    }
                }
            }
        }
        
        ts("done including images");
        
        // now put images in the range after <a> tags
        for (int i = 0; i < _linkTags.size(); i++) {
            HTMLTag linkTag = (HTMLTag)_linkTags.get(i);
            for (int j = 0; j < _styleRanges.length; j++) {
                if (_styleRanges[j].start == linkTag.endIndex) {
                    //System.out.println("link in range @ " + _styleRanges[j].start + ": " + linkTag);
                    includeLinkEnd(_styleRanges[j], linkTag);
                    break;
                }
            }
        }
        
        ts("done including anchor end images");
    }
    
    private void setBodyOptions(final String bodyBgImage, final String bodyBgColor) {
        Display.getDefault().syncExec(new Runnable() { 
            public void run() {
                if (bodyBgImage != null) {
                    Image img = getImage(getURI(bodyBgImage));
                    if (img != null)
                        _bgImage = img;
                }
                if (bodyBgColor != null) {
                    Color color = ColorUtil.getColor(bodyBgColor, _customColors);
                    if (color != null) {
                        _bgColor = color;
                        _ui.debugMessage("setting the body bgcolor to " + color);
                    }
                }
            }
        });
    }
    
    private final void ts(String msg) { _ui.debugMessage(msg); }
    
    public ArrayList getImageIndexes() { return _imageIndexes; }
    public ArrayList getImages() { return _images; }
    public ArrayList getLinkEndIndexes() { return _linkIndexes; }
    public ArrayList getListItemIndexes() { return _listItemIndexes; }
    public ArrayList getFonts() {
        ArrayList rv = new ArrayList(_customFonts);
        rv.add(_fontA);
        rv.add(_fontCODE);
        rv.add(_fontDefault);
        rv.add(_fontH1);
        rv.add(_fontH2);
        rv.add(_fontH3);
        rv.add(_fontH4);
        rv.add(_fontH5);
        rv.add(_fontLI);
        rv.add(_fontP);
        rv.add(_fontPRE);
        return rv;
    }
    public ArrayList getCustomColors() { return new ArrayList(_customColors.values()); }
    
    private void insertCharBreakpoints(char placeholder, Map breakpoints, List indexes) {
        int start = 0;
        for (;;) {
            int index = _msgText.indexOf(placeholder, start);
            if (index == -1) {
                break;
            } else {
                Integer idx = new Integer(index);
                indexes.add(idx);
                List indexTags = (List)breakpoints.get(idx);
                if (indexTags == null) {
                    breakpoints.put(idx, new ArrayList());
                    //System.out.println("char breakpoint " + (int)placeholder + ": " + index + ", no tags @ breakpoint");
                } else {
                    //System.out.println("char breakpoint " + (int)placeholder + ": " + index + ", tags @ breakpoint: " + indexTags);
                }
                start = index+1;
            }
        }
    }
    
    private StyleRange buildStyle(List tags, int start, int length) {
        StringBuffer buf = new StringBuffer();
        buf.append("building style for [" + start + " through " + (start+length) + "]: ");
        for (int i = 0; i < tags.size(); i++)
            buf.append(((HTMLTag)tags.get(i)).name + " ");
        if (length > 0)
            buf.append("\t[" + _msgText.substring(start, start+length).trim() + "]");
        else
            buf.append("\n");
        //ts(buf.toString());
        
        return getStyle(start, length, tags);
    }
    
    public StyleRange[] getStyleRanges() { return _styleRanges; }
    
    static boolean containsTag(List tags, String tagName) {
        for (int i = 0; i < tags.size(); i++)
            if (((HTMLTag)tags.get(i)).name.equalsIgnoreCase(tagName))
                return true;
        return false;
    }
    
    private StyleRange getStyle(int start, int length, List tags) {
        // turn the given tags into a style
        StyleRange style = new StyleRange();
        style.start = start;
        style.length = length;
        if (containsTag(tags, "h1")) {
            style.font = _fontH1;
        } else if (containsTag(tags, "h2")) {
            style.font = _fontH2;
        } else if (containsTag(tags, "h3")) {
            style.font = _fontH3;
        } else if (containsTag(tags, "h4")) {
            style.font = _fontH4;
        } else if (containsTag(tags, "h5")) {
            style.font = _fontH5;
        } else if (containsTag(tags, "code")) {
            style.font = _fontCODE;
        } else if (containsTag(tags, "pre")) {
            style.font = _fontPRE;
        } else if (containsTag(tags, "li")) {
            style.font = _fontLI;
        } else if (containsTag(tags, "a")) {
            style.font = _fontA;
            style.underline = true;
        } else if (containsTag(tags, "p")) {
            style.font = _fontP;
        }
        
        // these two props (U and SO) are separate from the font, so no trouble
        if (containsTag(tags, "u"))
            style.underline = true;
        if (containsTag(tags, "so"))
            style.strikeout = true;
        
        if (style.font == null)
            style.font = _fontDefault;
        
        int customStyle = 0;
        if (containsTag(tags, "i"))
            customStyle |= SWT.ITALIC;
        if (containsTag(tags, "b") || containsTag(tags, "em"))
            customStyle |= SWT.BOLD;
        
        int sizeModifier = 0;
        // innermost font size is used
        for (int i = 0; i < tags.size(); i++) {
            HTMLTag tag = (HTMLTag)tags.get(i);
            if (tag.name.equalsIgnoreCase("font")) {
                String sz = tag.getAttribValue("size");
                if (sz != null) {
                    sz = sz.trim();
                    if (sz.startsWith("+") && (sz.length() > 1))
                        sz = sz.substring(1).trim();
                    try {
                        int offset = Integer.parseInt(sz);
                        sizeModifier += offset;
                        break;
                    } catch (NumberFormatException nfe) {
                        // ignore
                    }
                }
            }
        }
        
        String fontName = null;
        // innermost font name is used
        for (int i = 0; i < tags.size(); i++) {
            HTMLTag tag = (HTMLTag)tags.get(i);
            String name = null;
            if (tag.name.equalsIgnoreCase("font"))
                name = tag.getAttribValue("name");
            else
                name = tag.getAttribValue("font");
            if (name != null) {
                fontName = name.trim();
                break;
            }
        }
        
        Color bgColor = null;
        Color fgColor = null;
        // innermost bgcolor is used
        //System.out.println("Looking for a bgcolor in " + tags);
        for (int i = 0; (bgColor == null) && (i < tags.size()); i++) {
            HTMLTag tag = (HTMLTag)tags.get(i);
            bgColor = ColorUtil.getColor(tag.getAttribValue("bgcolor"), _customColors);
        }
        if ((bgColor == null) && (containsTag(tags, "quote"))) {
            bgColor = _bgColorQuote;
            fgColor = ColorUtil.getColor("black");
        }
        
        if (_styled && (bgColor != null))
            style.background = bgColor;
        
        // innermost fgcolor/color is used
        //System.out.println("Looking for a fgcolor in " + tags);
        for (int i = 0; i < tags.size(); i++) {
            HTMLTag tag = (HTMLTag)tags.get(i);
            String color = tag.getAttribValue("fgcolor");
            if (color == null)
                color = tag.getAttribValue("color");
            if (color == null)
                continue;
            
            fgColor = ColorUtil.getColor(color, _customColors);
            if (fgColor != null) break;
        }
        
        if (_styled && (fgColor != null))
            style.foreground = fgColor;
        //else
        //    style.foreground = ColorUtil.getColor("black");
        
        if ( (customStyle != 0) || (sizeModifier != 0) || (fontName != null) || (_viewSizeModifier != 0) ) {
            // ok, we can't use a default font, so lets construct a new one (or use a cached one)
            //style.font = 
            long before = System.currentTimeMillis();
            buildFont(style.font, customStyle, sizeModifier, fontName, style);
            long after = System.currentTimeMillis();
            //if (after-before > 10)
            //    ts("custom font took " + (after-before) + ": " + fontName + "/" + customStyle+"/"+sizeModifier+"/"+_viewSizeModifier);
        }
        
        // images are built later
        return style;
    }
    
    private void includeImage(final StyleRange style, final HTMLTag imgTag) {
        final SyndieURI srcURI = getURI(imgTag.getAttribValue("src"));
        if ( (imgTag == null) || (srcURI == null) || (!srcURI.isChannel()) || (srcURI.getAttachment() == null)) {
            _images.add(ImageUtil.ICON_IMAGE_UNKNOWN);
            return;
        }
        Display.getDefault().syncExec(new Runnable() { 
            public void run() {
                Image img = getImage(srcURI);
                if (img == null)
                    img = ImageUtil.ICON_IMAGE_UNKNOWN;
                int width = img.getBounds().width;
                int ascent = img.getBounds().height;
                _images.add(img);

                int descent = 0;
                style.metrics = new GlyphMetrics(ascent, 0, width);
                //style.background = _imgBGColor;
            }
        });
    }
    
    private Image getImage(SyndieURI imgURI) {
        Image img = null;
        if (imgURI != null) {
            Long attachmentId = imgURI.getLong("attachment");
            if (attachmentId != null) {
                // the attachment may not be from this message...
                Hash scope = imgURI.getScope();
                long scopeId = -1;
                Long msgId = imgURI.getMessageId();
                if ( (scope == null) || (msgId == null) ) {
                    // ok, yes, its implicitly from this message
                    scopeId = _msg.getScopeChannelId();
                    msgId = new Long(_msg.getMessageId());
                } else {
                    scopeId = _source.getChannelId(scope);
                }
        
                long internalMsgId = _source.getMessageId(scopeId, msgId.longValue());
                byte imgData[] = _source.getMessageAttachmentData(internalMsgId, attachmentId.intValue());
                if (imgData != null) {
                    img = ImageUtil.createImage(imgData);
                    //System.out.println("image attachment is valid [" + imgData.length + " bytes]");
                } else {
                    //System.out.println("image attachment is null (" + attachmentId + ")");
                }
            }
        }
        if (img == null)
            _ui.debugMessage("no image attachment for " + imgURI);
        return img;
    }
    
    private void includeLinkEnd(final StyleRange style, final HTMLTag aTag) {
        //SyndieURI targetURI = getURI(aTag.getAttribValue("href"));
        //if (targetURI == null) {
        //    System.out.println("no target uri in " + aTag);
        //    return;
        //}
        
        // perhaps use different icons depending upon who or what is being targetted?
        // e.g. the channel icon for channels or messages, a special archive icon for
        // archives, a special external link icon for generic URLs, a special icon
        // for links with keys on them, etc
        
        Display.getDefault().syncExec(new Runnable() { 
            public void run() {
                Image img = ImageUtil.ICON_LINK_END;
                int width = img.getBounds().width;
                int ascent = img.getBounds().height;
                Integer idx = new Integer(style.start);
                if (_imageIndexes.contains(idx))
                    throw new RuntimeException("wtf, already have an image at " + idx + ": " + _imageIndexes);
                _imageIndexes.add(idx);
                _images.add(img);

                int descent = 0;
                style.metrics = new GlyphMetrics(ascent, 0, width);
            }
        });
    }
    
    
    private SyndieURI getURI(String src) { return getURI(src, _msg); }
    static SyndieURI getURI(String src, MessageInfo scope) {
        if (src == null) return null;
        
        SyndieURI uri = null;
        try {
            return new SyndieURI(src);
        } catch (URISyntaxException use) {
            //System.out.println("parse error with [" + src + "]");
            //use.printStackTrace();
        }
        // not a syndie uri directly, lets try for some variants...
        if (src.startsWith("attachment") && (src.length() > "attachment".length())) {
            String attachmentNum = src.substring("attachment".length()).trim();
            try {
                int num = Integer.parseInt(attachmentNum);
                SyndieURI msgURI = scope.getURI();
                return SyndieURI.createAttachment(msgURI.getScope(), msgURI.getMessageId().longValue(), num);
            } catch (NumberFormatException nfe) {
                return null;
            }
        }
        if (src.startsWith("page") && (src.length() > "page".length())) {
            String pageNum = src.substring("page".length()).trim();
            try {
                int num = Integer.parseInt(pageNum);
                SyndieURI msgURI = scope.getURI();
                return SyndieURI.createMessage(msgURI.getScope(), msgURI.getMessageId().longValue(), num);
            } catch (NumberFormatException nfe) {
                return null;
            }
        }
        // not a valid syndie URI, nor is it a relative URI to a page or attachment.
        // treat 'er like a URL then
        return SyndieURI.createURL(src);
    }
    
    /**
     * get the client's font preferences. values should be in the form of
     * "fontname;sizeInPoints;(NORMAL|BOLD|ITALIC|BOLDITALIC)".
     */
    private Properties getFontConfig(PageRendererSource source) {
        Properties rv = new Properties();
        Properties defaultCfg = getDefaultConfig();
        rv.putAll(defaultCfg);
        // now override it with the nym's preferences
        // (aka create a table to store the gui prefs in, and pull 'em out)
        // ((or not, and just use syndie's theming))
        return rv;
    }
    
    private Properties getDefaultConfig() {
        Properties rv = new Properties();
        Theme theme = _source.getTheme();
        
        String face = Theme.getFace(theme.CONTENT_FONT);
        int size = Theme.getSize(theme.CONTENT_FONT);
        
        rv.setProperty("default", face + ';' + size + ";NORMAL");
        rv.setProperty("p", face + ';' + size + ";NORMAL");
        rv.setProperty("li", face + ';' + size + ";NORMAL");
        
        rv.setProperty("h1", face + ';' + (size+16) + ";BOLD");
        rv.setProperty("h2", face + ';' + (size+8) + ";BOLD");
        rv.setProperty("h3", face + ';' + size + ";BOLD");
        rv.setProperty("h4", face + ';' + Math.max(size-4, 6) + ";BOLD");
        rv.setProperty("h5", face + ';' + Math.max(size-8, 6) + ";BOLD");
        rv.setProperty("pre", "Courier;" + size + ";NORMAL");
        rv.setProperty("code", "Courier;" + size + ";NORMAL");
        
        face = Theme.getFace(theme.LINK_FONT);
        size = Theme.getSize(theme.LINK_FONT);
        rv.setProperty("a", face + ';' + size + ";NORMAL");
        
        return rv;
    }
    
    private void buildFonts(Properties fontConfig) {
        _fontDefault = buildFont(_fontDefault, fontConfig.getProperty("default"), null);
        _fontP = buildFont(_fontP, fontConfig.getProperty("p"), _fontDefault);
        _fontLI = buildFont(_fontLI, fontConfig.getProperty("li"), _fontDefault);
        _fontH1 = buildFont(_fontH1, fontConfig.getProperty("h1"), _fontDefault);
        _fontH2 = buildFont(_fontH2, fontConfig.getProperty("h2"), _fontDefault);
        _fontH3 = buildFont(_fontH3, fontConfig.getProperty("h3"), _fontDefault);
        _fontH4 = buildFont(_fontH4, fontConfig.getProperty("h4"), _fontDefault);
        _fontH5 = buildFont(_fontH5, fontConfig.getProperty("h5"), _fontDefault);
        _fontPRE = buildFont(_fontPRE, fontConfig.getProperty("pre"), _fontDefault);
        _fontCODE = buildFont(_fontCODE, fontConfig.getProperty("code"), _fontDefault);
        _fontA = buildFont(_fontA, fontConfig.getProperty("a"), _fontDefault);
    }
    
    private Font buildFont(Font oldFont, String config, Font defaultFont) {
        if ( (oldFont != null) && (!oldFont.isDisposed()) ) oldFont.dispose();
        if (config == null) return defaultFont;
        String cfg[] = Constants.split(';', config);
        if ( (cfg == null) || (cfg.length != 3) ) return defaultFont;
        String fontName = cfg[0].trim();
        int fontHeight = 12;
        try {
            fontHeight = Integer.parseInt(cfg[1]);
        } catch (NumberFormatException nfe) {
            // ignore
        }
        int fontStyle = SWT.NORMAL;
        String styleLower = Constants.lowercase(cfg[2]);
        if (styleLower.indexOf("bold") >= 0)
            fontStyle |= SWT.BOLD;
        if (styleLower.indexOf("italic") >= 0)
            fontStyle |= SWT.ITALIC;
        return new Font(Display.getDefault(), fontName, fontHeight, fontStyle);
    }
    
    private void buildFont(final Font oldFont, final int swtAttribs, final int sizeModifier, final String fontName, final StyleRange style) {
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                style.font = buildFont(oldFont, swtAttribs, sizeModifier, fontName);
            }
        });
    }
    
    /**
     * Build a font by adding the specified swt attributes to the given old font.
     * This does not adjust the old font in any way, and may return a cached value
     * from _customFonts
     */
    private Font buildFont(Font oldFont, int swtAttribs, int sizeModifier, String fontName) {
        FontData oldData[] = null;
        if (!oldFont.isDisposed()) {
            oldData = oldFont.getFontData();
        } else {
            throw new RuntimeException("old font is disposed: " + oldFont.toString());
        }
        FontData newData[] = new FontData[oldData.length];
        for (int i = 0; i < oldData.length; i++)
            newData[i] = new FontData((fontName != null ? fontName : oldData[i].getName()), getSize(oldData[i].getHeight(), sizeModifier), oldData[i].getStyle() | swtAttribs);
        Font rv = null;
        // search through _customFonts to see if we are already using this combination
        for (int i = 0; i < _customFonts.size(); i++) {
            Font f = (Font)_customFonts.get(i);
            FontData curData[] = f.getFontData();
            if (curData.length == newData.length) {
                boolean diffFound = false;;
                for (int j = 0; !diffFound && j < newData.length; j++) {
                    if (!curData[j].equals(newData[j]))
                        diffFound = true;
                }
                if (!diffFound) {
                    // w00t.  (font reuse)++
                    rv = f;
                    break;
                }
            }
        }
        if (rv == null) {
            long before = System.currentTimeMillis();
            rv = new Font(Display.getDefault(), newData);
            long after = System.currentTimeMillis();
            _ui.debugMessage("new custom font built: " + (after-before) + " [" + fontName + "/" + swtAttribs + "/" + sizeModifier);
            _customFonts.add(rv);
        }
        return rv;
    }
    
    private int getSize(int baseSize, int sizeModifier) {
        int rv = baseSize + _viewSizeModifier + 2*sizeModifier;
        //System.out.println("size: " + rv + " base: " + baseSize + " sizeMod: " + sizeModifier + " viewSizeMod: " + _viewSizeModifier);
        if (rv <= 4)
            rv = 4;
        if (rv >= 32)
            rv = 32;
        //System.out.println("new size: " + rv + " baseSize: " + baseSize + " mod: " + sizeModifier);
        return rv;
    }

    public static void main(String args[]) {
        test("<html><body>hi<br />how are you?</body></html>");
        test("<html><body>hi<br />how are you?</html>");
        test("hi<br />how are you?");
        test("a b  c   d\n\n\t\re");
        test("<!-- you can't see me -->now you can<br /><!-- -->la la la");
        test(HTMLStateBuilder.COMPREHENSIVE_TEST);
    }
    
    private static void test(String body) {
        HTMLStateBuilder b = new HTMLStateBuilder(body, -1);
        b.buildState();
        String text = b.getAsText();
        System.out.println("parsed: [" + body + "]");
        System.out.println("text: [" + text + "]");
        HTMLStyleBuilder sb = new HTMLStyleBuilder(new NullUI(), null, b.getTags(), text, null, true, true);
        try {
            sb.buildStyles();
        } catch (Exception e) {
            e.printStackTrace();
            try { Thread.sleep(3000); } catch (InterruptedException ie) {}
            throw new RuntimeException();
        }
        System.out.println("Image indexes: " + sb.getImageIndexes());
        System.out.println("Link end indexes: " + sb.getLinkEndIndexes());
        System.out.println("List item indexes: " + sb.getListItemIndexes());
        StyleRange ranges[] = sb.getStyleRanges();
        for (int i = 0; i < ranges.length; i++)
            System.out.println("range: " + ranges[i]);
    }
}
