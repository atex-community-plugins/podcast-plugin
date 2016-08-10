package com.atex.plugins.podcast;

import java.util.logging.Level;

import com.atex.onecms.content.metadata.MetadataInfo;
import com.atex.plugins.baseline.policy.BaselinePolicy;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.collections.ContentList;
import com.polopoly.cm.collections.ContentListProvider;
import com.polopoly.cm.collections.ContentListUtil;
import com.polopoly.metadata.Metadata;
import com.polopoly.metadata.MetadataAware;
import com.polopoly.metadata.util.MetadataUtil;
import com.polopoly.model.DescribesModelType;

/**
 * PodcastPolicy
 *
 * @author mnova
 */
@DescribesModelType
public class PodcastPolicy extends BaselinePolicy implements MetadataAware {

    // Matching defaults in input template
    private static final String DEFAULT_NUMBER_OF_ITEMS = "10";

    public int getNumberOfItems() {
        return Math.min(Integer.parseInt(getChildValue("numberOfItems", DEFAULT_NUMBER_OF_ITEMS)),
                (int) (double) getDefaultList().size());
    }

    public ContentList getDefaultList() {
        ContentList list = ContentListUtil.EMPTY_CONTENT_LIST;
        try {
            ContentList queue = getContentList("publishingQueue");
            if (queue.size() > 0) {
                ContentId queueId = queue.getEntry(0).getReferredContentId();
                ContentListProvider queuePolicy = (ContentListProvider) getCMServer().getPolicy(queueId);
                list = queuePolicy.getContentList();
            }
        } catch (CMException e) {
            logger.log(Level.WARNING, "Unable to get content list publishingQueue", e);
        }
        return list;
    }

    public ContentId getImageId() {
        try {
            ContentList images = getContentList("images");
            if (images.size() > 0) {
                ContentId imageId = images.getEntry(0).getReferredContentId();
                if (imageId != null) {
                    return imageId;
                }
            }
        } catch (CMException e) {
            logger.log(Level.WARNING, "Unable to get content list images", e);
        }
        return null;
    }

    private MetadataAware getMetadataAware() {
        return MetadataUtil.getMetadataAware(this);
    }

    @Override
    public Metadata getMetadata() {
        return getMetadataAware().getMetadata();
    }

    @Override
    public void setMetadata(final Metadata metadata) {
        getMetadataAware().setMetadata(metadata);
    }
}
