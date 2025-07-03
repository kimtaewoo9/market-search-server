package software.market.product.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Repository;

@Entity
@Table(name = "products")
@Getter
@Repository
@NoArgsConstructor
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	@Column(columnDefinition = "TEXT")
	private String description; // 상품 설명 .

	private int price;

	private double rating;

	private String category;

	public static Product create(String name, String description, int price, double rating,
		String category) {
		Product product = new Product();
		product.name = name;
		product.description = description;
		product.price = price;
		product.rating = rating;
		product.category = category;
		return product;
	}
}
