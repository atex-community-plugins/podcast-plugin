package com.atex.plugins.podcast;

import java.util.logging.Logger;

import com.polopoly.cm.ContentId;
import com.polopoly.model.ModelPathUtil;
import com.polopoly.render.CacheInfo;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;

/**
 * Podcast feed render controller, it will be called when rendering the rss feed and also for
 * the rendering in page.
 *
 * @author mnova
 */
public class PodcastController extends BasePodcastController {

    private static final Logger LOGGER = Logger.getLogger(PodcastController.class.getName());

    @Override
    public void populateModelAfterCacheKey(final RenderRequest request, final TopModel m, final CacheInfo cacheInfo, final ControllerContext context) {

        super.populateModelAfterCacheKey(request, m, cacheInfo, context);

        if (isRssFeed(request)) {
            ModelPathUtil.set(m.getLocal(), "rssFeed", true);
        }
        
        final PodcastPolicy podcastPolicy = (PodcastPolicy) ModelPathUtil.getBean(context.getContentModel());

        // Add the image of the feed
        final ContentId imageId = podcastPolicy.getImageId();
        if (imageId != null) {
            populateModelWithImage(imageId, m, context);
        }
    }

}
