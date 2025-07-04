package software.market.product;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import software.market.product.domain.Product;
import software.market.product.domain.ProductDocument;
import software.market.product.dto.ProductCreateRequest;

@RestController
@RequiredArgsConstructor
public class ProductController {

	private final ProductService productService;

	@GetMapping("/products")
	public ResponseEntity<List<Product>> read(
		@RequestParam("page") int page,
		@RequestParam("pageSize") int pageSize
	) {
		List<Product> products = productService.getProducts(page, pageSize);
		return ResponseEntity.ok(products);
	}

	@GetMapping("/products/suggestions")
	public ResponseEntity<List<String>> getSuggestions(
		@RequestParam String query) {
		List<String> suggestions = productService.getSuggestions(query);
		return ResponseEntity.ok(suggestions);
	}

	@GetMapping("/products/search")
	public ResponseEntity<List<ProductDocument>> searchProducts(
		@RequestParam("query") String query,
		@RequestParam(value = "category", required = false) String category,
		@RequestParam(defaultValue = "1") Double minPrice,
		@RequestParam(defaultValue = "1000000000", required = false) Double maxPrice,
		@RequestParam(defaultValue = "1") int page,
		@RequestParam(defaultValue = "10") int pageSize
	) {
		List<ProductDocument> productDocuments = productService.searchProducts(query, category,
			minPrice, maxPrice, page, pageSize);
		return ResponseEntity.ok(productDocuments);
	}

	@PostMapping("/products")
	public ResponseEntity<Product> create(@RequestBody ProductCreateRequest request) {
		Product product = productService.create(request);
		return ResponseEntity.ok(product);
	}

	@DeleteMapping("/products/{productId}")
	public ResponseEntity<Void> delete(
		@PathVariable("productId") Long productId
	) {
		productService.delete(productId);
		return ResponseEntity.noContent().build();
	}
}
