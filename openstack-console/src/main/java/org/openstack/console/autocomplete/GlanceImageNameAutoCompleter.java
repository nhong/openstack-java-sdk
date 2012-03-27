package org.openstack.console.autocomplete;

import java.util.List;

import org.openstack.console.OpenstackCliContext;
import org.openstack.console.common.CliContext;
import org.openstack.console.common.autocomplete.SimpleArgumentAutoCompleter;
import org.openstack.model.images.Image;

import com.google.common.collect.Lists;

public class GlanceImageNameAutoCompleter extends SimpleArgumentAutoCompleter {

	@Override
	public List<String> doComplete(CliContext context, String prefix) throws Exception {
		List<String> strings = Lists.newArrayList();

		OpenstackCliContext osContext = (OpenstackCliContext) context;
		for (Image image : osContext.getImageClient().get().getList()) {
			strings.add(image.getName());
		}

		addSuffix(strings, " ");

		return strings;
	}

}
