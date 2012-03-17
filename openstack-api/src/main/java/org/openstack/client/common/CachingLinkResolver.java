package org.openstack.client.common;

import java.util.List;

import org.openstack.model.atom.Link;
import org.openstack.model.compute.NovaFlavor;
import org.openstack.model.compute.NovaImage;
import org.openstack.model.exceptions.OpenstackException;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

/**
 * Resolves links with caching; typically by fetching the entire collection in one request
 */
public class CachingLinkResolver extends SimpleLinkResolver {

	private List<NovaFlavor> flavorCache;
	private List<NovaImage> imageCache;

	public CachingLinkResolver(OpenStackSession session) {
		super(session);
	}

	@Override
	public NovaFlavor resolveFlavor(String flavorId, List<Link> links) {
		if (flavorCache == null) {
			flavorCache = Lists.newArrayList(session.getComputeClient().root().flavors().list(true).getList());
		}

		for (NovaFlavor flavor : flavorCache) {
			if (Objects.equal(flavor.getId(), flavorId))
				return flavor;
		}

		return null;
	}

	@Override
	public NovaImage resolveImage(String imageId, List<Link> links) throws OpenstackException {
		if (imageCache == null) {
			imageCache = Lists.newArrayList(session.getComputeClient().root().images().list(true).getList());
		}

		for (NovaImage image : imageCache) {
			if (Objects.equal(image.getId(), imageId))
				return image;
		}

		return null;
	}

}
