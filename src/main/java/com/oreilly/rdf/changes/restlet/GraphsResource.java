package com.oreilly.rdf.changes.restlet;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

public class GraphsResource extends JenaModelResource {

	public GraphsResource(Context content, Request request, Response responce) {
		super(content, request, responce);
		getVariants().add(new Variant(MediaType.TEXT_URI_LIST));
	}

	@Override
	public Representation represent(Variant variant) throws ResourceException {
		if (MediaType.TEXT_URI_LIST.equals(variant.getMediaType())) {
			StringBuilder urilist = new StringBuilder();
			for (String name : modelNames()) {
				urilist.append(name);
				urilist.append("\0x0D\0x0A");
			}
			return new StringRepresentation(urilist.toString(), MediaType.TEXT_URI_LIST);
		}
		return null;	
	}
	
	

}
