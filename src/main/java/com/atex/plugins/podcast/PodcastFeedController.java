package com.atex.plugins.podcast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Logger;

import com.polopoly.cm.ContentId;
import com.polopoly.model.ModelPathUtil;
import com.polopoly.render.CacheInfo;
import com.polopoly.render.RenderException;
import com.polopoly.render.RenderRequest;
import com.polopoly.render.RenderResponse;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;
import com.polopoly.siteengine.mvc.Renderer;
import com.polopoly.util.StringUtil;

/**
 * Podcast feed render controller, it will be called when rendering the rss feed and also for
 * the rendering in page.
 *
 * @author mnova
 */
public class PodcastFeedController extends BasePodcastController {

    private static final Logger LOGGER = Logger.getLogger(PodcastFeedController.class.getName());

    private static final TimeZone standardTimeZone = TimeZone.getTimeZone("UTC Universal");

    private final SimpleDateFormat rssDateFormatter = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH);

    public PodcastFeedController() {
        super();
        rssDateFormatter.setTimeZone(standardTimeZone);
    }

    @Override
    public void populateModelAfterCacheKey(final RenderRequest request, final TopModel m, final CacheInfo cacheInfo, final ControllerContext context) {

        super.populateModelAfterCacheKey(request, m, cacheInfo, context);

        if (isRssFeed(request)) {
            ModelPathUtil.set(m.getLocal(), "rssFeed", true);
        }

        // matching xml from https://www.seriouslysimplepodcasting.com/documentation/podcast-rss-feed-contents/
        final PodcastFeedPolicy podcastPolicy = (PodcastFeedPolicy) ModelPathUtil.getBean(context.getContentModel());
        final Date lastBuildDate = new Date();

        // Add last build date
        ModelPathUtil.set(m.getLocal(), "lastBuildDate", rssDateFormatter.format(lastBuildDate));

        // Add the image of the feed
        final ContentId imageId = podcastPolicy.getImageId();
        if (imageId != null) {
            populateModelWithImage(imageId, m, context);
        }
    }

    @Override
    public Renderer getRenderer(final RenderRequest request, final TopModel m, final Renderer defaultRenderer,
                                final ControllerContext context) {

        // set the content type only if we are dealing with an rss feed.

        if (isRssFeed(request)) {
            return new Renderer() {
                @Override
                public void render(final TopModel m, final RenderRequest req, final RenderResponse resp,
                                   final CacheInfo cacheInfo, final ControllerContext context)
                        throws RenderException {

                    resp.setContentType("application/xml");
                    defaultRenderer.render(m, req, resp, cacheInfo, context);

                }

            };
        } else {
            return defaultRenderer;
        }
    }

    private boolean isRssFeed(final RenderRequest request) {
        return StringUtil.equalsIgnoreCase(request.getParameter("rss"), "true");
    }

}
