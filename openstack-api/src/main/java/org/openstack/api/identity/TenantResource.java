package org.openstack.api.identity;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Target;
import javax.ws.rs.core.MediaType;

import org.openstack.api.common.Resource;
import org.openstack.model.identity.KeyStoneTenant;

public class TenantResource extends Resource {

	public TenantResource(Target target) {
		super(target);
	}

	public KeyStoneTenant get() {
		return target.request(MediaType.APPLICATION_XML).get(KeyStoneTenant.class);
	}

	public KeyStoneTenant update(Entity<KeyStoneTenant> entity) {
		return target.request(MediaType.APPLICATION_JSON).put(entity, KeyStoneTenant.class);
	}

	public void delete() {
		target.request().delete();
	}

}
