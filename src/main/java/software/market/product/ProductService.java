package software.market.product;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.NumberRangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightParameters;
import org.springframework.stereotype.Service;
import software.market.product.domain.Product;
import software.market.product.domain.ProductDocument;
import software.market.product.dto.ProductCreateRequest;

@Service
@RequiredArgsConstructor
public class ProductService {

	private final ProductRepository productRepository;
	private final ProductDocumentRepository productDocumentRepository;
	private final ElasticsearchOperations elasticsearchOperations;


	public List<Product> getProducts(int page, int size) {
		// 1번 페이지 입력시 0번 페이지 반환 ..0-based index
		Pageable pageable = PageRequest.of(page - 1, size);
		return productRepository.findAll(pageable).getContent(); // 실제 상품 리스트만 반환함 .
	}

	public List<String> getSuggestions(String query) {
		Query multiMatchQuery = MultiMatchQuery.of(m -> m
			.query(query) // query "갤럭"
			.type(TextQueryType.BoolPrefix) // Bool 여러 토큰들을 논리적으로 결, Prefix 마지막 단어는 접두어 검색.
			.fields("name.auto_complete", "name.auto_complete.2gram", "name.auto_complete.3gram")
		)._toQuery();

		HighlightParameters highlightParameters = HighlightParameters.builder()
			.withPreTags("<b>")
			.withPostTags("</b>")
			.build();
		// name 필드를 하이라이트 처리해줌 .
		Highlight highlight = new Highlight(highlightParameters,
			List.of(new HighlightField("name")));
		HighlightQuery highlightQuery = new HighlightQuery(highlight, ProductDocument.class);

		NativeQuery nativeQuery = NativeQuery.builder()
			.withQuery(multiMatchQuery)
			.withHighlightQuery(highlightQuery) // 하이라이트 쿼리 추가 .
			.withPageable(PageRequest.of(0, 5)) // 5개 가져오기 .
			.build();

		SearchHits<ProductDocument> searchHits = elasticsearchOperations.search(nativeQuery,
			ProductDocument.class);

		return searchHits.getSearchHits().stream()
			.map(hit -> {
				ProductDocument productDocument = hit.getContent();
				return productDocument.getName();
			})
			.toList();
	}

	public List<ProductDocument> searchProducts(
		String query,
		String category,
		double minPrice,
		double maxPrice,
		int page,
		int size
	) {
		Query multiMatchQuery = MultiMatchQuery.of(m -> m
			.query(query)
			.fields("name^3", "description^1", "category^2") // 검색할 필드와 가중치.
			.fuzziness("AUTO") // 오타 허용 ..
		)._toQuery();

		ArrayList<Query> filters = new ArrayList<>();
		if (category != null & !category.isEmpty()) {
			Query categoryFilter = TermQuery.of(t -> t
				.field("category.raw")
				.value(category)
			)._toQuery();
			filters.add(categoryFilter);
		}

		Query priceRangeFilter = NumberRangeQuery.of(r -> r
			.field("price")
			.gte(minPrice)
			.lte(maxPrice)
		)._toRangeQuery()._toQuery();
		filters.add(priceRangeFilter);

		Query ratingShould = NumberRangeQuery.of(b -> b
			.field("rating")
			.gt(4.0)
		)._toRangeQuery()._toQuery();

		Query boolQuery = BoolQuery.of(b -> b
			.must(multiMatchQuery)
			.filter(filters)
			.should(ratingShould)
		)._toQuery();

		HighlightParameters highlightParameters = HighlightParameters.builder()
			.withPreTags("<b>")
			.withPostTags("</b>")
			.build();
		Highlight highlight = new Highlight(highlightParameters,
			List.of(new HighlightField("name")));
		HighlightQuery highlightQuery = new HighlightQuery(highlight, ProductDocument.class);

		NativeQuery nativeQuery = NativeQuery.builder()
			.withQuery(boolQuery)
			.withHighlightQuery(highlightQuery)
			.withPageable(PageRequest.of(page - 1, size))
			.build();

		// nativeQuery 의 응답 결과를 SearchHits<ProductDocument> 타입으로 반환함 .
		SearchHits<ProductDocument> searchHits = elasticsearchOperations
			.search(nativeQuery, ProductDocument.class);

		return searchHits.getSearchHits().stream()
			.map(hit -> {
				ProductDocument productDocument = hit.getContent();
				String highlightName = hit.getHighlightField("name").get(0);
				productDocument.setName(highlightName);
				return productDocument;
			})
			.toList();
	}

	public Product create(ProductCreateRequest request) {

		Product product = Product.create(
			request.getName(),
			request.getDescription(),
			request.getPrice(),
			request.getRating(),
			request.getCategory()
		);

		Product savedProduct = productRepository.save(product);

		ProductDocument productDocument = ProductDocument.create(
			savedProduct.getId(),
			savedProduct.getName(),
			savedProduct.getDescription(),
			savedProduct.getPrice(),
			savedProduct.getRating(),
			savedProduct.getCategory()
		);

		productDocumentRepository.save(productDocument);

		return savedProduct;
	}

	public void delete(Long productId) {
		productRepository.deleteById(productId);
		productDocumentRepository.deleteById(productId.toString());
	}
}
