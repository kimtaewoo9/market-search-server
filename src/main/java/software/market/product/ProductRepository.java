package software.market.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import software.market.product.domain.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

}
