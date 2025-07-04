package software.market.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductCreateRequest {

	private String name;
	private String description;
	private int price;
	private double rating;
	private String category;
}
