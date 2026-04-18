package com.vmetrix.querymanager.application.service;

import com.vmetrix.querymanager.domain.model.EntityMetadata;
import com.vmetrix.querymanager.domain.model.FieldMetadata;
import com.vmetrix.querymanager.domain.model.RelationshipMetadata;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
class MetadataServiceIntegrationTest {

    @Autowired
    private MetadataService metadataService;

    @Test
    void should_populate_cache_with_seed_data() {
        // Assert Entities (Total 3)
        EntityMetadata transaction = metadataService.findEntityByLogicalName("transaction");
        EntityMetadata instrument = metadataService.findEntityByLogicalName("instrument");
        EntityMetadata party = metadataService.findEntityByLogicalName("party");

        assertThat(transaction).isNotNull();
        assertThat(instrument).isNotNull();
        assertThat(party).isNotNull();

        // Assert Fields (Checking one from each to ensure cache correctly associates them, total 32 loaded overall)
        FieldMetadata txnId = metadataService.findField("transaction", "txnId");
        assertThat(txnId.getPhysicalName()).isEqualTo("TXN_ID");
        assertThat(txnId.isPk()).isTrue();

        FieldMetadata ticker = metadataService.findField("instrument", "ticker");
        assertThat(ticker.getPhysicalName()).isEqualTo("TICKER");

        FieldMetadata partyName = metadataService.findField("party", "partyName");
        assertThat(partyName.getPhysicalName()).isEqualTo("PARTY_NAME");

        // Assert Relationships (Total 3)
        List<RelationshipMetadata> txRels = metadataService.findRelationships("transaction");
        assertThat(txRels).hasSize(2);
        assertThat(txRels).extracting(RelationshipMetadata::getRelationAlias).containsExactlyInAnyOrder("instrument", "counterparty");

        List<RelationshipMetadata> instRels = metadataService.findRelationships("instrument");
        assertThat(instRels).hasSize(1);
        assertThat(instRels.get(0).getRelationAlias()).isEqualTo("issuer");
        
        List<RelationshipMetadata> partyRels = metadataService.findRelationships("party");
        assertThat(partyRels).isEmpty();
    }
}
