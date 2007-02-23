package syndie.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import net.i2p.data.Hash;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeItem;
import syndie.data.ReferenceNode;
import syndie.data.SyndieURI;
import syndie.db.ThreadAccumulator;
import syndie.db.ThreadAccumulatorJWZ;

/**
 * message tree that organizes threads first by forum, then by thread
 */
public class WatchedMessageTree extends MessageTree {
    public WatchedMessageTree(BrowserControl browser, Composite parent, MessageTreeListener lsnr) { this(browser, parent, lsnr, false); }
    public WatchedMessageTree(BrowserControl browser, Composite parent, MessageTreeListener lsnr, boolean hideFilter) {
        this(browser, parent, lsnr, true, true, true, true, hideFilter);
    }
    public WatchedMessageTree(BrowserControl browser, Composite parent, MessageTreeListener lsnr, boolean showAuthor, boolean showChannel, boolean showDate, boolean showTags, boolean hideFilter) {
        // don't show the forum column, don't show the flags column, don't expand anything by default
        super(browser, parent, lsnr, showAuthor, false, showDate, showTags, hideFilter, false, false, false);
    }
    
    /** given the list of thread roots, munge them into forums w/ threads underneath */
    void setMessages(List referenceNodes) {
        Map forumToNodeList = new HashMap();
        Map forumNameToForum = new TreeMap();
        Map forumToDesc = new HashMap();
        for (int i = 0; i < referenceNodes.size(); i++) {
            ReferenceNode node = (ReferenceNode)referenceNodes.get(i);
            Hash forum = getForum(node);
            if (forum == null) continue; // all dummies?
            List nodes = (List)forumToNodeList.get(forum);
            if (nodes == null) {
                nodes = new ArrayList();
                forumToNodeList.put(forum, nodes);
            }
            String name = _browser.getClient().getChannelName(forum);
            if ( (name == null) || (name.trim().length() <= 0) )
                name = forum.toBase64().substring(0,6);
            else
                name = name + " [" + forum.toBase64().substring(0,6) + "]";
            forumNameToForum.put(name, forum);
            forumToDesc.put(forum, name);
            nodes.add(node);
        }
        List forumNodes = new ArrayList();
        // sorted by forum name
        for (Iterator iter = forumNameToForum.values().iterator(); iter.hasNext(); ) {
            Hash forum = (Hash)iter.next();
            List nodes = (List)forumToNodeList.get(forum);
            String name = _browser.getClient().getChannelName(forum);
            String desc = (String)forumToDesc.get(forum);
            String type = nodes.size() + "";
            if (name == null)
                name = "";
            name = "(" + nodes.size() + ") " + name + " [" + forum.toBase64().substring(0,6) + "]";
            ReferenceNode node = new ReferenceNode(name, SyndieURI.createScope(forum), "", "");
            for (int i = 0; i < nodes.size(); i++)
                node.addChild((ReferenceNode)nodes.get(i));
            forumNodes.add(node);
        }
        super.setMessages(forumNodes);
    }
    
    private Hash getForum(ReferenceNode node) {
        SyndieURI uri = node.getURI();
        if (uri != null) {
            long msgId = _browser.getClient().getMessageId(uri.getScope(), uri.getMessageId());
            if (msgId >= 0) {
                long scopeId = _browser.getClient().getMessageTarget(msgId);
                Hash scope = _browser.getClient().getChannelHash(scopeId);
                if (scope != null)
                    return scope;
            }
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            Hash forum = getForum(node.getChild(i));
            if (forum != null)
                return forum;
        }
        return null;
    }
    
    protected void markUnreadChild(TreeItem item) {
        if (item.getParentItem() != null) // don't mark the forum nodes
            super.markUnreadChild(item);
    }
    
    protected long markAllRead(TreeItem item) {
        if (item.getParentItem() == null) {
            ReferenceNode node = (ReferenceNode)_itemToNode.get(item);
            if ( (node != null) && (node.getURI() != null) ) {
                Hash scope = node.getURI().getScope();
                if (scope != null) {
                    long target = _browser.getClient().getChannelId(scope);
                    _browser.getClient().markChannelRead(target);
                    return target;
                }
            }
            return -1;
        } else {
            return super.markAllRead(item);
        }
    }
    
    protected int getMessageSelectedCount() {
        TreeItem sel[] = _tree.getSelection();
        int rv = 0;
        for (int i = 0; i < sel.length; i++) {
            if (sel[i].getParentItem() != null)
                rv++;
        }
        return rv;
    }
    
    protected TreeItem getThreadRoot(TreeItem item) {
        TreeItem root = item;
        while ( (root.getParentItem() != null) && (root.getParentItem().getParentItem() != null) )
            root = root.getParentItem();
        return root;
    }
    
    protected long renderNode(ReferenceNode node, TreeItem item) {
        if ( (node != null) && (node.getURI() != null) && (node.getURI().getMessageId() == null) )
            return renderForumNode(node, item);
        else
            return super.renderNode(node, item);
    }
    
    private long renderForumNode(ReferenceNode node, TreeItem item) {
        String title = node.getName();
        item.setText(0, title);
        setMinWidth(_colSubject, title, 0, 100);
        return 0;
    }
       
    protected BookmarkDnD getBookmark(TreeItem item, ReferenceNode node) {
        if ( (node != null) && (node.getURI() != null) && (node.getURI().getMessageId() == null) ) {
            Hash forum = getForum(node);
            if (forum == null) return null;
            String name = _browser.getClient().getChannelName(forum);
            if (name == null) name = "";
            
            SyndieURI uri = SyndieURI.createScope(forum);
            BookmarkDnD bookmark = new BookmarkDnD();
            bookmark.uri = uri;
            bookmark.name = name;
            bookmark.desc = forum.toBase64();
            return bookmark;
        } else {
            return super.getBookmark(item, node);
        }
    }    
}
