# Clean Architecture - Wishlist Service

## Visão Geral da Arquitetura
O Wishlist Service implementa Clean Architecture seguindo os princípios do Uncle Bob, garantindo separação de responsabilidades e testabilidade.

### Princípios Fundamentais
- Inversão de Dependências: Dependências apontam para dentro.
- Separação de Responsabilidades: Cada camada tem responsabilidade única.
- Testabilidade: Camadas podem ser testadas isoladamente.
- Independência: Framework, UI, Database são detalhes.

### Estrutura das Camadas
```
┌─────────────────────────────────────────────────────────┐
│                 🔴 Camada de Apresentação               │ ← Controllers, Exception Handlers
├─────────────────────────────────────────────────────────┤
│                 🟢 Camada de Aplicação                  │ ← Casos de Uso, DTOs, Services  
├─────────────────────────────────────────────────────────┤
│                   🔵 Camada de Domínio                  │ ← Entidades, Regras de Negócio
├─────────────────────────────────────────────────────────┤
│                🟡 Camada de Infraestrutura              │ ← Database, Serviços Externos
└─────────────────────────────────────────────────────────┘
```

### Camada de Domínio (Núcleo do Negócio)

### Entidades
```java
// Lógica de negócio pura, sem dependências externas
public class Wishlist {
private String customerId;
private List<WishlistProduct> products;
private LocalDateTime createdAt;

    // Regras de negócio encapsuladas
    public void addProduct(String productId) {
        if (!canAddProduct()) {
            throw new IllegalStateException("Não pode exceder 20 produtos");
        }
        if (hasProduct(productId)) {
            throw new IllegalArgumentException("Produto já existe");
        }
        products.add(new WishlistProduct(productId));
    }
    
    public boolean canAddProduct() {
        return products.size() < 20;
    }
}
```

### Interfaces de Repositório (Portas)
```java
// Contrato definido no domínio, implementado na infraestrutura
public interface WishlistRepository {
    Optional<Wishlist> findByCustomerId(String customerId);
    Wishlist save(Wishlist wishlist);
    void deleteByCustomerId(String customerId);
    boolean existsByCustomerId(String customerId);
}
```

### Camada de Aplicação (Casos de Uso)
#### Interfaces de Casos de Uso
```java
public interface WishlistUseCase {
    WishlistResponse getWishlist(String customerId);
    AddProductResponse addProduct(String customerId, String productId);
    void removeProduct(String customerId, String productId);
    ProductExistsResponse checkProductExists(String customerId, String productId);
}
```

#### Implementação do Serviço
```java
@Service
@Transactional
public class WishlistService implements WishlistUseCase {

    private final WishlistRepository wishlistRepository; // Porta
    
    public AddProductResponse addProduct(String customerId, String productId) {
        // Orquestração do caso de uso
        validateCustomerId(customerId);
        validateProductId(productId);
        
        Wishlist wishlist = wishlistRepository.findByCustomerId(customerId)
            .orElse(new Wishlist(customerId));
            
        wishlist.addProduct(productId); // Regra de negócio na entidade
        wishlistRepository.save(wishlist);
        
        return mapToResponse(wishlist, productId);
    }
}
```

### Camada de Infraestrutura (Adaptadores)
#### Implementação do Repositório
```java
@Repository
public class WishlistRepositoryImpl implements WishlistRepository {

    private final MongoTemplate mongoTemplate;
    
    @Override
    public Optional<Wishlist> findByCustomerId(String customerId) {
        Query query = new Query(Criteria.where("customerId").is(customerId));
        Wishlist wishlist = mongoTemplate.findOne(query, Wishlist.class, "wishlists");
        return Optional.ofNullable(wishlist);
    }
}
```

### Camada de Apresentação (Interface Externa)
#### Controller REST
```java
@RestController
@RequestMapping("/api/v1/customers/{customerId}/wishlist")
public class WishlistController {

    private final WishlistUseCase wishlistUseCase; // Interface do caso de uso
    
    @PostMapping("/products/{productId}")
    public ResponseEntity<AddProductResponse> addProduct(
            @PathVariable String customerId,
            @PathVariable String productId) {
        
        AddProductResponse response = wishlistUseCase.addProduct(customerId, productId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
```

### Benefícios Alcançados
#### Testabilidade
- Cada camada pode ser testada isoladamente.
- Mock de dependências facilmente com interfaces.
- Regras de negócio testadas independentemente.

#### Flexibilidade
- Trocar banco sem tocar lógica de negócio.
- Alterar formato da API sem afetar casos de uso.
- Adicionar novos mecanismos de entrega facilmente.

#### Manutenibilidade
- Separação clara de responsabilidades.
- Regras de negócio centralizadas nas entidades.
- Núcleo independente de framework.
