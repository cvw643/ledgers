package de.adorsys.ledgers.data.importer;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import de.adorsys.ledgers.data.importer.core.ImportOperationProccessor;
import de.adorsys.ledgers.data.importer.core.LedgerClientDataImporter;
import de.adorsys.ledgers.data.importer.operations.OperationFactory;
import de.adorsys.ledgers.middleware.client.rest.AccountRestClient;
import de.adorsys.ledgers.middleware.client.rest.PaymentRestClient;
import de.adorsys.ledgers.middleware.client.rest.UserMgmtRestClient;

public class LedgerClientDataImportTest {
	private final String tmpFolder = System.getProperty("java.io.tmpdir")+File.pathSeparator;
	
	// @Test
    public void should_import_given_data_in_ledgers_and_return_successful_state() {
    	// Required data : user
		// String ledgerUrl = "http://localhost:8088";
		
		String testFilePath = tmpFolder+"mockbank-simple-data-init-data.yml";
		copyTo("mockbank-simple-data-init-data.yml", testFilePath);
		
		PaymentRestClient paymentRestClient = null;
		UserMgmtRestClient userMgmtRestClient = null;
		AccountRestClient accountRestClient = null;
		
		OperationFactory factory = new OperationFactory(accountRestClient, userMgmtRestClient, paymentRestClient);
		ImportOperationProccessor operationProcessor = new ImportOperationProccessor(factory );
		assertTrue(new LedgerClientDataImporter(operationProcessor ).readFileAndImportData(testFilePath));
    }
	

    private void copyTo(String fileName,String dest) {
        try {
            Files.copy(LedgerClientDataImportTest.class.getClassLoader().getResourceAsStream(fileName)
                    , (new File(dest)).toPath()
                    , StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException(String.format("Unable to copy %s to %s", fileName, dest), e);
        }
    }
}