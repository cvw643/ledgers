package de.adorsys.ledgers.data.importer.core;

import org.apache.commons.lang3.StringUtils;

final class SystemChecker {

	private String ledgerUrl;
	private String filePath;

	public SystemChecker ledgerUrl(String ledgerUrl) {
		this.ledgerUrl = ledgerUrl;
		return this;
	}

	public SystemChecker validateUrl() {
		if(StringUtils.isBlank(ledgerUrl)) {
			throw new IllegalStateException("Ledger url should not be empty");
		}
		return this;
	}

	public SystemChecker filePath(String filePath) {
		this.filePath = filePath;
		return this;
	}

	public SystemChecker validatePath() {
		if(StringUtils.isBlank(filePath)) {
			throw new IllegalStateException("File path is required");
		}
		// if(Files.exists(Paths.get(this.filePath))) throw new IllegalStateException("File does not exist");
		return this;
	}

}
