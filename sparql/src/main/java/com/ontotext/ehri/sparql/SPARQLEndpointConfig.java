package com.ontotext.ehri.sparql;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

class SPARQLEndpointConfig {

    String repositoryURL;
    String username;
    String password;

    SPARQLEndpointConfig() {
        InputStream inputStream = null;
        try {
            Properties prop = new Properties();
            String propFileName = "ehri_sparql_endpoint.properties";

            inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);

            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
            }

            repositoryURL = prop.getProperty("ehri.sparql.endpoint.repository.url");
            username = prop.getProperty("ehri.sparql.endpoint.username");
            password = prop.getProperty("ehri.sparql.endpoint.password");

        } catch (Exception e) {
            System.out.println("Exception: " + e);
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
