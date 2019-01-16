/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.ledgers.data.importer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;

import de.adorsys.ledgers.data.importer.core.LedgerClientDataImporter;
import de.adorsys.ledgers.middleware.client.rest.AccountRestClient;
import de.adorsys.ledgers.middleware.client.rest.AuthRequestInterceptor;

@SpringBootApplication
@EnableFeignClients(basePackageClasses=AccountRestClient.class)
public class LedgersClientImportApplication implements ApplicationListener<ApplicationReadyEvent> {
	
	@Autowired
	private LedgerClientDataImporter importer;
	
	@Bean
	public AuthRequestInterceptor getClientAuth() {
		return new AuthRequestInterceptor();
	}
	
	public static void main(String[] args) {
		System.setProperty("server.port", "8088");
		new SpringApplicationBuilder(LedgersClientImportApplication.class).run(args);
	}

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		String tmpFolder = System.getProperty("java.io.tmpdir")+File.pathSeparator;
		String testFilePath = tmpFolder+"mockbank-simple-data.yml";
		copyTo("mockbank-simple-data.yml", testFilePath);
		importer.readFileAndImportData(testFilePath);
	}

    private void copyTo(String fileName,String dest) {
        try {
            Files.copy(LedgersClientImportApplication.class.getClassLoader().getResourceAsStream(fileName)
                    , (new File(dest)).toPath()
                    , StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException(String.format("Unable to copy %s to %s", fileName, dest), e);
        }
    }
}
