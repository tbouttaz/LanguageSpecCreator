package SpecificationCreation.look;

import com.hp.hpl.jena.ontology.OntProperty;

public class OntPropertyModel {
	OntProperty ontProperty;
	
	public OntPropertyModel(OntProperty ontProperty) {
		this.ontProperty = ontProperty;
	}
	
	@Override
	public String toString() {
		return ontProperty.getLocalName();
	}

	public OntProperty getOntProperty() {
		return ontProperty;
	}
}
