package com.ontotext.ehri.sparql;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads the configuration parameters for the Sesame server remote repository from properties file
 */

class SesameServerRepositoryConfiguration {

    String repositoryURL;
    String username;
    String password;

    SesameServerRepositoryConfiguration() {
        InputStream inputStream = null;
        try {
            Properties prop = new Properties();
            String propFileName = "ehri_person_deduplication_sesame_server.properties";

            inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);

            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
            }

            repositoryURL = prop.getProperty("ehri.person.deduplication.sesame.server.repository.url");
            username = prop.getProperty("ehri.person.deduplication.sesame.server.username");
            password = prop.getProperty("ehri.person.deduplication.sesame.server.password");

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
