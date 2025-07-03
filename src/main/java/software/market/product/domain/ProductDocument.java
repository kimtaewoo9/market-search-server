package software.market.product.domain;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;
import org.springframework.data.elasticsearch.annotations.Setting;

@Document(indexName = "products")
@Setting(settingPath = "/elasticsearch/product-settings.json") // 인덱스 생성시 사용할 설정 파일 지정
@Getter
@Setter
@ToString
public class ProductDocument {

	@Id
	private Long id;

	@MultiField(
		mainField = @Field(type = FieldType.Text, analyzer = "products_name_analyzer"),
		otherFields = {
			// Search_As_You_Type -> 자동 완성 전용 특수 타입 ..
			@InnerField(suffix = "auto_complete", type = FieldType.Search_As_You_Type, analyzer = "nori")
		}
	)
	private String name;

	@Field(type = FieldType.Text, analyzer = "products_description_analyzer")
	private String description;

	@Field(type = FieldType.Integer)
	private Integer price;

	@Field(type = FieldType.Double)
	private Double rating;

	// FieldType -> text 형태소 분석, 동의어 처리 등 "검색" 에 최적화 .
	// Keyword -> 분석하지 않고 .. 전체 문자열 그대로 !
	@MultiField(
		mainField = @Field(type = FieldType.Text, analyzer = "products_category_analyzer"),
		otherFields = {
			@InnerField(suffix = "raw", type = FieldType.Keyword)
		}
	)
	private String category;

	public static ProductDocument create(Long id, String name, String description, Integer price,
		Double rating, String category) {
		ProductDocument productDocument = new ProductDocument();
		productDocument.id = id;
		productDocument.name = name;
		productDocument.description = description;
		productDocument.price = price;
		productDocument.rating = rating;
		productDocument.category = category;
		return productDocument;
	}
}
