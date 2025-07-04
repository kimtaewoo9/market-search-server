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

// mapping 정보를 ProductDocument 객체에 기입
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
		// @MultiField -> 다른 타입이나 분석기 색인해서 자동완성을 쓸 수 있게 만들어줌 .
		otherFields = {
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
		// 가전제품 -> 가전, 제품 .
		mainField = @Field(type = FieldType.Text, analyzer = "products_category_analyzer"),
		// 카테고리에 쓰려면 .. Keyword 타입도 필요함 . 정확히 일치
		otherFields = {
			// 토큰화 하지 않고 저장한 색인도 필요함 . 가전제품 -> 가전제품
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
