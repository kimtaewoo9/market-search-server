package software.market.product;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import software.market.product.domain.ProductDocument;

@Repository
public interface ProductDocumentRepository extends
	ElasticsearchRepository<ProductDocument, String> {

}
