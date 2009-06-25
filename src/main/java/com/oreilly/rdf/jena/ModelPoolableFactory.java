package com.oreilly.rdf.jena;

import java.util.List;

import org.apache.commons.pool.PoolableObjectFactory;

import com.hp.hpl.jena.rdf.model.Model;

public interface ModelPoolableFactory extends PoolableObjectFactory {

	public abstract Model getModel(String modelName) throws Exception;
	
	public List<String> listModels() throws Exception;

}