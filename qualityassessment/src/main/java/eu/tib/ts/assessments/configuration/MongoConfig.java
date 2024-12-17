package eu.tib.ts.assessments.configuration;

import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import eu.tib.ts.assessments.repository.GitRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoClientFactoryBean;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@EnableMongoRepositories(basePackageClasses = GitRepository.class)
@Configuration
public class MongoConfig {

    @Value("${ols.mongo.readpreference:}")
    String readPreference = "";

    @Value("${ols.mongo.seedlist:}")
    String seedList = "";

    @Bean
    MongoClientFactoryBean mongoFactory(MongoProperties properties) {

        MongoClientFactoryBean mongoClientFactoryBean = new MongoClientFactoryBean();

        if (properties.getAuthenticationDatabase() != null) {
            MongoCredential credential = MongoCredential.createCredential(properties.getUsername(), properties.getAuthenticationDatabase(), properties.getPassword());
            mongoClientFactoryBean.setCredential(new MongoCredential[]{credential});
        }


        if (!("").equals(readPreference) && !("").equals(seedList)) {
            List<ServerAddress> seedListArray = new ArrayList<>();

            for (String seed : seedList.split(",")) {
                seedListArray.add(new ServerAddress(seed));
            }

            mongoClientFactoryBean.setReplicaSet(Arrays.toString(seedListArray.toArray(new ServerAddress[seedListArray.size()])));

        } else {
            mongoClientFactoryBean.setHost(properties.getHost());
            if (properties.getPort() != null) {
                mongoClientFactoryBean.setPort(properties.getPort());
            }
        }
        return mongoClientFactoryBean;

    }
}
