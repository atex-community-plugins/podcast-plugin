package com.atex.plugins.podcast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.atex.onecms.content.Content;
import com.atex.onecms.content.ContentManager;
import com.atex.onecms.content.ContentResult;
import com.atex.onecms.content.ContentVersionId;
import com.atex.onecms.content.IdUtil;
import com.atex.onecms.content.Subject;
import com.atex.onecms.image.ImageInfoAspectBean;
import com.atex.onecms.ws.image.ImageServiceConfigurationProvider;
import com.atex.onecms.ws.image.ImageServiceUrlBuilder;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.model.Model;
import com.polopoly.model.ModelPathUtil;
import com.polopoly.render.CacheInfo;
import com.polopoly.render.RenderException;
import com.polopoly.render.RenderRequest;
import com.polopoly.render.RenderResponse;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;
import com.polopoly.siteengine.mvc.RenderControllerBase;
import com.polopoly.siteengine.mvc.Renderer;

/**
 * Podcast feed render controller
 *
 * @author mnova
 */
public class PodcastFeedController extends RenderControllerBase {

    private static final Logger LOGGER = Logger.getLogger(PodcastFeedController.class.getName());

    private static final String imageFormatName = "default";

    private static final TimeZone standardTimeZone = TimeZone.getTimeZone("UTC Universal");

    private final SimpleDateFormat rssDateFormatter = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH);

    public PodcastFeedController() {
        super();
        rssDateFormatter.setTimeZone(standardTimeZone);
    }

    @Override
    public void populateModelAfterCacheKey(final RenderRequest request, final TopModel m, final CacheInfo cacheInfo, final ControllerContext context) {

        super.populateModelAfterCacheKey(request, m, cacheInfo, context);

        final Model thisModel = context.getContentModel();
        final Model localModel = m.getLocal();

        // matching xml from https://www.seriouslysimplepodcasting.com/documentation/podcast-rss-feed-contents/
        final PodcastPolicy podcastPolicy = (PodcastPolicy) ModelPathUtil.getBean(thisModel);
        final Date lastBuildDate = new Date();

        // Add last build date
        ModelPathUtil.set(localModel, "lastBuildDate", rssDateFormatter.format(lastBuildDate));

        // Add the image of the feed
        final ContentId imageId = podcastPolicy.getImageId();
        if (imageId != null) {
            populateModelWithImage(imageId, m, context);
        }
    }

    @Override
    public Renderer getRenderer(final RenderRequest request, final TopModel m, final Renderer defaultRenderer,
                                final ControllerContext context) {

        return new Renderer() {
            @Override
            public void render(final TopModel m, final RenderRequest req, final RenderResponse resp,
                               final CacheInfo cacheInfo, final ControllerContext context)
                    throws RenderException {

                resp.setContentType("application/rss+xml");
                defaultRenderer.render(m, req, resp, cacheInfo, context);

            }

        };
    }

    protected void populateModelWithImage(final com.polopoly.cm.ContentId imageId, final TopModel topModel,
                                          final ControllerContext context) {

        if (imageId == null) {
            return;
        }


        final ContentManager contentManager = getContentManager(context);
        final com.atex.onecms.content.ContentId contentId = IdUtil.fromPolicyContentId(imageId);
        final ContentVersionId versionId = contentManager.resolve(contentId, Subject.NOBODY_CALLER);
        ContentResult<?> result =
                contentManager.get(versionId, null, null, null, Subject.NOBODY_CALLER);
        Content<?> content = result.getContent();
        ImageInfoAspectBean imageInfo = (ImageInfoAspectBean) content.getAspectData(ImageInfoAspectBean.ASPECT_NAME);

        if (imageInfo != null && imageInfo.getFilePath() != null) {
            String secret = getSecret(getCmClient(context));
            ImageServiceUrlBuilder urlBuilder = new ImageServiceUrlBuilder(result, secret).format(imageFormatName);
            topModel.getLocal().setAttribute("hasImage", true);
            topModel.getLocal().setAttribute("urlBuilder", urlBuilder);
        } else {
            LOGGER.log(Level.WARNING, "Content with id " + content.getId()
                    + " does not have a valid image info aspect.");
        }
    }

    /**
     * Get the secret key used to authenticate requests to the image service.
     *
     * @param cmClient The cmClient.
     * @return The secret key.
     */
    protected String getSecret(final CmClient cmClient) {
        try {
            return new ImageServiceConfigurationProvider(
                    cmClient.getPolicyCMServer()).getImageServiceConfiguration().getSecret();
        } catch (CMException e) {
            throw new RuntimeException("Secret not found", e);
        }
    }

}
