package com.atex.plugins.podcast;

import java.util.logging.Level;

import com.atex.plugins.baseline.policy.BaselinePolicy;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.collections.ContentList;
import com.polopoly.metadata.Metadata;
import com.polopoly.metadata.MetadataAware;
import com.polopoly.metadata.util.MetadataUtil;
import com.polopoly.model.DescribesModelType;

/**
 * PodcastFeedPolicy
 *
 * @author mnova
 */
@DescribesModelType
public class PodcastPolicy extends BaselinePolicy implements MetadataAware {

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
