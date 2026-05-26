package com.IntelligentDocumentQA.config;

import org.hibernate.dialect.H2Dialect;
import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.type.StandardBasicTypes;

public class TestH2Dialect extends H2Dialect {

	@Override
	public void initializeFunctionRegistry(FunctionContributions functionContributions) {
		super.initializeFunctionRegistry(functionContributions);
		functionContributions.getFunctionRegistry().registerPattern(
				"cosine_distance",
				"(?1 <=> ?2)",
				functionContributions.getTypeConfiguration().getBasicTypeRegistry().resolve(StandardBasicTypes.DOUBLE)
		);
	}
}
