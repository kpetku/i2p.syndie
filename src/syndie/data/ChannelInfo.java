package syndie.data;

import java.util.*;

import net.i2p.data.Base64;
import net.i2p.data.DataHelper;
import net.i2p.data.Hash;
import net.i2p.data.PublicKey;
import net.i2p.data.SessionKey;
import net.i2p.data.Signature;
import net.i2p.data.SigningPublicKey;

/**
 *  Internal representation of a channel.
 *  Does not include private keys.
 */
public class ChannelInfo {
    private long _channelId;
    private Hash _channelHash;
    private SigningPublicKey _identKey;
    private PublicKey _encryptKey;
    private long _edition;
    private String _name;
    private String _description;
    private boolean _allowPublicPosts;
    private boolean _allowPublicReplies;
    private long _expiration;
    private long _receiveDate;
    /** set of Strings that anyone can know about the channel */
    private Set<String> _publicTags;
    /** set of Strings only authorized people can see */
    private Set<String> _privateTags;
    /** set of SigningPublicKeys that are allowed to sign posts to the channel */
    private Set<SigningPublicKey> _authorizedPosters;
    private Set<Hash> _authorizedPosterHashes;
    /** set of SigningPublicKeys that are allowed to sign metadata posts for the channel */
    private Set<SigningPublicKey> _authorizedManagers;
    private Set<Hash> _authorizedManagerHashes;
    /** set of ArchiveInfo instances that anyone can see to get more posts */
    private Set<ArchiveInfo> _publicArchives;
    /** set of ArchiveInfo instances that only authorized people can see to get more posts */
    private Set<ArchiveInfo> _privateArchives;
    /** set of SessionKey instances that posts can be encrypted with */
    private Set<SessionKey> _readKeys;
    private boolean _readKeysArePublic;
    /** publicly visible headers delivered with the metadata */
    private Properties _publicHeaders;
    /** privately visible headers delivered with the metadata */
    private Properties _privateHeaders;
    /** list of ReferenceNode instances that the channel refers to */
    private List<ReferenceNode> _references;
    private boolean _readKeyUnknown;
    private String _passphrasePrompt;
    
    public ChannelInfo() {
        _channelId = -1;
        _edition = -1;
        _expiration = -1;
        _receiveDate = -1;
        _publicTags = Collections.EMPTY_SET;
        _privateTags = Collections.EMPTY_SET;
        _authorizedPosters = Collections.EMPTY_SET;
        _authorizedPosterHashes = Collections.EMPTY_SET;
        _authorizedManagers = Collections.EMPTY_SET;
        _authorizedManagerHashes = Collections.EMPTY_SET;
        _publicArchives = Collections.EMPTY_SET;
        _privateArchives = Collections.EMPTY_SET;
        _readKeys = Collections.EMPTY_SET;
        _readKeysArePublic = true;
        _publicHeaders = new Properties();
        _privateHeaders = new Properties();
        _references = Collections.EMPTY_LIST;
    }
    
    public long getChannelId() { return _channelId; }
    public void setChannelId(long id) { _channelId = id; }
    public Hash getChannelHash() { return _channelHash; }
    public void setChannelHash(Hash hash) { _channelHash = hash; }
    public SigningPublicKey getIdentKey() { return _identKey; }
    public void setIdentKey(SigningPublicKey key) { _identKey = key; }
    public PublicKey getEncryptKey() { return _encryptKey; }
    public void setEncryptKey(PublicKey key) { _encryptKey = key; }
    public long getEdition() { return _edition; }
    public void setEdition(long edition) { _edition = edition; }
    public long getReceiveDate() { return _receiveDate; }
    public void setReceiveDate(long when) { _receiveDate = when; }
    public String getName() { return _name; }
    public void setName(String name) { _name = name; }
    public String getDescription() { return _description; }
    public void setDescription(String desc) { _description = desc; }
    public boolean getAllowPublicPosts() { return _allowPublicPosts; }
    public void setAllowPublicPosts(boolean val) { _allowPublicPosts = val; }
    public boolean getAllowPublicReplies() { return _allowPublicReplies; }
    public void setAllowPublicReplies(boolean val) { _allowPublicReplies = val; }
    public long getExpiration() { return _expiration; }
    public void setExpiration(long when) { _expiration = when; }

    /** set of Strings that anyone can know about the channel */
    public Set<String> getPublicTags() { return _publicTags; }
    public void setPublicTags(Set<String> tags) { _publicTags = tags; }

    /** set of Strings only authorized people can see */
    public Set<String> getPrivateTags() { return _privateTags; }
    public void setPrivateTags(Set<String> tags) { _privateTags = tags; }

    /** set of SigningPublicKeys that are allowed to sign posts to the channel */
    public Set<SigningPublicKey> getAuthorizedPosters() { return _authorizedPosters; }
    public Set<Hash> getAuthorizedPosterHashes() { return _authorizedPosterHashes; }
    public void setAuthorizedPosters(Set<SigningPublicKey> who) { _authorizedPosters = who; _authorizedPosterHashes = hash(who); }

    /** set of SigningPublicKeys that are allowed to sign metadata posts for the channel */
    public Set<SigningPublicKey> getAuthorizedManagers() { return _authorizedManagers; }
    public Set<Hash> getAuthorizedManagerHashes() { return _authorizedManagerHashes; }
    public void setAuthorizedManagers(Set<SigningPublicKey> who) { _authorizedManagers = who; _authorizedManagerHashes = hash(who); }

    /** set of ArchiveInfo instances that anyone can see to get more posts */
    public Set<ArchiveInfo> getPublicArchives() { return _publicArchives; }
    public void setPublicArchives(Set<ArchiveInfo> where) { _publicArchives = where; }

    /** set of ArchiveInfo instances that only authorized people can see to get more posts */
    public Set<ArchiveInfo> getPrivateArchives() { return _privateArchives; }
    public void setPrivateArchives(Set<ArchiveInfo> where) { _privateArchives = where; }

    /** set of SessionKey instances that posts can be encrypted with */
    public Set<SessionKey> getReadKeys() { return _readKeys; }
    public void setReadKeys(Set<SessionKey> keys) { _readKeys = keys; }

    /** if true, the current read keys are/were publicly readable */
    public boolean getReadKeysArePublic() { return _readKeysArePublic; }
    public void setReadKeysArePublic(boolean pub) { _readKeysArePublic = pub; }

    /** publicly visible headers delivered with the metadata */
    public Properties getPublicHeaders() { return _publicHeaders; }
    public void setPublicHeaders(Properties headers) { _publicHeaders = headers; }

    /** privately visible headers delivered with the metadata */
    public Properties getPrivateHeaders() { return _privateHeaders; }
    public void setPrivateHeaders(Properties props) { _privateHeaders = props; }

    /** list of ReferenceNode instances that the channel refers to */
    public List<ReferenceNode> getReferences() { return _references; }
    public void setReferences(List refs) { _references = refs; }

    public boolean getReadKeyUnknown() { return _readKeyUnknown; }
    public void setReadKeyUnknown(boolean unknown) { _readKeyUnknown = unknown; }
    public String getPassphrasePrompt() { return _passphrasePrompt; }
    public void setPassphrasePrompt(String prompt) { _passphrasePrompt = prompt; }
    
    private Set<Hash> hash(Set<SigningPublicKey> keys) {
        if (keys.size() == 0)
            return Collections.EMPTY_SET;
        Set<Hash> rv = new HashSet<Hash>(keys.size());
        for (SigningPublicKey pub : keys) {
            rv.add(pub.calculateHash());
        }
        return rv;
    }
    
    public boolean equals(Object obj) { return (obj instanceof ChannelInfo) ? ((ChannelInfo)obj)._channelId == _channelId : false; }

    public int hashCode() { return (int)_channelId; }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        if (_channelHash == null)
            buf.append("Channel not yet defined (edition " + _edition + ")\n");
        else
            buf.append("Channel " + _channelHash.toBase64() + " (ID " + _channelId + " edition " + _edition + ")\n");
        if (_encryptKey == null)
            buf.append("Replies should be encrypted to a key not yet determined\n");
        else
            buf.append("Replies should be encrypted to " + _encryptKey.calculateHash().toBase64() + " / " + _encryptKey.toBase64() + "\n");
        if (_name == null)
            buf.append("Suggested name: not yet determined\n");
        else
            buf.append("Suggested name: " + _name + "\n");
        if (_description == null)
            buf.append("Suggested description: not yet determined\n");
        else
            buf.append("Suggested description: " + _description + "\n");
        if (_expiration <= 0)
            buf.append("Channel expiration: never\n");
        else
            buf.append("Channel expiration: " + new Date(_expiration) + "\n");
        buf.append("Allow anyone to post new threads? " + _allowPublicPosts + "\n");
        buf.append("Allow anyone to post replies to existing threads? " + _allowPublicReplies + "\n");
        buf.append("Publicly known tags: " + _publicTags + "\n");
        buf.append("Hidden tags: " + _privateTags + "\n");
        
        buf.append("Allow posts by: ");
        for (SigningPublicKey key : _authorizedPosters) {
            buf.append(key.calculateHash().toBase64()).append(", ");
        }
        // managers can post too
        for (SigningPublicKey key : _authorizedManagers) {
            buf.append(key.calculateHash().toBase64()).append(", ");
        }
        if (_channelHash != null)
            buf.append(_channelHash.toBase64());
        else
            buf.append("the channel identity");
        buf.append("\n");
        
        buf.append("Allow management by: ");
        for (SigningPublicKey key : _authorizedManagers) {
            buf.append(key.calculateHash().toBase64()).append(", ");
        }
        if (_channelHash != null)
            buf.append(_channelHash.toBase64());
        else
            buf.append("the channel identity");
        buf.append("\n");
        if ( (_publicArchives != null) && (_publicArchives.size() > 0) ) {
            buf.append("Publicly known channel archives: \n");
            for (ArchiveInfo archive : _publicArchives) {
                buf.append('\t').append(archive).append('\n');
            }
        }
        if ( (_privateArchives != null) && (_privateArchives.size() > 0) ) {
            buf.append("Hidden channel archives: \n");
            for (ArchiveInfo archive : _privateArchives) {
                buf.append('\t').append(archive).append('\n');
            }
        }
        if (_readKeys != null)
            buf.append("Known channel read keys: " + _readKeys.size() + "\n");
        
        Set<String> headers = new TreeSet<String>();
        if (_publicHeaders != null)
            headers.addAll(_publicHeaders.stringPropertyNames());
        if (_privateHeaders != null)
            headers.addAll(_privateHeaders.stringPropertyNames());
        if (headers.size() > 0) {
            buf.append("Metadata headers:\n");
            for (String name : headers) {
                boolean isPublic = false;
                String val = null;
                if (_privateHeaders != null)
                    val = _privateHeaders.getProperty(name);
                if (val != null) {
                    isPublic = false;
                } else {
                    isPublic = true;
                    val = _publicHeaders.getProperty(name);
                }
                buf.append("\t");
                if (isPublic)
                    buf.append("+");
                else
                    buf.append("-");
                buf.append(name).append(":\t").append(val).append("\n");
            }
            buf.append("(hidden headers prepended with -, public headers prepended with +)\n");
        }
        if (_references.size() > 0) {
            String refs = ReferenceNode.walk(_references);
            buf.append("References: \n");
            buf.append(refs);
            buf.append("\n");
        }
        return buf.toString();
    }
}
