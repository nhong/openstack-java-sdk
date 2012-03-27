package org.openstack.client;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Target;
import javax.ws.rs.ext.FilterContext;
import javax.ws.rs.ext.RequestFilter;

import org.glassfish.jersey.filter.LoggingFilter;
import org.openstack.api.common.Resource;
import org.openstack.api.common.RestClient;
import org.openstack.api.compute.TenantResource;
import org.openstack.api.identity.IdentityAdministrationEndpoint;
import org.openstack.api.identity.IdentityInternalEndpoint;
import org.openstack.api.identity.IdentityPublicEndpoint;
import org.openstack.api.images.ImagesResource;
import org.openstack.api.storage.AccountResource;
import org.openstack.model.exceptions.OpenstackException;
import org.openstack.model.identity.Access;
import org.openstack.model.identity.Authentication;
import org.openstack.model.identity.Service;
import org.openstack.model.identity.ServiceEndpoint;
import org.openstack.model.identity.keystone.KeystoneAuthentication;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class OpenStackClient {

	private Client client;
	
	//private LoggingFilter loggingFilter = new LoggingFilter(Logger.getLogger(OpenStackClient.class.getPackage().getName()),true);

	private LoggingFilter loggingFilter = new LoggingFilter();
	
	private Properties properties;

	private Access access;

	private RequestFilter authFilter = new RequestFilter() {

		@Override
		public void preFilter(FilterContext context) throws IOException {
			context.getRequestBuilder().header("X-Auth-Token", access.getToken().getId());

		}

	};
	
	private RequestFilter authAsAdministratorFilter = new RequestFilter() {

		@Override
		public void preFilter(FilterContext context) throws IOException {
			context.getRequestBuilder().header("X-Auth-Token", properties.get("identity.admin.token"));

		}

	};

	OpenStackClient(Properties properties, Access access) {
		this.client = RestClient.INSTANCE.getJerseyClient();
		this.properties = properties;
		this.access = access;
	}

	public Access getAccess() {
		return access;
	}
	
	public Properties getProperties() {
		return this.properties;
	}

	public void setAccess(Access access) {
		this.access = access;
	}

	public void exchangeTokenForTenant(String tenantId) {
		String endpoint = properties.getProperty("identity.endpoint.publicURL");
		Authentication authentication = new KeystoneAuthentication().withTokenAndTenant(access.getToken().getId(), tenantId);
		Access access = target(endpoint, IdentityPublicEndpoint.class).tokens().post(authentication);
		setAccess(access);
	}
	
	public IdentityPublicEndpoint getIdentityEndpoint() {
		String url = properties.getProperty("identity.endpoint.publicURL");
		Preconditions.checkNotNull(url, "'identity.endpoint.publicURL' property not found");
		return target(url, IdentityPublicEndpoint.class);
	}
	
	public IdentityInternalEndpoint getIdentityInternalEndpoint() {
		String url = properties.getProperty("identity.endpoint.public");
		Preconditions.checkNotNull(url, "'identity.endpoint.internalURL' property not found");
		return target(url, IdentityInternalEndpoint.class);
	}

	public IdentityAdministrationEndpoint getIdentityAdministationEndpoint() {
		String url = properties.getProperty("identity.endpoint.adminURL");
		Preconditions.checkNotNull(url, "'identity.endpoint.adminURL' property not found");
		return target(url, IdentityAdministrationEndpoint.class, true);
	}
	
	public TenantResource getComputeEndpoint() {
		return target(getEndpoint("compute", null).getPublicURL(), TenantResource.class);
	}
	
	public TenantResource getComputeInternalEndpoint() {
		return target(getEndpoint("compute", null).getInternalURL(), TenantResource.class);
	}

	public TenantResource getComputeAdministationEndpoint() {
		return target(getEndpoint("compute", null).getAdminURL(), TenantResource.class);
	}
	
	public ImagesResource getImagesEndpoint() {
		return target(getEndpoint("image", null).getPublicURL().concat("/images"), ImagesResource.class);
	}
	
	public ImagesResource getImagesInternalEndpoint() {
		return target(getEndpoint("image", null).getAdminURL().concat("/images"), ImagesResource.class);
	}

	public ImagesResource getImagesAdministationEndpoint() {
		return target(getEndpoint("image", null).getAdminURL().concat("/images"), ImagesResource.class);
	}
	
	public AccountResource getStorageEndpoint() {
		return target(getEndpoint("object-store", null).getPublicURL(), AccountResource.class);
	}
	
	public AccountResource getStorageInternalEndpoint() {
		return target(getEndpoint("object-store", null).getInternalURL(), AccountResource.class);
	}

	public AccountResource getStorageAdministationEndpoint() {
		return target(getEndpoint("object-store", null).getAdminURL(), AccountResource.class);
	}
	
	public <T extends Resource> T target(String absoluteURL, Class<T> clazz) {
		return target(absoluteURL, clazz, false);
	}

	private <T extends Resource> T target(String absoluteURL, Class<T> clazz, boolean useAdministrationToken) {
		try {
			Target target = client.target(absoluteURL);
			if(Boolean.parseBoolean(properties.getProperty("verbose"))) {
				target.configuration().register(loggingFilter);
			}
			if (access != null) {
				target.configuration().register(useAdministrationToken ? authAsAdministratorFilter : authFilter);
			}
			return clazz.getConstructor(Target.class).newInstance(target);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}

	}

	private ServiceEndpoint getEndpoint(final String type, final String region) {
		Preconditions.checkNotNull(access, "You must be authenticated before get a identity client");
		try {
			Service service = Iterables.find(access.getServices(), new Predicate<Service>() {

						@Override
						public boolean apply(Service service) {
							System.out.println(service);
							return type.equals(service.getType());
						}

					});
			List<? extends ServiceEndpoint> endpoints = service.getEndpoints();
			if (region != null) {
				return  Iterables.find(endpoints,
						new Predicate<ServiceEndpoint>() {

							@Override
							public boolean apply(ServiceEndpoint endpoint) {
								return region.equals(endpoint.getRegion());
							}
						});
			} else {
				return endpoints.get(0);
			}
		} catch (NoSuchElementException e) {
			throw new OpenstackException("Service " + type + " not found, you can try openstack.target(<endpoint>, <resource class>) method instead", e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public OpenStackClient reauthenticateOnTenant(String tenantName) {
		properties.setProperty("auth.tenant.name", tenantName);
		return OpenStackClientFactory.authenticate(properties);
	}

	

}
