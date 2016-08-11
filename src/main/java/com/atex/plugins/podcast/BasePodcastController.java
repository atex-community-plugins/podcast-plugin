package com.atex.plugins.podcast;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.atex.onecms.content.Content;
import com.atex.onecms.content.ContentManager;
import com.atex.onecms.content.ContentResult;
import com.atex.onecms.content.ContentVersionId;
import com.atex.onecms.content.IdUtil;
import com.atex.onecms.content.Subject;
import com.atex.onecms.image.ImageInfoAspectBean;
import com.atex.onecms.ws.image.ImageServiceConfigurationProvider;
import com.atex.onecms.ws.image.ImageServiceUrlBuilder;
import com.atex.plugins.podcast.util.DomainBuilder;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.model.ModelPathUtil;
import com.polopoly.render.CacheInfo;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;
import com.polopoly.siteengine.mvc.RenderControllerBase;

/**
 * Base podcast controller
 *
 * @author mnova
 */
public abstract class BasePodcastController extends RenderControllerBase {

    private static final Logger LOGGER = Logger.getLogger(BasePodcastController.class.getName());

    private static final String imageFormatName = "default";

    @Override
    public void populateModelAfterCacheKey(final RenderRequest request, final TopModel m, final CacheInfo cacheInfo, final ControllerContext context) {
        super.populateModelAfterCacheKey(request, m, cacheInfo, context);

        final URI uri = new DomainBuilder()
                .setTopModel(m)
                .setRequest((HttpServletRequest) request)
                .build();
        final String domain = (uri != null ? uri.toASCIIString() : "");
        if (uri != null) {
            ModelPathUtil.set(m.getLocal(), "domain", domain);
        }
    }

    protected void populateModelWithImage(final ContentId imageId, final TopModel topModel,
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
