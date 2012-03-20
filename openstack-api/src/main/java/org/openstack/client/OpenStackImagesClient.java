package org.openstack.client;

import java.util.Map;

import org.openstack.api.compute.TenantResource;
import org.openstack.api.imagestore.ImagesResource;
import org.openstack.model.exceptions.OpenstackException;
import org.openstack.model.identity.KeyStoneService;
import org.openstack.model.identity.KeyStoneServiceEndpoint;

import com.google.common.collect.Maps;

public class OpenStackImagesClient {
	
	private OpenStackClient client;
	
	private KeyStoneService service;
	
	private Map<String, KeyStoneServiceEndpoint> regions = Maps.newHashMap();

	public OpenStackImagesClient(OpenStackClient client, KeyStoneService service) {
		this.client = client;
		this.service = service;
		for(KeyStoneServiceEndpoint region : service.getEndpoints()) {
			this.regions.put(region.getRegion(), region);
		}
	}

	public ImagesResource publicEndpoint() throws OpenstackException {
		String defaultRegion = regions.keySet().iterator().next();
		return publicEndpoint(defaultRegion);
	}
	
	public ImagesResource publicEndpoint(String region) {
		return client.target(regions.get(region).getPublicURL(), ImagesResource.class);
	}
	
	public ImagesResource internalEndpoint() throws OpenstackException {
		String defaultRegion = regions.keySet().iterator().next();
		return internalEndpoint(defaultRegion);
	}
	
	public ImagesResource internalEndpoint(String region) {
		return client.target(regions.get(region).getInternalURL(), ImagesResource.class);
	}
	
	public ImagesResource administrationEndpoint() throws OpenstackException {
		String defaultRegion = regions.keySet().iterator().next();
		return administrationEndpoint(defaultRegion);
	}
	
	public ImagesResource administrationEndpoint(String region) {
		return client.target(regions.get(region).getAdminURL(), ImagesResource.class);
	}
	
}
